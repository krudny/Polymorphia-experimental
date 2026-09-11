import { RefObject } from "react";
import { AvailableCoursesDTO } from "@/interfaces/api/user-context";

export interface CourseChoiceProps {
  courses: AvailableCoursesDTO[];
  currentCourseId?: number;
  containerRef: RefObject<HTMLDivElement | null>;
  fastForward: boolean;
}

export interface CourseChoiceClickedDetails {
  courseId: number;
  courseGroupId: number;
}
