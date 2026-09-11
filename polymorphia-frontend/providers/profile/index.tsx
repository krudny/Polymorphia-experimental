import { createContext, ReactNode, useState } from "react";
import {
  ProfileContextInterface,
  ProfileFilterId,
} from "@/providers/profile/types";
import { useProfileFilterConfigs } from "@/hooks/course/filters/useProfileFilterConfigs";
import { useFilters } from "@/hooks/course/filters/useFilters";
import useStudentProfile from "@/hooks/course/course-group/useStudentProfile";
import { filterXpDetails } from "@/providers/hall-of-fame/utils/filterXpDetails";

export const ProfileContext = createContext<
  ProfileContextInterface | undefined
>(undefined);

export const ProfileProvider = ({ children }: { children: ReactNode }) => {
  const [areFiltersOpen, setAreFiltersOpen] = useState(false);

  const { data: profile, isLoading, isError } = useStudentProfile();
  const {
    data: filterConfigs,
    isLoading: isFiltersLoading,
    isError: isFiltersError,
  } = useProfileFilterConfigs();
  const filters = useFilters<ProfileFilterId>(filterConfigs ?? [], "profile");
  const lastEvolutionStageId = profile
    ? profile.evolutionStageThresholds.length - 1
    : 0;
  const maxPoints = profile
    ? (profile.evolutionStageThresholds[lastEvolutionStageId]?.minXp ?? 0)
    : 0;

  const filteredXpDetails = filterXpDetails(
    profile?.xpDetails ?? {},
    filters.configs.find((config) => config.id === "rankingOptions"),
    filters.getAppliedFilterValues
  );

  return (
    <ProfileContext.Provider
      value={{
        areFiltersOpen,
        setAreFiltersOpen,
        isLoading,
        isError,
        profile,
        filteredXpDetails,
        maxPoints,
        isFiltersLoading,
        isFiltersError,
        filters,
      }}
    >
      {children}
    </ProfileContext.Provider>
  );
};
