import type { Dispatch, ReactNode, RefObject } from "react";
import type {
  TaskTestCaseDTO,
  TestCaseResultDTO,
  TaskSubmissionStatusResponseDTO,
  SupportedLanguage,
} from "@/interfaces/api/tasks/types";
import type { SelectorOption } from "@/components/selector/types";
import type { editor } from "monaco-editor";
import type { TaskAction } from "@/providers/task/reducer/types";

export interface LanguageInfo {
  language: SupportedLanguage;
  sampleCode: string;
  isDefault: boolean;
}

export interface TaskDetailsView {
  testCases: TaskTestCaseDTO[];
  languages: ReadonlyMap<SupportedLanguage, LanguageInfo>;
  defaultLanguage: SupportedLanguage;
}

export type MonacoEditor = editor.IStandaloneCodeEditor;

export const TaskTab = {
  TESTCASES: "testcases",
  SUBMISSION_RESULT: "submissionResult",
} as const;

export type TaskTab = (typeof TaskTab)[keyof typeof TaskTab];

export interface TaskContextInterface {
  editorRef: RefObject<MonacoEditor | null>;
  language: SupportedLanguage;
  sampleCode: string;
  runResults: TestCaseResultDTO[] | null;
  activeTestCaseIndex: number;
  activeTestCase: TaskTestCaseDTO | undefined;
  activeResult: TestCaseResultDTO | undefined;
  resultsByOrderIndex: ReadonlyMap<number, TestCaseResultDTO>;
  allowedLanguages: SelectorOption[];
  testCases: TaskTestCaseDTO[];
  handleRunTask: (code?: string) => void;
  activeTab: TaskTab;
  dispatch: Dispatch<TaskAction>;
  submissionStatus: TaskSubmissionStatusResponseDTO | null;
  hasSubmission: boolean;
  isSubmitting: boolean;
  isRunTaskPending: boolean;
  isRunTaskError: boolean;
  isSubmitTaskPending: boolean;
  isSubmitTaskError: boolean;
  isTaskStatusError: boolean;
  handleSubmitTask: (code?: string) => void;
}

export interface TaskProviderProps {
  children: ReactNode;
  taskId: number;
}
