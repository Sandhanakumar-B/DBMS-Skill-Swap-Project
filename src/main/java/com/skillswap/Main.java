package com.skillswap;

import com.skillswap.config.DatabaseConnection;
import com.skillswap.model.ProficiencyLevel;
import com.skillswap.model.Skill;
import com.skillswap.model.SkillType;
import com.skillswap.model.Student;
import com.skillswap.service.SkillService;
import com.skillswap.service.StudentService;

import java.sql.SQLException;
import java.util.List;

/**
 * SkillSwap - Student Skill Exchange Platform
 * Phase 4: Skill Management, Search, Normalization, and Profiling.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("       SKILLSWAP - STUDENT SKILL EXCHANGE         ");
        System.out.println("==================================================");
        System.out.println("Phase 4: Skill Model, DAO, Service & Search");
        System.out.println("--------------------------------------------------");

        if (!DatabaseConnection.testConnection()) {
            System.err.println("Fatal: Database is unavailable. Exiting.");
            return;
        }

        StudentService studentService = new StudentService();
        SkillService skillService = new SkillService();

        try {
            // 1. Get or create active student
            String email = "demo.student@university.edu";
            Student student = studentService.getStudentByEmail(email)
                    .orElseGet(() -> {
                        try {
                            return studentService.registerStudent("Demo Student", email, "+91-9876543210");
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
            studentService.setActiveStudent(student);
            System.out.println("[SESSION] Active Student: " + student.getStudentName() + " (ID: " + student.getStudentId() + ")");

            // 2. Add TEACH and LEARN skills with normalization
            System.out.println("\n--- Step 1: Adding Skills with Normalization ---");
            try {
                Skill teachJava = skillService.addSkill(
                        student.getStudentId(),
                        "   java   programming  ",
                        SkillType.TEACH,
                        ProficiencyLevel.ADVANCED,
                        "Core Java, Multithreading, JDBC"
                );
                System.out.println("[SUCCESS] Added TEACH Skill: " + teachJava.getSkillName() + " (ID: " + teachJava.getSkillId() + ")");
            } catch (IllegalArgumentException e) {
                System.out.println("[INFO] " + e.getMessage());
            }

            try {
                Skill learnUI = skillService.addSkill(
                        student.getStudentId(),
                        "ui/ux design",
                        SkillType.LEARN,
                        ProficiencyLevel.BEGINNER,
                        "Figma wireframing and design systems"
                );
                System.out.println("[SUCCESS] Added LEARN Skill: " + learnUI.getSkillName() + " (ID: " + learnUI.getSkillId() + ")");
            } catch (IllegalArgumentException e) {
                System.out.println("[INFO] " + e.getMessage());
            }

            // 3. Duplicate Skill Prevention
            System.out.println("\n--- Step 2: Testing Duplicate Skill Validation ---");
            try {
                skillService.addSkill(student.getStudentId(), "JAVA PROGRAMMING", SkillType.TEACH, ProficiencyLevel.BEGINNER, "Duplicate");
                System.err.println("[FAIL] Duplicate skill was not prevented!");
            } catch (IllegalArgumentException e) {
                System.out.println("[SUCCESS] Expected exception caught: " + e.getMessage());
            }

            // 4. View Active Student's Skills
            System.out.println("\n--- Step 3: Active Student's Listed Skills ---");
            List<Skill> mySkills = skillService.getSkillsByStudent(student.getStudentId());
            System.out.println(skillService.formatSkillTable(mySkills));

            // 5. Search Skills Demo
            System.out.println("--- Step 4: Search Skills by Keyword ('Java') ---");
            List<Skill> searchResults = skillService.searchSkills("java", null, null);
            System.out.println(skillService.formatSkillTable(searchResults));

            System.out.println("--------------------------------------------------");
            System.out.println("Phase 4 Verification Complete!");
            System.out.println("Ready for Phase 5 (Exchange Request Model, DAO & Service).");
            System.out.println("--------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Database error during Phase 4 demonstration: " + e.getMessage());
        } finally {
            DatabaseConnection.shutdown();
        }
    }
}
