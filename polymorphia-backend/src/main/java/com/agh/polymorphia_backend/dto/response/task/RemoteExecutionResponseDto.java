package com.agh.polymorphia_backend.dto.response.task;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RemoteExecutionResponseDto {
    private String stdout;

    private String stderr;

    private Integer exitCode;

    private Integer durationMs;

    private Boolean timedOut;
}
