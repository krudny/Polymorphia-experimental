"use client";

import "./index.css";
import dynamic from "next/dynamic";
import { BeforeMount, Editor, OnMount } from "@monaco-editor/react";
import Selector from "@/components/selector";
import { useTaskContext } from "@/hooks/contexts/useTaskContext";

import { mapSupportedLanguageToMonaco } from "./mapper";
import { TaskEditorLazy } from "@/components/task/general/task-editor/lazy";
import { TaskActions } from "@/providers/task/reducer/types";
import type { SupportedLanguage } from "@/interfaces/api/tasks/types";

const defineGlassTheme: BeforeMount = (monaco) => {
  monaco.editor.defineTheme("glass-dark", {
    base: "vs-dark",
    inherit: true,
    rules: [],
    colors: {
      "editor.background": "#262626",
      "editor.foreground": "#fafafa",
      "editor.lineHighlightBackground": "#262626",
      "editorGutter.background": "#262626",
      "editorLineNumber.foreground": "#fafafa",
      "editorLineNumber.activeForeground": "#fafafa",
      "editor.selectionBackground": "#3b82f680",
      "editor.lineHighlightBorder": "#00000000",
      "editorIndentGuide.background1": "#262626",
      "minimap.background": "#00000000",
      "scrollbar.shadow": "#00000000",
      "scrollbarSlider.background": "#ffffff1a",
      "scrollbarSlider.hoverBackground": "#ffffff2a",
      "editorWidget.background": "#1e293bf2",
      "editorWidget.border": "#ffffff1a",
      "editorSuggestWidget.background": "#262626",
    },
  });
};

export default function TaskEditor() {
  const { editorRef, language, dispatch, sampleCode, allowedLanguages } =
    useTaskContext();

  const handleBeforeMount: BeforeMount = (monaco) => {
    defineGlassTheme(monaco);
  };

  const handleMount: OnMount = (editor) => {
    editorRef.current = editor;
  };

  return (
    <div className="task-editor-container">
      <div className="task-editor-toolbar">
        <div className="task-editor-selector">
          <Selector
            options={allowedLanguages}
            value={language}
            onChange={(value) =>
              dispatch({
                type: TaskActions.SET_LANGUAGE,
                payload: value as SupportedLanguage,
              })
            }
            size="md"
            padding="sm"
            className="rounded-lg!"
          />
        </div>
      </div>
      <div className="task-editor-content">
        <TaskEditorLazy
          key={language}
          height="100%"
          theme="glass-dark"
          language={mapSupportedLanguageToMonaco(language)}
          defaultValue={sampleCode}
          beforeMount={handleBeforeMount}
          onMount={handleMount}
          options={{
            automaticLayout: true,
            minimap: { enabled: false },
            fontSize: 14,
            padding: { top: 12 },
            scrollBeyondLastLine: false,
            renderLineHighlight: "none",
            overviewRulerLanes: 0,
          }}
        />
      </div>
    </div>
  );
}
