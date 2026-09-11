package com.polymorphia.codeexecutor

enum class SupportedLanguage (val image: String, val fileName: String, val command: List<String>) {
    PYTHON("python:3.12-slim", "main.py", listOf("python", "/app/main.py")),
    JAVASCRIPT("node:22-slim", "main.js", listOf("node", "/app/main.js")),
}