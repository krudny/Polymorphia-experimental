import { NewCardMode } from "@/components/new-card/types";

export interface GridParams {
  cols: number;
  rows: number;
  mode: NewCardMode;
  isDesktop: boolean;
  isReady: boolean;
  cardMaxWidth: number;
}
