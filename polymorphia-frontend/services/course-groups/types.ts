import {
  CourseGroupsResponseDTO,
  CourseGroupsShortResponseDTO,
} from "@/interfaces/api/course-groups";

export const CourseGroupTypes = {
  INDIVIDUAL_SHORT: "individual-short",
  INDIVIDUAL_FULL: "individual-full",
  ALL_SHORT: "all-short",
  ALL_FULL: "all-full",
} as const;

export type CourseGroupType =
  (typeof CourseGroupTypes)[keyof typeof CourseGroupTypes];

export type CourseGroupResponse<T extends CourseGroupType> = T extends
  typeof CourseGroupTypes.INDIVIDUAL_SHORT | typeof CourseGroupTypes.ALL_SHORT
  ? CourseGroupsShortResponseDTO[]
  : CourseGroupsResponseDTO[];
