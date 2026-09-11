import { ReactNode } from "react";

export type UseSpeedDialItemDynamicBehaviorHook =
  () => SpeedDialItemDynamicBehavior;

export type SpeedDialItemDynamicBehavior = {
  onClick?: () => void;
  modal?: (onClose: () => void) => ReactNode;
  shouldBeRendered?: boolean; // defaults to `true`
};

export interface SpeedDialItem {
  id: number;
  orderIndex: number;
  label: string;
  icon: string;
  useDynamicBehavior: UseSpeedDialItemDynamicBehaviorHook;
  color?: string;
}

export interface SpeedDialProps {
  speedDialKey: SpeedDialKey;
}

export const SpeedDialKeys = {
  ASSIGNMENT_MARKDOWN: "ASSIGNMENT_MARKDOWN",
  PROJECT_MARKDOWN: "PROJECT_MARKDOWN",
  RULES_MARKDOWN: "RULES_MARKDOWN",
  TEST_GRADING: "TEST_GRADING",
  ASSIGNMENT_GRADING: "ASSIGNMENT_GRADING",
  PROJECT_GRADING: "PROJECT_GRADING",
  COURSE_GROUP: "COURSE_GROUP",
  COURSE_GROUP_GRID: "COURSE_GROUP_GRID",
  PROFILE_STUDENT: "PROFILE_STUDENT",
  TASK_MARKDOWN: "TASK_MARKDOWN",
} as const;

export type SpeedDialKey = (typeof SpeedDialKeys)[keyof typeof SpeedDialKeys];
