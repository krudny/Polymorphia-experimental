package com.agh.polymorphia_backend.repository.submission.projection;

public interface SubmissionRequirementDetailsProjection {
    Long getId();

    String getName();

    Boolean getIsMandatory();

    Long getGradableEventId();

    String getKey();
}
