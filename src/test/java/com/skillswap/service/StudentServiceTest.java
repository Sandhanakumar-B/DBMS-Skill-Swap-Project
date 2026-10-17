package com.skillswap.service;

import com.skillswap.dao.StudentDAO;
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

class StudentServiceTest {

    private StudentDAO studentDAO;
    private StudentService studentService;
    private final List<Integer> createdStudentIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        studentDAO = new StudentDAO();
        studentService = new StudentService(studentDAO);
    }

    @AfterEach
    void tearDown() {
        // Clean up test data inserted into the database
        for (int id : createdStudentIds) {
            try {
                studentDAO.deleteStudent(id);
            } catch (SQLException ignored) {
            }
        }
    }

    @Test
    @DisplayName("Should register a new student with valid data successfully")
    void testRegisterStudentSuccess() throws SQLException {
        String testEmail = "alice.phase3." + System.currentTimeMillis() + "@test.edu";
        Student student = studentService.registerStudent("Alice Test", testEmail, "+1-555-0101");

        assertNotNull(student);
        assertTrue(student.getStudentId() > 0, "Generated student ID should be > 0");
        assertEquals("Alice Test", student.getStudentName());
        assertEquals(testEmail, student.getEmail());
        assertEquals("+1-555-0101", student.getContactInfo());
        createdStudentIds.add(student.getStudentId());

        // Verify that newly registered student is set as active
        assertTrue(studentService.hasActiveStudent());
        assertEquals(student.getStudentId(), studentService.getActiveStudent().getStudentId());
    }

    @Test
    @DisplayName("Should reject student registration with blank name")
    void testRegisterStudentBlankName() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                studentService.registerStudent("   ", "valid@test.edu", "1234567890"));
        assertTrue(ex.getMessage().contains("name cannot be blank"));
    }

    @Test
    @DisplayName("Should reject student registration with invalid email format")
    void testRegisterStudentInvalidEmail() {
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () ->
                studentService.registerStudent("Bob Test", "notanemail", "1234567890"));
        assertTrue(ex1.getMessage().contains("Invalid email format"));

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
                studentService.registerStudent("Bob Test", "bob@bad", "1234567890"));
        assertTrue(ex2.getMessage().contains("Invalid email format"));
    }

    @Test
    @DisplayName("Should prevent duplicate email registration")
    void testPreventDuplicateEmail() throws SQLException {
        String duplicateEmail = "duplicate." + System.currentTimeMillis() + "@test.edu";
        Student s1 = studentService.registerStudent("Original Student", duplicateEmail, "111");
        createdStudentIds.add(s1.getStudentId());

        // Attempting to register again with the same email
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                studentService.registerStudent("Imposter Student", duplicateEmail, "222"));
        assertTrue(ex.getMessage().contains("already registered"));
    }

    @Test
    @DisplayName("Should switch and clear active student session")
    void testSwitchAndClearActiveStudent() throws SQLException {
        String email1 = "user1." + System.currentTimeMillis() + "@test.edu";
        String email2 = "user2." + System.currentTimeMillis() + "@test.edu";

        Student s1 = studentService.registerStudent("User One", email1, "111");
        createdStudentIds.add(s1.getStudentId());

        Student s2 = studentService.registerStudent("User Two", email2, "222");
        createdStudentIds.add(s2.getStudentId());

        // Switch active session to User One
        studentService.switchActiveStudent(s1.getStudentId());
        assertEquals("User One", studentService.getActiveStudent().getStudentName());

        // Switch active session to User Two
        studentService.switchActiveStudent(s2.getStudentId());
        assertEquals("User Two", studentService.getActiveStudent().getStudentName());

        // Clear active session
        studentService.clearActiveStudent();
        assertFalse(studentService.hasActiveStudent());
    }

    @Test
    @DisplayName("Should throw exception when switching to non-existent student ID")
    void testSwitchToNonExistentStudent() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                studentService.switchActiveStudent(99999999));
        assertTrue(ex.getMessage().contains("No student found with ID"));
    }

    @Test
    @DisplayName("Should retrieve student by ID and format profile string")
    void testGetStudentByIdAndFormatProfile() throws SQLException {
        String email = "profile." + System.currentTimeMillis() + "@test.edu";
        Student registered = studentService.registerStudent("Profile Tester", email, "999-000");
        createdStudentIds.add(registered.getStudentId());

        Optional<Student> found = studentService.getStudentById(registered.getStudentId());
        assertTrue(found.isPresent());
        assertEquals("Profile Tester", found.get().getStudentName());

        String profile = studentService.formatStudentProfile(found.get());
        assertTrue(profile.contains("STUDENT PROFILE"));
        assertTrue(profile.contains("Profile Tester"));
        assertTrue(profile.contains(email));
    }
}
