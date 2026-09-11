import {
  FilterConfig,
  FilterState,
} from "@/hooks/course/filters/useFilters/types";

export function readFiltersFromLocalStorage<FilterIdType extends string>(
  appliedState: FilterState<FilterIdType>,
  configs: FilterConfig<FilterIdType>[],
  key: string
): FilterState<FilterIdType> | null {
  const localStorageData = localStorage.getItem("filters_" + key);
  if (localStorageData === null) {
    return null;
  }

  const localStorageFilterState: FilterState<FilterIdType> =
    JSON.parse(localStorageData);

  let validNewFilterState = {};
  configs.forEach((config) => {
    if (!(config.id in localStorageFilterState)) {
      return;
    }

    const validOptions: string[] = localStorageFilterState[config.id].filter(
      (localStorageOption) =>
        config.options.some((option) => option.value === localStorageOption)
    );

    if (validOptions.length > 0) {
      validNewFilterState = {
        ...validNewFilterState,
        [config.id]: validOptions,
      };
    }
  });

  const newState = {
    ...appliedState,
    ...validNewFilterState,
  };

  return newState;
}
