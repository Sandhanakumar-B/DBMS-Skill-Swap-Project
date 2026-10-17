package com.skillswap.service;

import com.skillswap.dao.SkillDAO;
import com.skillswap.dao.StudentDAO;
import com.skillswap.model.ProficiencyLevel;
import com.skillswap.model.Skill;
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

class SkillServiceTest {

    private StudentDAO studentDAO;
    private SkillDAO skillDAO;
    private SkillService skillService;

    private Student student1;
    private Student student2;
    private final List<Integer> createdStudentIds = new ArrayList<>();
    private final List<Integer> createdSkillIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws SQLException {
        studentDAO = new StudentDAO();
        skillDAO = new SkillDAO();
        skillService = new SkillService(skillDAO);

        // Create two test students for skill ownership tests
        student1 = studentDAO.insertStudent(new Student(
                "Skill Tester 1",
                "skilltester1." + System.currentTimeMillis() + "@test.edu",
                "111"
        ));
        createdStudentIds.add(student1.getStudentId());

        student2 = studentDAO.insertStudent(new Student(
                "Skill Tester 2",
                "skilltester2." + System.currentTimeMillis() + "@test.edu",
                "222"
        ));
        createdStudentIds.add(student2.getStudentId());
    }

    @AfterEach
    void tearDown() {
        // Clean up skills first
        for (int id : createdSkillIds) {
            try {
                skillDAO.deleteSkill(id);
            } catch (SQLException ignored) {
            }
        }
        // Then clean up students
        for (int id : createdStudentIds) {
            try {
                studentDAO.deleteStudent(id);
            } catch (SQLException ignored) {
            }
        }
    }

    @Test
    @DisplayName("Should add TEACH and LEARN skills with name normalization")
    void testAddSkillsSuccess() throws SQLException {
        Skill teachSkill = skillService.addSkill(
                student1.getStudentId(),
                "   java   programming  ",
                SkillType.TEACH,
                ProficiencyLevel.ADVANCED,
                "Core Java, OOP, Streams"
        );

        assertNotNull(teachSkill);
        assertTrue(teachSkill.getSkillId() > 0);
        assertEquals("Java Programming", teachSkill.getSkillName(), "Skill name should be normalized");
        assertEquals(SkillType.TEACH, teachSkill.getSkillType());
        assertEquals(ProficiencyLevel.ADVANCED, teachSkill.getProficiencyLevel());
        createdSkillIds.add(teachSkill.getSkillId());

        Skill learnSkill = skillService.addSkill(
                student1.getStudentId(),
                "ui/ux",
                SkillType.LEARN,
                ProficiencyLevel.BEGINNER,
                "Figma and wireframing"
        );
        assertEquals("UI/UX", learnSkill.getSkillName());
        assertEquals(SkillType.LEARN, learnSkill.getSkillType());
        createdSkillIds.add(learnSkill.getSkillId());
    }

    @Test
    @DisplayName("Should reject blank skill name")
    void testAddSkillBlankName() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                skillService.addSkill(student1.getStudentId(), "   ", SkillType.TEACH, ProficiencyLevel.BEGINNER, ""));
        assertTrue(ex.getMessage().contains("cannot be blank"));
    }

    @Test
    @DisplayName("Should prevent duplicate skill with same name and type for the same student")
    void testPreventDuplicateSkill() throws SQLException {
        Skill first = skillService.addSkill(student1.getStudentId(), "Python", SkillType.TEACH,
                ProficiencyLevel.INTERMEDIATE, "Flask & Scripting");
        createdSkillIds.add(first.getSkillId());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                skillService.addSkill(student1.getStudentId(), "  python  ", SkillType.TEACH,
                        ProficiencyLevel.ADVANCED, "Duplicate attempt"));
        assertTrue(ex.getMessage().contains("already added"));
    }

    @Test
    @DisplayName("Should filter skills by TEACH and LEARN types")
    void testFilterSkillsByType() throws SQLException {
        Skill s1 = skillService.addSkill(student1.getStudentId(), "Kotlin", SkillType.TEACH, ProficiencyLevel.ADVANCED, "");
        Skill s2 = skillService.addSkill(student1.getStudentId(), "Docker", SkillType.LEARN, ProficiencyLevel.BEGINNER, "");
        createdSkillIds.add(s1.getSkillId());
        createdSkillIds.add(s2.getSkillId());

        List<Skill> teachSkills = skillService.getSkillsByStudentAndType(student1.getStudentId(), SkillType.TEACH);
        assertEquals(1, teachSkills.size());
        assertEquals("Kotlin", teachSkills.get(0).getSkillName());

        List<Skill> learnSkills = skillService.getSkillsByStudentAndType(student1.getStudentId(), SkillType.LEARN);
        assertEquals(1, learnSkills.size());
        assertEquals("Docker", learnSkills.get(0).getSkillName());
    }

    @Test
    @DisplayName("Should update skill details when student is owner")
    void testUpdateSkillSuccess() throws SQLException {
        Skill skill = skillService.addSkill(student1.getStudentId(), "React", SkillType.TEACH,
                ProficiencyLevel.BEGINNER, "Basics");
        createdSkillIds.add(skill.getSkillId());

        boolean updated = skillService.updateSkill(
                student1.getStudentId(),
                skill.getSkillId(),
                "React Native",
                ProficiencyLevel.ADVANCED,
                "Mobile apps with React"
        );
        assertTrue(updated);

        Optional<Skill> fetched = skillService.getSkillById(skill.getSkillId());
        assertTrue(fetched.isPresent());
        assertEquals("React Native", fetched.get().getSkillName());
        assertEquals(ProficiencyLevel.ADVANCED, fetched.get().getProficiencyLevel());
    }

    @Test
    @DisplayName("Should reject skill update from non-owner student")
    void testUpdateSkillUnauthorized() throws SQLException {
        Skill skill = skillService.addSkill(student1.getStudentId(), "Rust", SkillType.TEACH,
                ProficiencyLevel.BEGINNER, "Systems");
        createdSkillIds.add(skill.getSkillId());

        // student2 tries to update student1's skill
        SecurityException ex = assertThrows(SecurityException.class, () ->
                skillService.updateSkill(student2.getStudentId(), skill.getSkillId(),
                        "Rust Lang", ProficiencyLevel.ADVANCED, "Hacked"));
        assertTrue(ex.getMessage().contains("Unauthorized"));
    }

    @Test
    @DisplayName("Should delete skill successfully by owner")
    void testDeleteSkillSuccess() throws SQLException {
        Skill skill = skillService.addSkill(student1.getStudentId(), "Go", SkillType.TEACH,
                ProficiencyLevel.BEGINNER, "");

        boolean deleted = skillService.deleteSkill(student1.getStudentId(), skill.getSkillId());
        assertTrue(deleted);

        Optional<Skill> fetched = skillService.getSkillById(skill.getSkillId());
        assertFalse(fetched.isPresent());
    }

    @Test
    @DisplayName("Should search skills by keyword and respect exclusion of current student")
    void testSearchSkills() throws SQLException {
        String uniqueSkill = "Microservices" + System.currentTimeMillis();
        Skill s1 = skillService.addSkill(student1.getStudentId(), uniqueSkill, SkillType.TEACH, ProficiencyLevel.ADVANCED, "");
        Skill s2 = skillService.addSkill(student2.getStudentId(), uniqueSkill, SkillType.TEACH, ProficiencyLevel.INTERMEDIATE, "");
        createdSkillIds.add(s1.getSkillId());
        createdSkillIds.add(s2.getSkillId());

        // Search all
        List<Skill> allResults = skillService.searchSkills(uniqueSkill, SkillType.TEACH, null);
        assertEquals(2, allResults.size());

        // Search excluding student1 (potential partners search)
        List<Skill> partnerResults = skillService.searchSkills(uniqueSkill, SkillType.TEACH, student1.getStudentId());
        assertEquals(1, partnerResults.size());
        assertEquals(student2.getStudentId(), partnerResults.get(0).getStudentId());
    }
}
