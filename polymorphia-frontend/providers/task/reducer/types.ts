import type { SupportedLanguage } from "@/interfaces/api/tasks/types";
import type { TaskTab } from "@/providers/task/types";

export const TaskActions = {
  SET_LANGUAGE: "SET_LANGUAGE",
  SET_ACTIVE_TEST_CASE_INDEX: "SET_ACTIVE_TEST_CASE_INDEX",
  SET_ACTIVE_TAB: "SET_ACTIVE_TAB",
  SET_ACTIVE_SUBMISSION_ID: "SET_ACTIVE_SUBMISSION_ID",
} as const;

export type TaskActionType = (typeof TaskActions)[keyof typeof TaskActions];

export interface TaskState {
  language: SupportedLanguage | null;
  activeTestCaseIndex: number;
  activeTab: TaskTab;
  activeSubmissionId: number | null;
}

export type TaskAction =
  | {
      type: typeof TaskActions.SET_LANGUAGE;
      payload: SupportedLanguage;
    }
  | {
      type: typeof TaskActions.SET_ACTIVE_TEST_CASE_INDEX;
      payload: number;
    }
  | {
      type: typeof TaskActions.SET_ACTIVE_TAB;
      payload: TaskTab;
    }
  | {
      type: typeof TaskActions.SET_ACTIVE_SUBMISSION_ID;
      payload: number | null;
    };
