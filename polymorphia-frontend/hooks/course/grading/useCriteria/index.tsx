import { useQuery } from "@tanstack/react-query";
import { UseCriteria } from "@/hooks/course/grading/useCriteria/types";
import { useEventParams } from "@/hooks/app/params/useEventParams";
import { CriteriaService } from "@/services/criteria";

export default function useCriteria(
  gradableEventIdOverride?: number
): UseCriteria {
  const { gradableEventId: paramsGradableEventId } = useEventParams();
  const gradableEventId =
    gradableEventIdOverride === undefined
      ? paramsGradableEventId
      : gradableEventIdOverride;

  const { data, isLoading, isError } = useQuery({
    queryKey: ["criteria", gradableEventId],
    queryFn: () => CriteriaService.getCriteria(gradableEventId),
    staleTime: 5 * 60 * 1000,
  });

  return { data, isLoading, isError };
}
