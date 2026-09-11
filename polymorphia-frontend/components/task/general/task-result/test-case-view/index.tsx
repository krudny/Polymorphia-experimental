"use client";

import "./index.css";
import TaskTestCase from "@/components/task/general/task-test-case";
import { useTaskContext } from "@/hooks/contexts/useTaskContext";

export default function TestCaseView() {
  const { activeTestCase, activeResult, runResults } = useTaskContext();

  return (
    <>
      {activeResult && runResults !== null && (
        <div>
          {activeResult.passed ? (
            <h3 className="task-result-status-accepted">Zaakceptowano</h3>
          ) : (
            <h3 className="task-result-status-rejected">Odrzucono</h3>
          )}
        </div>
      )}

      {activeTestCase && (
        <div className="task-result-section-list">
          <div className="task-result-section-item">
            <h4 className="task-result-section-title">Wejście</h4>
            <TaskTestCase content={activeTestCase.input} />
          </div>

          {runResults !== null && (
            <div className="task-result-section-item">
              <h4 className="task-result-section-title">Twoje wyjście</h4>
              <TaskTestCase
                content={
                  activeResult?.actualOutput || activeResult?.stderr || ""
                }
              />
            </div>
          )}

          <div className="task-result-section-item">
            <h4 className="task-result-section-title">Oczekiwane wyjście</h4>
            <TaskTestCase content={activeTestCase.expectedOutput} />
          </div>
        </div>
      )}
    </>
  );
}
