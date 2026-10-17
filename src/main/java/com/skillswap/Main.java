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
 * Phase 6: Request Lifecycle Management (Accept, Reject, Cancel, Complete).
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("       SKILLSWAP - STUDENT SKILL EXCHANGE         ");
        System.out.println("==================================================");
        System.out.println("Phase 6: Request Lifecycle (Accept, Reject, Cancel, Complete)");
        System.out.println("--------------------------------------------------");

        if (!DatabaseConnection.testConnection()) {
            System.err.println("Fatal: Database is unavailable. Exiting.");
            return;
        }

        StudentService studentService = new StudentService();
        SkillService skillService = new SkillService();
        ExchangeService exchangeService = new ExchangeService();

        try {
            // Setup two students
            Student alice = studentService.getStudentByEmail("alice.phase6@university.edu")
                    .orElseGet(() -> {
                        try {
                            return studentService.registerStudent("Alice Developer", "alice.phase6@university.edu", "555-6001");
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

            Student bob = studentService.getStudentByEmail("bob.phase6@university.edu")
                    .orElseGet(() -> {
                        try {
                            return studentService.registerStudent("Bob Designer", "bob.phase6@university.edu", "555-6002");
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

            System.out.println("[SETUP] Alice ID: " + alice.getStudentId() + " | Bob ID: " + bob.getStudentId());

            // Setup Skills
            Skill aliceJava = getOrAddSkill(skillService, alice.getStudentId(), "Java", SkillType.TEACH, ProficiencyLevel.ADVANCED, "Core Java");
            Skill bobUI = getOrAddSkill(skillService, bob.getStudentId(), "UI/UX", SkillType.TEACH, ProficiencyLevel.ADVANCED, "Design Systems");

            // 1. Create a fresh request
            System.out.println("\n--- Step 1: Alice Sends Request to Bob ---");
            SkillExchangeRequest request = null;
            try {
                request = exchangeService.createRequest(
                        alice.getStudentId(), bob.getStudentId(),
                        aliceJava.getSkillId(), bobUI.getSkillId()
                );
                System.out.printf("[SUCCESS] Created Request #%d (Status: %s)\n", request.getRequestId(), request.getStatus());
            } catch (IllegalArgumentException e) {
                // If already created, find the pending or accepted one
                List<SkillExchangeRequest> list = exchangeService.getOutgoingRequests(alice.getStudentId());
                if (!list.isEmpty()) {
                    request = list.get(0);
                    System.out.println("[INFO] Using existing Request #" + request.getRequestId() + " (Status: " + request.getStatus() + ")");
                }
            }

            if (request != null) {
                int reqId = request.getRequestId();

                // 2. Unauthorized Accept attempt: Alice tries to accept her own outgoing request!
                System.out.println("\n--- Step 2: Testing Unauthorized Accept Prevention ---");
                try {
                    exchangeService.acceptRequest(alice.getStudentId(), reqId);
                    System.err.println("[FAIL] Requester was able to accept their own request!");
                } catch (SecurityException e) {
                    System.out.println("[SUCCESS] Expected exception caught: " + e.getMessage());
                }

                // 3. Bob accepts the request
                System.out.println("\n--- Step 3: Receiver (Bob) Accepts the Request ---");
                if (request.getStatus() == com.skillswap.model.RequestStatus.PENDING) {
                    SkillExchangeRequest accepted = exchangeService.acceptRequest(bob.getStudentId(), reqId);
                    System.out.printf("[SUCCESS] Bob accepted Request #%d! New Status: %s\n", accepted.getRequestId(), accepted.getStatus());
                    request = accepted;
                } else {
                    System.out.println("[INFO] Request status is already: " + request.getStatus());
                }

                // 4. Marking exchange as COMPLETED
                System.out.println("\n--- Step 4: Completing the Exchange ---");
                if (request.getStatus() == com.skillswap.model.RequestStatus.ACCEPTED) {
                    SkillExchangeRequest completed = exchangeService.completeExchange(alice.getStudentId(), reqId);
                    System.out.printf("[SUCCESS] Alice marked exchange #%d as COMPLETED! New Status: %s\n",
                            completed.getRequestId(), completed.getStatus());
                    request = completed;
                }

                // 5. Attempting to cancel COMPLETED request must fail
                System.out.println("\n--- Step 5: Testing Cancellation Rule on COMPLETED Request ---");
                try {
                    exchangeService.cancelRequest(alice.getStudentId(), reqId);
                    System.err.println("[FAIL] Completed request was cancelled!");
                } catch (IllegalStateException e) {
                    System.out.println("[SUCCESS] Expected exception caught: " + e.getMessage());
                }

                // 6. View final status table
                System.out.println("\n--- Step 6: Alice's Final Outgoing Request Status ---");
                List<SkillExchangeRequest> outgoing = exchangeService.getOutgoingRequests(alice.getStudentId());
                System.out.println(exchangeService.formatRequestTable(outgoing, false));
            }

            System.out.println("--------------------------------------------------");
            System.out.println("Phase 6 Verification Complete!");
            System.out.println("Ready for Phase 7 (Smart Skill Matching and Aggregate Reports).");
            System.out.println("--------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Database error during Phase 6 demonstration: " + e.getMessage());
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
