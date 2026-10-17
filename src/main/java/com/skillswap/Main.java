package com.skillswap;

import com.skillswap.config.DatabaseConnection;
import com.skillswap.model.ProficiencyLevel;
import com.skillswap.model.Skill;
import com.skillswap.model.SkillExchangeRequest;
import com.skillswap.model.SkillType;
import com.skillswap.model.Student;
import com.skillswap.service.ExchangeService;
import com.skillswap.service.SkillService;
import com.skillswap.service.StudentService;

import java.sql.SQLException;
import java.util.List;

/**
 * SkillSwap - Student Skill Exchange Platform
 * Phase 5: Exchange Request Creation and Validation.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("       SKILLSWAP - STUDENT SKILL EXCHANGE         ");
        System.out.println("==================================================");
        System.out.println("Phase 5: Exchange Request Creation & Ownership Rules");
        System.out.println("--------------------------------------------------");

        if (!DatabaseConnection.testConnection()) {
            System.err.println("Fatal: Database is unavailable. Exiting.");
            return;
        }

        StudentService studentService = new StudentService();
        SkillService skillService = new SkillService();
        ExchangeService exchangeService = new ExchangeService();

        try {
            // 1. Setup Student A (Alice)
            Student alice = studentService.getStudentByEmail("alice.main@university.edu")
                    .orElseGet(() -> {
                        try {
                            return studentService.registerStudent("Alice Johnson", "alice.main@university.edu", "555-0101");
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

            // 2. Setup Student B (Bob)
            Student bob = studentService.getStudentByEmail("bob.main@university.edu")
                    .orElseGet(() -> {
                        try {
                            return studentService.registerStudent("Bob Smith", "bob.main@university.edu", "555-0102");
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

            System.out.println("[SETUP] Alice ID: " + alice.getStudentId() + " | Bob ID: " + bob.getStudentId());

            // 3. Setup Alice's Skills (TEACH Java, LEARN UI/UX)
            Skill aliceTeachJava = getOrAddSkill(skillService, alice.getStudentId(), "Java Programming", SkillType.TEACH, ProficiencyLevel.ADVANCED, "Core Java & OOP");
            getOrAddSkill(skillService, alice.getStudentId(), "UI/UX Design", SkillType.LEARN, ProficiencyLevel.BEGINNER, "Figma");

            // 4. Setup Bob's Skills (TEACH UI/UX, LEARN Java)
            Skill bobTeachUI = getOrAddSkill(skillService, bob.getStudentId(), "UI/UX Design", SkillType.TEACH, ProficiencyLevel.ADVANCED, "Wireframes & Prototypes");
            getOrAddSkill(skillService, bob.getStudentId(), "Java Programming", SkillType.LEARN, ProficiencyLevel.BEGINNER, "Basics");

            // 5. Test Rule: Reject Self-Request
            System.out.println("\n--- Step 1: Testing Self-Request Prevention ---");
            try {
                exchangeService.createRequest(alice.getStudentId(), alice.getStudentId(), aliceTeachJava.getSkillId(), aliceTeachJava.getSkillId());
                System.err.println("[FAIL] Self-request was not blocked!");
            } catch (IllegalArgumentException e) {
                System.out.println("[SUCCESS] Expected exception caught: " + e.getMessage());
            }

            // 6. Test Rule: Reject Wrong Skill Ownership
            System.out.println("\n--- Step 2: Testing Skill Ownership Validation ---");
            try {
                // Alice tries to offer Bob's skill!
                exchangeService.createRequest(alice.getStudentId(), bob.getStudentId(), bobTeachUI.getSkillId(), bobTeachUI.getSkillId());
                System.err.println("[FAIL] Unowned skill offering was not blocked!");
            } catch (IllegalArgumentException e) {
                System.out.println("[SUCCESS] Expected exception caught: " + e.getMessage());
            }

            // 7. Create Valid Exchange Request from Alice to Bob
            System.out.println("\n--- Step 3: Creating Valid Skill Exchange Request ---");
            try {
                SkillExchangeRequest req = exchangeService.createRequest(
                        alice.getStudentId(),
                        bob.getStudentId(),
                        aliceTeachJava.getSkillId(),
                        bobTeachUI.getSkillId()
                );
                System.out.printf("[SUCCESS] Created Request #%d: Alice offers '%s' to Bob for '%s' (Status: %s)\n",
                        req.getRequestId(), req.getOfferedSkillName(), req.getWantedSkillName(), req.getStatus());
            } catch (IllegalArgumentException e) {
                System.out.println("[INFO] " + e.getMessage());
            }

            // 8. View Alice's Outgoing Requests
            System.out.println("\n--- Step 4: Alice's Outgoing Requests ---");
            List<SkillExchangeRequest> outgoing = exchangeService.getOutgoingRequests(alice.getStudentId());
            System.out.println(exchangeService.formatRequestTable(outgoing, false));

            // 9. View Bob's Incoming Requests
            System.out.println("--- Step 5: Bob's Incoming Requests ---");
            List<SkillExchangeRequest> incoming = exchangeService.getIncomingRequests(bob.getStudentId());
            System.out.println(exchangeService.formatRequestTable(incoming, true));

            System.out.println("--------------------------------------------------");
            System.out.println("Phase 5 Verification Complete!");
            System.out.println("Ready for Phase 6 (Request Lifecycle: Accept, Reject, Cancel, Complete).");
            System.out.println("--------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Database error during Phase 5 demonstration: " + e.getMessage());
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
