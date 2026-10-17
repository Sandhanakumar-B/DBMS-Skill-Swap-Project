package com.skillswap.dao;

import com.skillswap.config.DatabaseConnection;
import com.skillswap.model.ProficiencyLevel;
import com.skillswap.model.Skill;
import com.skillswap.model.SkillType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) for the SKILLS table.
 * Encapsulates skill persistence, search queries, ownership checks, and reference validations.
 */
public class SkillDAO {

    /**
     * Inserts a new skill into the database and populates its generated skill_id.
     */
    public Skill insertSkill(Skill skill) throws SQLException {
        String sql = """
            INSERT INTO SKILLS (student_id, skill_name, skill_type, proficiency_level, description)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, skill.getStudentId());
            stmt.setString(2, skill.getSkillName());
            stmt.setString(3, skill.getSkillType().name());
            stmt.setString(4, skill.getProficiencyLevel().name());
            stmt.setString(5, skill.getDescription());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Inserting skill failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    skill.setSkillId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Inserting skill failed, no ID obtained.");
                }
            }

            return findSkillById(skill.getSkillId()).orElse(skill);
        }
    }

    /**
     * Finds a skill by ID, joining with STUDENTS to populate the owner's name.
     */
    public Optional<Skill> findSkillById(int skillId) throws SQLException {
        String sql = """
            SELECT s.skill_id, s.student_id, s.skill_name, s.skill_type, s.proficiency_level, 
                   s.description, s.created_at, st.student_name
            FROM SKILLS s
            JOIN STUDENTS st ON s.student_id = st.student_id
            WHERE s.skill_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, skillId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSkill(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves all skills belonging to a specific student.
     */
    public List<Skill> findSkillsByStudentId(int studentId) throws SQLException {
        String sql = """
            SELECT s.skill_id, s.student_id, s.skill_name, s.skill_type, s.proficiency_level, 
                   s.description, s.created_at, st.student_name
            FROM SKILLS s
            JOIN STUDENTS st ON s.student_id = st.student_id
            WHERE s.student_id = ?
            ORDER BY s.skill_type DESC, s.skill_name ASC
            """;
        List<Skill> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSkill(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves skills for a student filtered by TEACH or LEARN.
     */
    public List<Skill> findSkillsByStudentIdAndType(int studentId, SkillType skillType) throws SQLException {
        String sql = """
            SELECT s.skill_id, s.student_id, s.skill_name, s.skill_type, s.proficiency_level, 
                   s.description, s.created_at, st.student_name
            FROM SKILLS s
            JOIN STUDENTS st ON s.student_id = st.student_id
            WHERE s.student_id = ? AND s.skill_type = ?
            ORDER BY s.skill_name ASC
            """;
        List<Skill> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setString(2, skillType.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSkill(rs));
                }
            }
        }
        return list;
    }

    /**
     * Updates an existing skill's name, proficiency level, and description.
     */
    public boolean updateSkill(Skill skill) throws SQLException {
        String sql = """
            UPDATE SKILLS 
            SET skill_name = ?, proficiency_level = ?, description = ?
            WHERE skill_id = ? AND student_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, skill.getSkillName());
            stmt.setString(2, skill.getProficiencyLevel().name());
            stmt.setString(3, skill.getDescription());
            stmt.setInt(4, skill.getSkillId());
            stmt.setInt(5, skill.getStudentId());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a skill by ID.
     */
    public boolean deleteSkill(int skillId) throws SQLException {
        String sql = "DELETE FROM SKILLS WHERE skill_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, skillId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Checks if a skill is currently referenced in an active (PENDING or ACCEPTED) exchange request.
     */
    public boolean isSkillReferencedInActiveRequests(int skillId) throws SQLException {
        String sql = """
            SELECT 1 FROM SKILL_EXCHANGE_REQUESTS 
            WHERE (offered_skill_id = ? OR wanted_skill_id = ?) 
              AND status IN ('PENDING', 'ACCEPTED')
            LIMIT 1
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, skillId);
            stmt.setInt(2, skillId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Searches skills with optional keyword search, type filter, and current student exclusion.
     * Uses PreparedStatement with parameter placeholders.
     */
    public List<Skill> searchSkills(String keyword, SkillType skillType, Integer excludeStudentId) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT s.skill_id, s.student_id, s.skill_name, s.skill_type, s.proficiency_level, 
                   s.description, s.created_at, st.student_name
            FROM SKILLS s
            JOIN STUDENTS st ON s.student_id = st.student_id
            WHERE 1=1
            """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND LOWER(s.skill_name) LIKE ?");
            params.add("%" + keyword.trim().toLowerCase() + "%");
        }

        if (skillType != null) {
            sql.append(" AND s.skill_type = ?");
            params.add(skillType.name());
        }

        if (excludeStudentId != null) {
            sql.append(" AND s.student_id <> ?");
            params.add(excludeStudentId);
        }

        sql.append(" ORDER BY s.skill_name ASC, st.student_name ASC");

        List<Skill> results = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String s) {
                    stmt.setString(i + 1, s);
                } else if (p instanceof Integer num) {
                    stmt.setInt(i + 1, num);
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToSkill(rs));
                }
            }
        }
        return results;
    }

    /**
     * Checks if a student already has a skill with the same normalized name and type.
     */
    public boolean existsByStudentAndNameAndType(int studentId, String skillName, SkillType type) throws SQLException {
        String sql = "SELECT 1 FROM SKILLS WHERE student_id = ? AND LOWER(skill_name) = LOWER(?) AND skill_type = ? LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setString(2, skillName.trim());
            stmt.setString(3, type.name());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Returns the total count of skills across the entire database.
     */
    public int getTotalSkillsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM SKILLS";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Skill mapResultSetToSkill(ResultSet rs) throws SQLException {
        Skill skill = new Skill(
                rs.getInt("skill_id"),
                rs.getInt("student_id"),
                rs.getString("skill_name"),
                SkillType.valueOf(rs.getString("skill_type")),
                ProficiencyLevel.valueOf(rs.getString("proficiency_level")),
                rs.getString("description"),
                rs.getTimestamp("created_at")
        );
        skill.setStudentName(rs.getString("student_name"));
        return skill;
    }
}
