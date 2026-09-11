import { useQuery } from "@tanstack/react-query";
import TaskService from "@/services/tasks";
import type { UseTaskDetails } from "@/hooks/course/tasks/useTaskDetails/types";
import { toTaskDetailsView } from "./mapper";

export default function useTaskDetails(taskId: number): UseTaskDetails {
  const { data, isLoading, isError, error } = useQuery({
    queryKey: ["taskDetails", taskId],
    queryFn: () => TaskService.getTaskDetails(taskId),
    select: toTaskDetailsView,
    enabled: !!taskId,
  });

  return { data, isLoading, isError, error };
}
