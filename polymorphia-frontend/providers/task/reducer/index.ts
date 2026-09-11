import { TaskTab } from "@/providers/task/types";
import {
  TaskAction,
  TaskActions,
  TaskState,
} from "@/providers/task/reducer/types";

export const initialTaskState: TaskState = {
  language: null,
  activeTestCaseIndex: 0,
  activeTab: TaskTab.TESTCASES,
  activeSubmissionId: null,
};

export const taskReducer = (
  state: TaskState,
  action: TaskAction
): TaskState => {
  switch (action.type) {
    case TaskActions.SET_LANGUAGE:
      return {
        ...state,
        language: action.payload,
      };
    case TaskActions.SET_ACTIVE_TEST_CASE_INDEX:
      return {
        ...state,
        activeTestCaseIndex: action.payload,
      };
    case TaskActions.SET_ACTIVE_TAB:
      return {
        ...state,
        activeTab: action.payload,
      };
    case TaskActions.SET_ACTIVE_SUBMISSION_ID:
      return {
        ...state,
        activeSubmissionId: action.payload,
      };
    default:
      return state;
  }
};
