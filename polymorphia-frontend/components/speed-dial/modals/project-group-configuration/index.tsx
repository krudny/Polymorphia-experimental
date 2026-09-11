import {
  ProjectGroupConfigurationModalProps,
  ProjectGroupConfigurationModalResult,
} from "@/components/speed-dial/modals/project-group-configuration/types";
import { ProjectGroupConfigurationProvider } from "@/providers/project-group-configuration";
import { ReactNode } from "react";
import { SpeedDialModalProps } from "@/components/speed-dial/modals/types";
import Modal from "@/components/modal";
import useProjectGroupConfigurationContext from "@/hooks/contexts/useProjectGroupConfigurationContext";
import Loading from "@/components/loading";
import ErrorComponent from "@/components/error";
import { ErrorComponentSizes } from "@/components/error/types";
import {
  ProjectGroupConfigurationFilterId,
  ProjectGroupConfigurationSteps,
} from "@/providers/project-group-configuration/types";
import ProjectGroupPick from "@/components/speed-dial/modals/project-group-configuration/group-pick";
import ProjectVariantPick from "@/components/speed-dial/modals/project-group-configuration/variant-pick";
import FiltersModal from "@/components/filters-modals/FiltersModal";
import "./index.css";

function ProjectGroupConfigurationModalContent({
  onClosedAction,
}: SpeedDialModalProps) {
  const {
    isGeneralDataLoading,
    isGeneralDataError,
    currentStep,
    filters,
    areFiltersOpen,
    setAreFiltersOpen,
  } = useProjectGroupConfigurationContext();

  const renderData = (): ProjectGroupConfigurationModalResult => {
    switch (true) {
      case isGeneralDataLoading:
        return {
          content: (
            <div className="project-group-configuration-loading">
              <Loading />
            </div>
          ),
          subtitle: "",
        };

      case isGeneralDataError:
        return {
          content: (
            <ErrorComponent
              message="Nie udało się załadować konfiguracji grupy projektowej."
              size={ErrorComponentSizes.COMPACT}
            />
          ),
          subtitle: "",
        };

      case currentStep === ProjectGroupConfigurationSteps.GROUP_PICK:
        return {
          content: <ProjectGroupPick />,
          subtitle: "Wybierz skład grupy projektowej",
        };
      case currentStep === ProjectGroupConfigurationSteps.VARIANT_PICK:
        return {
          content: <ProjectVariantPick />,
          subtitle: "Wybierz warianty projektu",
        };
    }

    return {
      content: <ErrorComponent size={ErrorComponentSizes.COMPACT} />,
      subtitle: "",
    };
  };

  const { content, subtitle } = renderData();

  return (
    <>
      <Modal
        isDataPresented={!areFiltersOpen}
        onClosed={areFiltersOpen ? () => {} : onClosedAction}
        title="Konfiguracja grupy"
        subtitle={subtitle}
      >
        <div className="project-group-configuration">{content}</div>
      </Modal>
      <FiltersModal<ProjectGroupConfigurationFilterId>
        filters={filters}
        isModalOpen={areFiltersOpen}
        setIsModalOpen={setAreFiltersOpen}
        isFiltersLoading={isGeneralDataLoading}
        isFiltersError={isGeneralDataError}
      />
    </>
  );
}

export default function ProjectGroupConfigurationModal({
  onClosedAction,
  initialTarget,
}: ProjectGroupConfigurationModalProps): ReactNode {
  return (
    <ProjectGroupConfigurationProvider initialTarget={initialTarget}>
      <ProjectGroupConfigurationModalContent onClosedAction={onClosedAction} />
    </ProjectGroupConfigurationProvider>
  );
}
