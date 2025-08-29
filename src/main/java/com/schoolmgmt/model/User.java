package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User entity representing system users across all tenants.
 * Implements UserDetails for Spring Security integration.
 */
@Entity
@Table(name = "users",
       indexes = {
           @Index(name = "idx_user_email_tenant", columnList = "email, tenant_id", unique = true),
           @Index(name = "idx_user_username_tenant", columnList = "username, tenant_id", unique = true),
           @Index(name = "idx_user_status", columnList = "status"),
           @Index(name = "idx_user_role", columnList = "primary_role")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"password", "roles"})
public class User extends BaseEntity implements UserDetails {


    @Column(name = "userId", nullable = false, length = 50, unique = true)
    private String userId;

    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username;


    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_role", nullable = false, length = 20)
    private UserRole primaryRole;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<UserRole> roles = new HashSet<>();

    // Security fields
    @Column(name = "is_account_non_expired")
    @Builder.Default
    private boolean accountNonExpired = true;

    @Column(name = "is_account_non_locked")
    @Builder.Default
    private boolean accountNonLocked = true;

    @Column(name = "is_credentials_non_expired")
    @Builder.Default
    private boolean credentialsNonExpired = true;

    @Column(name = "is_enabled")
    @Builder.Default
    private boolean enabled = false;

    // MFA fields
    @Column(name = "is_mfa_enabled")
    @Builder.Default
    private boolean mfaEnabled = false;

    @Column(name = "mfa_secret")
    private String mfaSecret;

    // Additional security fields
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_password_change_at")
    private LocalDateTime lastPasswordChangeAt;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_token_expiry")
    private LocalDateTime passwordResetTokenExpiry;

    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @Column(name = "email_verified")
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private int failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    // Reference to specific entity based on role
    @Column(name = "reference_id")
    private String referenceId; // Can be studentId, teacherId, or parentId based on role

    @Column(name = "reference_type")
    private String referenceType; // STUDENT, TEACHER, PARENT, ADMIN

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (lockedUntil != null) {
            return LocalDateTime.now().isAfter(lockedUntil);
        }
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled && status == UserStatus.ACTIVE;
    }

    // Business methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    public void lockAccount(int minutes) {
        this.accountNonLocked = false;
        this.lockedUntil = LocalDateTime.now().plusMinutes(minutes);
    }

    public boolean isPasswordResetTokenValid() {
        return passwordResetToken != null && 
               passwordResetTokenExpiry != null && 
               LocalDateTime.now().isBefore(passwordResetTokenExpiry);
    }

    // Enums
    public enum UserStatus {
        PENDING,    // Awaiting email verification
        ACTIVE,     // Active user
        INACTIVE,   // Deactivated by admin
        SUSPENDED,  // Temporarily suspended
        DELETED     // Soft deleted
    }

    public enum UserRole {
        SUPER_ADMIN,    // System super admin (cross-tenant)
        ADMIN,          // School admin
        TEACHER,        // Teacher
        STUDENT,        // Student
        PARENT,         // Parent/Guardian
        STAFF,          // Other staff members
        ACCOUNTANT,     // Finance/Accounts
        LIBRARIAN,      // Library management
        RECEPTIONIST    // Front desk
    }
}
