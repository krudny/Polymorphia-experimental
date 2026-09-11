import { ProjectGroupConfigurationResponseDTO } from "@/interfaces/api/project";
import { TargetRequestDTO } from "@/interfaces/api/target";
import { UseMutationResult } from "@tanstack/react-query";

export interface UseProjectGroupConfigurationUpdateParams {
  target: TargetRequestDTO | null;
  configuration: ProjectGroupConfigurationResponseDTO;
}

export interface UseProjectGroupConfigurationUpdate {
  mutation: UseMutationResult<
    void,
    Error,
    UseProjectGroupConfigurationUpdateParams
  >;
}
