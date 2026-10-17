-- ==============================================================================
-- SkillSwap – Student Skill Exchange Sample Seed Data
-- Database Management System (DBMS) Project
-- Supported RDBMS: MySQL 8.0+
-- ==============================================================================

USE skillswap;

-- Clean existing data in reverse order of foreign key dependency
DELETE FROM SKILL_EXCHANGE_REQUESTS;
DELETE FROM SKILLS;
DELETE FROM STUDENTS;

-- ------------------------------------------------------------------------------
-- 1. Insert Sample Students
-- ------------------------------------------------------------------------------
INSERT INTO STUDENTS (student_id, student_name, email, contact_info) VALUES
(1, 'Alice Sharma',  'alice.sharma@university.edu',  '+91-9876543210'),
(2, 'Bob Chen',      'bob.chen@university.edu',      '+91-9876543211'),
(3, 'Charlie Davis', 'charlie.davis@university.edu', '+91-9876543212'),
(4, 'Dana Patel',    'dana.patel@university.edu',    '+91-9876543213'),
(5, 'Evan Wright',   'evan.wright@university.edu',   '+91-9876543214');

-- Reset auto_increment to start safely after seeded records
ALTER TABLE STUDENTS AUTO_INCREMENT = 10;

-- ------------------------------------------------------------------------------
-- 2. Insert Sample Skills (TEACH & LEARN)
-- ------------------------------------------------------------------------------
INSERT INTO SKILLS (skill_id, student_id, skill_name, skill_type, proficiency_level, description) VALUES
-- Alice Sharma (Student 1)
(1, 1, 'Java',           'TEACH', 'ADVANCED',     'Core Java, OOP, Collections, Multi-threading, and JDBC'),
(2, 1, 'Spring Boot',    'TEACH', 'INTERMEDIATE', 'REST APIs, Spring Data, and MVC architecture'),
(3, 1, 'UI/UX Design',   'LEARN', 'BEGINNER',     'Interested in wireframing, Figma components, and user flows'),
(4, 1, 'Graphic Design', 'LEARN', 'BEGINNER',     'Want to learn vector graphics and layout composition'),

-- Bob Chen (Student 2)
(5, 2, 'UI/UX Design',   'TEACH', 'ADVANCED',     'Design systems, prototyping in Figma, mobile app UX patterns'),
(6, 2, 'Figma',          'TEACH', 'ADVANCED',     'Auto-layout, variables, interactive component prototyping'),
(7, 2, 'Java',           'LEARN', 'BEGINNER',     'Want to learn backend programming with Core Java'),
(8, 2, 'Python',         'LEARN', 'BEGINNER',     'Interested in Python for automation and scripting'),

-- Charlie Davis (Student 3)
(9,  3, 'Video Editing',       'TEACH', 'ADVANCED',     'Premiere Pro, DaVinci Resolve, color grading, sound design'),
(10, 3, 'Adobe Premiere Pro',  'TEACH', 'INTERMEDIATE', 'Timeline editing, multi-cam editing, audio normalization'),
(11, 3, 'Graphic Design',      'LEARN', 'BEGINNER',     'Looking to create custom thumbnails and branding posters'),
(12, 3, 'Web Development',     'LEARN', 'BEGINNER',     'Want to build portfolio websites with HTML/CSS/JS'),

-- Dana Patel (Student 4)
(13, 4, 'Graphic Design', 'TEACH', 'ADVANCED',     'Brand identity, typography, Adobe Illustrator, and vector art'),
(14, 4, 'Photoshop',      'TEACH', 'ADVANCED',     'Photo manipulation, digital retouching, raster graphics'),
(15, 4, 'Video Editing',  'LEARN', 'BEGINNER',     'Want to learn video cuts, transitions, and audio sync for reels'),
(16, 4, 'UI/UX Design',   'LEARN', 'BEGINNER',     'Interested in user interface aesthetics and design guidelines'),

-- Evan Wright (Student 5)
(17, 5, 'Python',           'TEACH', 'ADVANCED',     'Python 3, pandas, NumPy, automated bots, scripting'),
(18, 5, 'Machine Learning', 'TEACH', 'INTERMEDIATE', 'Scikit-learn, regression, classification models'),
(19, 5, 'UI/UX Design',     'LEARN', 'BEGINNER',     'Want to design clean dashboards for ML models');

-- Reset auto_increment to start safely after seeded records
ALTER TABLE SKILLS AUTO_INCREMENT = 30;

-- ------------------------------------------------------------------------------
-- 3. Insert Sample Skill Exchange Requests (Demonstrating Lifecycle States)
-- ------------------------------------------------------------------------------
INSERT INTO SKILL_EXCHANGE_REQUESTS (request_id, requester_id, receiver_id, offered_skill_id, wanted_skill_id, status) VALUES
-- Mutual Match 1: Alice Sharma & Bob Chen (Java <-> UI/UX Design) -> COMPLETED
(1, 1, 2, 1, 5, 'COMPLETED'),

-- Mutual Match 2: Charlie Davis & Dana Patel (Video Editing <-> Graphic Design) -> ACCEPTED
(2, 3, 4, 9, 13, 'ACCEPTED'),

-- Mutual Match 3: Evan Wright & Bob Chen (Python <-> UI/UX Design) -> PENDING
(3, 5, 2, 17, 5, 'PENDING');

-- Reset auto_increment to start safely after seeded records
ALTER TABLE SKILL_EXCHANGE_REQUESTS AUTO_INCREMENT = 10;
