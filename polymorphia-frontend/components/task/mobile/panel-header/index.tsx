"use client";

import "./index.css";
import type { TaskMobilePanelHeaderProps } from "./types";

export default function TaskMobilePanelHeader({
  title,
  isCollapsed,
  onToggle,
}: TaskMobilePanelHeaderProps) {
  return (
    <button onClick={onToggle} className="task-mobile-header">
      <span className="task-mobile-header-title">{title}</span>
      <span className="task-mobile-header-chevron">
        {isCollapsed ? "expand_more" : "expand_less"}
      </span>
    </button>
  );
}
