package com.skillswap.model;

/**
 * Enumeration representing the type of skill in SkillSwap.
 * TEACH: The student can teach this skill.
 * LEARN: The student wants to learn this skill.
 */
public enum SkillType {
    TEACH,
    LEARN;

    /**
     * Case-insensitive parser for SkillType.
     * 
     * @param text input string ("TEACH", "LEARN", "teach", "learn")
     * @return SkillType corresponding enum
     * @throws IllegalArgumentException if the text does not match any SkillType
     */
    public static SkillType fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Skill type cannot be empty. Must be TEACH or LEARN.");
        }
        String clean = text.trim().toUpperCase();
        try {
            return SkillType.valueOf(clean);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid skill type: '" + text + "'. Allowed values: TEACH, LEARN.");
        }
    }
}
