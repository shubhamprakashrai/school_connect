package com.schoolmgmt.util;

/**
 * Utility class to generate unique IDs with a prefix and numeric sequence.
 */
public class UserIdGeneratorBasedonTenantIdentifies{

//    private TenantIdGeneratorUtil() {
//        // private constructor to prevent instantiation
//    }

    /**
     * Generate a unique code with tenant prefix and integer sequence.
     *
     * @param tenantPrefix  The prefix (e.g., TEN001)
     * @param lastSequence  The last used integer sequence (0 if none)
     * @param numberLength  Number of digits for the integer part (e.g., 5 -> 00001)
     * @return Generated unique code (e.g., TEN00100001)
     */
    public static String generateNextCode(String tenantPrefix, int lastSequence, int numberLength) {
        int nextSequence = lastSequence + 1;
        return tenantPrefix + String.format("%0" + numberLength + "d", nextSequence);
    }
}
