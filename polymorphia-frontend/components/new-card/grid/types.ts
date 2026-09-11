import { PointsSummaryResponseDTO } from "@/interfaces/api/points-summary";
import { RefObject } from "react";
import { NewCardProps } from "@/components/new-card/card/types";

export interface NewCardGridViewProps {
  ref: RefObject<HTMLDivElement | null>;
  cardConfigurations: Omit<NewCardProps, "mode">[];
  usesPointsSummary: boolean;
  pointsSummaryConfiguration?: PointsSummaryResponseDTO;
  mobileRows?: number;
}
