import { ProjectCategoryWithVariantsResponseDTO } from "@/interfaces/api/project";

export interface UseProjectCategories {
  data: ProjectCategoryWithVariantsResponseDTO[] | undefined;
  isLoading: boolean;
  isError: boolean;
}
