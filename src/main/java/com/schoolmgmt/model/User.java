package com.schoolmgmt.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Central entity for all application users, handling authentication and authorization.
 * Each user has a role and is linked to a specific profile (Teacher, Student, etc.).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "email"}, name = "uk_user_tenant_email")
})
public class User extends BaseEntity implements UserDetails, TenantAware {

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "username", length = 100)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "mfa_enabled")
    @Builder.Default
    private Boolean mfaEnabled = false;

    @Column(name = "mfa_secret")
    private String mfaSecret;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_token_expiry")
    private java.time.LocalDateTime passwordResetTokenExpiry;

    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "account_non_locked")
    @Builder.Default
    private Boolean accountNonLocked = true;

    @Column(name = "locked_until")
    private java.time.LocalDateTime lockedUntil;

    @Column(name = "last_login_at")
    private java.time.LocalDateTime lastLoginAt;

    @Column(name = "last_password_change_at")
    private java.time.LocalDateTime lastPasswordChangeAt;

    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    // Link to the specific profile entity
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Teacher teacherProfile;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Student studentProfile;

    @Column(name = "is_temporary_password")
    private boolean temporaryPassword ;

    @Column(name = "tempPasswordForFirstTime")
    private String tempPasswordForFirstTime;

    // We can add ParentProfile later

    public enum UserRole {
        SUPER_ADMIN,
        ADMIN,
        TEACHER,
        STUDENT,
        PARENT
    }

    public enum UserStatus {
        PENDING,
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        BANNED,
        DELETED
    }

    // --- UserDetails Implementation ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // The role is prefixed with "ROLE_" as is the convention in Spring Security.
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        // Our email field serves as the username.
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Or add a field for this logic
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Or add a field for this logic
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Or add a field for this logic
    }

    @Override
    public boolean isEnabled() {
        return this.isActive;
    }

    // Business Methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public UserRole getPrimaryRole() {
        return role;
    }

    public List<UserRole> getRoles() {
        return List.of(role);
    }

    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(emailVerified);
    }

    public boolean isMfaEnabled() {
        return Boolean.TRUE.equals(mfaEnabled);
    }

    public boolean isPasswordResetTokenValid() {
        return passwordResetToken != null && 
               passwordResetTokenExpiry != null && 
               passwordResetTokenExpiry.isAfter(java.time.LocalDateTime.now());
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts != null ? failedLoginAttempts : 0;
    }

    public void setEmailVerificationToken(String token) {
        this.emailVerificationToken = token;
    }

    public String getEmailVerificationToken() {
        return emailVerificationToken;
    }

    public void setEnabled(boolean enabled) {
        this.isActive = enabled;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }



}