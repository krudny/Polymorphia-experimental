import { useQuery } from "@tanstack/react-query";
import { useEventParams } from "@/hooks/app/params/useEventParams";
import { EventTypes } from "@/interfaces/general";
import { ProjectService } from "@/services/project";
import { TargetTypes } from "@/interfaces/api/target";
import {
  UseProjectGroupConfigurationGroupPickStudents,
  UseProjectGroupConfigurationGroupPickStudentsProps,
} from "@/hooks/course/projects/useProjectGroupConfigurationGroupPickStudents/types";

export function useProjectGroupConfigurationGroupPickStudents({
  target,
  groups,
}: UseProjectGroupConfigurationGroupPickStudentsProps): UseProjectGroupConfigurationGroupPickStudents {
  const { gradableEventId, eventType } = useEventParams();

  const { data, isLoading, isError } = useQuery({
    queryKey: target
      ? [
          "projectGroupConfigurationStudents",
          target.type,
          target.type === TargetTypes.STUDENT ? target.id : target.groupId,
          gradableEventId,
          groups,
        ]
      : ["projectGroupConfigurationStudents", "noTarget", groups],
    queryFn: () =>
      ProjectService.getProjectGroupConfigurationGroupPickStudents(
        target,
        groups,
        gradableEventId
      ),
    enabled: eventType === EventTypes.PROJECT,
  });

  return { data, isLoading, isError };
}
