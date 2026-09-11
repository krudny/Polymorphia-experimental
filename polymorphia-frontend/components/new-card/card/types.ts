import { NewCardMode } from "@/components/new-card/types";
import { XPCardColor } from "@/components/xp-card/types";
import { ReactNode } from "react";

export interface NewCardProps {
  mode: NewCardMode;
  title: string;
  subtitle?: string;
  details?: string;
  leftComponent?: (props: {
    mode: NewCardMode;
    color: XPCardColor;
  }) => ReactNode;
  rightComponent?: (props: {
    mode: NewCardMode;
    color: XPCardColor;
  }) => ReactNode;
  onClick?: () => void;
  useDynamicBehavior?: () => { onClick: () => void };
  color: XPCardColor;
  sizeBonus?: number;
  isLocked?: boolean;
}
