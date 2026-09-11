import { Size } from "@/interfaces/general";

export interface UserPointsProps {
  separators?: boolean;
  titleSize?: Size;
  xpSize?: Size;
  xpDetails: Record<string, string>;
}
