import { useState } from "react";
import { usePanelRef } from "react-resizable-panels";
import type { UseTaskCollapsiblePanel } from "./types";

export default function useTaskCollapsiblePanel(
  initialCollapsed: boolean
): UseTaskCollapsiblePanel {
  const ref = usePanelRef();
  const [isCollapsed, setIsCollapsed] = useState(initialCollapsed);

  const toggle = () => {
    const panel = ref.current;
    if (!panel) return;
    if (panel.isCollapsed()) {
      panel.expand();
    } else {
      panel.collapse();
    }
  };

  return {
    ref,
    isCollapsed,
    setIsCollapsed,
    toggle,
  };
}
