package com.schoolmgmt.service;

import com.schoolmgmt.model.CustomRole;
import com.schoolmgmt.model.User;
import com.schoolmgmt.repository.CustomRoleRepository;
import com.schoolmgmt.repository.UserRepository;
import com.schoolmgmt.security.Permission;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Resolves the effective set of permission keys for a given user:
 * {@code defaults(systemRole) ∪ customRole.permissions ∪ additionalPermissions}.
 *
 * SUPER_ADMIN and ADMIN roles always return all permissions — custom
 * roles cannot take away their platform-level access.
 *
 * All reads are tenant-scoped: the custom role is only loaded if its
 * tenantId matches the current context (or the user's recorded tenant).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final CustomRoleRepository customRoleRepository;
    private final UserRepository userRepository;

    /** Compute effective permission keys for the given user. */
    public Set<String> effectivePermissions(User user) {
        if (user == null) return Set.of();

        // Platform-wide roles bypass the catalog — they can do everything.
        if (user.getRole() == User.UserRole.SUPER_ADMIN
            || user.getRole() == User.UserRole.ADMIN) {
            return Permission.allKeys();
        }

        Set<String> effective = new HashSet<>(defaultsFor(user.getRole()));

        if (user.getCustomRoleId() != null && user.getTenantId() != null) {
            customRoleRepository
                .findByIdAndTenantId(user.getCustomRoleId(), user.getTenantId())
                .ifPresent(role -> {
                    if (!Boolean.TRUE.equals(role.getIsDeleted())) {
                        effective.addAll(role.getPermissions());
                    }
                });
        }

        if (user.getAdditionalPermissions() != null) {
            effective.addAll(user.getAdditionalPermissions());
        }

        return effective;
    }

    public boolean hasPermission(User user, String permissionKey) {
        return effectivePermissions(user).contains(permissionKey);
    }

    /**
     * Throws {@link AccessDeniedException} if the current principal lacks
     * the given permission. Used by {@code PermissionAspect} and can be
     * called directly inside services for conditional checks.
     */
    public void require(String permissionKey) {
        User current = currentUser();
        if (current == null || !hasPermission(current, permissionKey)) {
            throw new AccessDeniedException(
                "Missing required permission: " + permissionKey);
        }
    }

    /**
     * Resolve the currently authenticated {@link User} from Spring Security
     * context (JwtAuthenticationFilter has already populated it).
     */
    private User currentUser() {
        var auth = org.springframework.security.core.context.SecurityContextHolder
            .getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof User u) return u;
        if (principal instanceof String email) {
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    /**
     * Baseline permissions granted implicitly to users holding a system
     * role. Custom roles can grant MORE on top of these; they cannot
     * revoke defaults (principle of least surprise for existing features).
     */
    private Set<String> defaultsFor(User.UserRole role) {
        return switch (role) {
            case TEACHER -> Set.of(
                Permission.STUDENT_VIEW,
                Permission.CLASS_VIEW,
                Permission.ATTENDANCE_MARK,
                Permission.ATTENDANCE_VIEW,
                Permission.ASSIGNMENT_MANAGE,
                Permission.EXAM_MANAGE,
                Permission.LIBRARY_MANAGE,
                Permission.GALLERY_MANAGE,
                Permission.ANNOUNCEMENT_CREATE,
                Permission.MESSAGING_USE,
                Permission.LEAVE_APPROVE
            );
            case PARENT -> Set.of(
                Permission.STUDENT_VIEW,
                Permission.ATTENDANCE_VIEW,
                Permission.FEES_VIEW,
                Permission.PAYMENT_SUBMIT,
                Permission.MESSAGING_USE
            );
            case STUDENT -> Set.of(
                Permission.CLASS_VIEW,
                Permission.ATTENDANCE_VIEW,
                Permission.FEES_VIEW,
                Permission.PAYMENT_SUBMIT,
                Permission.MESSAGING_USE
            );
            // SUPER_ADMIN / ADMIN handled above (all perms).
            default -> Set.of();
        };
    }

    // ── Role CRUD (tenant-scoped) ──────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CustomRole> listRoles() {
        return customRoleRepository
            .findByTenantIdAndIsDeletedFalseOrderByNameAsc(
                TenantContext.requireCurrentTenant());
    }

    @Transactional(readOnly = true)
    public CustomRole getRole(UUID id) {
        String tenantId = TenantContext.requireCurrentTenant();
        return customRoleRepository.findByIdAndTenantId(id, tenantId)
            .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
            .orElseThrow(() ->
                new NoSuchElementException("Role not found: " + id));
    }

    @Transactional
    public CustomRole createRole(String name, String description, Set<String> perms) {
        Permission.validate(perms == null ? Set.of() : perms);
        String tenantId = TenantContext.requireCurrentTenant();
        if (customRoleRepository.existsByTenantIdAndNameIgnoreCase(tenantId, name)) {
            throw new IllegalArgumentException("Role with that name already exists");
        }
        CustomRole role = CustomRole.builder()
            .name(name)
            .description(description)
            .permissions(perms == null ? new HashSet<>() : new HashSet<>(perms))
            .isSystem(false)
            .build();
        log.info("Creating custom role {} for tenant {}", name, tenantId);
        return customRoleRepository.save(role);
    }

    @Transactional
    public CustomRole updateRole(UUID id, String name, String description, Set<String> perms) {
        if (perms != null) Permission.validate(perms);
        CustomRole role = getRole(id);
        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new IllegalArgumentException("System roles cannot be modified");
        }
        if (name != null) role.setName(name);
        if (description != null) role.setDescription(description);
        if (perms != null) {
            role.getPermissions().clear();
            role.getPermissions().addAll(perms);
        }
        return customRoleRepository.save(role);
    }

    @Transactional
    public void deleteRole(UUID id) {
        CustomRole role = getRole(id);
        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new IllegalArgumentException("System roles cannot be deleted");
        }
        role.softDelete("system");
        customRoleRepository.save(role);
    }

    /** Assign / clear a custom role for a user (tenant-isolated). */
    @Transactional
    public User assignCustomRoleToUser(UUID userId, UUID roleId) {
        String tenantId = TenantContext.requireCurrentTenant();
        User user = userRepository.findById(userId)
            .filter(u -> tenantId.equals(u.getTenantId()))
            .orElseThrow(() ->
                new NoSuchElementException("User not found: " + userId));
        if (roleId != null) {
            // Validate role belongs to same tenant.
            getRole(roleId);
        }
        user.setCustomRoleId(roleId);
        return userRepository.save(user);
    }

    /** Overwrite a user's direct permission grants (tenant-isolated). */
    @Transactional
    public User setUserPermissions(UUID userId, Set<String> perms) {
        Permission.validate(perms == null ? Set.of() : perms);
        String tenantId = TenantContext.requireCurrentTenant();
        User user = userRepository.findById(userId)
            .filter(u -> tenantId.equals(u.getTenantId()))
            .orElseThrow(() ->
                new NoSuchElementException("User not found: " + userId));
        user.getAdditionalPermissions().clear();
        if (perms != null) user.getAdditionalPermissions().addAll(perms);
        return userRepository.save(user);
    }
}
