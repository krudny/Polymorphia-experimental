import {
  FilterConfig,
  SpecialBehaviors,
} from "@/hooks/course/filters/useFilters/types";
import { GradingFilterId } from "@/providers/grading/types";
import { useQuery } from "@tanstack/react-query";
import TargetListService from "@/services/target-list";

export function useGradingFilterConfigs(gradableEventId: number) {
  return useQuery({
    queryKey: ["gradingFilters", gradableEventId],
    queryFn: async () => {
      const groups =
        await TargetListService.getGroupsForGradingFilters(gradableEventId);

      const configs: FilterConfig<GradingFilterId>[] = [
        {
          id: "sortOrder",
          title: "Sortowanie",
          options: [
            { value: "desc", label: "Malejąco" },
            { value: "asc", label: "Rosnąco" },
          ],
          defaultValues: ["asc"],
        },
        {
          id: "sortBy",
          title: "Sortowanie po kategorii",
          options: [
            { value: "name", label: "Nazwa" },
            { value: "total", label: "Suma" },
          ],
          defaultValues: ["name"],
        },
        {
          id: "groups",
          title: "Grupy",
          options: [
            {
              value: "all",
              label: "Wszystkie",
              specialBehavior: SpecialBehaviors.EXCLUSIVE,
            },
            ...groups.map((group) =>
              group === "assigned"
                ? { value: "assigned", label: "Własne" }
                : { value: group }
            ),
          ],
          defaultValues: ["all"],
          max: groups.length,
        },
        {
          id: "gradeStatus",
          title: "Status oceny",
          options: [
            {
              value: "all",
              label: "Wszystkie",
              specialBehavior: SpecialBehaviors.EXCLUSIVE,
            },
            {
              value: "ungraded",
              label: "Nieocenione",
              specialBehavior: SpecialBehaviors.EXCLUSIVE,
            },
            {
              value: "graded",
              label: "Ocenione",
              specialBehavior: SpecialBehaviors.EXCLUSIVE,
            },
          ],
          defaultValues: ["all"],
        },
        {
          id: "searchBy",
          title: "Nazwa studenta",
          options: [
            { value: "studentName", label: "Student" },
            { value: "animalName", label: "Zwierzak" },
          ],
          defaultValues: ["studentName"],
        },
      ];

      return configs;
    },
    staleTime: 5 * 60 * 1000,
  });
}
