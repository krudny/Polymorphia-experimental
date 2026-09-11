package com.agh.polymorphia_backend.dto.request.task;

import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskSupportedLanguage;
import com.agh.polymorphia_backend.service.task.dto.TaskTestCaseSpec;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RemoteExecutionRequestDto {

    private final Long executionId;
    private final TaskSupportedLanguage language;
    private final String sourceCode;
    private final String stdin;
    private final Integer cpuTimeLimitMs;
    private final Integer wallTimeLimitMs;
    private final Integer memoryLimitMb;
    private final String callbackUrl;

    public static RemoteExecutionRequestDto from(
        TaskTestCaseSpec testCaseSpec,
        TaskSupportedLanguage language,
        String sourceCode
    ) {
        return RemoteExecutionRequestDto.builder()
            .language(language)
            .sourceCode(sourceCode)
            .stdin(testCaseSpec.input())
            .cpuTimeLimitMs(testCaseSpec.limits().cpuTimeLimitMs())
            .wallTimeLimitMs(testCaseSpec.limits().wallTimeLimitMs())
            .memoryLimitMb(testCaseSpec.limits().memoryLimitMb())
            .build();
    }
}
