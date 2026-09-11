package com.polymorphia.codeexecutor

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ExecutionImagePrewarmer(private val executor: DockerExecutor) {

    @EventListener(ApplicationReadyEvent::class)
    fun prewarmOnStartup() {
        executor.prewarmImages(SupportedLanguage.entries.map { it.image })
    }
}
