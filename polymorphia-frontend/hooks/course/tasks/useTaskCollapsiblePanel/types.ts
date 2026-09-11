import type { RefObject } from "react";
import type { PanelImperativeHandle } from "react-resizable-panels";

export interface UseTaskCollapsiblePanel {
  ref: RefObject<PanelImperativeHandle | null>;
  isCollapsed: boolean;
  setIsCollapsed: (collapsed: boolean) => void;
  toggle: () => void;
}
