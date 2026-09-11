import ProjectGroupDeletionModal from "@/components/speed-dial/modals/project-group-deletion";
import { SpeedDialItemDynamicBehavior } from "@/components/speed-dial/types";
import useTargetContext from "@/hooks/contexts/useTargetContext";

export function useDeleteProjectGroupModalSpeedDialDynamicBehavior(): SpeedDialItemDynamicBehavior {
  const { selectedTarget } = useTargetContext();

  return {
    modal: (onClose) => (
      <ProjectGroupDeletionModal
        onClosedAction={onClose}
        initialTarget={selectedTarget}
      />
    ),
    shouldBeRendered: selectedTarget !== null,
  };
}
