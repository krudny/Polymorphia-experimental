import { UseMutationResult } from "@tanstack/react-query";

export interface CreateCourseConfigVariables {
  file: File;
}

export type UseCourseCreate = UseMutationResult<
  void,
  Error,
  CreateCourseConfigVariables,
  unknown
>;
