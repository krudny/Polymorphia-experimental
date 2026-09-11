import { Dispatch } from "react";
import { useFilters } from "@/hooks/course/filters/useFilters";
import {
  GradingReducerActionType,
  GradingReducerState,
} from "@/providers/grading/reducer/types";
import {
  SubmissionDetails,
  SubmissionRequirementResponseDTO,
} from "@/interfaces/api/grade/submission";
import { CriterionResponseDTO } from "@/interfaces/api/grade/criteria";
import { ProjectVariantWithCategoryNameResponseDTO } from "@/interfaces/api/project";

export interface GradingContextInterface {
  isFiltersLoading: boolean;
  isFiltersError: boolean;
  filters: ReturnType<typeof useFilters<GradingFilterId>>;
  criteria?: CriterionResponseDTO[];
  submissionRequirements?: SubmissionRequirementResponseDTO[];
  projectVariants?: ProjectVariantWithCategoryNameResponseDTO[];
  isGeneralDataLoading: boolean;
  isGeneralDataError: boolean;
  isSpecificDataLoading: boolean;
  isSpecificDataError: boolean;
  state: GradingReducerState;
  dispatch: Dispatch<GradingReducerActionType>;
  submitGrade: () => void;
  submitSubmissions: (submissionDetails: SubmissionDetails) => void;
}

export type GradingFilterId =
  "searchBy" | "sortOrder" | "sortBy" | "groups" | "gradeStatus";
