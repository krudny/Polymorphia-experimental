import { useQuery } from "@tanstack/react-query";
import { TaskSubmissionStatus } from "@/interfaces/api/tasks/types";
import type { UseTaskStatus } from "@/hooks/course/tasks/useTaskStatus/types";
import TaskService from "@/services/tasks";

export default function useTaskStatus(
  taskId: number,
  activeSubmissionId: number | null
): UseTaskStatus {
  const query = useQuery({
    queryKey: ["taskStatus", taskId, activeSubmissionId],
    queryFn: () => TaskService.getSubmissionStatus(taskId, activeSubmissionId!),
    enabled: activeSubmissionId !== null,
    refetchInterval(query) {
      const status = query.state.data?.status;
      if (
        status === TaskSubmissionStatus.QUEUED ||
        status === TaskSubmissionStatus.RUNNING
      ) {
        return 2000;
      }
      return false;
    },
  });

  const submissionStatus = query.data ?? null;

  return {
    ...query,
    submissionStatus,
    hasSubmission: activeSubmissionId !== null,
    isSubmissionProcessing:
      query.isLoading ||
      submissionStatus?.status === TaskSubmissionStatus.QUEUED ||
      submissionStatus?.status === TaskSubmissionStatus.RUNNING,
  };
}
