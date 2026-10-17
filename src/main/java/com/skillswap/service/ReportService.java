package com.skillswap.service;

import com.skillswap.config.DatabaseConnection;
import com.skillswap.model.PlatformReport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Service for generating DBMS aggregate reports and platform statistics.
 */
public class ReportService {

    /**
     * Generates a comprehensive summary report using SQL aggregate functions.
     * 
     * @param activeStudentId Optional student ID to compute active session metrics for
     * @return PlatformReport containing all statistics
     */
    public PlatformReport generatePlatformSummary(Integer activeStudentId) throws SQLException {
        PlatformReport report = new PlatformReport();
        report.setActiveStudentId(activeStudentId);

        try (Connection conn = DatabaseConnection.getConnection()) {

            // 1. Total students count
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM STUDENTS");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    report.setTotalStudents(rs.getInt(1));
                }
            }

            // 2. Total skills and breakdown by type
            String skillSql = """
                SELECT 
                    COUNT(*) AS total_skills,
                    SUM(CASE WHEN skill_type = 'TEACH' THEN 1 ELSE 0 END) AS total_teach,
                    SUM(CASE WHEN skill_type = 'LEARN' THEN 1 ELSE 0 END) AS total_learn
                FROM SKILLS
                """;
            try (PreparedStatement stmt = conn.prepareStatement(skillSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    report.setTotalSkills(rs.getInt("total_skills"));
                    report.setTotalTeachSkills(rs.getInt("total_teach"));
                    report.setTotalLearnSkills(rs.getInt("total_learn"));
                }
            }

            // 3. Requests breakdown by status
            String reqSql = """
                SELECT 
                    COUNT(*) AS total_req,
                    SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) AS pending_req,
                    SUM(CASE WHEN status = 'ACCEPTED' THEN 1 ELSE 0 END) AS accepted_req,
                    SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_req,
                    SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) AS rejected_req,
                    SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled_req
                FROM SKILL_EXCHANGE_REQUESTS
                """;
            try (PreparedStatement stmt = conn.prepareStatement(reqSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    report.setTotalRequests(rs.getInt("total_req"));
                    report.setPendingRequests(rs.getInt("pending_req"));
                    report.setAcceptedRequests(rs.getInt("accepted_req"));
                    report.setCompletedExchanges(rs.getInt("completed_req"));
                    report.setRejectedRequests(rs.getInt("rejected_req"));
                    report.setCancelledRequests(rs.getInt("cancelled_req"));
                }
            }

            // 4. Active student specific metrics (if active student is provided)
            if (activeStudentId != null) {
                String studentSql = """
                    SELECT 
                        SUM(CASE WHEN requester_id = ? THEN 1 ELSE 0 END) AS outgoing_count,
                        SUM(CASE WHEN receiver_id = ? THEN 1 ELSE 0 END) AS incoming_count,
                        SUM(CASE WHEN (requester_id = ? OR receiver_id = ?) AND status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_count
                    FROM SKILL_EXCHANGE_REQUESTS
                    """;
                try (PreparedStatement stmt = conn.prepareStatement(studentSql)) {
                    stmt.setInt(1, activeStudentId);
                    stmt.setInt(2, activeStudentId);
                    stmt.setInt(3, activeStudentId);
                    stmt.setInt(4, activeStudentId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            report.setStudentOutgoingRequests(rs.getInt("outgoing_count"));
                            report.setStudentIncomingRequests(rs.getInt("incoming_count"));
                            report.setStudentCompletedExchanges(rs.getInt("completed_count"));
                        }
                    }
                }
            }
        }

        return report;
    }

    /**
     * Formats the report into a clean dashboard for console display.
     */
    public String formatReport(PlatformReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("==================================================\n");
        sb.append("         SKILLSWAP - PLATFORM SUMMARY REPORT      \n");
        sb.append("==================================================\n");
        sb.append(" [PLATFORM OVERVIEW]\n");
        sb.append(String.format("  * Registered Students   : %d\n", report.getTotalStudents()));
        sb.append(String.format("  * Total Listed Skills   : %d (TEACH: %d | LEARN: %d)\n",
                report.getTotalSkills(), report.getTotalTeachSkills(), report.getTotalLearnSkills()));
        sb.append("--------------------------------------------------\n");
        sb.append(" [EXCHANGE REQUESTS STATUS]\n");
        sb.append(String.format("  * Total Requests Created: %d\n", report.getTotalRequests()));
        sb.append(String.format("  * Pending Requests      : %d\n", report.getPendingRequests()));
        sb.append(String.format("  * Accepted Requests     : %d\n", report.getAcceptedRequests()));
        sb.append(String.format("  * Completed Exchanges   : %d\n", report.getCompletedExchanges()));
        sb.append(String.format("  * Rejected Requests     : %d\n", report.getRejectedRequests()));
        sb.append(String.format("  * Cancelled Requests    : %d\n", report.getCancelledRequests()));

        if (report.getActiveStudentId() != null) {
            sb.append("--------------------------------------------------\n");
            sb.append(String.format(" [ACTIVE STUDENT METRICS - ID: %d]\n", report.getActiveStudentId()));
            sb.append(String.format("  * Sent (Outgoing) Requests     : %d\n", report.getStudentOutgoingRequests()));
            sb.append(String.format("  * Received (Incoming) Requests : %d\n", report.getStudentIncomingRequests()));
            sb.append(String.format("  * Completed Exchanges          : %d\n", report.getStudentCompletedExchanges()));
        }

        sb.append("==================================================\n");
        return sb.toString();
    }
}
