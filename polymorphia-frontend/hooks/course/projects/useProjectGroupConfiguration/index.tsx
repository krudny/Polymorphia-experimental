import { useQuery } from "@tanstack/react-query";
import { useEventParams } from "@/hooks/app/params/useEventParams";
import { EventTypes } from "@/interfaces/general";
import { ProjectService } from "@/services/project";
import {
  UseProjectGroupConfiguration,
  UseProjectGroupConfigurationProps,
} from "@/hooks/course/projects/useProjectGroupConfiguration/types";
import { TargetTypes } from "@/interfaces/api/target";

export function useProjectGroupConfiguration({
  target,
}: UseProjectGroupConfigurationProps): UseProjectGroupConfiguration {
  const { gradableEventId, eventType } = useEventParams();

  const { data, isLoading, isError } = useQuery({
    queryKey: target
      ? [
          "projectGroupConfiguration",
          target.type,
          target.type === TargetTypes.STUDENT ? target.id : target.groupId,
          gradableEventId,
        ]
      : ["projectGroupConfiguration", "noTarget"],
    queryFn: () =>
      ProjectService.getProjectGroupConfiguration(target!, gradableEventId),
    enabled: eventType === EventTypes.PROJECT && target !== null,
  });

  return { data, isLoading, isError };
}
