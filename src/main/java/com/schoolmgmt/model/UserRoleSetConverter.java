package com.schoolmgmt.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Converter for storing Set<UserRole> as a comma-separated string in the database.
 * This allows multiple roles to be stored in a single column without changing the column name.
 */
@Converter
public class UserRoleSetConverter implements AttributeConverter<Set<User.UserRole>, String> {

    private static final String SEPARATOR = ",";

    @Override
    public String convertToDatabaseColumn(Set<User.UserRole> roles) {
        if (roles == null || roles.isEmpty()) {
            return "";
        }
        return roles.stream()
                .map(Enum::name)
                .collect(Collectors.joining(SEPARATOR));
    }

    @Override
    public Set<User.UserRole> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new HashSet<>();
        }
        
        try {
            return Arrays.stream(dbData.split(SEPARATOR))
                    .filter(role -> !role.trim().isEmpty())
                    .map(role -> User.UserRole.valueOf(role.trim()))
                    .collect(Collectors.toSet());
        } catch (IllegalArgumentException e) {
            // Handle case where database contains invalid role names
            // Return empty set to avoid breaking the application
            return new HashSet<>();
        }
    }
}
