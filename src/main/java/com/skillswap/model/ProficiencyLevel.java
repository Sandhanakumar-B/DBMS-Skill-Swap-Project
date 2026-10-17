package com.skillswap.model;

/**
 * Enumeration representing a student's proficiency level in a skill.
 */
public enum ProficiencyLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED;

    /**
     * Case-insensitive parser for ProficiencyLevel.
     * 
     * @param text input string ("BEGINNER", "INTERMEDIATE", "ADVANCED")
     * @return ProficiencyLevel corresponding enum
     * @throws IllegalArgumentException if the text does not match any ProficiencyLevel
     */
    public static ProficiencyLevel fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return BEGINNER; // Default level
        }
        String clean = text.trim().toUpperCase();
        try {
            return ProficiencyLevel.valueOf(clean);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid proficiency level: '" + text + "'. Allowed values: BEGINNER, INTERMEDIATE, ADVANCED.");
        }
    }
}
