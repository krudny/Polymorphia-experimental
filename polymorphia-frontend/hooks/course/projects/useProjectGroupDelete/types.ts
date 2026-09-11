import { TargetRequestDTO } from "@/interfaces/api/target";
import { UseMutationResult } from "@tanstack/react-query";

export interface UseProjectGroupDeleteParams {
  target: TargetRequestDTO;
}

export interface UseProjectGroupDelete {
  mutation: UseMutationResult<void, Error, UseProjectGroupDeleteParams>;
}
