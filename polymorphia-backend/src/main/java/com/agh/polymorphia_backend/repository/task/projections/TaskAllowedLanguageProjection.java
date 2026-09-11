package com.agh.polymorphia_backend.repository.task.projections;

import com.agh.polymorphia_backend.model.gradable_event.subtypes.task.TaskSupportedLanguage;

public interface TaskAllowedLanguageProjection {
    TaskSupportedLanguage getLanguage();
    Boolean getIsDefault();
    String getSampleCode();
}
