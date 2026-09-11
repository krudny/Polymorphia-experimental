import { createContext, useReducer, useEffect, useRef } from "react";
import type {
  TaskContextInterface,
  TaskProviderProps,
  MonacoEditor,
} from "@/providers/task/types";
import { TaskTab } from "@/providers/task/types";
import useTaskDetails from "@/hooks/course/tasks/useTaskDetails";
import useRunTask from "@/hooks/course/tasks/useRunTask";
import useSubmitTask from "@/hooks/course/tasks/useSubmitTask";
import useTaskStatus from "@/hooks/course/tasks/useTaskStatus";
import type { SupportedLanguage } from "@/interfaces/api/tasks/types";
import { taskReducer, initialTaskState } from "@/providers/task/reducer";
import { TaskActions } from "@/providers/task/reducer/types";
import Loading from "@/components/loading";
import ErrorComponent from "@/components/error";
import toast from "react-hot-toast";

export const TaskContext = createContext<TaskContextInterface | undefined>(
  undefined
);

export const TaskProvider = ({ children, taskId }: TaskProviderProps) => {
  const [state, dispatch] = useReducer(taskReducer, initialTaskState);
  const editorRef = useRef<MonacoEditor | null>(null);

  const {
    data: taskDetails,
    isLoading: isTaskDetailsLoading,
    isError: isTaskDetailsError,
  } = useTaskDetails(taskId);

  const {
    mutate: runTask,
    results: runResults,
    resultsByOrderIndex,
    isPending: isRunTaskPending,
    isError: isRunTaskError,
  } = useRunTask(taskId);

  const {
    mutateAsync: submitTask,
    isPending: isSubmitTaskPending,
    isError: isSubmitTaskError,
  } = useSubmitTask(taskId);

  const {
    submissionStatus,
    hasSubmission,
    isSubmissionProcessing,
    isError: isTaskStatusError,
  } = useTaskStatus(taskId, state.activeSubmissionId);

  useEffect(() => {
    if (!hasSubmission || isSubmissionProcessing) {
      return;
    }

    dispatch({
      type: TaskActions.SET_ACTIVE_TAB,
      payload: TaskTab.SUBMISSION_RESULT,
    });
  }, [hasSubmission, isSubmissionProcessing]);

  if (isTaskDetailsLoading) {
    return <Loading />;
  }

  if (isTaskDetailsError || !taskDetails) {
    return <ErrorComponent message="Nie udało się załadować zadania." />;
  }

  const language: SupportedLanguage =
    state.language ?? taskDetails.defaultLanguage;
  const sampleCode = taskDetails.languages.get(language)?.sampleCode ?? "";
  const allowedLanguages = Array.from(taskDetails.languages.keys()).map(
    (languageOption) => ({
      value: languageOption,
      label: languageOption,
    })
  );

  const testCases = taskDetails.testCases;
  const activeTestCase = testCases[state.activeTestCaseIndex];
  const activeResult = resultsByOrderIndex.get(
    activeTestCase?.orderIndex ?? -1
  );

  const handleRunTask = () => {
    const code = editorRef.current?.getValue() ?? "";
    if (!code) {
      return;
    }
    dispatch({
      type: TaskActions.SET_ACTIVE_TAB,
      payload: TaskTab.TESTCASES,
    });
    runTask({ taskLanguage: language, sourceCode: code });
  };

  const handleSubmitTask = async () => {
    const code = editorRef.current?.getValue() ?? "";
    if (!code) {
      return;
    }
    try {
      const response = await submitTask({
        taskLanguage: language,
        sourceCode: code,
      });
      dispatch({
        type: TaskActions.SET_ACTIVE_SUBMISSION_ID,
        payload: response.submissionId,
      });
    } catch (error) {
      toast.error("Nie udało się wysłać zgłoszenia");
    }
  };

  return (
    <TaskContext.Provider
      value={{
        editorRef,
        language,
        sampleCode,
        runResults,
        activeTestCaseIndex: state.activeTestCaseIndex,
        activeTestCase,
        activeResult,
        resultsByOrderIndex,
        allowedLanguages,
        testCases,
        activeTab: state.activeTab,
        dispatch,
        submissionStatus,
        hasSubmission,
        isSubmitting: isSubmitTaskPending || isSubmissionProcessing,
        isRunTaskPending,
        isRunTaskError,
        isSubmitTaskPending,
        isSubmitTaskError,
        isTaskStatusError,
        handleSubmitTask,
        handleRunTask,
      }}
    >
      {children}
    </TaskContext.Provider>
  );
};
