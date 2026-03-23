package com.schoolmgmt.util;

public class TenantIdFormatter {

    /**
     * Extract abbreviation from school name.
     * "SRPS" → "SRPS"
     * "Delhi Public School" → "DPS"
     * "St. Mary's High School" → "SMHS"
     */
    public static String extractInitials(String schoolName) {
        if (schoolName == null || schoolName.isBlank()) {
            throw new IllegalArgumentException("School name cannot be empty");
        }

        String cleaned = schoolName.trim();

        // If name is already an abbreviation (all uppercase, no spaces), use as-is
        if (cleaned.matches("^[A-Z]{2,10}$")) {
            return cleaned;
        }

        // Extract first letter of each word
        String[] words = cleaned.split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                // Skip common words like "of", "the", "and"
                String lower = word.toLowerCase();
                if (lower.equals("of") || lower.equals("the") || lower.equals("and") || lower.equals("for")) {
                    continue;
                }
                initials.append(word.substring(0, 1).toUpperCase());
            }
        }

        String result = initials.toString();
        return result.length() >= 2 ? result : cleaned.substring(0, Math.min(2, cleaned.length())).toUpperCase();
    }

    /**
     * Format tenant identifier: SRPS00001
     */
    public static String format(String initials, int sequence) {
        return initials + String.format("%05d", sequence);
    }

    /**
     * Generate employee ID for a teacher: SRPSE0001
     */
    public static String generateEmployeeId(String tenantInitials, int sequence) {
        return tenantInitials + "E" + String.format("%04d", sequence);
    }

    /**
     * Generate student ID: SRPSS0001
     */
    public static String generateStudentId(String tenantInitials, int sequence) {
        return tenantInitials + "S" + String.format("%04d", sequence);
    }

    /**
     * Generate parent ID: SRPSP0001
     */
    public static String generateParentId(String tenantInitials, int sequence) {
        return tenantInitials + "P" + String.format("%04d", sequence);
    }
}
