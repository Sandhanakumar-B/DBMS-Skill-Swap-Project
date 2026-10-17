package com.skillswap.model;

/**
 * Enumeration representing the lifecycle status of a SkillExchangeRequest.
 */
public enum RequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    COMPLETED,
    CANCELLED;

    /**
     * Case-insensitive parser for RequestStatus.
     */
    public static RequestStatus fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty.");
        }
        try {
            return RequestStatus.valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid request status: '" + text + "'. Allowed values: PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED.");
        }
    }
}
