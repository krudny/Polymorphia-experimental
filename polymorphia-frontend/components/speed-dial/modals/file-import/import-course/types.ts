import { CourseFileAction } from "@/components/speed-dial/modals/file-import/import-course/upload/types";

export interface UseUploadCourseModalProps {
  onClosedAction: () => void;
  courseFileAction: CourseFileAction;
}
