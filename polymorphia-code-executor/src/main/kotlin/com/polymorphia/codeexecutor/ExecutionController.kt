package com.polymorphia.codeexecutor

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class ExecuteRequest(
    val executionId: Long? = null,
    val language: SupportedLanguage,
    val sourceCode: String,
    val stdin: String? = null,
    val cpuTimeLimitMs: Int? = null,
    val wallTimeLimitMs: Int? = null,
    val memoryLimitMb: Int? = null,
    val callbackUrl: String? = null,
)

data class ExecuteResponse(
    val stdout: String,
    val stderr: String,
    val exitCode: Int,
    val durationMs: Int,
    val timedOut: Boolean = false,
)

@RestController
@RequestMapping("/executions")
class ExecutionController(private val service: ExecutionService) {

    @PostMapping("/sync")
    fun executeSync(@RequestBody request: ExecuteRequest): ExecuteResponse =
        service.run(request)
}