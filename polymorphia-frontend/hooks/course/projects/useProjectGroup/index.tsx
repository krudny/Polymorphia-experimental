import { useQuery } from "@tanstack/react-query";
import { useUserDetails } from "@/hooks/contexts/useUserContext";
import { useEventParams } from "@/hooks/app/params/useEventParams";
import { EventTypes } from "@/interfaces/general";
import { UseProjectGroup } from "@/hooks/course/projects/useProjectGroup/types";
import { ProjectService } from "@/services/project";

export function useProjectGroup(): UseProjectGroup {
  const { gradableEventId, eventType } = useEventParams();
  const { id: userId } = useUserDetails();

  const { data, isLoading, isError } = useQuery({
    queryKey: ["projectGroup", gradableEventId, userId],
    queryFn: () => ProjectService.getProjectGroup(userId, gradableEventId),
    enabled:
      gradableEventId !== undefined &&
      gradableEventId !== null &&
      eventType === EventTypes.PROJECT,

    staleTime: 5 * 60 * 1000,
  });

  return { data, isLoading, isError };
}
