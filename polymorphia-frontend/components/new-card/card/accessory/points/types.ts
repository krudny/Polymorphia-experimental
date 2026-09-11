import { NewCardMode } from "@/components/new-card/types";
import { XPCardColor } from "@/components/xp-card/types";

export interface NewCardPointsAccessoryProps {
  mode: NewCardMode;
  points?: string;
  isSumLabelVisible?: boolean;
  isXPLabelVisible?: boolean;
  hasChest?: boolean;
  shouldGrayOutReward?: boolean;
  backgroundColor: XPCardColor;
}
