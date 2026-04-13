package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Tenant-scoped dynamic role defined by a school admin (e.g. "Fee Approver",
 * "Finance Helper", "Vice Principal"). Holds a curated set of permission
 * keys from {@link com.schoolmgmt.security.Permission}.
 *
 * System-default roles (ADMIN / TEACHER / PARENT / STUDENT) use the existing
 * {@code User.role} enum; custom roles live here and are assigned through
 * {@code User.customRoleId}. When both are set, the effective permission set
 * is the union of system-role defaults ∪ custom-role perms ∪ user extras.
 */
@Entity
@Table(
    name = "custom_roles",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_custom_roles_tenant_name",
        columnNames = {"tenant_id", "name"}
    ),
    indexes = {
        @Index(name = "idx_custom_roles_tenant", columnList = "tenant_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomRole extends BaseEntity {

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Column(name = "description", length = 280)
    private String description;

    /** If true the role is created by the system and cannot be deleted. */
    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private Boolean isSystem = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "custom_role_permissions",
        joinColumns = @JoinColumn(name = "role_id")
    )
    @Column(name = "permission_key", length = 80)
    @Builder.Default
    private Set<String> permissions = new HashSet<>();
}
