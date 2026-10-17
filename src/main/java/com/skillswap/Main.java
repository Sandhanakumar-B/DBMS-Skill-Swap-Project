package com.skillswap;

import com.skillswap.config.DatabaseConnection;
import com.skillswap.model.Student;
import com.skillswap.service.StudentService;

import java.sql.SQLException;
import java.util.List;

/**
 * SkillSwap - Student Skill Exchange Platform
 * Phase 3: Student Registration, Active Session, and Profile Viewing.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("       SKILLSWAP - STUDENT SKILL EXCHANGE         ");
        System.out.println("==================================================");
        System.out.println("Phase 3: Student Model, DAO, Service & Profile");
        System.out.println("--------------------------------------------------");

        // 1. Verify database connectivity
        if (!DatabaseConnection.testConnection()) {
            System.err.println("Fatal: Database is unavailable. Exiting.");
            return;
        }

        StudentService studentService = new StudentService();

        try {
            System.out.println("\n--- Step 1: Student Registration Demonstration ---");
            String demoEmail = "demo.student@university.edu";
            
            // Check if demo student exists or register
            Student activeStudent;
            var existing = studentService.getStudentByEmail(demoEmail);
            if (existing.isPresent()) {
                activeStudent = existing.get();
                studentService.setActiveStudent(activeStudent);
                System.out.println("[INFO] Found existing student: " + activeStudent.getStudentName() + " (ID: " + activeStudent.getStudentId() + ")");
            } else {
                activeStudent = studentService.registerStudent(
                        "Demo Student", 
                        demoEmail, 
                        "+91-9876543210"
                );
                System.out.println("[SUCCESS] Registered new student with ID: " + activeStudent.getStudentId());
            }

            // 2. Demonstrate Duplicate Email Prevention
            System.out.println("\n--- Step 2: Testing Duplicate Email Validation ---");
            try {
                studentService.registerStudent("Another Student", demoEmail, "12345");
                System.err.println("[FAIL] Duplicate email check failed to catch duplicate!");
            } catch (IllegalArgumentException e) {
                System.out.println("[SUCCESS] Expected exception caught: " + e.getMessage());
            }

            // 3. Demonstrate Active Session & Profile Display
            System.out.println("\n--- Step 3: Active Student Profile Display ---");
            System.out.println("Currently Active Session: " + studentService.getActiveStudent().getStudentName());
            System.out.println(studentService.formatStudentProfile(studentService.getActiveStudent()));

            // 4. List All Registered Students
            System.out.println("--- Step 4: Registered Students in Database ---");
            List<Student> students = studentService.getAllStudents();
            System.out.printf("Total registered students: %d\n", students.size());
            for (Student s : students) {
                System.out.printf(" - [ID: %d] %s (%s) | Contact: %s\n",
                        s.getStudentId(), s.getStudentName(), s.getEmail(),
                        s.getContactInfo() != null ? s.getContactInfo() : "None");
            }

            System.out.println("\n--------------------------------------------------");
            System.out.println("Phase 3 Verification Complete!");
            System.out.println("Ready for Phase 4 (Skill Model, DAO, and Service).");
            System.out.println("--------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Database error during Phase 3 demonstration: " + e.getMessage());
        } finally {
            DatabaseConnection.shutdown();
        }
    }
}
