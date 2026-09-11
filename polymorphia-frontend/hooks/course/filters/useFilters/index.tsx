import { useReducer, useState, useEffect } from "react";
import {
  FilterConfig,
  FilterState,
  FilterAction,
  FilterActions,
} from "@/hooks/course/filters/useFilters/types";
import { filterReducer } from "@/hooks/course/filters/useFilters/utils/filterReducer";
import { getInitialState } from "@/hooks/course/filters/useFilters/utils/getInitialState";
import { validateFilters } from "@/hooks/course/filters/useFilters/utils/validateFilters";
import { readFiltersFromLocalStorage } from "@/hooks/course/filters/useFilters/utils/readFiltersFromLocalStorage";
import { saveFiltersToLocalStorage } from "@/hooks/course/filters/useFilters/utils/saveFiltersToLocalStorage";

export function useFilters<FilterIdType extends string>(
  configs: FilterConfig<FilterIdType>[],
  localStorageKey?: string
) {
  const reducer = (
    state: FilterState<FilterIdType>,
    action: FilterAction<FilterIdType>
  ): FilterState<FilterIdType> => filterReducer(state, action, configs);

  const [state, dispatch] = useReducer(
    reducer,
    getInitialState<FilterIdType>(configs)
  );

  const [appliedState, setAppliedState] = useState<FilterState<FilterIdType>>(
    getInitialState(configs)
  );

  useEffect(() => {
    if (configs.length > 0) {
      dispatch({ type: FilterActions.RESET });
      setAppliedState(getInitialState<FilterIdType>(configs));
      if (localStorageKey !== undefined) {
        const newState = readFiltersFromLocalStorage(
          appliedState,
          configs,
          localStorageKey
        );
        if (newState !== null) {
          setAppliedState(newState);
          dispatch({ type: FilterActions.SET, state: newState });
        }
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps -- We want to run this effect only on config change or key change.
  }, [configs, localStorageKey]);

  const applyFilters = () => {
    const { valid, errors } = validateFilters(state, configs);
    if (valid) {
      setAppliedState(state);
      if (localStorageKey !== undefined) {
        saveFiltersToLocalStorage(state, localStorageKey);
      }
      return { ok: true };
    }
    return { ok: false, errors };
  };

  const resetFiltersToApplied = () => {
    dispatch({ type: FilterActions.SET, state: appliedState });
  };

  const resetFiltersToInitial = () => {
    dispatch({ type: FilterActions.RESET });
    setAppliedState(getInitialState<FilterIdType>(configs));
  };

  const getFilterValues = (filterId: FilterIdType): string[] => {
    return state[filterId] || [];
  };

  const getAppliedFilterValues = (filterId: FilterIdType): string[] => {
    return appliedState[filterId] || [];
  };

  return {
    configs,
    state,
    appliedState,
    dispatch,
    getFilterValues,
    getAppliedFilterValues,
    applyFilters,
    resetFiltersToInitial,
    resetFiltersToApplied,
  };
}
