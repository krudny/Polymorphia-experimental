import { StudentDetailsDTOWithName } from "@/interfaces/api/user";

export interface StudentTargetData extends Omit<
  StudentDetailsDTOWithName,
  "position"
> {
  gainedXp?: string;
  animalId: number;
}

export const TargetTypes = {
  STUDENT: "STUDENT",
  STUDENT_GROUP: "STUDENT_GROUP",
} as const;

export type TargetType = (typeof TargetTypes)[keyof typeof TargetTypes];

export const GroupTargetTypes = {
  MATCHING: "MATCHING",
  DIVERGENT: "DIVERGENT",
} as const;

export type GroupTargetType =
  (typeof GroupTargetTypes)[keyof typeof GroupTargetTypes];

export const TargetListTypes = {
  COURSE_GROUP: "COURSE_GROUP",
  GRADING: "GRADING",
} as const;

export type TargetListType =
  (typeof TargetListTypes)[keyof typeof TargetListTypes];

export interface StudentTargetResponseDTO {
  type: typeof TargetTypes.STUDENT;
  id: number;
  student: StudentTargetData;
}

export interface StudentGroupTargetResponseDTO {
  type: typeof TargetTypes.STUDENT_GROUP;
  groupType: GroupTargetType;
  groupId: number;
  members: StudentTargetData[];
}

export type TargetResponseDTO =
  StudentTargetResponseDTO | StudentGroupTargetResponseDTO;

export type TargetRequestDTO =
  | { type: typeof TargetTypes.STUDENT; id: number }
  | { type: typeof TargetTypes.STUDENT_GROUP; groupId: number };
