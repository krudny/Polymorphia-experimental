package com.agh.polymorphia_backend.dto.request.project;

import com.agh.polymorphia_backend.dto.request.target.TargetRequestDto;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ProjectGroupConfigurationRequestDto {
    private TargetRequestDto target;
    @NotNull
    private ProjectGroupUpdateRequestDto projectGroupUpdate;
}
