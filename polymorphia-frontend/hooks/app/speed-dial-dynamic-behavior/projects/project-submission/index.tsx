import SubmissionsModal from "@/components/speed-dial/modals/submission";
import { SpeedDialItemDynamicBehavior } from "@/components/speed-dial/types";
import { useProjectGroup } from "@/hooks/course/projects/useProjectGroup";

export function useProjectSubmissionModalSpeedDialDynamicBehavior(): SpeedDialItemDynamicBehavior {
  const { data, isLoading, isError } = useProjectGroup();

  return {
    modal: (onClose) => <SubmissionsModal onClosedAction={onClose} />,
    shouldBeRendered:
      !isLoading && !isError && data !== undefined && data.length > 0,
  };
}
