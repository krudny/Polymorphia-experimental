import { useQuery } from "@tanstack/react-query";
import { useEventParams } from "@/hooks/app/params/useEventParams";
import { UseTargets, UseTargetsParams } from "@/providers/target/types";
import TargetListService from "@/services/target-list";

export default function useGradingTargets(
  params: UseTargetsParams
): UseTargets {
  const {
    search = "",
    sortBy = [],
    sortOrder = [],
    searchBy = [],
    groups = [],
    gradeStatus = [],
  } = params;
  const { gradableEventId } = useEventParams();

  const { data, isLoading, isError } = useQuery({
    queryKey: [
      "gradingTargets",
      gradableEventId,
      search,
      sortBy,
      sortOrder,
      searchBy,
      groups,
      gradeStatus,
    ],
    queryFn: () =>
      TargetListService.getGradingTargetList(
        gradableEventId,
        search,
        searchBy[0],
        sortBy[0].length > 0 ? sortBy[0] : "total",
        sortOrder[0] === "asc" || sortOrder[0] === "desc"
          ? sortOrder[0]
          : "desc",
        groups,
        gradeStatus[0]
      ),
    staleTime: 5 * 60 * 1000,
  });

  return { data, isLoading, isError };
}
