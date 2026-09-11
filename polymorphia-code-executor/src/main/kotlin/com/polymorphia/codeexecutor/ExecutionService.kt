package com.polymorphia.codeexecutor

import org.springframework.stereotype.Service

@Service
class ExecutionService(private val executor: DockerExecutor) {

    fun run(request: ExecuteRequest): ExecuteResponse {
        val result = executor.execute(request)
        return ExecuteResponse(
            stdout = result.stdout,
            stderr = result.stderr,
            exitCode = result.exitCode,
            durationMs = result.durationMs,
            timedOut = result.timedOut,
        )
    }
}