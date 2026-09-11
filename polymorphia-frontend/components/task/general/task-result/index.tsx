"use client";

import "./index.css";
import TestCaseTabs from "./test-case-tabs";
import TestCaseView from "./test-case-view";
import TaskSubmissionResultView from "./task-submission-result";
import TaskActionsBar from "./task-actions-bar";
import { useTaskContext } from "@/hooks/contexts/useTaskContext";
import { TaskTab } from "./types";

export default function TaskResult() {
  const { activeTab, hasSubmission } = useTaskContext();

  return (
    <div className="task-result-container">
      <div className="task-result-content">
        <TestCaseTabs />
        {activeTab === TaskTab.TESTCASES && <TestCaseView />}
        {hasSubmission && activeTab === TaskTab.SUBMISSION_RESULT && (
          <TaskSubmissionResultView />
        )}
      </div>
      <TaskActionsBar />
    </div>
  );
}
