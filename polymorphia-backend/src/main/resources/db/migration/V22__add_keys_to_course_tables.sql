ALTER TABLE criteria
    ADD key VARCHAR(64);

ALTER TABLE event_sections
    ADD key VARCHAR(64);

ALTER TABLE evolution_stages
    ADD key VARCHAR(64);

ALTER TABLE gradable_events
    ADD key VARCHAR(64);

ALTER TABLE project_variant_categories
    ADD key VARCHAR(64);

ALTER TABLE project_variants
    ADD key VARCHAR(64);

ALTER TABLE rewards
    ADD key VARCHAR(64);

ALTER TABLE submission_requirements
    ADD key VARCHAR(64);

UPDATE criteria
SET key = CONCAT('criterion_', id);
UPDATE submission_requirements
SET key = CONCAT('submission_requirement_', id);
UPDATE event_sections
SET key = CONCAT('event_section_', id);
UPDATE gradable_events
SET key = CONCAT('gradable_event_', id);
UPDATE evolution_stages
SET key = CONCAT('evolution_stage_', id);
UPDATE project_variant_categories
SET key = CONCAT('project_variant_category_', id);
UPDATE project_variants
SET key = CONCAT('project_variant_', id);
UPDATE rewards
SET key = CONCAT('reward_', id);

ALTER TABLE event_sections
    ALTER COLUMN key SET NOT NULL;
ALTER TABLE gradable_events
    ALTER COLUMN key SET NOT NULL;
ALTER TABLE evolution_stages
    ALTER COLUMN key SET NOT NULL;
ALTER TABLE project_variant_categories
    ALTER COLUMN key SET NOT NULL;
ALTER TABLE project_variants
    ALTER COLUMN key SET NOT NULL;
ALTER TABLE rewards
    ALTER COLUMN key SET NOT NULL;
ALTER TABLE criteria
    ALTER COLUMN key SET NOT NULL;
ALTER TABLE submission_requirements
    ALTER COLUMN key SET NOT NULL;

CREATE TABLE assignments
(
    id BIGINT NOT NULL,
    CONSTRAINT pk_assignments PRIMARY KEY (id)
);

CREATE TABLE tests
(
    id BIGINT NOT NULL,
    CONSTRAINT pk_tests PRIMARY KEY (id)
);

ALTER TABLE assignments
    ADD CONSTRAINT FK_ASSIGNMENTS_ON_ID FOREIGN KEY (id) REFERENCES gradable_events (id);

ALTER TABLE tests
    ADD CONSTRAINT FK_TESTS_ON_ID FOREIGN KEY (id) REFERENCES gradable_events (id);

INSERT INTO assignments(id)
SELECT ge.id
FROM gradable_events ge
         JOIN assignment_sections asn ON ge.event_section_id = asn.id;

INSERT INTO tests(id)
SELECT ge.id
FROM gradable_events ge
         JOIN test_sections ts ON ge.event_section_id = ts.id;