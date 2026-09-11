package com.polymorphia.codeexecutor

data class ExecutionResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val durationMs: Int,
    val timedOut: Boolean,
)