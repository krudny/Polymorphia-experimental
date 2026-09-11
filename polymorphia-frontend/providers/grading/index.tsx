"use client";
import {
  createContext,
  ReactNode,
  useEffect,
  useMemo,
  useReducer,
} from "react";
import { useEventParams } from "@/hooks/app/params/useEventParams";
import useGradeUpdate from "@/hooks/course/grading/useGradeUpdate";
import {
  GradingContextInterface,
  GradingFilterId,
} from "@/providers/grading/types";
import { useFilters } from "@/hooks/course/filters/useFilters";
import { useGradingFilterConfigs } from "@/hooks/course/grading/useGradingFilterConfigs";
import { GradingReducerActions } from "@/providers/grading/reducer/types";
import { GradingReducer, initialState } from "@/providers/grading/reducer";
import useShortGrade from "@/hooks/course/grading/useShortGrade";
import { getRequestTargetFromResponseTarget } from "@/providers/grading/utils/getRequestTargetFromResponseTarget";
import useSubmissionDetails from "@/hooks/course/submission/useSubmissionDetails";
import { SubmissionDetails } from "@/interfaces/api/grade/submission";
import useSubmissionsUpdate from "@/hooks/course/submission/useSubmissionsUpdate";
import useSubmissionRequirements from "@/hooks/course/submission/useSubmissionRequirements";
import useCriteria from "@/hooks/course/grading/useCriteria";
import useTargetContext from "@/hooks/contexts/useTargetContext";
import {
  DEFAULT_GRADE_STATUS,
  DEFAULT_GROUPS,
  DEFAULT_SEARCH_BY,
  DEFAULT_SORT_BY_NAME,
  DEFAULT_SORT_ORDER_ASC,
} from "@/hooks/course/filters/useFilters/utils/filterDefaults";
import useProjectVariant from "@/hooks/course/projects/useProjectVariant";
import toast from "react-hot-toast";

export const GradingContext = createContext<
  GradingContextInterface | undefined
>(undefined);

export const GradingProvider = ({ children }: { children: ReactNode }) => {
  const { state: targetState, applyFiltersCallback } = useTargetContext();
  const { gradableEventId } = useEventParams();
  const [state, dispatch] = useReducer(GradingReducer, initialState);
  const {
    data: filterConfigs,
    isLoading: isFiltersLoading,
    isError: isFiltersError,
  } = useGradingFilterConfigs(gradableEventId);

  const filters = useFilters<GradingFilterId>(filterConfigs ?? [], "grading");
  const searchBy = useMemo(
    () => filters.getAppliedFilterValues("searchBy") ?? DEFAULT_SEARCH_BY,
    [filters]
  );
  const sortBy = useMemo(
    () => filters.getAppliedFilterValues("sortBy") ?? DEFAULT_SORT_BY_NAME,
    [filters]
  );
  const sortOrder = useMemo(
    () => filters.getAppliedFilterValues("sortOrder") ?? DEFAULT_SORT_ORDER_ASC,
    [filters]
  );
  const groups = useMemo(
    () => filters.getAppliedFilterValues("groups") ?? DEFAULT_GROUPS,
    [filters]
  );
  const gradeStatus = useMemo(
    () => filters.getAppliedFilterValues("gradeStatus") ?? DEFAULT_GRADE_STATUS,
    [filters]
  );

  useEffect(() => {
    if (isFiltersLoading || !filterConfigs) {
      return;
    }

    applyFiltersCallback({
      searchBy,
      sortBy: sortBy.map((value) => (value === "name" ? searchBy[0] : value)),
      sortOrder,
      groups,
      gradeStatus,
    });
  }, [
    sortBy,
    sortOrder,
    groups,
    gradeStatus,
    applyFiltersCallback,
    searchBy,
    isFiltersLoading,
    filterConfigs,
  ]);

  const {
    data: criteria,
    isLoading: isCriteriaLoading,
    isError: isCriteriaError,
  } = useCriteria();
  const {
    data: grade,
    isLoading: isGradeLoading,
    isError: isGradeError,
  } = useShortGrade(targetState.selectedTarget);
  const { mutate: mutateGrade } = useGradeUpdate();
  const {
    data: submissionRequirements,
    isLoading: isSubmissionRequirementsLoading,
    isError: isSubmissionRequirementsError,
  } = useSubmissionRequirements();
  const {
    data: submissionDetails,
    isLoading: isSubmissionDetailsLoading,
    isError: isSubmissionDetailsError,
  } = useSubmissionDetails(targetState.selectedTarget);
  const { mutate: mutateSubmissions } = useSubmissionsUpdate({
    target: targetState.selectedTarget,
  });
  const {
    data: projectVariants,
    isLoading: isProjectVariantsLoading,
    isError: isProjectVariantsError,
  } = useProjectVariant({ target: targetState.selectedTarget });

  useEffect(() => {
    if (!grade || !criteria) {
      return;
    }

    dispatch({
      type: GradingReducerActions.SET_GRADE,
      payload: {
        grade,
        criteria,
      },
    });
  }, [grade, criteria]);

  useEffect(() => {
    if (!submissionDetails) {
      return;
    }

    dispatch({
      type: GradingReducerActions.SET_SUBMISSION_DETAILS,
      payload: {
        submissionDetails,
      },
    });
  }, [submissionDetails]);

  const submitGrade = () => {
    if (!targetState.selectedTarget) {
      return;
    }

    const allCriteriaEmpty =
      Object.values(state.criteria).every(
        (criterion) =>
          (!criterion.gainedXp || String(criterion.gainedXp).trim() === "") &&
          (!criterion.assignedRewards || criterion.assignedRewards.length === 0)
      ) && !state.comment?.trim();

    if (allCriteriaEmpty) {
      toast.error("Nie możesz zapisać pustej oceny.");
      return;
    }

    mutateGrade({
      target: getRequestTargetFromResponseTarget(targetState.selectedTarget),
      gradableEventId,
      criteria: state.criteria,
      comment: state.comment,
    });
  };

  const submitSubmissions = (submissionDetails: SubmissionDetails) => {
    if (!targetState.selectedTarget) {
      return;
    }

    mutateSubmissions(submissionDetails);
  };

  return (
    <GradingContext.Provider
      value={{
        isFiltersLoading,
        isFiltersError,
        filters,
        state,
        dispatch,
        criteria,
        submissionRequirements,
        projectVariants,
        isGeneralDataLoading:
          isCriteriaLoading || isSubmissionRequirementsLoading,
        isGeneralDataError: isCriteriaError || isSubmissionRequirementsError,
        isSpecificDataLoading:
          isGradeLoading ||
          isSubmissionDetailsLoading ||
          isProjectVariantsLoading,
        isSpecificDataError:
          isGradeError || isSubmissionDetailsError || isProjectVariantsError,
        submitGrade,
        submitSubmissions,
      }}
    >
      {children}
    </GradingContext.Provider>
  );
};
