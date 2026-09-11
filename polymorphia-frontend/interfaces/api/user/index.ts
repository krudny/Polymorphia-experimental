export const Roles = {
  STUDENT: "STUDENT",
  INSTRUCTOR: "INSTRUCTOR",
  COORDINATOR: "COORDINATOR",
  UNDEFINED: "UNDEFINED",
} as const;

export type Role = (typeof Roles)[keyof typeof Roles];

export const RoleTextMap: Record<Role, string> = {
  [Roles.STUDENT]: "Student",
  [Roles.INSTRUCTOR]: "Prowadzący",
  [Roles.COORDINATOR]: "Koordynator",
  [Roles.UNDEFINED]: "Nieokreślony",
};

export interface BaseUserDetails {
  id: number;
  fullName?: string;
  courseId: number;
  imageUrl: string;
}

interface BaseUserDetailsDTOWithType<
  T extends Role,
  R extends BaseUserDetails,
> {
  userRole: T;
  userDetails: R;
}

export interface StudentDetailsDTOWithNullableName extends BaseUserDetails {
  animalName: string;
  evolutionStage: string;
  group: string;
  position: number;
}

export interface BaseUserDetailsDTOWithName extends BaseUserDetails {
  fullName: string;
}

export interface StudentDetailsDTOWithName extends StudentDetailsDTOWithNullableName {
  fullName: string;
}

export type StudentDetailsDTOWithType = BaseUserDetailsDTOWithType<
  typeof Roles.STUDENT,
  StudentDetailsDTOWithName
>;

export type InstructorDetailsDTOWithType = BaseUserDetailsDTOWithType<
  typeof Roles.INSTRUCTOR,
  BaseUserDetailsDTOWithName
>;
export type CoordinatorDetailsDTOWithType = BaseUserDetailsDTOWithType<
  typeof Roles.COORDINATOR,
  BaseUserDetailsDTOWithName
>;
export type UndefinedDetailsDTOWithType = BaseUserDetailsDTOWithType<
  typeof Roles.UNDEFINED,
  BaseUserDetailsDTOWithName
>;

export type UserDetailsDTO =
  | StudentDetailsDTOWithType
  | InstructorDetailsDTOWithType
  | CoordinatorDetailsDTOWithType
  | UndefinedDetailsDTOWithType;

export interface InviteRequestDTO {
  firstName: string;
  lastName: string;
  indexNumber?: number;
  email: string;
  role: Role;
  courseId: number;
}

export interface RegisterRequestDTO {
  invitationToken: string;
  password: string;
}

export type HallOfFameUserDTO =
  StudentDetailsDTOWithNullableName | StudentDetailsDTOWithName;
