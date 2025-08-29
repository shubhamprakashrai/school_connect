package com.schoolmgmt.util;

public class TenantIdFormatter {

    // Extract initials from first & last word of school name
    public static String extractInitials(String schoolName) {
        if (schoolName == null || schoolName.isBlank()) {
            throw new IllegalArgumentException("School name cannot be empty");
        }

        String[] words = schoolName.trim().split("\\s+");
        String first = words[0].substring(0, 1).toUpperCase();
        String last = words[words.length - 1].substring(0, 1).toUpperCase();

        return first + last;
    }

    // Format with sequence like SM0001
    public static String format(String initials, int sequence) {
        return initials + String.format("%05d", sequence);
    }
}
