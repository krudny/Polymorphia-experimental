import { NewCardMode, NewCardModes } from "@/components/new-card/types";
import { NewCardProps } from "@/components/new-card/card/types";
import {
  BaseCardMetrics,
  EvaluatedCardMetrics,
  GetCardStylesProps,
  GetCardMetricsProps,
} from "@/components/new-card/card/metrics/types";
import { HTMLAttributes } from "react";

export const CARD_METRICS: Record<NewCardMode, BaseCardMetrics> = {
  [NewCardModes.NORMAL]: {
    height: 170,
    baseMinWidth: 210,
    baseMaxWidth: 290,
    widthStep: 170,
  },
  [NewCardModes.COMPACT]: {
    height: 100,
    baseMinWidth: 140,
    baseMaxWidth: 260,
    widthStep: 100,
  },
};

export function getCardMetrics({
  mode,
  stepCount,
}: GetCardMetricsProps): EvaluatedCardMetrics {
  return {
    height: CARD_METRICS[mode].height,
    minWidth:
      CARD_METRICS[mode].baseMinWidth +
      stepCount * CARD_METRICS[mode].widthStep,
    maxWidth:
      CARD_METRICS[mode].baseMaxWidth +
      stepCount * CARD_METRICS[mode].widthStep,
  };
}

export function getCardStepCount({
  leftComponent,
  rightComponent,
  sizeBonus,
}: Pick<NewCardProps, "leftComponent" | "rightComponent" | "sizeBonus">) {
  return (
    (leftComponent !== undefined ? 1 : 0) +
    (rightComponent !== undefined ? 1 : 0) +
    (sizeBonus ?? 0)
  );
}

export function getCardStyles({
  cardMetrics,
}: GetCardStylesProps): HTMLAttributes<HTMLDivElement>["style"] {
  return {
    height: `${cardMetrics.height}px`,
    minWidth: 0,
    maxWidth: `${cardMetrics.maxWidth}px`,
  };
}
