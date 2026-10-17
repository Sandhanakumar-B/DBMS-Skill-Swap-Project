package com.skillswap.service;

import com.skillswap.dao.SkillDAO;
import com.skillswap.model.ProficiencyLevel;
import com.skillswap.model.Skill;
import com.skillswap.model.SkillType;
import com.skillswap.util.InputValidator;
import com.skillswap.util.SkillNormalizer;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Skill management, ownership checks,
 * safe edits/deletions, and parameterized search.
 */
public class SkillService {

    private final SkillDAO skillDAO;

    public SkillService() {
        this.skillDAO = new SkillDAO();
    }

    public SkillService(SkillDAO skillDAO) {
        this.skillDAO = skillDAO;
    }

    /**
     * Adds a new skill for a student.
     * 
     * @param studentId The ID of the student owning the skill
     * @param rawSkillName The raw name of the skill
     * @param skillType TEACH or LEARN
     * @param proficiencyLevel BEGINNER, INTERMEDIATE, or ADVANCED
     * @param description Brief description or goals
     * @return The persisted Skill entity with ID populated
     */
    public Skill addSkill(int studentId, String rawSkillName, SkillType skillType,
                          ProficiencyLevel proficiencyLevel, String description) throws SQLException {
        String normalizedName = SkillNormalizer.normalize(rawSkillName);

        if (!InputValidator.isNotBlank(normalizedName)) {
            throw new IllegalArgumentException("Skill name cannot be blank.");
        }

        if (normalizedName.length() > 100) {
            throw new IllegalArgumentException("Skill name cannot exceed 100 characters.");
        }

        if (skillType == null) {
            throw new IllegalArgumentException("Skill type must be specified (TEACH or LEARN).");
        }

        if (proficiencyLevel == null) {
            proficiencyLevel = ProficiencyLevel.BEGINNER;
        }

        if (skillDAO.existsByStudentAndNameAndType(studentId, normalizedName, skillType)) {
            throw new IllegalArgumentException("You have already added '" + normalizedName + "' as a " + skillType + " skill.");
        }

        Skill skill = new Skill(
                studentId,
                normalizedName,
                skillType,
                proficiencyLevel,
                InputValidator.sanitize(description)
        );

        return skillDAO.insertSkill(skill);
    }

    /**
     * Retrieves all skills listed by a student.
     */
    public List<Skill> getSkillsByStudent(int studentId) throws SQLException {
        return skillDAO.findSkillsByStudentId(studentId);
    }

    /**
     * Retrieves skills for a student filtered by TEACH or LEARN.
     */
    public List<Skill> getSkillsByStudentAndType(int studentId, SkillType skillType) throws SQLException {
        return skillDAO.findSkillsByStudentIdAndType(studentId, skillType);
    }

    /**
     * Retrieves a skill by its ID.
     */
    public Optional<Skill> getSkillById(int skillId) throws SQLException {
        return skillDAO.findSkillById(skillId);
    }

    /**
     * Updates an existing skill after verifying ownership and ensuring it is not
     * referenced in active exchange requests.
     */
    public boolean updateSkill(int studentId, int skillId, String newRawName,
                               ProficiencyLevel newLevel, String newDescription) throws SQLException {
        Optional<Skill> existingOpt = skillDAO.findSkillById(skillId);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Skill not found with ID: " + skillId);
        }

        Skill existing = existingOpt.get();
        if (existing.getStudentId() != studentId) {
            throw new SecurityException("Unauthorized: You do not own skill ID " + skillId + ".");
        }

        if (skillDAO.isSkillReferencedInActiveRequests(skillId)) {
            throw new IllegalStateException("Cannot edit skill: It is currently involved in an active (PENDING/ACCEPTED) exchange request.");
        }

        String normalizedName = SkillNormalizer.normalize(newRawName);
        if (!InputValidator.isNotBlank(normalizedName)) {
            throw new IllegalArgumentException("Skill name cannot be blank.");
        }

        if (normalizedName.length() > 100) {
            throw new IllegalArgumentException("Skill name cannot exceed 100 characters.");
        }

        existing.setSkillName(normalizedName);
        if (newLevel != null) {
            existing.setProficiencyLevel(newLevel);
        }
        existing.setDescription(InputValidator.sanitize(newDescription));

        return skillDAO.updateSkill(existing);
    }

    /**
     * Deletes a skill after verifying ownership and active request safety.
     */
    public boolean deleteSkill(int studentId, int skillId) throws SQLException {
        Optional<Skill> existingOpt = skillDAO.findSkillById(skillId);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Skill not found with ID: " + skillId);
        }

        Skill existing = existingOpt.get();
        if (existing.getStudentId() != studentId) {
            throw new SecurityException("Unauthorized: You do not own skill ID " + skillId + ".");
        }

        if (skillDAO.isSkillReferencedInActiveRequests(skillId)) {
            throw new IllegalStateException("Cannot delete skill: It is currently involved in an active (PENDING/ACCEPTED) exchange request.");
        }

        return skillDAO.deleteSkill(skillId);
    }

    /**
     * Searches skills by keyword and type, optionally excluding the current student.
     */
    public List<Skill> searchSkills(String keyword, SkillType skillType, Integer excludeStudentId) throws SQLException {
        return skillDAO.searchSkills(keyword, skillType, excludeStudentId);
    }

    /**
     * Returns total skills count across the platform.
     */
    public int getTotalSkillsCount() throws SQLException {
        return skillDAO.getTotalSkillsCount();
    }

    /**
     * Formats a list of skills into a clean, human-readable console table.
     */
    public String formatSkillTable(List<Skill> skills) {
        if (skills == null || skills.isEmpty()) {
            return "No skills found.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-6s | %-18s | %-7s | %-12s | %-16s | %s\n",
                "ID", "Skill Name", "Type", "Proficiency", "Student", "Description"));
        sb.append("-".repeat(85)).append("\n");

        for (Skill s : skills) {
            sb.append(String.format("%-6d | %-18s | %-7s | %-12s | %-16s | %s\n",
                    s.getSkillId(),
                    s.getSkillName(),
                    s.getSkillType(),
                    s.getProficiencyLevel(),
                    s.getStudentName() != null ? s.getStudentName() : ("Student #" + s.getStudentId()),
                    s.getDescription() != null ? s.getDescription() : ""));
        }
        return sb.toString();
    }
}
