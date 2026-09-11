import { XPCardColor } from "@/components/xp-card/types";
import { ReactNode } from "react";

export interface NewCardTextAccessoryProps {
  backgroundColor: XPCardColor;
  topText?: string;
  bottomText?: string;
  additionalView?: ReactNode;
}
