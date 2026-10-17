package com.skillswap.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Domain model representing a Skill entry in the SkillSwap platform.
 */
public class Skill {

    private int skillId;
    private int studentId;
    private String skillName;
    private SkillType skillType;
    private ProficiencyLevel proficiencyLevel;
    private String description;
    private Timestamp createdAt;

    // Optional field populated when joined with the STUDENTS table
    private String studentName;

    public Skill() {
    }

    public Skill(int studentId, String skillName, SkillType skillType, ProficiencyLevel proficiencyLevel, String description) {
        this.studentId = studentId;
        this.skillName = skillName;
        this.skillType = skillType;
        this.proficiencyLevel = proficiencyLevel != null ? proficiencyLevel : ProficiencyLevel.BEGINNER;
        this.description = description;
    }

    public Skill(int skillId, int studentId, String skillName, SkillType skillType,
                 ProficiencyLevel proficiencyLevel, String description, Timestamp createdAt) {
        this.skillId = skillId;
        this.studentId = studentId;
        this.skillName = skillName;
        this.skillType = skillType;
        this.proficiencyLevel = proficiencyLevel != null ? proficiencyLevel : ProficiencyLevel.BEGINNER;
        this.description = description;
        this.createdAt = createdAt;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public SkillType getSkillType() {
        return skillType;
    }

    public void setSkillType(SkillType skillType) {
        this.skillType = skillType;
    }

    public ProficiencyLevel getProficiencyLevel() {
        return proficiencyLevel;
    }

    public void setProficiencyLevel(ProficiencyLevel proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Skill skill = (Skill) o;
        return skillId == skill.skillId &&
                studentId == skill.studentId &&
                Objects.equals(skillName, skill.skillName) &&
                skillType == skill.skillType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(skillId, studentId, skillName, skillType);
    }

    @Override
    public String toString() {
        return String.format("Skill [ID=%d, Name='%s', Type=%s, Level=%s, OwnerID=%d%s]",
                skillId, skillName, skillType, proficiencyLevel, studentId,
                studentName != null ? ", Owner='" + studentName + "'" : "");
    }
}
