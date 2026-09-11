import { NewCardMode } from "@/components/new-card/types";
import { PointsSummaryDetailsResponseDTO } from "@/interfaces/api/points-summary";

export interface NewPointsSummaryElementProps {
  mode: NewCardMode;
  bonus: PointsSummaryDetailsResponseDTO;
  onClick?: () => void;
  inline?: boolean;
  isDesktop: boolean;
}
