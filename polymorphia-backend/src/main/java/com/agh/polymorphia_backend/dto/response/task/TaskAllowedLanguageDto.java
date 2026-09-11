package com.agh.polymorphia_backend.dto.response.task;

import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskSupportedLanguage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAllowedLanguageDto {
    private TaskSupportedLanguage taskLanguage;

    private Boolean isDefault;

    private String sampleCode;
}
