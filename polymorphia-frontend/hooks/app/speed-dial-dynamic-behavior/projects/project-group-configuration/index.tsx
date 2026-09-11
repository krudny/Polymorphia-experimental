import ProjectGroupConfigurationModal from "@/components/speed-dial/modals/project-group-configuration";
import { SpeedDialItemDynamicBehavior } from "@/components/speed-dial/types";
import useTargetContext from "@/hooks/contexts/useTargetContext";

export function useEditProjectGroupConfigurationModalSpeedDialDynamicBehavior(): SpeedDialItemDynamicBehavior {
  const { selectedTarget } = useTargetContext();

  return {
    modal: (onClose) => (
      <ProjectGroupConfigurationModal
        onClosedAction={onClose}
        initialTarget={selectedTarget}
      />
    ),
    shouldBeRendered: selectedTarget !== null,
  };
}
