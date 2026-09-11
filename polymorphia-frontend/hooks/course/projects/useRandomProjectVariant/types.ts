import { TargetRequestDTO } from "@/interfaces/api/target";
import { UseMutationResult } from "@tanstack/react-query";

export interface UseRandomProjectVariantParams {
  target: TargetRequestDTO | null;
}

export interface UseRandomProjectVariant {
  mutation: UseMutationResult<
    Record<number, number>,
    Error,
    UseRandomProjectVariantParams
  >;
}
