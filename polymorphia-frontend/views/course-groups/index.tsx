import { useMediaQuery } from "react-responsive";
import { LazySpeedDial } from "@/components/speed-dial/lazy";
import ColumnSchema from "@/components/column-schema";
import { SpeedDialKeys } from "@/components/speed-dial/types";
import { useCourseGroupsStrategy } from "@/hooks/app/strategy/useCourseGroupsStrategy";
import { CourseGroupsFilterId } from "@/providers/course-groups/types";
import FiltersModal from "@/components/filters-modals/FiltersModal";
import useTargetContext from "@/hooks/contexts/useTargetContext";
import useCourseGroupsContext from "@/hooks/contexts/useCourseGroupsContext";
import EquipmentModals from "@/components/equipment/modals";
import { LazyGradeModal } from "@/components/speed-dial/modals/lazy";

export default function CourseGroupsView() {
  const isXL = useMediaQuery({ minWidth: "1280px" });
  const isMd = useMediaQuery({ minWidth: "768px" });
  const components = useCourseGroupsStrategy();
  const { filters } = useCourseGroupsContext();
  const { gradableEventId, setGradableEventId } = useCourseGroupsContext();
  const { targetId } = useTargetContext();
  const { areFiltersOpen, setAreFiltersOpen, handleApplyFilters } =
    useTargetContext();

  if (!components) {
    return null;
  }

  return (
    <div>
      <LazySpeedDial speedDialKey={SpeedDialKeys.COURSE_GROUP} />
      <ColumnSchema columns={isXL ? 3 : isMd ? 2 : 1} components={components} />
      <FiltersModal<CourseGroupsFilterId>
        filters={filters}
        isModalOpen={areFiltersOpen}
        setIsModalOpen={setAreFiltersOpen}
        onFiltersApplied={() => handleApplyFilters()}
      />
      <EquipmentModals targetStudentIdOverride={targetId} />
      {gradableEventId && (
        <LazyGradeModal
          gradableEventIdProp={gradableEventId}
          onClosedAction={() => setGradableEventId(null)}
          targetStudentIdOverride={targetId}
        />
      )}
    </div>
  );
}
