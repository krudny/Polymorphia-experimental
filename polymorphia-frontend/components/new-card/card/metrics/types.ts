import { NewCardMode } from "@/components/new-card/types";

export interface BaseCardMetrics {
  height: number;
  baseMinWidth: number;
  baseMaxWidth: number;
  widthStep: number;
}

export interface EvaluatedCardMetrics {
  height: number;
  minWidth: number;
  maxWidth: number;
}

export interface GetCardMetricsProps {
  mode: NewCardMode;
  stepCount: number;
}

export interface GetCardStylesProps {
  cardMetrics: EvaluatedCardMetrics;
}
