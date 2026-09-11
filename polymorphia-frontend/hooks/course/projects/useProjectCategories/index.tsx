import { useQuery } from "@tanstack/react-query";
import { useEventParams } from "@/hooks/app/params/useEventParams";
import { EventTypes } from "@/interfaces/general";
import { ProjectService } from "@/services/project";
import { UseProjectCategories } from "@/hooks/course/projects/useProjectCategories/types";

export function useProjectCategories(): UseProjectCategories {
  const { gradableEventId, eventType } = useEventParams();

  const { data, isLoading, isError } = useQuery({
    queryKey: ["projectCategories", gradableEventId],
    queryFn: () => ProjectService.getProjectCategories(gradableEventId),
    enabled: eventType === EventTypes.PROJECT,
  });

  return { data, isLoading, isError };
}
