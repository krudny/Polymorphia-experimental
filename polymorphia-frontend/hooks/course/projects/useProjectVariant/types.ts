import { ProjectVariantWithCategoryNameResponseDTO } from "@/interfaces/api/project";
import { TargetRequestDTO } from "@/interfaces/api/target";

export interface UseProjectVariant {
  data: ProjectVariantWithCategoryNameResponseDTO[] | undefined;
  isLoading: boolean;
  isError: boolean;
}

export interface UseProjectVariantProps {
  target: TargetRequestDTO | null;
}
