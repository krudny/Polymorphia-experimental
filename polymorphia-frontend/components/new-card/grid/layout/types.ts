import { NewCardProps } from "@/components/new-card/card/types";
import { GridParams } from "@/hooks/new-card/useNewCardGridParams/types";
import { PointsSummaryResponseDTO } from "@/interfaces/api/points-summary";

export interface NewCardGridLayoutProps {
  gridParams: GridParams;
  cardConfigurations: Omit<NewCardProps, "mode">[];
  usesPointsSummary: boolean;
  pointsSummaryConfiguration?: PointsSummaryResponseDTO;
}
