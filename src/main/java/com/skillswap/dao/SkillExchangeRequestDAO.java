package com.skillswap.dao;

import com.skillswap.config.DatabaseConnection;
import com.skillswap.model.RequestStatus;
import com.skillswap.model.SkillExchangeRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) for SKILL_EXCHANGE_REQUESTS table.
 * Encapsulates CRUD operations, conditional status updates, and relationship joins.
 */
public class SkillExchangeRequestDAO {

    private static final String BASE_SELECT_JOIN = """
        SELECT r.request_id, r.requester_id, r.receiver_id, r.offered_skill_id, r.wanted_skill_id, 
               r.request_date, r.status,
               req.student_name AS requester_name,
               rec.student_name AS receiver_name,
               off.skill_name   AS offered_skill_name,
               wnt.skill_name   AS wanted_skill_name
        FROM SKILL_EXCHANGE_REQUESTS r
        JOIN STUDENTS req ON r.requester_id = req.student_id
        JOIN STUDENTS rec ON r.receiver_id = rec.student_id
        JOIN SKILLS off   ON r.offered_skill_id = off.skill_id
        JOIN SKILLS wnt   ON r.wanted_skill_id = wnt.skill_id
        """;

    /**
     * Inserts a new exchange request and returns the entity with generated request_id.
     */
    public SkillExchangeRequest insertRequest(SkillExchangeRequest request) throws SQLException {
        String sql = """
            INSERT INTO SKILL_EXCHANGE_REQUESTS (requester_id, receiver_id, offered_skill_id, wanted_skill_id, status)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, request.getRequesterId());
            stmt.setInt(2, request.getReceiverId());
            stmt.setInt(3, request.getOfferedSkillId());
            stmt.setInt(4, request.getWantedSkillId());
            stmt.setString(5, request.getStatus() != null ? request.getStatus().name() : RequestStatus.PENDING.name());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating exchange request failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    request.setRequestId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating exchange request failed, no ID obtained.");
                }
            }

            return findRequestById(request.getRequestId()).orElse(request);
        }
    }

    /**
     * Finds a single request by its primary key ID with all joined student/skill names.
     */
    public Optional<SkillExchangeRequest> findRequestById(int requestId) throws SQLException {
        String sql = BASE_SELECT_JOIN + " WHERE r.request_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, requestId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRequest(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Checks if an active duplicate request (status PENDING or ACCEPTED) exists
     * between the same students for the same skills.
     */
    public boolean hasActiveDuplicateRequest(int requesterId, int receiverId,
                                            int offeredSkillId, int wantedSkillId) throws SQLException {
        String sql = """
            SELECT 1 FROM SKILL_EXCHANGE_REQUESTS
            WHERE requester_id = ? AND receiver_id = ?
              AND offered_skill_id = ? AND wanted_skill_id = ?
              AND status IN ('PENDING', 'ACCEPTED')
            LIMIT 1
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, requesterId);
            stmt.setInt(2, receiverId);
            stmt.setInt(3, offeredSkillId);
            stmt.setInt(4, wantedSkillId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Finds incoming requests addressed to a specific receiver.
     */
    public List<SkillExchangeRequest> findIncomingRequests(int receiverId) throws SQLException {
        String sql = BASE_SELECT_JOIN + " WHERE r.receiver_id = ? ORDER BY r.request_date DESC";
        List<SkillExchangeRequest> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, receiverId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRequest(rs));
                }
            }
        }
        return list;
    }

    /**
     * Finds outgoing requests sent by a specific requester.
     */
    public List<SkillExchangeRequest> findOutgoingRequests(int requesterId) throws SQLException {
        String sql = BASE_SELECT_JOIN + " WHERE r.requester_id = ? ORDER BY r.request_date DESC";
        List<SkillExchangeRequest> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, requesterId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRequest(rs));
                }
            }
        }
        return list;
    }

    /**
     * Safely updates request status using conditional concurrency control.
     * Only updates if the record is currently in the expected current status.
     * 
     * @return true if status was updated, false if status was already changed or request not found
     */
    public boolean updateRequestStatus(int requestId, RequestStatus newStatus, RequestStatus expectedCurrentStatus) throws SQLException {
        String sql = """
            UPDATE SKILL_EXCHANGE_REQUESTS
            SET status = ?
            WHERE request_id = ? AND status = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus.name());
            stmt.setInt(2, requestId);
            stmt.setString(3, expectedCurrentStatus.name());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Counts requests matching a specific status (for summaries and reporting).
     */
    public int countByStatus(RequestStatus status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM SKILL_EXCHANGE_REQUESTS WHERE status = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Deletes a request by ID (primarily for testing cleanup).
     */
    public boolean deleteRequest(int requestId) throws SQLException {
        String sql = "DELETE FROM SKILL_EXCHANGE_REQUESTS WHERE request_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, requestId);
            return stmt.executeUpdate() > 0;
        }
    }

    private SkillExchangeRequest mapResultSetToRequest(ResultSet rs) throws SQLException {
        SkillExchangeRequest req = new SkillExchangeRequest(
                rs.getInt("request_id"),
                rs.getInt("requester_id"),
                rs.getInt("receiver_id"),
                rs.getInt("offered_skill_id"),
                rs.getInt("wanted_skill_id"),
                rs.getTimestamp("request_date"),
                RequestStatus.valueOf(rs.getString("status"))
        );
        req.setRequesterName(rs.getString("requester_name"));
        req.setReceiverName(rs.getString("receiver_name"));
        req.setOfferedSkillName(rs.getString("offered_skill_name"));
        req.setWantedSkillName(rs.getString("wanted_skill_name"));
        return req;
    }
}
