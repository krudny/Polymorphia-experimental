"use client";

import "./index.css";
import { Group, Panel, Separator } from "react-resizable-panels";
import { LazyMarkdownViewer } from "@/components/markdown/lazy";
import TaskEditor from "@/components/task/general/task-editor";
import TaskResult from "@/components/task/general/task-result";
import { useTheme } from "next-themes";

export default function TaskDesktop() {
  const { resolvedTheme } = useTheme();

  return (
    <Group orientation="horizontal" className="task-desktop-group">
      <Panel defaultSize="40%" minSize="20%" className="min-w-0">
        <div className="task-desktop-description-panel">
          <LazyMarkdownViewer forceLight={resolvedTheme === "light"} />
        </div>
      </Panel>
      <Separator className="task-desktop-separator-horizontal" />
      <Panel defaultSize="60%" minSize="30%" className="min-w-0">
        <Group orientation="vertical" className="task-desktop-group">
          <Panel defaultSize="65%" minSize="20%" className="min-h-0">
            <div className="task-desktop-editor-panel">
              <TaskEditor />
            </div>
          </Panel>
          <Separator className="task-desktop-separator-vertical" />
          <Panel defaultSize="35%" minSize="10%" className="min-h-0">
            <div className="task-desktop-result-panel">
              <TaskResult />
            </div>
          </Panel>
        </Group>
      </Panel>
    </Group>
  );
}
