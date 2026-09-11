import { useMutation } from "@tanstack/react-query";
import type {
  ExecuteRequestDTO,
  TestCaseResultDTO,
} from "@/interfaces/api/tasks/types";
import type { UseRunTask } from "@/hooks/course/tasks/useRunTask/types";
import TaskService from "@/services/tasks";

export default function useRunTask(taskId: number): UseRunTask {
  const { mutate, isPending, isError, data } = useMutation({
    mutationFn: (payload: ExecuteRequestDTO) =>
      TaskService.runTask(taskId, payload),
  });

  const results: TestCaseResultDTO[] | null = data?.results ?? null;
  const resultsByOrderIndex = new Map<number, TestCaseResultDTO>(
    (results ?? []).map((result) => [result.orderIndex, result])
  );

  return {
    mutate,
    isPending,
    isError,
    results,
    resultsByOrderIndex,
  };
}
