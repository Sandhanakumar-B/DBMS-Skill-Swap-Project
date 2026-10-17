package com.skillswap.service;

import com.skillswap.dao.SkillDAO;
import com.skillswap.dao.SkillExchangeRequestDAO;
import com.skillswap.dao.StudentDAO;
import com.skillswap.model.ProficiencyLevel;
import com.skillswap.model.RequestStatus;
import com.skillswap.model.Skill;
import com.skillswap.model.SkillExchangeRequest;
import com.skillswap.model.SkillMatch;
import com.skillswap.model.SkillType;
import com.skillswap.model.Student;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 9: Comprehensive Automated Edge Cases and Security Testing.
 * Validates input boundary limits, SQL injection immunity, concurrency status transitions,
 * unauthorized operation blocks, and active foreign key integrity constraints.
 */
class EdgeCaseTest {

    private StudentDAO studentDAO;
    private SkillDAO skillDAO;
    private SkillExchangeRequestDAO requestDAO;

    private StudentService studentService;
    private SkillService skillService;
    private ExchangeService exchangeService;
    private SkillMatchingService matchingService;

    private final List<Integer> createdRequestIds = new ArrayList<>();
    private final List<Integer> createdSkillIds = new ArrayList<>();
    private final List<Integer> createdStudentIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        studentDAO = new StudentDAO();
        skillDAO = new SkillDAO();
        requestDAO = new SkillExchangeRequestDAO();

        studentService = new StudentService(studentDAO);
        skillService = new SkillService(skillDAO);
        exchangeService = new ExchangeService(requestDAO, studentDAO, skillDAO);
        matchingService = new SkillMatchingService();
    }

    @AfterEach
    void tearDown() {
        // Clean up created requests first to avoid foreign key violations
        for (int id : createdRequestIds) {
            try {
                requestDAO.deleteRequest(id);
            } catch (SQLException ignored) {
            }
        }
        // Clean up created skills second
        for (int id : createdSkillIds) {
            try {
                skillDAO.deleteSkill(id);
            } catch (SQLException ignored) {
            }
        }
        // Clean up created students third
        for (int id : createdStudentIds) {
            try {
                studentDAO.deleteStudent(id);
            } catch (SQLException ignored) {
            }
        }
    }

    // =========================================================================
    // 1. STUDENT BOUNDARY AND VALIDATION EDGE CASES
    // =========================================================================

    @Test
    @DisplayName("Should reject student name exceeding 100 characters")
    void testStudentNameExceedingMaxLength() {
        String longName = "A".repeat(101);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                studentService.registerStudent(longName, "valid@test.edu", "123"));
        assertTrue(ex.getMessage().contains("cannot exceed 100 characters"));
    }

    @Test
    @DisplayName("Should reject student email exceeding 150 characters")
    void testStudentEmailExceedingMaxLength() {
        String longEmail = "a".repeat(145) + "@test.edu"; // > 150 chars
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                studentService.registerStudent("Valid Name", longEmail, "123"));
        assertTrue(ex.getMessage().contains("cannot exceed 150 characters"));
    }

    @Test
    @DisplayName("Should reject student contact info exceeding 100 characters")
    void testStudentContactExceedingMaxLength() {
        String longContact = "9".repeat(101);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                studentService.registerStudent("Valid Name", "valid@test.edu", longContact));
        assertTrue(ex.getMessage().contains("cannot exceed 100 characters"));
    }

    @Test
    @DisplayName("Should reject duplicate email regardless of uppercase/lowercase differences")
    void testCaseInsensitiveDuplicateEmail() throws SQLException {
        String baseEmail = "unique.case." + System.currentTimeMillis() + "@univ.edu";
        Student s1 = studentService.registerStudent("Original Case", baseEmail.toLowerCase(), "111");
        createdStudentIds.add(s1.getStudentId());

        // Attempt registering with uppercase version
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                studentService.registerStudent("Duplicate Case", baseEmail.toUpperCase(), "222"));
        assertTrue(ex.getMessage().contains("already registered"));
    }

    // =========================================================================
    // 2. SQL INJECTION IMMUNITY TESTS
    // =========================================================================

    @Test
    @DisplayName("Should safely handle SQL injection payloads in student registration without syntax errors")
    void testSqlInjectionInStudentRegistration() throws SQLException {
        String sqlInjectionName = "Robert'); DROP TABLE STUDENTS;--";
        String testEmail = "sqli." + System.currentTimeMillis() + "@security.edu";

        Student s = studentService.registerStudent(sqlInjectionName, testEmail, "' OR '1'='1");
        assertNotNull(s);
        createdStudentIds.add(s.getStudentId());

        // Verify the student was saved with the literal string and tables remain intact
        Optional<Student> fetched = studentService.getStudentById(s.getStudentId());
        assertTrue(fetched.isPresent());
        assertEquals(sqlInjectionName, fetched.get().getStudentName());
        assertTrue(studentService.getTotalStudentsCount() > 0, "STUDENTS table must remain intact");
    }

    @Test
    @DisplayName("Should safely handle SQL injection payloads in skill search query")
    void testSqlInjectionInSkillSearch() throws SQLException {
        Student s = studentService.registerStudent("Search Tester", "search." + System.currentTimeMillis() + "@test.edu", "111");
        createdStudentIds.add(s.getStudentId());

        Skill sk = skillService.addSkill(s.getStudentId(), "PostgreSQL", SkillType.TEACH, ProficiencyLevel.ADVANCED, "SQL basics");
        createdSkillIds.add(sk.getSkillId());

        // Malicious SQL injection payloads in search query
        List<Skill> injectionResult1 = skillService.searchSkills("' OR '1'='1", null, null);
        assertNotNull(injectionResult1);

        List<Skill> injectionResult2 = skillService.searchSkills("'; DROP TABLE SKILLS; --", null, null);
        assertNotNull(injectionResult2);

        // Verify table still exists and data is untouched
        assertTrue(skillService.getTotalSkillsCount() > 0, "SKILLS table must remain intact");
    }

    // =========================================================================
    // 3. SKILL MANAGEMENT EDGE CASES
    // =========================================================================

    @Test
    @DisplayName("Should support programming skill names with special characters (C++, C#, Node.js, UI/UX)")
    void testSkillNamesWithSpecialCharacters() throws SQLException {
        Student s = studentService.registerStudent("Polyglot Dev", "polyglot." + System.currentTimeMillis() + "@test.edu", "111");
        createdStudentIds.add(s.getStudentId());

        Skill cpp = skillService.addSkill(s.getStudentId(), "C++", SkillType.TEACH, ProficiencyLevel.ADVANCED, "Modern C++20");
        createdSkillIds.add(cpp.getSkillId());
        assertEquals("C++", cpp.getSkillName());

        Skill csharp = skillService.addSkill(s.getStudentId(), "c#", SkillType.TEACH, ProficiencyLevel.INTERMEDIATE, ".NET");
        createdSkillIds.add(csharp.getSkillId());
        assertEquals("C#", csharp.getSkillName());

        Skill node = skillService.addSkill(s.getStudentId(), "node.js", SkillType.LEARN, ProficiencyLevel.BEGINNER, "Backend JS");
        createdSkillIds.add(node.getSkillId());
        assertEquals("Node.js", node.getSkillName());

        Skill ui = skillService.addSkill(s.getStudentId(), "ui/ux", SkillType.TEACH, ProficiencyLevel.ADVANCED, "Design");
        createdSkillIds.add(ui.getSkillId());
        assertEquals("UI/UX", ui.getSkillName());
    }

    @Test
    @DisplayName("Should reject skill name exceeding 100 characters")
    void testSkillNameExceedingMaxLength() throws SQLException {
        Student s = studentService.registerStudent("Skill Max Tester", "skillmax." + System.currentTimeMillis() + "@test.edu", "111");
        createdStudentIds.add(s.getStudentId());

        String longSkillName = "J".repeat(101);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                skillService.addSkill(s.getStudentId(), longSkillName, SkillType.TEACH, ProficiencyLevel.BEGINNER, ""));
        assertTrue(ex.getMessage().contains("cannot exceed 100 characters"));
    }

    @Test
    @DisplayName("Should prevent deleting or modifying a skill involved in an active (PENDING/ACCEPTED) request")
    void testPreventDeleteOrEditSkillInActiveRequest() throws SQLException {
        // Setup two students and active request
        Student sA = studentService.registerStudent("Alice Active", "alice.active." + System.currentTimeMillis() + "@test.edu", "111");
        Student sB = studentService.registerStudent("Bob Active", "bob.active." + System.currentTimeMillis() + "@test.edu", "222");
        createdStudentIds.add(sA.getStudentId());
        createdStudentIds.add(sB.getStudentId());

        Skill sATeach = skillService.addSkill(sA.getStudentId(), "Java", SkillType.TEACH, ProficiencyLevel.ADVANCED, "");
        Skill sBTeach = skillService.addSkill(sB.getStudentId(), "Python", SkillType.TEACH, ProficiencyLevel.ADVANCED, "");
        createdSkillIds.add(sATeach.getSkillId());
        createdSkillIds.add(sBTeach.getSkillId());

        SkillExchangeRequest req = exchangeService.createRequest(sA.getStudentId(), sB.getStudentId(),
                sATeach.getSkillId(), sBTeach.getSkillId());
        createdRequestIds.add(req.getRequestId());

        // 1. Attempt to delete offered skill while request is PENDING
        IllegalStateException exDel = assertThrows(IllegalStateException.class, () ->
                skillService.deleteSkill(sA.getStudentId(), sATeach.getSkillId()));
        assertTrue(exDel.getMessage().contains("active (PENDING/ACCEPTED)"));

        // 2. Attempt to update offered skill while request is PENDING
        IllegalStateException exUpd = assertThrows(IllegalStateException.class, () ->
                skillService.updateSkill(sA.getStudentId(), sATeach.getSkillId(), "Java Enterprise", ProficiencyLevel.ADVANCED, ""));
        assertTrue(exUpd.getMessage().contains("active (PENDING/ACCEPTED)"));

        // 3. Reject the request -> Now skill can be safely updated and deleted
        exchangeService.rejectRequest(sB.getStudentId(), req.getRequestId());

        boolean updated = skillService.updateSkill(sA.getStudentId(), sATeach.getSkillId(), "Java 17 LTS", ProficiencyLevel.ADVANCED, "Updated");
        assertTrue(updated, "Skill should be updateable once request is REJECTED (no longer active)");
    }

    // =========================================================================
    // 4. EXCHANGE REQUEST INTEGRITY & STATUS LIFECYCLE EDGE CASES
    // =========================================================================

    @Test
    @DisplayName("Should reject exchange request referencing non-existent student or skill IDs")
    void testNonExistentIdsInExchangeRequest() throws SQLException {
        Student s = studentService.registerStudent("Exist Student", "exist." + System.currentTimeMillis() + "@test.edu", "111");
        createdStudentIds.add(s.getStudentId());

        Skill sk = skillService.addSkill(s.getStudentId(), "Docker", SkillType.TEACH, ProficiencyLevel.INTERMEDIATE, "");
        createdSkillIds.add(sk.getSkillId());

        // Non-existent receiver
        assertThrows(IllegalArgumentException.class, () ->
                exchangeService.createRequest(s.getStudentId(), 9999999, sk.getSkillId(), 8888888));

        // Non-existent offered skill
        assertThrows(IllegalArgumentException.class, () ->
                exchangeService.createRequest(s.getStudentId(), s.getStudentId() + 1, 9999999, 8888888));
    }

    @Test
    @DisplayName("Should reject invalid lifecycle transitions: Cannot accept or reject after completion")
    void testInvalidTransitionsAfterCompletion() throws SQLException {
        Student sA = studentService.registerStudent("Alice Comp", "alice.comp." + System.currentTimeMillis() + "@test.edu", "111");
        Student sB = studentService.registerStudent("Bob Comp", "bob.comp." + System.currentTimeMillis() + "@test.edu", "222");
        createdStudentIds.add(sA.getStudentId());
        createdStudentIds.add(sB.getStudentId());

        Skill sATeach = skillService.addSkill(sA.getStudentId(), "C#", SkillType.TEACH, ProficiencyLevel.ADVANCED, "");
        Skill sBTeach = skillService.addSkill(sB.getStudentId(), "Rust", SkillType.TEACH, ProficiencyLevel.ADVANCED, "");
        createdSkillIds.add(sATeach.getSkillId());
        createdSkillIds.add(sBTeach.getSkillId());

        SkillExchangeRequest req = exchangeService.createRequest(sA.getStudentId(), sB.getStudentId(),
                sATeach.getSkillId(), sBTeach.getSkillId());
        createdRequestIds.add(req.getRequestId());

        // Bob accepts, then Alice completes
        exchangeService.acceptRequest(sB.getStudentId(), req.getRequestId());
        exchangeService.completeExchange(sA.getStudentId(), req.getRequestId());

        // 1. Attempt to accept an already COMPLETED request
        IllegalStateException ex1 = assertThrows(IllegalStateException.class, () ->
                exchangeService.acceptRequest(sB.getStudentId(), req.getRequestId()));
        assertTrue(ex1.getMessage().contains("Only PENDING requests"));

        // 2. Attempt to reject an already COMPLETED request
        IllegalStateException ex2 = assertThrows(IllegalStateException.class, () ->
                exchangeService.rejectRequest(sB.getStudentId(), req.getRequestId()));
        assertTrue(ex2.getMessage().contains("Only PENDING requests"));

        // 3. Attempt to cancel an already COMPLETED request
        IllegalStateException ex3 = assertThrows(IllegalStateException.class, () ->
                exchangeService.cancelRequest(sA.getStudentId(), req.getRequestId()));
        assertTrue(ex3.getMessage().contains("Only PENDING or ACCEPTED"));
    }

    // =========================================================================
    // 5. SMART MATCHING BOUNDARY EDGE CASES
    // =========================================================================

    @Test
    @DisplayName("Should return empty match list cleanly when student has no registered skills")
    void testMatchingWithNoRegisteredSkills() throws SQLException {
        Student s = studentService.registerStudent("Empty Student", "empty." + System.currentTimeMillis() + "@test.edu", "111");
        createdStudentIds.add(s.getStudentId());

        List<SkillMatch> matches = matchingService.findMatchesForStudent(s.getStudentId());
        assertNotNull(matches);
        assertTrue(matches.isEmpty(), "A student with no listed skills should receive zero matches");

        String formatted = matchingService.formatMatchResults(matches);
        assertTrue(formatted.contains("No skill matches found"));
    }

    @Test
    @DisplayName("Should return empty match list when student has skills but no complementary partners exist")
    void testMatchingWithNoComplementaryPartners() throws SQLException {
        Student s = studentService.registerStudent("Niche Skill Student", "niche." + System.currentTimeMillis() + "@test.edu", "111");
        createdStudentIds.add(s.getStudentId());

        Skill teachSkill = skillService.addSkill(s.getStudentId(), "Ancient Sanskrit", SkillType.TEACH, ProficiencyLevel.ADVANCED, "");
        Skill learnSkill = skillService.addSkill(s.getStudentId(), "Quantum Assembly", SkillType.LEARN, ProficiencyLevel.BEGINNER, "");
        createdSkillIds.add(teachSkill.getSkillId());
        createdSkillIds.add(learnSkill.getSkillId());

        List<SkillMatch> matches = matchingService.findMatchesForStudent(s.getStudentId());
        assertNotNull(matches);
        assertTrue(matches.isEmpty(), "Should receive 0 matches when no other student has complementary skills");
    }
}
