-- ==============================================================================
-- SkillSwap – Student Skill Exchange Database Schema
-- Database Management System (DBMS) Project
-- Supported RDBMS: MySQL 8.0+
-- ==============================================================================

-- 1. Create and switch to the database
CREATE DATABASE IF NOT EXISTS skillswap
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE skillswap;

-- 2. Drop existing tables in reverse order of foreign key dependency
DROP TABLE IF EXISTS SKILL_EXCHANGE_REQUESTS;
DROP TABLE IF EXISTS SKILLS;
DROP TABLE IF EXISTS STUDENTS;

-- ==============================================================================
-- Table: STUDENTS
-- Stores registered students participating in the skill exchange platform.
-- ==============================================================================
CREATE TABLE STUDENTS (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    student_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    contact_info VARCHAR(100) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_student_name_not_empty CHECK (CHAR_LENGTH(TRIM(student_name)) > 0),
    CONSTRAINT chk_email_not_empty CHECK (CHAR_LENGTH(TRIM(email)) > 0)
) ENGINE=InnoDB;

-- ==============================================================================
-- Table: SKILLS
-- Stores skills associated with students, marked as TEACH or LEARN.
-- ==============================================================================
CREATE TABLE SKILLS (
    skill_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    skill_type ENUM('TEACH', 'LEARN') NOT NULL,
    proficiency_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') NOT NULL DEFAULT 'BEGINNER',
    description TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_skills_student 
        FOREIGN KEY (student_id) REFERENCES STUDENTS(student_id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT chk_skill_name_not_empty CHECK (CHAR_LENGTH(TRIM(skill_name)) > 0),
    CONSTRAINT uq_student_skill_type UNIQUE (student_id, skill_name, skill_type)
) ENGINE=InnoDB;

-- Indexes for rapid searching and smart skill matching
CREATE INDEX idx_skills_name_type ON SKILLS(skill_name, skill_type);
CREATE INDEX idx_skills_student_type ON SKILLS(student_id, skill_type);

-- ==============================================================================
-- Table: SKILL_EXCHANGE_REQUESTS
-- Tracks exchange requests sent between students.
-- ==============================================================================
CREATE TABLE SKILL_EXCHANGE_REQUESTS (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    requester_id INT NOT NULL,
    receiver_id INT NOT NULL,
    offered_skill_id INT NOT NULL,
    wanted_skill_id INT NOT NULL,
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_req_requester 
        FOREIGN KEY (requester_id) REFERENCES STUDENTS(student_id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT fk_req_receiver 
        FOREIGN KEY (receiver_id) REFERENCES STUDENTS(student_id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT fk_req_offered_skill 
        FOREIGN KEY (offered_skill_id) REFERENCES SKILLS(skill_id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    CONSTRAINT fk_req_wanted_skill 
        FOREIGN KEY (wanted_skill_id) REFERENCES SKILLS(skill_id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Indexes for incoming/outgoing query performance
CREATE INDEX idx_req_requester_status ON SKILL_EXCHANGE_REQUESTS(requester_id, status);
CREATE INDEX idx_req_receiver_status ON SKILL_EXCHANGE_REQUESTS(receiver_id, status);
