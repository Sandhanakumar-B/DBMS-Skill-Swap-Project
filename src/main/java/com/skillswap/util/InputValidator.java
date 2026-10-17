package com.skillswap.util;

import java.util.regex.Pattern;

/**
 * Utility class providing validation routines for user inputs.
 */
public class InputValidator {

    // RFC 5322 compliant simplified email regex
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private InputValidator() {
        // Utility class private constructor
    }

    /**
     * Validates if the provided string is not null and contains non-whitespace characters.
     */
    public static boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Validates if the provided email string conforms to a valid email format.
     */
    public static boolean isValidEmail(String email) {
        if (!isNotBlank(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Sanitizes a string by trimming leading and trailing whitespace.
     */
    public static String sanitize(String str) {
        return str == null ? "" : str.trim();
    }
}
