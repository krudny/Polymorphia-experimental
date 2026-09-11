import type { TaskDetailsView } from "@/providers/task/types";

export interface UseTaskDetails {
  data: TaskDetailsView | undefined;
  isLoading: boolean;
  isError: boolean;
  error: Error | null;
}
