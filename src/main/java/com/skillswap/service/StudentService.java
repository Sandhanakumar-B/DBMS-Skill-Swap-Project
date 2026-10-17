package com.skillswap.service;

import com.skillswap.dao.StudentDAO;
import com.skillswap.model.Student;
import com.skillswap.util.InputValidator;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Student-related business logic, validations,
 * and active student session management.
 */
public class StudentService {

    private final StudentDAO studentDAO;
    private Student activeStudent;

    public StudentService() {
        this.studentDAO = new StudentDAO();
    }

    public StudentService(StudentDAO studentDAO) {
        this.studentDAO = studentDAO;
    }

    /**
     * Registers a new student after validating required fields and email format,
     * and ensuring no duplicate email exists.
     * 
     * @param name Student's full name
     * @param email Student's unique email address
     * @param contactInfo Optional contact details (phone, LinkedIn, etc.)
     * @return The registered Student entity with generated ID
     * @throws IllegalArgumentException if input validation fails
     * @throws SQLException if a database operation fails
     */
    public Student registerStudent(String name, String email, String contactInfo) throws SQLException {
        String cleanName = InputValidator.sanitize(name);
        String cleanEmail = InputValidator.sanitize(email);
        String cleanContact = InputValidator.sanitize(contactInfo);

        if (!InputValidator.isNotBlank(cleanName)) {
            throw new IllegalArgumentException("Student name cannot be blank.");
        }

        if (cleanName.length() > 100) {
            throw new IllegalArgumentException("Student name cannot exceed 100 characters.");
        }

        if (!InputValidator.isValidEmail(cleanEmail)) {
            throw new IllegalArgumentException("Invalid email format: '" + cleanEmail + "'. Please enter a valid email address.");
        }

        if (cleanEmail.length() > 150) {
            throw new IllegalArgumentException("Email address cannot exceed 150 characters.");
        }

        if (cleanContact.length() > 100) {
            throw new IllegalArgumentException("Contact info cannot exceed 100 characters.");
        }

        if (studentDAO.emailExists(cleanEmail)) {
            throw new IllegalArgumentException("The email '" + cleanEmail + "' is already registered. Please use another email or select your ID.");
        }

        Student student = new Student(cleanName, cleanEmail, cleanContact.isEmpty() ? null : cleanContact);
        Student registered = studentDAO.insertStudent(student);

        // If no active student is currently selected, set this newly registered student as active
        if (this.activeStudent == null) {
            this.activeStudent = registered;
        }

        return registered;
    }

    /**
     * Selects and activates a student session by student ID.
     * Note: This simple ID selection is designed for classroom/project demonstrations,
     * not production authentication.
     * 
     * @param studentId ID of the student to switch to
     * @return The newly active Student
     * @throws IllegalArgumentException if the student ID does not exist
     * @throws SQLException if a database error occurs
     */
    public Student switchActiveStudent(int studentId) throws SQLException {
        Optional<Student> studentOpt = studentDAO.findStudentById(studentId);
        if (studentOpt.isEmpty()) {
            throw new IllegalArgumentException("No student found with ID: " + studentId);
        }
        this.activeStudent = studentOpt.get();
        return this.activeStudent;
    }

    /**
     * Returns the currently active student session.
     */
    public Student getActiveStudent() {
        return this.activeStudent;
    }

    /**
     * Sets the active student session.
     */
    public void setActiveStudent(Student student) {
        this.activeStudent = student;
    }

    /**
     * Checks if a student is currently logged in / active.
     */
    public boolean hasActiveStudent() {
        return this.activeStudent != null;
    }

    /**
     * Clears the current active student session.
     */
    public void clearActiveStudent() {
        this.activeStudent = null;
    }

    /**
     * Retrieves a student by their ID.
     */
    public Optional<Student> getStudentById(int studentId) throws SQLException {
        return studentDAO.findStudentById(studentId);
    }

    /**
     * Retrieves a student by their email.
     */
    public Optional<Student> getStudentByEmail(String email) throws SQLException {
        return studentDAO.findStudentByEmail(email);
    }

    /**
     * Retrieves all registered students.
     */
    public List<Student> getAllStudents() throws SQLException {
        return studentDAO.findAllStudents();
    }

    /**
     * Returns the total count of registered students.
     */
    public int getTotalStudentsCount() throws SQLException {
        return studentDAO.getTotalStudentsCount();
    }

    /**
     * Formats and prints a student's basic profile details.
     */
    public String formatStudentProfile(Student student) {
        if (student == null) {
            return "No student selected.";
        }
        return """
            ==================================================
                           STUDENT PROFILE
            ==================================================
             Student ID    : %d
             Full Name     : %s
             Email Address : %s
             Contact Info  : %s
             Member Since  : %s
            ==================================================
            """.formatted(
                student.getStudentId(),
                student.getStudentName(),
                student.getEmail(),
                student.getContactInfo() != null ? student.getContactInfo() : "N/A",
                student.getCreatedAt() != null ? student.getCreatedAt().toString() : "N/A"
        );
    }
}
