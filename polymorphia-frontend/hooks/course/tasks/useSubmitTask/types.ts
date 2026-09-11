import {
  ExecuteRequestDTO,
  SubmitTaskResponseDTO,
} from "@/interfaces/api/tasks/types";

export interface UseSubmitTask {
  mutateAsync: (payload: ExecuteRequestDTO) => Promise<SubmitTaskResponseDTO>;
  isPending: boolean;
  isError: boolean;
  data: SubmitTaskResponseDTO | undefined;
}
