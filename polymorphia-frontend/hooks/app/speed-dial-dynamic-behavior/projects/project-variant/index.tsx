import ProjectVariantModal from "@/components/speed-dial/modals/project-variant";
import { SpeedDialItemDynamicBehavior } from "@/components/speed-dial/types";
import { useUserDetails } from "@/hooks/contexts/useUserContext";
import useProjectVariant from "@/hooks/course/projects/useProjectVariant";
import { TargetTypes } from "@/interfaces/api/target";

export function useProjectVariantModalSpeedDialDynamicBehavior(): SpeedDialItemDynamicBehavior {
  const userDetails = useUserDetails();
  const target = {
    type: TargetTypes.STUDENT,
    id: userDetails.id,
  };
  const { data, isLoading, isError } = useProjectVariant({ target });

  return {
    modal: (onClose) => <ProjectVariantModal onClosedAction={onClose} />,
    shouldBeRendered:
      !isLoading && !isError && data !== undefined && data.length > 0,
  };
}
