import { NewCardMode, NewCardModes } from "@/components/new-card/types";
import {
  GetPointsSummaryStylesProps,
  PointsSummaryMetrics,
} from "@/components/new-card/grid/points-summary/metrics/types";
import { HTMLAttributes } from "react";

export const POINTS_SUMMARY_METRICS: Record<NewCardMode, PointsSummaryMetrics> =
  {
    [NewCardModes.NORMAL]: {
      width: 300,
      minHeight: 525,
      maxHeight: 720,
    },
    [NewCardModes.COMPACT]: {
      width: 240,
      minHeight: 425,
      maxHeight: 575,
    },
  };

export function getPointsSummaryStyles({
  mode,
}: GetPointsSummaryStylesProps): HTMLAttributes<HTMLDivElement>["style"] {
  return {
    width: `${POINTS_SUMMARY_METRICS[mode].width}px`,
    maxHeight: `${POINTS_SUMMARY_METRICS[mode].maxHeight}px`,
  };
}
