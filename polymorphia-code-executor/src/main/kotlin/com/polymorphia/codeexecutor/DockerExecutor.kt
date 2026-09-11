package com.polymorphia.codeexecutor

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model.Capability
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PullResponseItem
import com.github.dockerjava.api.model.StreamType
import com.github.dockerjava.api.model.Ulimit
import com.github.dockerjava.api.model.WaitResponse
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

private const val WORK_DIR = "/app"
private const val STDIN_FILE = "input.txt"
private const val DEFAULT_WALL_TIME_LIMIT_MS = 5_000
private const val MAX_WALL_TIME_LIMIT_MS = 20_000
private const val DEFAULT_MEMORY_LIMIT_MB = 256
private const val FILE_MODE = 493
private const val SANDBOX_USER = "65534:65534"
private const val MAX_PROCESSES = 128L
private const val MAX_OPEN_FILES = 64L
private const val MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024

@Component
class DockerExecutor(private val docker: DockerClient) {

    private val presentImages = ConcurrentHashMap.newKeySet<String>()

    fun prewarmImages(images: Collection<String>) {
        images.forEach(::ensureImage)
    }

    fun execute(request: ExecuteRequest): ExecutionResult {
        val language = request.language
        ensureImage(language.image)

        val wallTimeLimitMs = resolveWallTimeLimitMs(request.wallTimeLimitMs)
        val memoryLimitMb = request.memoryLimitMb ?: DEFAULT_MEMORY_LIMIT_MB
        val startedAt = System.nanoTime()

        val id = docker.createContainerCmd(language.image)
            .withWorkingDir(WORK_DIR)
            .withUser(SANDBOX_USER)
            .withEntrypoint("sh", "-c")
            .withCmd(buildShellCommand(language, request.stdin != null))
            .withHostConfig(buildHostConfig(memoryLimitMb, request.cpuTimeLimitMs))
            .withNetworkDisabled(true)
            .exec()
            .id

        try {
            copySources(id, language, request)
            return runContainer(id, wallTimeLimitMs, startedAt)
        } finally {
            docker.removeContainerCmd(id).withForce(true).exec()
        }
    }

    private fun resolveWallTimeLimitMs(requested: Int?): Int =
        (requested ?: DEFAULT_WALL_TIME_LIMIT_MS).coerceIn(1, MAX_WALL_TIME_LIMIT_MS)

    private fun buildShellCommand(language: SupportedLanguage, hasStdin: Boolean): String {
        val command = language.command.joinToString(" ")
        return if (hasStdin) "$command < $WORK_DIR/$STDIN_FILE" else command
    }

    private fun buildHostConfig(memoryLimitMb: Int, cpuTimeLimitMs: Int?): HostConfig {
        // readonlyRootfs+tmpfs was tried for /app but the tmpfs mount only exists once the
        // container is started, while sources are copied in before start - docker cp then
        // fails with "container rootfs is marked read-only". Left writable until the
        // create->copy->start flow changes (see DESIGN.md D4/D5 pooled-exec strategies).
        val hostConfig = HostConfig.newHostConfig()
            .withMemory(memoryLimitMb * 1024L * 1024L)
            .withMemorySwap(memoryLimitMb * 1024L * 1024L)
            .withPidsLimit(MAX_PROCESSES)
            .withReadonlyRootfs(false)
            .withCapDrop(Capability.ALL)
            .withSecurityOpts(listOf("no-new-privileges"))
            .withUlimits(
                listOf(
                    Ulimit("nproc", MAX_PROCESSES, MAX_PROCESSES),
                    Ulimit("nofile", MAX_OPEN_FILES, MAX_OPEN_FILES),
                    Ulimit("fsize", MAX_FILE_SIZE_BYTES, MAX_FILE_SIZE_BYTES),
                ),
            )

        if (cpuTimeLimitMs != null) {
            hostConfig.withCpuPeriod(100_000L).withCpuQuota(100_000L)
        }
        return hostConfig
    }

    private fun copySources(containerId: String, language: SupportedLanguage, request: ExecuteRequest) {
        val files = buildMap {
            put(language.fileName, request.sourceCode.toByteArray())
            request.stdin?.let { put(STDIN_FILE, it.toByteArray()) }
        }

        docker.copyArchiveToContainerCmd(containerId)
            .withTarInputStream(tarOf(files))
            .withRemotePath(WORK_DIR)
            .exec()
    }

    private fun runContainer(containerId: String, wallTimeLimitMs: Int, startedAt: Long): ExecutionResult {
        val stdout = StringBuilder()
        val stderr = StringBuilder()
        val deadlineNanos = System.nanoTime() + wallTimeLimitMs * 1_000_000L

        docker.startContainerCmd(containerId).exec()

        docker.logContainerCmd(containerId)
            .withStdOut(true)
            .withStdErr(true)
            .withFollowStream(true)
            .exec(object : ResultCallback.Adapter<Frame>() {
                override fun onNext(item: Frame) {
                    val text = String(item.payload)
                    if (item.streamType == StreamType.STDERR) stderr.append(text)
                    else stdout.append(text)
                }
            })
            .awaitCompletion(remainingMillis(deadlineNanos), TimeUnit.MILLISECONDS)

        var exitCode = -1
        val finished = docker.waitContainerCmd(containerId)
            .exec(object : ResultCallback.Adapter<WaitResponse>() {
                override fun onNext(item: WaitResponse) {
                    exitCode = item.statusCode
                }
            })
            .awaitCompletion(remainingMillis(deadlineNanos), TimeUnit.MILLISECONDS)

        val timedOut = !finished
        if (timedOut) {
            runCatching { docker.killContainerCmd(containerId).exec() }
        }

        val durationMs = ((System.nanoTime() - startedAt) / 1_000_000).toInt()

        return ExecutionResult(
            exitCode = exitCode,
            stdout = stdout.toString(),
            stderr = stderr.toString(),
            durationMs = durationMs,
            timedOut = timedOut,
        )
    }

    private fun remainingMillis(deadlineNanos: Long): Long =
        ((deadlineNanos - System.nanoTime()) / 1_000_000).coerceAtLeast(0L)

    private fun ensureImage(image: String) {
        if (presentImages.contains(image)) {
            return
        }
        val present = docker.listImagesCmd()
            .withFilter("reference", listOf(image))
            .exec()
            .isNotEmpty()
        if (!present) {
            docker.pullImageCmd(image)
                .exec(object : ResultCallback.Adapter<PullResponseItem>() {})
                .awaitCompletion()
        }
        presentImages.add(image)
    }

    private fun tarOf(files: Map<String, ByteArray>): ByteArrayInputStream {
        val bos = ByteArrayOutputStream()
        TarArchiveOutputStream(bos).use { tar ->
            files.forEach { (fileName, content) ->
                val entry = TarArchiveEntry(fileName).apply {
                    size = content.size.toLong()
                    mode = FILE_MODE
                }
                tar.putArchiveEntry(entry)
                tar.write(content)
                tar.closeArchiveEntry()
            }
        }
        return ByteArrayInputStream(bos.toByteArray())
    }
}
