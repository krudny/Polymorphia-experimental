import { NewCardMode, NewCardModes } from "@/components/new-card/types";
import {
  GetPointsSummaryElementStylesProps,
  PointsSummaryElementMetrics,
} from "@/components/new-card/grid/points-summary/element/metrics/types";
import { HTMLAttributes } from "react";

export const POINTS_SUMMARY_ELEMENT_METRICS: Record<
  NewCardMode,
  PointsSummaryElementMetrics
> = {
  [NewCardModes.NORMAL]: {
    height: 130,
  },
  [NewCardModes.COMPACT]: {
    height: 105,
  },
};

export function getPointsSummaryElementStyles({
  mode,
}: GetPointsSummaryElementStylesProps): HTMLAttributes<HTMLDivElement>["style"] {
  return {
    maxHeight: `${POINTS_SUMMARY_ELEMENT_METRICS[mode].height}px`,
  };
}
