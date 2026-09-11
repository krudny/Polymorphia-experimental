import { useQuery } from "@tanstack/react-query";
import { FilterConfig } from "@/hooks/course/filters/useFilters/types";
import { useUserDetails } from "@/hooks/contexts/useUserContext";
import { UseProfileFilterConfigs } from "@/hooks/course/filters/useProfileFilterConfigs/types";
import useEventSections from "@/hooks/course/event-section/useEventSections";
import { ProfileFilterId } from "@/providers/profile/types";

export function useProfileFilterConfigs(): UseProfileFilterConfigs {
  const { courseId } = useUserDetails();
  const { data: eventSections } = useEventSections();

  const { data, isLoading, isError } = useQuery({
    queryKey: ["profileFilters", courseId],
    enabled: !!eventSections,
    queryFn: async (): Promise<FilterConfig<ProfileFilterId>[]> => {
      if (!eventSections) {
        return [];
      }

      return [
        {
          id: "rankingOptions",
          title: "Wyświetlanie",
          options: [
            ...eventSections.map((eventSection) => ({
              value: eventSection.name,
            })),
            { value: "bonuses", label: "Bonusy" },
            { value: "total", label: "Suma" },
          ],
          min: Math.min(4, eventSections.length + 2),
          max: Math.min(4, eventSections.length + 2),
          defaultValues: [
            ...eventSections
              .slice(0, Math.min(2, eventSections.length))
              .map((eventSection) => eventSection.name),
            "bonuses",
            "total",
          ],
        },
      ];
    },
  });

  return { data, isLoading, isError };
}
