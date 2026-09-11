import { useQuery } from "@tanstack/react-query";
import {
  UseProjectVariant,
  UseProjectVariantProps,
} from "@/hooks/course/projects/useProjectVariant/types";
import { useEventParams } from "@/hooks/app/params/useEventParams";

import { EventTypes } from "@/interfaces/general";
import { ProjectService } from "@/services/project";

export default function useProjectVariant({
  target,
}: UseProjectVariantProps): UseProjectVariant {
  const { gradableEventId, eventType } = useEventParams();

  const { data, isLoading, isError } = useQuery({
    queryKey: target
      ? ["projectVariant", gradableEventId, target]
      : ["projectVariant", "noTarget"],
    queryFn: () => ProjectService.getProjectVariant(target!, gradableEventId),
    enabled:
      gradableEventId !== undefined &&
      gradableEventId !== null &&
      target !== null &&
      eventType === EventTypes.PROJECT,
    staleTime: 5 * 60 * 1000,
  });

  return { data, isLoading, isError };
}
