import { createContext, ReactNode, useEffect, useState } from "react";
import {
  ProjectGroupConfigurationContextInterface,
  ProjectGroupConfigurationFilterId,
  ProjectGroupConfigurationProviderProps,
  ProjectGroupConfigurationStep,
  ProjectGroupConfigurationSteps,
} from "@/providers/project-group-configuration/types";
import { useProjectGroupConfiguration } from "@/hooks/course/projects/useProjectGroupConfiguration";
import { ProjectGroupConfigurationResponseDTO } from "@/interfaces/api/project";
import { useProjectGroupConfigurationFilterConfigs } from "@/hooks/course/projects/useProjectGroupConfigurationFilterConfigs";
import { useFilters } from "@/hooks/course/filters/useFilters";

export const ProjectGroupConfigurationContext = createContext<
  ProjectGroupConfigurationContextInterface | undefined
>(undefined);

export function ProjectGroupConfigurationProvider({
  children,
  initialTarget,
}: ProjectGroupConfigurationProviderProps): ReactNode {
  const [currentStep, setCurrentStep] = useState<ProjectGroupConfigurationStep>(
    ProjectGroupConfigurationSteps.GROUP_PICK
  );
  const [
    currentProjectGroupConfiguration,
    setCurrentProjectGroupConfiguration,
  ] = useState<ProjectGroupConfigurationResponseDTO>({
    studentIds: [],
    selectedVariants: {},
  });
  const [areFiltersOpen, setAreFiltersOpen] = useState(false);

  const {
    data: filterConfigs,
    isLoading: isFilterConfigsLoading,
    isError: isFilterConfigsError,
  } = useProjectGroupConfigurationFilterConfigs(initialTarget);

  const filters = useFilters<ProjectGroupConfigurationFilterId>(
    filterConfigs ?? [],
    "projectGroupConfiguration"
  );

  const {
    data: initialProjectGroupConfiguration,
    isLoading: isInitialProjectGroupConfigurationLoading,
    isError: isInitialProjectGroupConfigurationError,
  } = useProjectGroupConfiguration({ target: initialTarget });

  useEffect(() => {
    if (initialProjectGroupConfiguration) {
      setCurrentProjectGroupConfiguration(initialProjectGroupConfiguration);
    }
  }, [initialProjectGroupConfiguration]);

  return (
    <ProjectGroupConfigurationContext.Provider
      value={{
        currentStep,
        setCurrentStep,
        initialTarget,
        isGeneralDataLoading:
          isInitialProjectGroupConfigurationLoading || isFilterConfigsLoading,
        isGeneralDataError:
          isInitialProjectGroupConfigurationError || isFilterConfigsError,
        currentProjectGroupConfiguration,
        setCurrentProjectGroupConfiguration,
        filters,
        areFiltersOpen,
        setAreFiltersOpen,
      }}
    >
      {children}
    </ProjectGroupConfigurationContext.Provider>
  );
}
