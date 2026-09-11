import type { TaskSubmissionStatusResponseDTO } from "@/interfaces/api/tasks/types";
import type { UseQueryResult } from "@tanstack/react-query";

export type UseTaskStatus = UseQueryResult<TaskSubmissionStatusResponseDTO> & {
  submissionStatus: TaskSubmissionStatusResponseDTO | null;
  hasSubmission: boolean;
  isSubmissionProcessing: boolean;
};
