export const TaskTab = {
  TESTCASES: "testcases",
  SUBMISSION_RESULT: "submissionResult",
} as const;

export type TaskTab = (typeof TaskTab)[keyof typeof TaskTab];

export const TabOutcome = {
  PASSED: "passed",
  FAILED: "failed",
  UNKNOWN: "unknown",
} as const;

export type TabOutcome = (typeof TabOutcome)[keyof typeof TabOutcome];
