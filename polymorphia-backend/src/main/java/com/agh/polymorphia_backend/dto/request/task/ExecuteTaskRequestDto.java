package com.agh.polymorphia_backend.dto.request.task;

import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskSupportedLanguage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExecuteTaskRequestDto {
    @NotNull
    private TaskSupportedLanguage taskLanguage;

    @NotBlank
    @Size(max = 64000, message = "Kod źródłowy jest zbyt długi.")
    private String sourceCode;
}