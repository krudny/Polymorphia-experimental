import { ProjectGroupConfigurationResponseDTO } from "@/interfaces/api/project";
import { TargetRequestDTO } from "@/interfaces/api/target";

export interface UseProjectGroupConfigurationProps {
  target: TargetRequestDTO | null;
}

export interface UseProjectGroupConfiguration {
  data: ProjectGroupConfigurationResponseDTO | undefined;
  isLoading: boolean;
  isError: boolean;
}
