package com.skillswap.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Domain model representing a skill exchange request between two students.
 */
public class SkillExchangeRequest {

    private int requestId;
    private int requesterId;
    private int receiverId;
    private int offeredSkillId;
    private int wantedSkillId;
    private Timestamp requestDate;
    private RequestStatus status;

    // Helper display fields populated by SQL joins
    private String requesterName;
    private String receiverName;
    private String offeredSkillName;
    private String wantedSkillName;

    public SkillExchangeRequest() {
    }

    public SkillExchangeRequest(int requesterId, int receiverId, int offeredSkillId, int wantedSkillId) {
        this.requesterId = requesterId;
        this.receiverId = receiverId;
        this.offeredSkillId = offeredSkillId;
        this.wantedSkillId = wantedSkillId;
        this.status = RequestStatus.PENDING;
    }

    public SkillExchangeRequest(int requestId, int requesterId, int receiverId, int offeredSkillId,
                                int wantedSkillId, Timestamp requestDate, RequestStatus status) {
        this.requestId = requestId;
        this.requesterId = requesterId;
        this.receiverId = receiverId;
        this.offeredSkillId = offeredSkillId;
        this.wantedSkillId = wantedSkillId;
        this.requestDate = requestDate;
        this.status = status;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(int requesterId) {
        this.requesterId = requesterId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public int getOfferedSkillId() {
        return offeredSkillId;
    }

    public void setOfferedSkillId(int offeredSkillId) {
        this.offeredSkillId = offeredSkillId;
    }

    public int getWantedSkillId() {
        return wantedSkillId;
    }

    public void setWantedSkillId(int wantedSkillId) {
        this.wantedSkillId = wantedSkillId;
    }

    public Timestamp getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Timestamp requestDate) {
        this.requestDate = requestDate;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getOfferedSkillName() {
        return offeredSkillName;
    }

    public void setOfferedSkillName(String offeredSkillName) {
        this.offeredSkillName = offeredSkillName;
    }

    public String getWantedSkillName() {
        return wantedSkillName;
    }

    public void setWantedSkillName(String wantedSkillName) {
        this.wantedSkillName = wantedSkillName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkillExchangeRequest that = (SkillExchangeRequest) o;
        return requestId == that.requestId &&
                requesterId == that.requesterId &&
                receiverId == that.receiverId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, requesterId, receiverId);
    }

    @Override
    public String toString() {
        return String.format("Request #%d [%s -> %s | Offers: %s, Wants: %s | Status: %s]",
                requestId,
                requesterName != null ? requesterName : ("Student #" + requesterId),
                receiverName != null ? receiverName : ("Student #" + receiverId),
                offeredSkillName != null ? offeredSkillName : ("Skill #" + offeredSkillId),
                wantedSkillName != null ? wantedSkillName : ("Skill #" + wantedSkillId),
                status);
    }
}
