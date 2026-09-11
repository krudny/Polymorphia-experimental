"use client";

import "./index.css";
import { useState } from "react";
import type { TaskTestCaseProps } from "./types";

export default function TaskTestCase({ content }: TaskTestCaseProps) {
  const [isCopied, setIsCopied] = useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText(content).then(() => setIsCopied(true));
    setTimeout(() => setIsCopied(false), 2000);
  };

  return (
    <div className="task-test-case-container group">
      <pre className="task-test-case-content">{content || "\u00A0"}</pre>
      <button
        onClick={handleCopy}
        title="Copy"
        className="task-test-case-copy-button"
      >
        <span className="task-test-case-icon">
          {isCopied ? "check" : "content_copy"}
        </span>
      </button>
    </div>
  );
}
