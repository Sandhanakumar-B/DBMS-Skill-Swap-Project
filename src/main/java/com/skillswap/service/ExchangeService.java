package com.skillswap.service;

import com.skillswap.dao.SkillDAO;
import com.skillswap.dao.SkillExchangeRequestDAO;
import com.skillswap.dao.StudentDAO;
import com.skillswap.model.RequestStatus;
import com.skillswap.model.Skill;
import com.skillswap.model.SkillExchangeRequest;
import com.skillswap.model.SkillType;
import com.skillswap.model.Student;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service class orchestrating skill exchange request creation,
 * strict ownership validation, and lifecycle inspection.
 */
public class ExchangeService {

    private final SkillExchangeRequestDAO requestDAO;
    private final StudentDAO studentDAO;
    private final SkillDAO skillDAO;

    public ExchangeService() {
        this.requestDAO = new SkillExchangeRequestDAO();
        this.studentDAO = new StudentDAO();
        this.skillDAO = new SkillDAO();
    }

    public ExchangeService(SkillExchangeRequestDAO requestDAO, StudentDAO studentDAO, SkillDAO skillDAO) {
        this.requestDAO = requestDAO;
        this.studentDAO = studentDAO;
        this.skillDAO = skillDAO;
    }

    /**
     * Creates a new skill exchange request after strictly validating:
     * 1. No self-requests (requester != receiver).
     * 2. Both students exist.
     * 3. Offered skill exists, belongs to requester, and is of type TEACH.
     * 4. Wanted skill exists, belongs to receiver, and is of type TEACH (as receiver will teach it).
     * 5. No active duplicate request exists.
     * 
     * @param requesterId The active student proposing the exchange
     * @param receiverId The target student receiving the exchange
     * @param offeredSkillId The skill the requester will teach
     * @param wantedSkillId The skill the receiver will teach
     * @return The created SkillExchangeRequest with generated request_id
     * @throws IllegalArgumentException on any validation failure
     * @throws SQLException on database errors
     */
    public SkillExchangeRequest createRequest(int requesterId, int receiverId,
                                             int offeredSkillId, int wantedSkillId) throws SQLException {
        // 1. Prevent self-requests
        if (requesterId == receiverId) {
            throw new IllegalArgumentException("Cannot send a skill exchange request to yourself.");
        }

        // 2. Validate student existence
        Optional<Student> requesterOpt = studentDAO.findStudentById(requesterId);
        if (requesterOpt.isEmpty()) {
            throw new IllegalArgumentException("Requester student not found with ID: " + requesterId);
        }

        Optional<Student> receiverOpt = studentDAO.findStudentById(receiverId);
        if (receiverOpt.isEmpty()) {
            throw new IllegalArgumentException("Receiver student not found with ID: " + receiverId);
        }

        // 3. Validate offered skill ownership and type (must be TEACH and belong to requester)
        Optional<Skill> offeredOpt = skillDAO.findSkillById(offeredSkillId);
        if (offeredOpt.isEmpty()) {
            throw new IllegalArgumentException("Offered skill not found with ID: " + offeredSkillId);
        }
        Skill offeredSkill = offeredOpt.get();
        if (offeredSkill.getStudentId() != requesterId) {
            throw new IllegalArgumentException("Invalid offered skill: Skill ID " + offeredSkillId + 
                    " ('" + offeredSkill.getSkillName() + "') does not belong to you.");
        }
        if (offeredSkill.getSkillType() != SkillType.TEACH) {
            throw new IllegalArgumentException("Invalid offered skill: You can only offer skills that you can TEACH.");
        }

        // 4. Validate wanted skill ownership and type (must be TEACH and belong to receiver)
        Optional<Skill> wantedOpt = skillDAO.findSkillById(wantedSkillId);
        if (wantedOpt.isEmpty()) {
            throw new IllegalArgumentException("Wanted skill not found with ID: " + wantedSkillId);
        }
        Skill wantedSkill = wantedOpt.get();
        if (wantedSkill.getStudentId() != receiverId) {
            throw new IllegalArgumentException("Invalid wanted skill: Skill ID " + wantedSkillId + 
                    " ('" + wantedSkill.getSkillName() + "') does not belong to receiver (Student #" + receiverId + ").");
        }
        if (wantedSkill.getSkillType() != SkillType.TEACH) {
            throw new IllegalArgumentException("Invalid wanted skill: The requested skill must be one that the receiver can TEACH.");
        }

        // 5. Prevent duplicate active requests
        if (requestDAO.hasActiveDuplicateRequest(requesterId, receiverId, offeredSkillId, wantedSkillId)) {
            throw new IllegalArgumentException("An active exchange request between you and " + 
                    receiverOpt.get().getStudentName() + " for these skills is already pending or accepted.");
        }

        // 6. Create request with initial status PENDING
        SkillExchangeRequest newRequest = new SkillExchangeRequest(
                requesterId, receiverId, offeredSkillId, wantedSkillId
        );
        newRequest.setStatus(RequestStatus.PENDING);

        return requestDAO.insertRequest(newRequest);
    }

    /**
     * Retrieves an exchange request by its primary key ID.
     */
    public Optional<SkillExchangeRequest> getRequestById(int requestId) throws SQLException {
        return requestDAO.findRequestById(requestId);
    }

    /**
     * Retrieves incoming requests for a student.
     */
    public List<SkillExchangeRequest> getIncomingRequests(int receiverId) throws SQLException {
        return requestDAO.findIncomingRequests(receiverId);
    }

    /**
     * Retrieves outgoing requests sent by a student.
     */
    public List<SkillExchangeRequest> getOutgoingRequests(int requesterId) throws SQLException {
        return requestDAO.findOutgoingRequests(requesterId);
    }

    /**
     * Formats exchange requests into a clean table for console display.
     */
    public String formatRequestTable(List<SkillExchangeRequest> requests, boolean isIncomingView) {
        if (requests == null || requests.isEmpty()) {
            return isIncomingView ? "No incoming requests found." : "No outgoing requests found.";
        }

        StringBuilder sb = new StringBuilder();
        String counterPartyHeader = isIncomingView ? "From (Requester)" : "To (Receiver)";
        
        sb.append(String.format("%-6s | %-18s | %-18s | %-18s | %-10s | %s\n",
                "Req ID", counterPartyHeader, "They Teach", "You Teach", "Status", "Date"));
        sb.append("-".repeat(95)).append("\n");

        for (SkillExchangeRequest r : requests) {
            String counterParty = isIncomingView ? r.getRequesterName() : r.getReceiverName();
            String theyTeach = isIncomingView ? r.getOfferedSkillName() : r.getWantedSkillName();
            String youTeach = isIncomingView ? r.getWantedSkillName() : r.getOfferedSkillName();
            String dateStr = r.getRequestDate() != null ? r.getRequestDate().toString().substring(0, 19) : "N/A";

            sb.append(String.format("%-6d | %-18s | %-18s | %-18s | %-10s | %s\n",
                    r.getRequestId(),
                    counterParty != null ? counterParty : ("Student #" + (isIncomingView ? r.getRequesterId() : r.getReceiverId())),
                    theyTeach != null ? theyTeach : "N/A",
                    youTeach != null ? youTeach : "N/A",
                    r.getStatus(),
                    dateStr));
        }
        return sb.toString();
    }
}
