import { PointsSummaryResponseDTO } from "@/interfaces/api/points-summary";
import { NewCardMode } from "@/components/new-card/types";

export interface NewPointsSummaryProps {
  pointsSummary?: PointsSummaryResponseDTO;
  mode: NewCardMode;
  isDesktop: boolean;
}
