import { XPCardColor } from "@/components/xp-card/types";
import { Size } from "@/interfaces/general";
import { ProjectVariantWithCategoryNameResponseDTO } from "@/interfaces/api/project";

export interface ProjectVariantInfoProps {
  size?: Size;
  color?: XPCardColor;
  projectVariants: ProjectVariantWithCategoryNameResponseDTO[];
}
