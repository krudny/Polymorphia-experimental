package com.agh.polymorphia_backend.dto.request.project;

import com.agh.polymorphia_backend.dto.request.target.TargetRequestDto;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class ProjectGroupPickStudentsRequestDto {
    private TargetRequestDto target;

    @NotNull
    private List<String> groups;
}
