import { useFilters } from "@/hooks/course/filters/useFilters";
import { FilterOption } from "@/hooks/course/filters/useFilters/types";
import { ProjectGroupConfigurationResponseDTO } from "@/interfaces/api/project";
import { TargetRequestDTO } from "@/interfaces/api/target";
import { Dispatch, ReactNode, SetStateAction } from "react";

export const ProjectGroupConfigurationSteps = {
  GROUP_PICK: "GROUP_PICK",
  VARIANT_PICK: "VARIANT_PICK",
} as const;

export type ProjectGroupConfigurationStep =
  (typeof ProjectGroupConfigurationSteps)[keyof typeof ProjectGroupConfigurationSteps];

export type ProjectGroupConfigurationFilterId = "groups";

export interface ProjectGroupConfigurationContextInterface {
  currentStep: ProjectGroupConfigurationStep;
  setCurrentStep: Dispatch<SetStateAction<ProjectGroupConfigurationStep>>;

  initialTarget: TargetRequestDTO | null;
  isGeneralDataLoading: boolean;
  isGeneralDataError: boolean;

  currentProjectGroupConfiguration: ProjectGroupConfigurationResponseDTO;
  setCurrentProjectGroupConfiguration: Dispatch<
    SetStateAction<ProjectGroupConfigurationResponseDTO>
  >;

  filters: ReturnType<typeof useFilters<ProjectGroupConfigurationFilterId>>;
  areFiltersOpen: boolean;
  setAreFiltersOpen: Dispatch<SetStateAction<boolean>>;
}

export interface ProjectGroupConfigurationPartialFilterConfig {
  options: FilterOption[];
  max: number;
  defaultValues: string[];
  additionalInfo: string;
}

export interface ProjectGroupConfigurationProviderProps {
  children: ReactNode;
  initialTarget: TargetRequestDTO | null;
}
