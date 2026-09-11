import ProjectVariantInfo from "@/components/speed-dial/modals/project-variant/project-variant-info";
import ColumnComponent from "@/components/column-schema/column-component";
import useTargetContext from "@/hooks/contexts/useTargetContext";
import useGradingContext from "@/hooks/contexts/useGradingContext";
import ColumnSwappableComponent from "@/components/column-schema/column-component/shared/column-swappable-component";
import { ProjectVariantWithCategoryNameResponseDTO } from "@/interfaces/api/project";
import {
  errorComponentCompact,
  noProjectVariantsErrorComponentCompact,
} from "@/components/speed-dial/modals/project-variant/project-variant-info/errors";

export function ProjectVariant() {
  const { projectVariants, isSpecificDataLoading, isSpecificDataError } =
    useGradingContext();
  const { state: targetState } = useTargetContext();

  const topComponent = () => <h1>Warianty</h1>;

  const mainComponent = () => (
    <ColumnSwappableComponent<ProjectVariantWithCategoryNameResponseDTO[]>
      data={projectVariants}
      isDataLoading={isSpecificDataLoading}
      isDataError={isSpecificDataError}
      renderComponent={(data, key) => (
        <ProjectVariantInfo
          key={key}
          projectVariants={data}
          size="xs"
          color="gray"
        />
      )}
      renderDataErrorComponent={() => errorComponentCompact}
      renderEmptyDataErrorComponent={() =>
        noProjectVariantsErrorComponentCompact
      }
      minHeightClassName="min-h-[250px]"
      selectedTarget={targetState.selectedTarget}
    />
  );

  return (
    <ColumnComponent
      topComponent={topComponent}
      mainComponent={mainComponent}
      hidden={targetState.selectedTarget === null}
    />
  );
}
