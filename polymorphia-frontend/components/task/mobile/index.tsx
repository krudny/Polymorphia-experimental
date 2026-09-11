"use client";

import "./index.css";
import { Group, Panel, Separator } from "react-resizable-panels";
import { LazyMarkdownViewer } from "@/components/markdown/lazy";
import TaskEditor from "@/components/task/general/task-editor";
import TaskResult from "@/components/task/general/task-result";
import useTaskCollapsiblePanel from "@/hooks/course/tasks/useTaskCollapsiblePanel";
import { useTheme } from "next-themes";
import TaskMobilePanelHeader from "./panel-header";

const COLLAPSED_SIZE = "56px";

export default function TaskMobile() {
  const { resolvedTheme } = useTheme();
  const descriptionPanel = useTaskCollapsiblePanel(false);
  const editorPanel = useTaskCollapsiblePanel(true);
  const resultPanel = useTaskCollapsiblePanel(true);

  return (
    <Group orientation="vertical" className="tasks-mobile-group h-full w-full">
      <Panel
        panelRef={descriptionPanel.ref}
        defaultSize="40%"
        minSize={COLLAPSED_SIZE}
        collapsible={true}
        collapsedSize={COLLAPSED_SIZE}
        onResize={(size) =>
          descriptionPanel.setIsCollapsed(size.inPixels <= 56)
        }
        className="min-h-0"
      >
        <div className="task-mobile-panel-container">
          <TaskMobilePanelHeader
            title="Opis zadania"
            isCollapsed={descriptionPanel.isCollapsed}
            onToggle={descriptionPanel.toggle}
          />
          <div className="task-mobile-description-content">
            <LazyMarkdownViewer forceLight={resolvedTheme === "light"} />
          </div>
        </div>
      </Panel>
      <Separator className="task-mobile-separator" />
      <Panel
        panelRef={editorPanel.ref}
        defaultSize="30%"
        minSize={COLLAPSED_SIZE}
        collapsible={true}
        collapsedSize={COLLAPSED_SIZE}
        onResize={(size) => editorPanel.setIsCollapsed(size.inPixels <= 56)}
        className="min-h-0"
      >
        <div className="task-mobile-panel-container">
          <TaskMobilePanelHeader
            title="Edytor"
            isCollapsed={editorPanel.isCollapsed}
            onToggle={editorPanel.toggle}
          />
          <div className="task-mobile-panel-content">
            <TaskEditor />
          </div>
        </div>
      </Panel>
      <Separator className="task-mobile-separator" />
      <Panel
        panelRef={resultPanel.ref}
        defaultSize="30%"
        minSize={COLLAPSED_SIZE}
        collapsible={true}
        collapsedSize={COLLAPSED_SIZE}
        onResize={(size) => resultPanel.setIsCollapsed(size.inPixels <= 56)}
        className="min-h-0"
      >
        <div className="task-mobile-panel-container">
          <TaskMobilePanelHeader
            title="Wyniki"
            isCollapsed={resultPanel.isCollapsed}
            onToggle={resultPanel.toggle}
          />
          <div className="task-mobile-panel-content">
            <TaskResult />
          </div>
        </div>
      </Panel>
    </Group>
  );
}
