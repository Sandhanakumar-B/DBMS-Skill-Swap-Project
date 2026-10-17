package com.skillswap.service;

import com.skillswap.config.DatabaseConnection;
import com.skillswap.model.ProficiencyLevel;
import com.skillswap.model.SkillMatch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service providing rule-based Smart Skill Matching using relational database joins.
 * 
 * Algorithm Explanation:
 * 1. Mutual Matches: Identifies pairs where Student A teaches what Student B wants to learn,
 *    AND Student B teaches what Student A wants to learn (Case-insensitive & whitespace-normalized).
 * 2. Partial (One-Way) Matches: Identifies students who either teach what you want to learn,
 *    or want to learn what you can teach, but without reciprocal balance.
 */
public class SkillMatchingService {

    /**
     * Finds all matches for a student, returning Mutual Matches first,
     * followed by Partial (One-Way) Matches.
     */
    public List<SkillMatch> findMatchesForStudent(int studentId) throws SQLException {
        List<SkillMatch> allMatches = new ArrayList<>();
        Set<String> mutualPairKeys = new HashSet<>();

        // 1. Fetch Mutual Matches first
        List<SkillMatch> mutualMatches = findMutualMatches(studentId);
        for (SkillMatch m : mutualMatches) {
            allMatches.add(m);
            // Track mutual pairs to avoid duplicate listing under partial matches
            mutualPairKeys.add(m.getPartnerStudentId() + "-" + m.getMyTeachSkillName().toLowerCase());
            mutualPairKeys.add(m.getPartnerStudentId() + "-" + m.getPartnerTeachSkillName().toLowerCase());
        }

        // 2. Fetch Partial Matches and filter out mutual matches
        List<SkillMatch> partialMatches = findOneWayMatches(studentId);
        for (SkillMatch pm : partialMatches) {
            String checkKey = pm.getPartnerStudentId() + "-" + 
                    (pm.getMyTeachSkillName() != null ? pm.getMyTeachSkillName().toLowerCase() : pm.getPartnerTeachSkillName().toLowerCase());
            if (!mutualPairKeys.contains(checkKey)) {
                allMatches.add(pm);
            }
        }

        return allMatches;
    }

    /**
     * Executes a relational 5-way JOIN to discover reciprocal (mutual) skill matches.
     */
    public List<SkillMatch> findMutualMatches(int studentId) throws SQLException {
        String sql = """
            SELECT DISTINCT
                b.student_id AS partner_id,
                b.student_name AS partner_name,
                b.email AS partner_email,
                b.contact_info AS partner_contact,
                s_a_teach.skill_id AS my_teach_skill_id,
                s_a_teach.skill_name AS my_teach_skill_name,
                s_a_teach.proficiency_level AS my_teach_level,
                s_b_teach.skill_id AS partner_teach_skill_id,
                s_b_teach.skill_name AS partner_teach_skill_name,
                s_b_teach.proficiency_level AS partner_teach_level
            FROM STUDENTS b
            JOIN SKILLS s_a_teach ON s_a_teach.student_id = ? AND s_a_teach.skill_type = 'TEACH'
            JOIN SKILLS s_b_learn ON s_b_learn.student_id = b.student_id AND s_b_learn.skill_type = 'LEARN' 
                 AND LOWER(TRIM(s_a_teach.skill_name)) = LOWER(TRIM(s_b_learn.skill_name))
            JOIN SKILLS s_a_learn ON s_a_learn.student_id = ? AND s_a_learn.skill_type = 'LEARN'
            JOIN SKILLS s_b_teach ON s_b_teach.student_id = b.student_id AND s_b_teach.skill_type = 'TEACH' 
                 AND LOWER(TRIM(s_a_learn.skill_name)) = LOWER(TRIM(s_b_teach.skill_name))
            WHERE b.student_id <> ?
            ORDER BY b.student_name ASC
            """;

        List<SkillMatch> matches = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setInt(2, studentId);
            stmt.setInt(3, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SkillMatch match = new SkillMatch();
                    match.setPartnerStudentId(rs.getInt("partner_id"));
                    match.setPartnerName(rs.getString("partner_name"));
                    match.setPartnerEmail(rs.getString("partner_email"));
                    match.setPartnerContact(rs.getString("partner_contact"));

                    match.setMyTeachSkillId(rs.getInt("my_teach_skill_id"));
                    match.setMyTeachSkillName(rs.getString("my_teach_skill_name"));
                    match.setMyTeachLevel(ProficiencyLevel.valueOf(rs.getString("my_teach_level")));

                    match.setPartnerTeachSkillId(rs.getInt("partner_teach_skill_id"));
                    match.setPartnerTeachSkillName(rs.getString("partner_teach_skill_name"));
                    match.setPartnerTeachLevel(ProficiencyLevel.valueOf(rs.getString("partner_teach_level")));

                    match.setMutual(true);
                    match.setMatchTypeDescription("MUTUAL MATCH (Reciprocal Exchange)");

                    matches.add(match);
                }
            }
        }
        return matches;
    }

    /**
     * Finds one-way matches: students who teach what you want to learn,
     * or want to learn what you can teach.
     */
    public List<SkillMatch> findOneWayMatches(int studentId) throws SQLException {
        List<SkillMatch> partials = new ArrayList<>();

        // Part 1: Partners who can teach what I want to learn
        String sql1 = """
            SELECT DISTINCT
                b.student_id AS partner_id,
                b.student_name AS partner_name,
                b.email AS partner_email,
                b.contact_info AS partner_contact,
                s_b_teach.skill_id AS partner_teach_skill_id,
                s_b_teach.skill_name AS partner_teach_skill_name,
                s_b_teach.proficiency_level AS partner_teach_level
            FROM STUDENTS b
            JOIN SKILLS s_b_teach ON s_b_teach.student_id = b.student_id AND s_b_teach.skill_type = 'TEACH'
            JOIN SKILLS s_a_learn ON s_a_learn.student_id = ? AND s_a_learn.skill_type = 'LEARN'
                 AND LOWER(TRIM(s_b_teach.skill_name)) = LOWER(TRIM(s_a_learn.skill_name))
            WHERE b.student_id <> ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql1)) {

            stmt.setInt(1, studentId);
            stmt.setInt(2, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SkillMatch match = new SkillMatch();
                    match.setPartnerStudentId(rs.getInt("partner_id"));
                    match.setPartnerName(rs.getString("partner_name"));
                    match.setPartnerEmail(rs.getString("partner_email"));
                    match.setPartnerContact(rs.getString("partner_contact"));

                    match.setPartnerTeachSkillId(rs.getInt("partner_teach_skill_id"));
                    match.setPartnerTeachSkillName(rs.getString("partner_teach_skill_name"));
                    match.setPartnerTeachLevel(ProficiencyLevel.valueOf(rs.getString("partner_teach_level")));

                    match.setMutual(false);
                    match.setMatchTypeDescription("They can teach: " + match.getPartnerTeachSkillName() + 
                            " (" + match.getPartnerTeachLevel() + ") [One-Way]");

                    partials.add(match);
                }
            }
        }

        return partials;
    }

    /**
     * Formats matched results into a clear console display.
     */
    public String formatMatchResults(List<SkillMatch> matches) {
        if (matches == null || matches.isEmpty()) {
            return "No skill matches found. Try adding more skills to teach and learn!";
        }

        StringBuilder sb = new StringBuilder();
        int mutualCount = 0;
        int partialCount = 0;

        sb.append("\n=========================================================================================\n");
        sb.append("                            SMART SKILL MATCHING RESULTS                                 \n");
        sb.append("=========================================================================================\n");

        for (SkillMatch m : matches) {
            if (m.isMutual()) {
                mutualCount++;
                sb.append(String.format(">> [MUTUAL MATCH #%d] Partner: %s (Student ID: %d) | %s\n",
                        mutualCount, m.getPartnerName(), m.getPartnerStudentId(), m.getPartnerEmail()));
                sb.append(String.format("   * You teach them    : %s (Your Level: %s) [Skill ID: %d]\n",
                        m.getMyTeachSkillName(), m.getMyTeachLevel(), m.getMyTeachSkillId()));
                sb.append(String.format("   * They teach you    : %s (Their Level: %s) [Skill ID: %d]\n",
                        m.getPartnerTeachSkillName(), m.getPartnerTeachLevel(), m.getPartnerTeachSkillId()));
                sb.append("   --------------------------------------------------------------------------------------\n");
            }
        }

        for (SkillMatch m : matches) {
            if (!m.isMutual()) {
                partialCount++;
                sb.append(String.format(">> [PARTIAL MATCH #%d] Partner: %s (Student ID: %d) | %s\n",
                        partialCount, m.getPartnerName(), m.getPartnerStudentId(), m.getPartnerEmail()));
                sb.append(String.format("   * Details: %s\n", m.getMatchTypeDescription()));
                sb.append("   --------------------------------------------------------------------------------------\n");
            }
        }

        sb.append(String.format("Summary: Found %d Mutual Match(es) and %d Partial Match(es).\n", mutualCount, partialCount));
        sb.append("=========================================================================================\n");
        return sb.toString();
    }
}
