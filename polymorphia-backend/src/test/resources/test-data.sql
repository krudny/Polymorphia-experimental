-- Truncate
TRUNCATE TABLE users cascade;
TRUNCATE TABLE chests_items cascade;
TRUNCATE TABLE courses cascade;
TRUNCATE TABLE coordinators cascade;
TRUNCATE TABLE user_course_roles cascade;
TRUNCATE TABLE course_groups cascade;
TRUNCATE TABLE animals cascade;
TRUNCATE TABLE students_course_groups cascade;
TRUNCATE TABLE evolution_stages cascade;
TRUNCATE TABLE event_sections cascade;
TRUNCATE TABLE rewards cascade;
TRUNCATE TABLE project_groups cascade;
TRUNCATE TABLE project_groups_animals cascade;
TRUNCATE TABLE project_groups_project_variants cascade;
TRUNCATE TABLE submission_requirements cascade;
TRUNCATE TABLE submissions cascade;
TRUNCATE TABLE teaching_role_users cascade;

-- Users
INSERT INTO users (id, first_name, last_name, email, password, preferred_course_id)
VALUES (3, 'Michał', 'Kowalski', 'coordinator@agh.com', '$2a$10$k/sZH/gK6qzlLpHw1MqEFOpPXTBi17gdlIs84q2MmevjqsoHWNF4O',null),
       (2, 'Piotr', 'Budynek', 'student@agh.com', '$2a$10$k/sZH/gK6qzlLpHw1MqEFOpPXTBi17gdlIs84q2MmevjqsoHWNF4O', null),
       (4, 'Sławomir', 'Nowak', 'instructor@agh.com', '$2a$10$k/sZH/gK6qzlLpHw1MqEFOpPXTBi17gdlIs84q2MmevjqsoHWNF4O',null),
       (5, 'Alicja', 'Nowak', 'anowak@agh.com', '$2a$10$k/sZH/gK6qzlLpHw1MqEFOpPXTBi17gdlIs84q2MmevjqsoHWNF4O', null),
       (6, 'Aleksander', 'Wielki', 'awielki@agh.com', '$2a$10$k/sZH/gK6qzlLpHw1MqEFOpPXTBi17gdlIs84q2MmevjqsoHWNF4O', null),
       (7, 'Tomek', 'Wtorek', 'twtorek@agh.com', '$2a$10$k/sZH/gK6qzlLpHw1MqEFOpPXTBi17gdlIs84q2MmevjqsoHWNF4O', null),
       (8, 'Andrzej', 'Bednarek', 'abednarek@agh.com', '$2a$10$k/sZH/gK6qzlLpHw1MqEFOpPXTBi17gdlIs84q2MmevjqsoHWNF4O', null),
       (9, 'Karol', 'Wójcik', 'kwojcik@agh.com', '$2a$10$k/sZH/gK6qzlLpHw1MqEFOpPXTBi17gdlIs84q2MmevjqsoHWNF4O', null),
       (10, 'Andrzej', 'Bednarek', 'abednarek2@agh.com', '$2a$10$k/sZH/gK6qzlLpHw1MqEFOpPXTBi17gdlIs84q2MmevjqsoHWNF4O', null),
       (11, 'Wojciech', 'Kot', 'instructor2@agh.com', '$2a$10$k/sZH/gK6qzlLpHw1MqEFOpPXTBi17gdlIs84q2MmevjqsoHWNF4O',
        null);

-- Coordinators
INSERT INTO teaching_role_users(user_id)
VALUES (3);

INSERT INTO coordinators(user_id)
VALUES (3);

-- Courses
INSERT INTO courses (id, name, coordinator_id, coordinator_image_url, instructor_image_url, image_url, markdown, markdown_source_url)
VALUES (1, 'Programowanie Obiektowe', 3, '/coord_url', '/instr_url', 'img_url', '', ''),
       (2, 'Programowanie Obiektowe', 3, '/coord_url', '/instr_url', 'img_url', '', ''),
       (3, 'Programowanie Obiektowe', 3, '/coord_url', '/instr_url', 'img_url', '', ''),
       (4, 'Programowanie Obiektowe', 3, '/coord_url', '/instr_url', 'img_url', '', '');


insert into users(id, first_name, last_name, email, password, preferred_course_id)
values (12, 'Sample', 'User', 'sampleuser@test.com', '$2a$10$k/sZH/gK6qzlLpHw1MqEFOpPXTBi17gdlIs84q2MmevjqsoHWNF4O',
        4),
       (13, 'Sample', 'Instructor', 'sampleinstructor@test.com', '$2a$10$k/sZH/gK6qzlLpHw1MqEFOpPXTBi17gdlIs84q2MmevjqsoHWNF4O', 4);
-- Students
INSERT INTO students(user_id, index_number)
VALUES (2, 123456),
       (5, 121212),
       (6, 131313),
       (7, 141414),
       (8, 151515),
       (9, 161616),
       (10, 171717),
       (12, 120000);

UPDATE users
SET preferred_course_id=1
WHERE id in (2, 5, 11);

-- Instructors
INSERT INTO teaching_role_users(user_id)
VALUES (4),
       (11),
       (13);

INSERT INTO instructors(user_id)
VALUES (4),
       (11),
       (13);

-- UserCourseRoles
INSERT INTO user_course_roles(user_id, course_id, role)
VALUES (3, 1, 'COORDINATOR'),
       (3, 2, 'COORDINATOR'),
       (4, 1, 'INSTRUCTOR'),
       (4, 3, 'COORDINATOR'),
       (2, 1, 'STUDENT'),
       (5, 1, 'STUDENT'),
       (6, 1, 'STUDENT'),
       (7, 1, 'STUDENT'),
       (8, 1, 'STUDENT'),
       (9, 1, 'STUDENT'),
       (10, 1, 'STUDENT'),
       (11, 1, 'INSTRUCTOR'),
       (12, 4, 'STUDENT'),
       (4, 4, 'INSTRUCTOR'),
       (13, 4, 'INSTRUCTOR');

-- Course Groups
INSERT INTO course_groups (id, name, room, course_id, teaching_role_user_id)
VALUES (1, 'MI-SR12', '3.27', 1, 4),
       (2, 'BM-SR13', '3.28', 1, 11),
       (3, 'BM-SR15', '1.37', 1, 11),
       (4, 'BM-SR15', '4.27', 4, 4),
       (5, 'BM-SR16', '3.27', 4, 13);

-- Animals
INSERT INTO animals (id, name)
VALUES (1, 'sowa'),

       (2, 'hof_1'),
       (3, 'hof_2'),
       (4, 'hof_3'),
       (5, 'hof_4'),
       (6, 'hof_6'),
       (7, 'hof_5'),
       (8, 'fenek');

-- Student Course Group Assignments
INSERT INTO students_course_groups (animal_id, student_id, course_group_id)
VALUES (1, 2, 1),

       (2, 5, 2),
       (3, 6, 2),
       (4, 7, 2),
       (5, 8, 2),
       (6, 9, 3),
       (7, 10, 2),
       (8, 12, 4);


-- Evolution Stages
INSERT INTO evolution_stages (id, key, name, min_xp, description, grade, image_url, order_index, course_id)
VALUES (1, 'evolution_stages1', 'Pisklak', 20, 'description', 2.0, 'imageUrl_pisklak', 1, 1),
       (2, 'evolution_stages2', 'Jajo', 0, 'description', 2.0, 'imageUrl_jajo', 0, 1),
       (3, 'evolution_stages3', 'Podlot', 50, 'description', 3.0, 'imageUrl_podlot', 2, 1),
       (4, 'evolution_stages14', 'Podlot2', 50, 'description2', 3.0, 'imageUrl2', 3, 2);

-- Event sections
INSERT INTO event_sections (id, key, has_gradable_events_with_topics, is_hidden, is_shown_in_road_map, name,
                            order_index, course_id)
VALUES (1, 'event_sections1', true, false, true, 'Kartkówka', 2, 1),
       (2, 'event_sections2', true, false, true, 'Lab', 1, 1),
       (3, 'event_sections3', true, false, true, 'Kartkówka', 2, 4),
       (4, 'event_sections4', true, false, true, 'Laboratorium', 1, 4),
       (5, 'event_sections5', true, false, true, 'Projekt', 3, 4);

-- Test sections
INSERT INTO public.test_sections (id)
VALUES (1),
       (3);

-- Assignment sections
INSERT INTO public.assignment_sections (id)
VALUES (2),
       (4);

-- Project sections
INSERT INTO public.project_sections (id)
VALUES (5);

-- Rewards
INSERT INTO public.rewards (id, key, description, image_url, name, order_index, course_id)
VALUES (1, 'rewards1', 'desc', 'url', 'Apteczka', 1, 1),
       (2, 'rewards2', 'desc', 'url', 'Apteczka2', 2, 1),
       (3, 'rewards3', 'desc', 'url', 'Marchewka', 0, 1),
       (4, 'rewards4', 'desc', 'url', 'Srebrna', 0, 1),
       (5, 'rewards5', 'desc', 'url', 'Złota', 1, 1),
       (6, 'rewards6', 'desc', 'url', 'ALL', 3, 4),
       (7, 'rewards7', 'desc', 'url', 'ONE_OF_MANY', 2, 4),
       (8, 'rewards8', 'desc', 'percentageBonus_url', 'PercentageBonusItem', 0, 4),
       (9, 'rewards9', 'desc', 'url', 'limit reached', 1, 4),
       (10, 'rewards10', 'desc', 'url', 'flatbonus ONE', 2, 4),
       (11, 'rewards11', 'desc', 'url', 'flatbonus multiple', 3, 4),
       (12, 'rewards12', 'desc', 'url', 'ONE_OF_MANY reached limit', 4, 4);

-- Items
INSERT INTO public.items ("limit", reward_id, event_section_id)
VALUES (2, 1, 1),
       (2, 2, 1),
       (2, 3, 1),
       (2, 8, 4),
       (1, 9, 3),
       (4, 10, 3),
       (2, 11, 3);

-- Flat bonus items
INSERT INTO public.flat_bonus_items (xp_bonus, item_id, behavior)
VALUES (2.0, 1, 'ONE_EVENT'),
       (3.0, 2, 'MULTIPLE_EVENTS'),
       (2.5, 9, 'ONE_EVENT'),
       (1.5, 10, 'ONE_EVENT'),
       (4.0, 11, 'MULTIPLE_EVENTS');

-- Percentage bonus items
INSERT INTO public.percentage_bonus_items (percentage_bonus, item_id)
VALUES (10, 3),
       (5, 8);

-- Chests
INSERT INTO public.chests (behavior, reward_id)
VALUES ('ONE_OF_MANY', 4),
       ('ALL', 5),
       ('ONE_OF_MANY', 7),
       ('ALL', 6),
       ('ONE_OF_MANY', 12);

-- Chests_items
INSERT INTO public.chests_items (item_id, chest_id)
VALUES (1, 4),
       (2, 4),
       (1, 5),
       (2, 5),
       (3, 5),
       (9, 6),
       (10, 6),
       (11, 6),
       (8, 7),
       (9, 7),
       (10, 6),
       (9, 12),
       (9, 12);

-- Gradable events
INSERT INTO public.gradable_events (id, key, event_section_id, name, topic, order_index, road_map_order_index,
                                    markdown_source_url, markdown, is_hidden, is_locked)
VALUES (1, 'gradable_events1', 1, 'kartkówka 1', null, 1, 1, '/url', 'markdown', false, false),
       (2, 'gradable_events2', 2, 'lab 1', null, 1, 1, '/url', 'markdown', false, false),
       (3, 'gradable_events3', 3, 'kartkówka 2', null, 1, 1, '/url', 'markdown', false, false),
       (4, 'gradable_events4', 4, 'lab 2', null, 1, 1, '/url', 'markdown', false, false),
       (5, 'gradable_events5', 3, 'kartkówka 3', null, 1, 1, '/url', 'markdown', false, false),
       (6, 'gradable_events6', 3, 'kartkówka 4', null, 1, 1, '/url', 'markdown', false, false),
       (7, 'gradable_events7', 5, 'projekt', null, 1, 1, '/url', 'markdown', false, false);

insert into projects(id, allow_cross_course_group_project_groups)
values (7, false);

-- Project groups
INSERT INTO public.project_groups (id, teaching_role_user_id, project_id)
VALUES (1, 13, 7);

INSERT INTO public.project_groups_animals (animal_id, project_group_id)
VALUES (8, 1);

-- Criteria
INSERT INTO public.criteria (id, gradable_event_id, name, max_xp, key)
VALUES (1, 1, 'uzyskane punkty', 20.0, 'criteria1'),
       (2, 1, 'jakość kodu', 20.0, 'criteria2'),
       (3, 4, 'uzyskane punkty', 4.0, 'criteria3'),
       (4, 3, 'kryterium2', 10.0, 'criteria4'),
       (5, 3, 'uzyskane punkty', 8.0, 'criteria5'),
       (6, 3, 'uzyskane punkty2', 6.0, 'criteria6'),
       (7, 7, 'uzyskane punkty', 10.0, 'criteria7');

-- Submission Requirements
INSERT INTO public.submission_requirements (id, gradable_event_id, name, is_mandatory, order_index, key)
VALUES (1, 2, 'Wykonanie zadania', TRUE, 1, 'submission_requirements1'),
       (2, 4, 'Wykonanie zadania 1', TRUE, 1, 'submission_requirements2'),
       (3, 4, 'Wykonanie zadania 2', TRUE, 2, 'submission_requirements3'),
       (4, 4, 'Zadanie dodatkowe 1', FALSE, 3, 'submission_requirements4'),
       (5, 4, 'Zadanie dodatkowe 2', FALSE, 4, 'submission_requirements5'),
       (6, 7, 'Repozytorium projektu', TRUE, 1, 'submission_requirements6'),
       (7, 7, 'Slodki kotek', FALSE, 2, 'submission_requirements7');

-- Submissions
INSERT INTO public.submissions (id, submission_requirement_id, animal_id, url, is_locked, created_date, modified_date)
VALUES (1, 6, 8, 'https://www.google.com', true, NOW(), NOW());

-- Grades
INSERT INTO public.grades (id, gradable_event_id, animal_id, created_date, modified_date, comment)
VALUES (1, 1, 2, NOW(), NOW(), null),
       (2, 1, 3, NOW(), NOW(), null),
       (3, 1, 4, NOW(), NOW(), null),
       (4, 1, 5, NOW(), NOW(), null),

       (5, 2, 2, NOW(), NOW(), null),
       (6, 2, 3, NOW(), NOW(), null),
       (7, 2, 4, NOW(), NOW(), null),

       (8, 1, 7, NOW(), NOW(), null),
       (9, 3, 8, NOW(), NOW(), null),
       (10, 4, 8, NOW(), NOW(), null),
       (11, 5, 8, NOW(), NOW(), null),
       (12, 6, 8, NOW(), NOW(), null);

-- Criteria grades
INSERT INTO public.criteria_grades (id, grade_id, criterion_id, xp)
VALUES (1, 1, 1, 1.0),
       (2, 2, 1, 4.0),
       (3, 3, 1, 8.0),
       (4, 4, 1, 2.0),

       (5, 5, 2, 1),
       (6, 6, 2, 8.0),
       (7, 7, 2, 2.0),

       (8, 8, 1, 2.0),
       (9, 10, 3, 4.0),
       (10, 9, 4, 6.8),
       (11, 11, 5, 5.0),
       (12, 12, 6, 4.0);


-- Assigned rewards
INSERT INTO public.assigned_rewards (id, criterion_grade_id, reward_id, received_date, used_date, is_used)
VALUES (1, 3, 1, NOW(), null, false),
       (2, 3, 2, NOW(), null, false),
       (3, 1, 3, NOW(), null, false),
       (4, 1, 3, NOW(), null, false),
       (5, 2, 4, NOW(), null, false),
       (6, 9, 6, '2025-09-20 17:23:03.000000', null, false),
       (7, 10, 7, '2025-08-20 17:23:03.000000', null, false),
       (8, 9, 9, '2025-09-20 17:23:03.000000', null, true),
       (9, 10, 10, '2025-09-21 17:23:03.000000', null, true),
       (10, 11, 10, '2025-09-20 17:23:03.000000', null, true),
       (11, 12, 11, '2025-09-20 17:23:03.000000', null, true);


-- Assigned chests
INSERT INTO public.assigned_chests (assigned_reward_id)
VALUES (5),
       (6),
       (7);

-- Assigned items
INSERT INTO public.assigned_items (assigned_reward_id, assigned_chest_id, bonus_xp)
VALUES (1, null, 2.0),
       (2, null, 3.0),
       (3, null, 0.0),
       (4, null, 0.0),
       (8, null, 2.5),
       (9, null, 1.5),
       (10, null, 1.5),
       (11, null, 2.7);

SELECT setval('animals_id_seq', COALESCE(MAX(id), 0) + 1, false)
FROM animals;
SELECT setval('assigned_rewards_id_seq', COALESCE(MAX(id), 0) + 1, false)
FROM assigned_rewards;
SELECT setval('course_groups_id_seq', COALESCE(MAX(id), 0) + 1, false)
FROM course_groups;
SELECT setval('courses_id_seq', COALESCE(MAX(id), 0) + 1, false)
FROM courses;
SELECT setval('criteria_grades_id_seq', COALESCE(MAX(id), 0) + 1, false)
FROM criteria_grades;
SELECT setval('criteria_id_seq', COALESCE(MAX(id), 0) + 1, false)
FROM criteria;
SELECT setval('event_sections_id_seq', COALESCE(MAX(id), 0) + 1, false)
FROM event_sections;
SELECT setval('evolution_stages_id_seq', COALESCE(MAX(id), 0) + 1, false)
FROM evolution_stages;
SELECT setval('gradable_events_id_seq', COALESCE(MAX(id), 0) + 1, false)
FROM gradable_events;
SELECT setval('grades_id_seq', COALESCE(MAX(id), 0) + 1, false)
FROM grades;
SELECT setval('rewards_id_seq', COALESCE(MAX(id), 0) + 1, false)
FROM rewards;
SELECT setval('users_id_seq', COALESCE(MAX(id), 0) + 1, false)
FROM users;
SELECT setval('submission_requirements_id_seq', COALESCE(MAX(id), 0) + 1, false)
FROM submission_requirements;
SELECT setval('submissions_id_seq', COALESCE(MAX(id), 0) + 1, false)
FROM submissions;
SELECT setval('project_groups_id_seq', COALESCE(MAX(id), 0) + 1, false)
FROM project_groups;