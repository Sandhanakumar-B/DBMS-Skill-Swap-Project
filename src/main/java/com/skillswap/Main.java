package com.skillswap;

import com.skillswap.config.DatabaseConnection;
import com.skillswap.model.PlatformReport;
import com.skillswap.model.ProficiencyLevel;
import com.skillswap.model.Skill;
import com.skillswap.model.SkillMatch;
import com.skillswap.model.SkillType;
import com.skillswap.model.Student;
import com.skillswap.service.ReportService;
import com.skillswap.service.SkillMatchingService;
import com.skillswap.service.SkillService;
import com.skillswap.service.StudentService;

import java.sql.SQLException;
import java.util.List;

/**
 * SkillSwap - Student Skill Exchange Platform
 * Phase 7: Smart Skill Matching and DBMS Summary Reports.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("       SKILLSWAP - STUDENT SKILL EXCHANGE         ");
        System.out.println("==================================================");
        System.out.println("Phase 7: Smart Skill Matching & Aggregate Reports");
        System.out.println("--------------------------------------------------");

        if (!DatabaseConnection.testConnection()) {
            System.err.println("Fatal: Database is unavailable. Exiting.");
            return;
        }

        StudentService studentService = new StudentService();
        SkillService skillService = new SkillService();
        SkillMatchingService matchingService = new SkillMatchingService();
        ReportService reportService = new ReportService();

        try {
            // 1. Get or create Student 1 (Charlie: Teaches Java, Wants UI/UX)
            Student charlie = studentService.getStudentByEmail("charlie.p7@university.edu")
                    .orElseGet(() -> {
                        try {
                            return studentService.registerStudent("Charlie Coder", "charlie.p7@university.edu", "555-7001");
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

            // 2. Get or create Student 2 (Dana: Teaches UI/UX, Wants Java) -> MUTUAL MATCH
            Student dana = studentService.getStudentByEmail("dana.p7@university.edu")
                    .orElseGet(() -> {
                        try {
                            return studentService.registerStudent("Dana Designer", "dana.p7@university.edu", "555-7002");
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

            // 3. Get or create Student 3 (Evan: Teaches UI/UX, Wants Python) -> PARTIAL MATCH for Charlie
            Student evan = studentService.getStudentByEmail("evan.p7@university.edu")
                    .orElseGet(() -> {
                        try {
                            return studentService.registerStudent("Evan Editor", "evan.p7@university.edu", "555-7003");
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

            getOrAddSkill(skillService, charlie.getStudentId(), "Java", SkillType.TEACH, ProficiencyLevel.ADVANCED, "Core Java");
            getOrAddSkill(skillService, charlie.getStudentId(), "UI/UX", SkillType.LEARN, ProficiencyLevel.BEGINNER, "Figma");

            getOrAddSkill(skillService, dana.getStudentId(), "UI/UX", SkillType.TEACH, ProficiencyLevel.ADVANCED, "UI/UX & Prototyping");
            getOrAddSkill(skillService, dana.getStudentId(), "Java", SkillType.LEARN, ProficiencyLevel.BEGINNER, "Java OOP");

            getOrAddSkill(skillService, evan.getStudentId(), "UI/UX", SkillType.TEACH, ProficiencyLevel.INTERMEDIATE, "Design Basics");
            getOrAddSkill(skillService, evan.getStudentId(), "Python", SkillType.LEARN, ProficiencyLevel.BEGINNER, "Django");

            // 4. Run Smart Matching for Charlie
            System.out.println("\n--- Step 1: Running Smart Skill Matching for " + charlie.getStudentName() + " ---");
            List<SkillMatch> matches = matchingService.findMatchesForStudent(charlie.getStudentId());
            System.out.println(matchingService.formatMatchResults(matches));

            // 5. Generate DBMS Aggregate Summary Report
            System.out.println("--- Step 2: Generating Platform Aggregate Summary Report ---");
            PlatformReport report = reportService.generatePlatformSummary(charlie.getStudentId());
            System.out.println(reportService.formatReport(report));

            System.out.println("--------------------------------------------------");
            System.out.println("Phase 7 Verification Complete!");
            System.out.println("Ready for Phase 8 (Console Menu Integration).");
            System.out.println("--------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Database error during Phase 7 demonstration: " + e.getMessage());
        } finally {
            DatabaseConnection.shutdown();
        }
    }

    private static Skill getOrAddSkill(SkillService service, int studentId, String name, SkillType type,
                                      ProficiencyLevel level, String desc) throws SQLException {
        for (Skill s : service.getSkillsByStudentAndType(studentId, type)) {
            if (s.getSkillName().equalsIgnoreCase(name)) {
                return s;
            }
        }
        return service.addSkill(studentId, name, type, level, desc);
    }
}
