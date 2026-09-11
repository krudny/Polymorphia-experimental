package com.agh.polymorphia_backend.dto.request.course_import.gradable_event;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class TestDetailsRequestDto extends GradableEventDetailsRequestDto {
}
