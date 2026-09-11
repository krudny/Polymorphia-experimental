import GroupModal from "@/components/speed-dial/modals/group-info";
import { SpeedDialItemDynamicBehavior } from "@/components/speed-dial/types";
import { useProjectGroup } from "@/hooks/course/projects/useProjectGroup";

export function useProjectGroupModalSpeedDialDynamicBehavior(): SpeedDialItemDynamicBehavior {
  const { data, isLoading, isError } = useProjectGroup();

  return {
    modal: (onClose) => <GroupModal onClosedAction={onClose} />,
    shouldBeRendered:
      !isLoading && !isError && data !== undefined && data.length > 0,
  };
}
