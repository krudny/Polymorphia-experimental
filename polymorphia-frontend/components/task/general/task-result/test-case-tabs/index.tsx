"use client";

import "./index.css";
import ButtonWithBorder from "@/components/button";
import { useTaskContext } from "@/hooks/contexts/useTaskContext";
import type { TestCaseResultDTO } from "@/interfaces/api/tasks/types";
import { TaskTab, TabOutcome } from "../types";
import { TaskActions } from "@/providers/task/reducer/types";

const getTabOutcome = (
  testCaseResult: TestCaseResultDTO | undefined
): TabOutcome => {
  if (!testCaseResult) {
    return TabOutcome.UNKNOWN;
  }
  return testCaseResult.passed ? TabOutcome.PASSED : TabOutcome.FAILED;
};

const getTabColorClass = (outcome: TabOutcome, isSelected: boolean): string => {
  switch (outcome) {
    case TabOutcome.PASSED:
      return isSelected
        ? "bg-primary-success! text-primary-dark! border-primary-success!"
        : "border-primary-success! text-secondary-gray! hover:bg-primary-success! hover:text-primary-dark!";
    case TabOutcome.FAILED:
      return isSelected
        ? "bg-primary-error! text-primary-dark! border-primary-error!"
        : "border-primary-error! text-secondary-gray! hover:bg-primary-error! hover:text-primary-dark!";
    case TabOutcome.UNKNOWN:
      return isSelected ? "bg-secondary-gray! text-primary-dark!" : "";
  }
};

export default function TestCaseTabs() {
  const {
    testCases,
    resultsByOrderIndex,
    activeTab,
    activeTestCaseIndex,
    dispatch,
    hasSubmission,
  } = useTaskContext();

  return (
    <div className="task-result-tabs">
      {testCases.map((testCase, index) => {
        const testCaseResult = resultsByOrderIndex.get(testCase.orderIndex);
        const isSelected =
          activeTab === TaskTab.TESTCASES && index === activeTestCaseIndex;

        const outcome = getTabOutcome(testCaseResult);

        return (
          <ButtonWithBorder
            key={index}
            text={testCase.name || `Test ${index + 1}`}
            size="sm"
            forceLight={true}
            className={`mx-0! rounded-lg! ${getTabColorClass(outcome, isSelected)}`}
            onClick={() => {
              dispatch({
                type: TaskActions.SET_ACTIVE_TAB,
                payload: TaskTab.TESTCASES,
              });
              dispatch({
                type: TaskActions.SET_ACTIVE_TEST_CASE_INDEX,
                payload: index,
              });
            }}
          />
        );
      })}

      {hasSubmission && (
        <ButtonWithBorder
          text="Wynik"
          size="sm"
          forceLight={true}
          className={`mx-0! rounded-lg! ${
            activeTab === TaskTab.SUBMISSION_RESULT
              ? "bg-secondary-gray! text-primary-dark!"
              : ""
          }`}
          onClick={() =>
            dispatch({
              type: TaskActions.SET_ACTIVE_TAB,
              payload: TaskTab.SUBMISSION_RESULT,
            })
          }
        />
      )}
    </div>
  );
}
