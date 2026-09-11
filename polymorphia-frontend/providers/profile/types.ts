import { Dispatch, SetStateAction } from "react";
import { StudentProfileResponseDTO } from "@/interfaces/api/student";
import { useFilters } from "@/hooks/course/filters/useFilters";

export type ProfileFilterId = "rankingOptions";

export type ProfileContextInterface = {
  areFiltersOpen: boolean;
  setAreFiltersOpen: Dispatch<SetStateAction<boolean>>;
  isLoading: boolean;
  isError: boolean;
  profile: StudentProfileResponseDTO | undefined;
  filteredXpDetails: Record<string, string>;
  maxPoints: number;
  isFiltersLoading: boolean;
  isFiltersError: boolean;
  filters: ReturnType<typeof useFilters<ProfileFilterId>>;
};
