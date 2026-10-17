package com.skillswap.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Domain model representing a Student in the SkillSwap platform.
 */
public class Student {

    private int studentId;
    private String studentName;
    private String email;
    private String contactInfo;
    private Timestamp createdAt;

    public Student() {
    }

    public Student(String studentName, String email, String contactInfo) {
        this.studentName = studentName;
        this.email = email;
        this.contactInfo = contactInfo;
    }

    public Student(int studentId, String studentName, String email, String contactInfo, Timestamp createdAt) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.email = email;
        this.contactInfo = contactInfo;
        this.createdAt = createdAt;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return studentId == student.studentId &&
                Objects.equals(email, student.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, email);
    }

    @Override
    public String toString() {
        return String.format("Student [ID=%d, Name='%s', Email='%s', Contact='%s', Joined=%s]",
                studentId, studentName, email, 
                contactInfo != null ? contactInfo : "N/A", 
                createdAt != null ? createdAt.toString() : "N/A");
    }
}
