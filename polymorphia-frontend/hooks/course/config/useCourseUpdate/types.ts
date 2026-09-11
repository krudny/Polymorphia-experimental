import { UseMutationResult } from "@tanstack/react-query";

export interface UpdateCourseConfigVariables {
  file: File;
}

export type UseCourseUpdate = UseMutationResult<
  void,
  Error,
  UpdateCourseConfigVariables,
  unknown
>;
