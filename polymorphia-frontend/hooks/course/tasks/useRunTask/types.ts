import type {
  ExecuteRequestDTO,
  TestCaseResultDTO,
} from "@/interfaces/api/tasks/types";

export interface UseRunTask {
  mutate: (payload: ExecuteRequestDTO) => void;
  isPending: boolean;
  isError: boolean;
  results: TestCaseResultDTO[] | null;
  resultsByOrderIndex: ReadonlyMap<number, TestCaseResultDTO>;
}
