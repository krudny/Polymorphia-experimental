package com.agh.polymorphia_backend.service.task.async_submission.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class TaskSubmissionExecutorConfig {

    @Bean
    public ThreadPoolTaskExecutor taskSubmissionExecutor(TaskSubmissionProperties taskSubmissionProperties) {
        ThreadPoolTaskExecutor taskSubmissionExecutor = new ThreadPoolTaskExecutor();
        taskSubmissionExecutor.setCorePoolSize(taskSubmissionProperties.workerThreads());
        taskSubmissionExecutor.setMaxPoolSize(taskSubmissionProperties.workerThreads());
        taskSubmissionExecutor.setQueueCapacity(0);
        taskSubmissionExecutor.setThreadNamePrefix("task-submission-");
        taskSubmissionExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        return taskSubmissionExecutor;
    }
}
