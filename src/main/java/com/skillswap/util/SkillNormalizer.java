package com.skillswap.util;

/**
 * Utility for normalizing skill names across inputs, queries, and matching algorithms.
 * Ensures consistent matching regardless of case differences or irregular spacing.
 */
public class SkillNormalizer {

    private SkillNormalizer() {
        // Utility class private constructor
    }

    /**
     * Normalizes a skill name for display and storage by trimming whitespace
     * and collapsing multiple spaces into a single space.
     * 
     * Example: "   java   programming  " -> "Java Programming"
     */
    public static String normalize(String skillName) {
        if (skillName == null) {
            return "";
        }
        String cleaned = skillName.trim().replaceAll("\\s+", " ");
        if (cleaned.isEmpty()) {
            return "";
        }

        // Capitalize each word for clean display presentation
        String[] words = cleaned.split(" ");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!word.isEmpty()) {
                // If it's already an acronym like UI/UX, preserve standard cases or capitalize first letter
                if (word.equalsIgnoreCase("ui/ux")) {
                    result.append("UI/UX");
                } else if (word.length() == 1) {
                    result.append(Character.toUpperCase(word.charAt(0)));
                } else {
                    result.append(Character.toUpperCase(word.charAt(0)))
                          .append(word.substring(1).toLowerCase());
                }
                if (i < words.length - 1) {
                    result.append(" ");
                }
            }
        }
        return result.toString();
    }

    /**
     * Converts a skill name to its canonical lower-case comparison key.
     * 
     * Example: " Java  " and "java" produce the same key "java".
     */
    public static String toCanonicalKey(String skillName) {
        if (skillName == null) {
            return "";
        }
        return skillName.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * Tests whether two skill names are equivalent, ignoring case and spacing variations.
     */
    public static boolean areEqual(String skillA, String skillB) {
        return toCanonicalKey(skillA).equals(toCanonicalKey(skillB));
    }
}
