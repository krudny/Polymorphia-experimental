export interface ProjectVariantResponseDTO {
  id: number;
  name: string;
  shortCode: string;
  imageUrl: string;
}

export interface ProjectVariantWithCategoryNameResponseDTO extends ProjectVariantResponseDTO {
  categoryName: string;
}

export interface ProjectCategoryWithVariantsResponseDTO {
  id: number;
  name: string;
  variants: ProjectVariantResponseDTO[];
}

export interface ProjectGroupConfigurationResponseDTO {
  studentIds: number[];
  selectedVariants: Record<number, number>; // maps categoryId to variantId
}
