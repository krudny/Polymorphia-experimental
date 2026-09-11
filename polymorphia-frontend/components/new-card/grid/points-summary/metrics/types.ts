import { NewCardMode } from "@/components/new-card/types";

export interface PointsSummaryMetrics {
  width: number;
  minHeight: number;
  maxHeight: number;
}

export interface GetPointsSummaryStylesProps {
  mode: NewCardMode;
}
