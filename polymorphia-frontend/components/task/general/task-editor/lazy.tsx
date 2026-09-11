import dynamic from "next/dynamic";

export const TaskEditorLazy = dynamic(() => import("@monaco-editor/react"), {
  ssr: false,
  loading: () => <div className="task-editor-loading">Loading editor...</div>,
});
