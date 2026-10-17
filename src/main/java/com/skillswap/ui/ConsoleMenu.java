package com.skillswap.ui;

import com.skillswap.model.PlatformReport;
import com.skillswap.model.ProficiencyLevel;
import com.skillswap.model.Skill;
import com.skillswap.model.SkillExchangeRequest;
import com.skillswap.model.SkillMatch;
import com.skillswap.model.SkillType;
import com.skillswap.model.Student;
import com.skillswap.service.ExchangeService;
import com.skillswap.service.ReportService;
import com.skillswap.service.SkillMatchingService;
import com.skillswap.service.SkillService;
import com.skillswap.service.StudentService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Interactive Console Menu for SkillSwap.
 * Provides user interaction, input sanitization, and graceful error handling.
 */
public class ConsoleMenu {

    private final StudentService studentService;
    private final SkillService skillService;
    private final ExchangeService exchangeService;
    private final SkillMatchingService matchingService;
    private final ReportService reportService;
    private final Scanner scanner;

    public ConsoleMenu() {
        this.studentService = new StudentService();
        this.skillService = new SkillService();
        this.exchangeService = new ExchangeService();
        this.matchingService = new SkillMatchingService();
        this.reportService = new ReportService();
        this.scanner = new Scanner(System.in);
    }

    public ConsoleMenu(StudentService studentService, SkillService skillService,
                       ExchangeService exchangeService, SkillMatchingService matchingService,
                       ReportService reportService, Scanner scanner) {
        this.studentService = studentService;
        this.skillService = skillService;
        this.exchangeService = exchangeService;
        this.matchingService = matchingService;
        this.reportService = reportService;
        this.scanner = scanner;
    }

    /**
     * Starts the interactive console menu loop.
     */
    public void start() {
        boolean running = true;

        while (running) {
            printHeader();
            printMenuOptions();

            System.out.print("Enter your choice (0-16): ");
            if (!scanner.hasNextLine()) {
                System.out.println("\n[INFO] End of input detected. Exiting SkillSwap.");
                break;
            }

            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }

            int choice;
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid selection. Please enter a valid number from the menu.\n");
                continue;
            }

            try {
                switch (choice) {
                    case 1 -> handleRegisterStudent();
                    case 2 -> handleSwitchStudent();
                    case 3 -> handleViewProfile();
                    case 4 -> handleAddSkill();
                    case 5 -> handleViewMySkills();
                    case 6 -> handleSearchSkills();
                    case 7 -> handleFindMatches();
                    case 8 -> handleSendExchangeRequest();
                    case 9 -> handleViewIncomingRequests();
                    case 10 -> handleAcceptRequest();
                    case 11 -> handleRejectRequest();
                    case 12 -> handleViewOutgoingRequests();
                    case 13 -> handleCancelRequest();
                    case 14 -> handleCompleteExchange();
                    case 15 -> handleViewAllStudents();
                    case 16 -> handleViewSummary();
                    case 0 -> {
                        System.out.println("\nThank you for using SkillSwap! Exiting...");
                        running = false;
                    }
                    default -> System.out.println("Invalid option. Please choose between 0 and 16.\n");
                }
            } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
                System.out.println("[ACTION FAILED] " + e.getMessage());
            } catch (SQLException e) {
                System.err.println("[DATABASE ERROR] " + e.getMessage());
            } catch (Exception e) {
                System.err.println("[UNEXPECTED ERROR] " + e.getMessage());
            }

            if (running) {
                System.out.println();
            }
        }
    }

    private void printHeader() {
        System.out.println("==================================================");
        System.out.println("       SKILLSWAP - STUDENT SKILL EXCHANGE         ");
        System.out.println("==================================================");
        if (studentService.hasActiveStudent()) {
            Student active = studentService.getActiveStudent();
            System.out.printf(" ACTIVE USER: %s (ID: %d | %s)\n",
                    active.getStudentName(), active.getStudentId(), active.getEmail());
        } else {
            System.out.println(" ACTIVE USER: [No Student Selected - Demonstrating as Guest]");
        }
        System.out.println("==================================================");
    }

    private void printMenuOptions() {
        System.out.println(" 1. Register Student");
        System.out.println(" 2. Switch / Select Active Student");
        System.out.println(" 3. View My Profile");
        System.out.println(" 4. Add Skill (TEACH or LEARN)");
        System.out.println(" 5. View / Manage My Skills");
        System.out.println(" 6. Search Skills");
        System.out.println(" 7. Find Smart Matches");
        System.out.println(" 8. Send Exchange Request");
        System.out.println(" 9. View Incoming Requests");
        System.out.println(" 10. Accept Incoming Request");
        System.out.println(" 11. Reject Incoming Request");
        System.out.println(" 12. View Outgoing Requests");
        System.out.println(" 13. Cancel Exchange Request");
        System.out.println(" 14. Complete Exchange");
        System.out.println(" 15. View All Registered Students");
        System.out.println(" 16. View Application Summary Report");
        System.out.println(" 0. Exit");
        System.out.println("--------------------------------------------------");
    }

    private boolean requireActiveStudent() {
        if (!studentService.hasActiveStudent()) {
            System.out.println("[ACCESS DENIED] This operation requires an active student session.");
            System.out.println("Please register (Option 1) or select an existing student (Option 2) first.");
            return false;
        }
        return true;
    }

    private void handleRegisterStudent() throws SQLException {
        System.out.println("\n--- Student Registration ---");
        System.out.print("Enter full name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter email address: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter contact info (optional, phone or social): ");
        String contact = scanner.nextLine().trim();

        Student registered = studentService.registerStudent(name, email, contact);
        System.out.printf("[SUCCESS] Student registered successfully! Assigned Student ID: %d\n", registered.getStudentId());
        System.out.println("[INFO] Set " + registered.getStudentName() + " as the currently active student.");
    }

    private void handleSwitchStudent() throws SQLException {
        System.out.println("\n--- Switch / Select Active Student ---");
        System.out.println("(Notice: Student ID selection is for classroom demonstrations, not secure auth)");

        List<Student> students = studentService.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("No students registered yet. Please register first.");
            return;
        }

        System.out.println("Registered Students:");
        for (Student s : students) {
            System.out.printf("  [ID: %-3d] %-20s (%s)\n", s.getStudentId(), s.getStudentName(), s.getEmail());
        }

        System.out.print("Enter Student ID to switch to: ");
        String input = scanner.nextLine().trim();
        try {
            int id = Integer.parseInt(input);
            Student switched = studentService.switchActiveStudent(id);
            System.out.printf("[SUCCESS] Switched active session to: %s (ID: %d)\n", switched.getStudentName(), switched.getStudentId());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format for Student ID.");
        }
    }

    private void handleViewProfile() throws SQLException {
        if (!requireActiveStudent()) return;
        Student active = studentService.getActiveStudent();
        System.out.println(studentService.formatStudentProfile(active));

        List<Skill> teachSkills = skillService.getSkillsByStudentAndType(active.getStudentId(), SkillType.TEACH);
        List<Skill> learnSkills = skillService.getSkillsByStudentAndType(active.getStudentId(), SkillType.LEARN);

        System.out.println("Skills I Can Teach (" + teachSkills.size() + "):");
        if (teachSkills.isEmpty()) {
            System.out.println("  (None added yet)");
        } else {
            for (Skill s : teachSkills) {
                System.out.printf("  * [ID: %d] %s (%s) - %s\n", s.getSkillId(), s.getSkillName(), s.getProficiencyLevel(),
                        s.getDescription() != null ? s.getDescription() : "No description");
            }
        }

        System.out.println("\nSkills I Want to Learn (" + learnSkills.size() + "):");
        if (learnSkills.isEmpty()) {
            System.out.println("  (None added yet)");
        } else {
            for (Skill s : learnSkills) {
                System.out.printf("  * [ID: %d] %s (%s) - %s\n", s.getSkillId(), s.getSkillName(), s.getProficiencyLevel(),
                        s.getDescription() != null ? s.getDescription() : "No description");
            }
        }
    }

    private void handleAddSkill() throws SQLException {
        if (!requireActiveStudent()) return;
        Student active = studentService.getActiveStudent();

        System.out.println("\n--- Add a Skill ---");
        System.out.print("Enter skill name (e.g. Java, Python, UI/UX Design): ");
        String name = scanner.nextLine().trim();

        System.out.println("Choose Skill Type:");
        System.out.println(" 1. TEACH (I can teach this skill)");
        System.out.println(" 2. LEARN (I want to learn this skill)");
        System.out.print("Selection (1 or 2): ");
        String typeChoice = scanner.nextLine().trim();
        SkillType type = typeChoice.equals("2") ? SkillType.LEARN : SkillType.TEACH;

        System.out.println("Choose Proficiency Level:");
        System.out.println(" 1. BEGINNER");
        System.out.println(" 2. INTERMEDIATE");
        System.out.println(" 3. ADVANCED");
        System.out.print("Selection (1-3, default 1): ");
        String levelChoice = scanner.nextLine().trim();
        ProficiencyLevel level = switch (levelChoice) {
            case "2" -> ProficiencyLevel.INTERMEDIATE;
            case "3" -> ProficiencyLevel.ADVANCED;
            default -> ProficiencyLevel.BEGINNER;
        };

        System.out.print("Enter brief description or goals (optional): ");
        String description = scanner.nextLine().trim();

        Skill created = skillService.addSkill(active.getStudentId(), name, type, level, description);
        System.out.printf("[SUCCESS] Added %s skill '%s' (Assigned Skill ID: %d)\n",
                created.getSkillType(), created.getSkillName(), created.getSkillId());
    }

    private void handleViewMySkills() throws SQLException {
        if (!requireActiveStudent()) return;
        Student active = studentService.getActiveStudent();

        System.out.println("\n--- My Listed Skills ---");
        List<Skill> skills = skillService.getSkillsByStudent(active.getStudentId());
        System.out.println(skillService.formatSkillTable(skills));

        if (!skills.isEmpty()) {
            System.out.print("Would you like to (E)dit, (D)elete, or (R)eturn to menu? (E/D/R): ");
            String opt = scanner.nextLine().trim().toUpperCase();
            if (opt.equals("E")) {
                handleEditSkill();
            } else if (opt.equals("D")) {
                handleDeleteSkill();
            }
        }
    }

    private void handleEditSkill() throws SQLException {
        Student active = studentService.getActiveStudent();
        System.out.print("Enter Skill ID to edit: ");
        int skillId = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Enter new skill name: ");
        String newName = scanner.nextLine().trim();

        System.out.println("Choose new Proficiency Level (1: BEGINNER, 2: INTERMEDIATE, 3: ADVANCED): ");
        String levelChoice = scanner.nextLine().trim();
        ProficiencyLevel level = switch (levelChoice) {
            case "2" -> ProficiencyLevel.INTERMEDIATE;
            case "3" -> ProficiencyLevel.ADVANCED;
            default -> ProficiencyLevel.BEGINNER;
        };

        System.out.print("Enter new description: ");
        String newDesc = scanner.nextLine().trim();

        boolean updated = skillService.updateSkill(active.getStudentId(), skillId, newName, level, newDesc);
        if (updated) {
            System.out.println("[SUCCESS] Skill updated successfully!");
        }
    }

    private void handleDeleteSkill() throws SQLException {
        Student active = studentService.getActiveStudent();
        System.out.print("Enter Skill ID to delete: ");
        int skillId = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Are you sure you want to delete skill #" + skillId + "? (y/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            boolean deleted = skillService.deleteSkill(active.getStudentId(), skillId);
            if (deleted) {
                System.out.println("[SUCCESS] Skill deleted successfully.");
            }
        }
    }

    private void handleSearchSkills() throws SQLException {
        System.out.println("\n--- Search Skills ---");
        System.out.print("Enter search keyword (or press Enter for all): ");
        String keyword = scanner.nextLine().trim();

        System.out.println("Filter by type:");
        System.out.println(" 1. All Types");
        System.out.println(" 2. TEACH Only (Find potential tutors)");
        System.out.println(" 3. LEARN Only (Find students seeking help)");
        System.out.print("Selection (1-3, default 1): ");
        String filterChoice = scanner.nextLine().trim();

        SkillType filter = switch (filterChoice) {
            case "2" -> SkillType.TEACH;
            case "3" -> SkillType.LEARN;
            default -> null;
        };

        Integer excludeStudentId = studentService.hasActiveStudent() ? studentService.getActiveStudent().getStudentId() : null;
        List<Skill> results = skillService.searchSkills(keyword, filter, excludeStudentId);
        System.out.println(skillService.formatSkillTable(results));
    }

    private void handleFindMatches() throws SQLException {
        if (!requireActiveStudent()) return;
        Student active = studentService.getActiveStudent();

        System.out.println("\nAnalyzing skills and finding smart exchange partners for " + active.getStudentName() + "...");
        List<SkillMatch> matches = matchingService.findMatchesForStudent(active.getStudentId());
        System.out.println(matchingService.formatMatchResults(matches));
    }

    private void handleSendExchangeRequest() throws SQLException {
        if (!requireActiveStudent()) return;
        Student active = studentService.getActiveStudent();

        System.out.println("\n--- Send Skill Exchange Request ---");
        List<Skill> myTeachSkills = skillService.getSkillsByStudentAndType(active.getStudentId(), SkillType.TEACH);
        if (myTeachSkills.isEmpty()) {
            System.out.println("[ERROR] You do not have any TEACH skills listed. Please add a TEACH skill before sending requests.");
            return;
        }

        System.out.println("Your TEACH Skills (What you can offer):");
        for (Skill s : myTeachSkills) {
            System.out.printf("  * [Skill ID: %d] %s (%s)\n", s.getSkillId(), s.getSkillName(), s.getProficiencyLevel());
        }

        System.out.print("\nEnter Receiver Student ID: ");
        int receiverId = Integer.parseInt(scanner.nextLine().trim());

        List<Skill> receiverTeachSkills = skillService.getSkillsByStudentAndType(receiverId, SkillType.TEACH);
        if (receiverTeachSkills.isEmpty()) {
            System.out.println("[ERROR] The selected receiver student does not have any TEACH skills to offer.");
            return;
        }

        System.out.println("\nReceiver's TEACH Skills (What you can request):");
        for (Skill s : receiverTeachSkills) {
            System.out.printf("  * [Skill ID: %d] %s (%s)\n", s.getSkillId(), s.getSkillName(), s.getProficiencyLevel());
        }

        System.out.print("\nEnter Offered Skill ID (from your TEACH skills): ");
        int offeredSkillId = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Enter Wanted Skill ID (from receiver's TEACH skills): ");
        int wantedSkillId = Integer.parseInt(scanner.nextLine().trim());

        SkillExchangeRequest req = exchangeService.createRequest(active.getStudentId(), receiverId, offeredSkillId, wantedSkillId);
        System.out.println("\n[SUCCESS] Exchange request sent successfully!");
        System.out.printf("Request #%d is now PENDING receiver's approval.\n", req.getRequestId());
    }

    private void handleViewIncomingRequests() throws SQLException {
        if (!requireActiveStudent()) return;
        Student active = studentService.getActiveStudent();

        System.out.println("\n--- Incoming Exchange Requests ---");
        List<SkillExchangeRequest> list = exchangeService.getIncomingRequests(active.getStudentId());
        System.out.println(exchangeService.formatRequestTable(list, true));
    }

    private void handleAcceptRequest() throws SQLException {
        if (!requireActiveStudent()) return;
        Student active = studentService.getActiveStudent();

        System.out.print("\nEnter Request ID to ACCEPT: ");
        int reqId = Integer.parseInt(scanner.nextLine().trim());

        SkillExchangeRequest updated = exchangeService.acceptRequest(active.getStudentId(), reqId);
        System.out.printf("[SUCCESS] Request #%d has been ACCEPTED! The exchange agreement is now active.\n", updated.getRequestId());
    }

    private void handleRejectRequest() throws SQLException {
        if (!requireActiveStudent()) return;
        Student active = studentService.getActiveStudent();

        System.out.print("\nEnter Request ID to REJECT: ");
        int reqId = Integer.parseInt(scanner.nextLine().trim());

        SkillExchangeRequest updated = exchangeService.rejectRequest(active.getStudentId(), reqId);
        System.out.printf("[SUCCESS] Request #%d has been REJECTED.\n", updated.getRequestId());
    }

    private void handleViewOutgoingRequests() throws SQLException {
        if (!requireActiveStudent()) return;
        Student active = studentService.getActiveStudent();

        System.out.println("\n--- Outgoing Exchange Requests ---");
        List<SkillExchangeRequest> list = exchangeService.getOutgoingRequests(active.getStudentId());
        System.out.println(exchangeService.formatRequestTable(list, false));
    }

    private void handleCancelRequest() throws SQLException {
        if (!requireActiveStudent()) return;
        Student active = studentService.getActiveStudent();

        System.out.print("\nEnter Outgoing Request ID to CANCEL: ");
        int reqId = Integer.parseInt(scanner.nextLine().trim());

        SkillExchangeRequest updated = exchangeService.cancelRequest(active.getStudentId(), reqId);
        System.out.printf("[SUCCESS] Request #%d has been CANCELLED.\n", updated.getRequestId());
    }

    private void handleCompleteExchange() throws SQLException {
        if (!requireActiveStudent()) return;
        Student active = studentService.getActiveStudent();

        System.out.print("\nEnter ACCEPTED Request ID to mark as COMPLETED: ");
        int reqId = Integer.parseInt(scanner.nextLine().trim());

        SkillExchangeRequest updated = exchangeService.completeExchange(active.getStudentId(), reqId);
        System.out.printf("[SUCCESS] Exchange #%d marked as COMPLETED! Great job exchanging skills.\n", updated.getRequestId());
    }

    private void handleViewAllStudents() throws SQLException {
        System.out.println("\n--- All Registered Students ---");
        List<Student> students = studentService.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("No registered students found.");
            return;
        }

        System.out.printf("%-6s | %-22s | %-28s | %s\n", "ID", "Student Name", "Email", "Contact Info");
        System.out.println("-".repeat(80));
        for (Student s : students) {
            System.out.printf("%-6d | %-22s | %-28s | %s\n",
                    s.getStudentId(), s.getStudentName(), s.getEmail(),
                    s.getContactInfo() != null ? s.getContactInfo() : "None");
        }
    }

    private void handleViewSummary() throws SQLException {
        Integer activeId = studentService.hasActiveStudent() ? studentService.getActiveStudent().getStudentId() : null;
        PlatformReport report = reportService.generatePlatformSummary(activeId);
        System.out.println(reportService.formatReport(report));
    }
}
