export const CourseFileActions = {
  UPDATE: "UPDATE",
  CREATE: "CREATE",
} as const;

export type CourseFileAction =
  (typeof CourseFileActions)[keyof typeof CourseFileActions];

export interface UseCourseUploadProps {
  courseFileAction: CourseFileAction;
}
