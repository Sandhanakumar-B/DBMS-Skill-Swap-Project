package com.skillswap.service;

import com.skillswap.dao.SkillDAO;
import com.skillswap.dao.StudentDAO;
import com.skillswap.model.ProficiencyLevel;
import com.skillswap.model.Skill;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillMatchingServiceTest {

    private StudentDAO studentDAO;
    private SkillDAO skillDAO;
    private SkillMatchingService matchingService;

    private Student studentA;
    private Student studentB;
    private Student studentC;

    private final List<Integer> createdStudentIds = new ArrayList<>();
    private final List<Integer> createdSkillIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws SQLException {
        studentDAO = new StudentDAO();
        skillDAO = new SkillDAO();
        matchingService = new SkillMatchingService();

        // Student A: Teaches Java, Wants UI/UX
        studentA = studentDAO.insertStudent(new Student("Student A", "a.match." + System.currentTimeMillis() + "@test.edu", "111"));
        createdStudentIds.add(studentA.getStudentId());

        Skill sA1 = skillDAO.insertSkill(new Skill(studentA.getStudentId(), "Java", SkillType.TEACH, ProficiencyLevel.ADVANCED, ""));
        Skill sA2 = skillDAO.insertSkill(new Skill(studentA.getStudentId(), "UI/UX Design", SkillType.LEARN, ProficiencyLevel.BEGINNER, ""));
        createdSkillIds.add(sA1.getSkillId());
        createdSkillIds.add(sA2.getSkillId());

        // Student B: Teaches UI/UX Design, Wants Java (Perfect mutual match!)
        studentB = studentDAO.insertStudent(new Student("Student B", "b.match." + System.currentTimeMillis() + "@test.edu", "222"));
        createdStudentIds.add(studentB.getStudentId());

        Skill sB1 = skillDAO.insertSkill(new Skill(studentB.getStudentId(), "ui/ux design", SkillType.TEACH, ProficiencyLevel.ADVANCED, ""));
        Skill sB2 = skillDAO.insertSkill(new Skill(studentB.getStudentId(), "   java   ", SkillType.LEARN, ProficiencyLevel.BEGINNER, ""));
        createdSkillIds.add(sB1.getSkillId());
        createdSkillIds.add(sB2.getSkillId());

        // Student C: Teaches UI/UX Design, Wants Rust (Only one-way match for Student A!)
        studentC = studentDAO.insertStudent(new Student("Student C", "c.match." + System.currentTimeMillis() + "@test.edu", "333"));
        createdStudentIds.add(studentC.getStudentId());

        Skill sC1 = skillDAO.insertSkill(new Skill(studentC.getStudentId(), "UI/UX Design", SkillType.TEACH, ProficiencyLevel.INTERMEDIATE, ""));
        Skill sC2 = skillDAO.insertSkill(new Skill(studentC.getStudentId(), "Rust", SkillType.LEARN, ProficiencyLevel.BEGINNER, ""));
        createdSkillIds.add(sC1.getSkillId());
        createdSkillIds.add(sC2.getSkillId());
    }

    @AfterEach
    void tearDown() {
        for (int id : createdSkillIds) {
            try {
                skillDAO.deleteSkill(id);
            } catch (SQLException ignored) {
            }
        }
        for (int id : createdStudentIds) {
            try {
                studentDAO.deleteStudent(id);
            } catch (SQLException ignored) {
            }
        }
    }

    @Test
    @DisplayName("Should detect mutual match between Student A and Student B")
    void testFindMutualMatches() throws SQLException {
        List<SkillMatch> mutuals = matchingService.findMutualMatches(studentA.getStudentId());

        assertFalse(mutuals.isEmpty());
        boolean foundStudentB = false;
        for (SkillMatch m : mutuals) {
            if (m.getPartnerStudentId() == studentB.getStudentId()) {
                foundStudentB = true;
                assertTrue(m.isMutual());
                assertEquals("Java", m.getMyTeachSkillName());
                assertEquals("ui/ux design", m.getPartnerTeachSkillName());
            }
        }
        assertTrue(foundStudentB, "Student B should be identified as a mutual match for Student A");
    }

    @Test
    @DisplayName("Should exclude self (Student A) from matches")
    void testExcludeSelfFromMatches() throws SQLException {
        List<SkillMatch> matches = matchingService.findMatchesForStudent(studentA.getStudentId());
        for (SkillMatch m : matches) {
            assertTrue(m.getPartnerStudentId() != studentA.getStudentId(), "Match list must never contain the current student");
        }
    }

    @Test
    @DisplayName("Should list mutual matches first and partial matches second")
    void testMutualMatchesFirst() throws SQLException {
        List<SkillMatch> allMatches = matchingService.findMatchesForStudent(studentA.getStudentId());

        assertFalse(allMatches.isEmpty());
        // First match must be mutual
        assertTrue(allMatches.get(0).isMutual(), "First match must be a mutual match");
        assertEquals(studentB.getStudentId(), allMatches.get(0).getPartnerStudentId());

        // Subsequent matches may be partial (Student C)
        boolean hasPartialForC = false;
        for (SkillMatch m : allMatches) {
            if (m.getPartnerStudentId() == studentC.getStudentId()) {
                assertFalse(m.isMutual(), "Student C should be marked as partial (one-way) match");
                hasPartialForC = true;
            }
        }
        assertTrue(hasPartialForC, "Student C should be identified as a partial match");
    }
}
