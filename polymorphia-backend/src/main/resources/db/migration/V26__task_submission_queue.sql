ALTER TABLE task_submissions
    RENAME COLUMN attempt TO user_attempt;

ALTER TABLE task_submissions
    ADD COLUMN locked_until TIMESTAMPTZ,
    ADD COLUMN processing_attempts INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN is_graded BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_task_submissions_status_created_date
    ON task_submissions (status, created_date);

ALTER TABLE task_submissions
    ADD CONSTRAINT uc_task_submissions_task_animal_attempt
    UNIQUE (task_id, animal_id, user_attempt);

ALTER TABLE task_submission_results
    DROP COLUMN IF EXISTS actual_output;

ALTER TABLE task_submission_results
    DROP CONSTRAINT IF EXISTS uq_submission_result_submission_test_case;

ALTER TABLE task_submission_results
    ADD CONSTRAINT uc_task_submission_results_submission_test_case
    UNIQUE (submission_id, test_case_id);
