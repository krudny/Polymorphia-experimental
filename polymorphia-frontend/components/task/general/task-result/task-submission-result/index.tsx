"use client";

import "./index.css";
import TaskTestCase from "@/components/task/general/task-test-case";
import { useTaskContext } from "@/hooks/contexts/useTaskContext";
import { TaskSubmissionStatus } from "@/interfaces/api/tasks/types";
import ErrorComponent from "@/components/error";
import { formatDate } from "@/utils/date";

export default function TaskSubmissionResultView() {
  const { submissionStatus, isSubmitting } = useTaskContext();

  if (isSubmitting) {
    return (
      <div className="task-result-submission-loading">
        Przetwarzanie zgłoszenia...
      </div>
    );
  }

  if (!submissionStatus) {
    return <ErrorComponent message="Nie udało się załadować wyniku." />;
  }

  const isAccepted =
    submissionStatus.passedCount != null &&
    submissionStatus.totalCount != null &&
    submissionStatus.passedCount === submissionStatus.totalCount &&
    submissionStatus.status === TaskSubmissionStatus.COMPLETED;

  const firstFailedResult = submissionStatus.visibleResults?.find(
    (result) => !result.passed
  );

  return (
    <div className="task-result-submission-details">
      <div className="task-result-submission-header">
        <div className="task-result-submission-header-main">
          <span
            className={
              isAccepted
                ? "task-result-status-accepted"
                : "task-result-status-rejected"
            }
          >
            {isAccepted ? "Zaakceptowano" : "Odrzucono"}
          </span>
          <span className="task-result-submission-passed-count">
            {submissionStatus.passedCount ?? 0} /{" "}
            {submissionStatus.totalCount ?? 0} przypadków testowych
          </span>
          <span className="task-result-submission-score">
            Ocena:{" "}
            {submissionStatus.score != null
              ? `${submissionStatus.score}%`
              : "Brak"}
          </span>
        </div>
        <div className="task-result-submission-meta">
          <span>Czas: {submissionStatus.totalExecutionTimeMs ?? 0}ms</span>
          <span>•</span>
          <span>Wysłano: {formatDate(submissionStatus.createdDate)}</span>
        </div>
      </div>

      {!isAccepted && firstFailedResult && (
        <div className="task-result-section-list">
          <div className="task-result-section-item">
            <h4 className="task-result-section-title">Wejście</h4>
            <TaskTestCase content={firstFailedResult.input} />
          </div>

          <div className="task-result-section-item">
            <h4 className="task-result-section-title">Twoje wyjście</h4>
            <TaskTestCase
              content={
                firstFailedResult.actualOutput || firstFailedResult.stderr || ""
              }
            />
          </div>

          <div className="task-result-section-item">
            <h4 className="task-result-section-title">Oczekiwane wyjście</h4>
            <TaskTestCase content={firstFailedResult.expectedOutput} />
          </div>
        </div>
      )}
    </div>
  );
}
