package com.schoolmgmt.controller;

import com.schoolmgmt.dto.request.RoleRequest;
import com.schoolmgmt.dto.response.RoleResponse;
import com.schoolmgmt.security.Permission;
import com.schoolmgmt.security.RequirePermission;
import com.schoolmgmt.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Role + permission management — all endpoints tenant-scoped through
 * {@link PermissionService} (which resolves tenant from context).
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class RoleController {

    private final PermissionService permissionService;

    // ── Public catalog (everyone can read; used by mobile UI) ──────────
    @GetMapping("/permissions/catalog")
    public ResponseEntity<List<Permission.Entry>> catalog() {
        return ResponseEntity.ok(Permission.catalog());
    }

    // ── Role CRUD ──────────────────────────────────────────────────────
    @GetMapping("/roles")
    @RequirePermission(Permission.ROLES_VIEW)
    public ResponseEntity<List<RoleResponse>> list() {
        var out = permissionService.listRoles().stream()
            .map(RoleResponse::from).toList();
        return ResponseEntity.ok(out);
    }

    @GetMapping("/roles/{id}")
    @RequirePermission(Permission.ROLES_VIEW)
    public ResponseEntity<RoleResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(
            RoleResponse.from(permissionService.getRole(id)));
    }

    @PostMapping("/roles")
    @RequirePermission(Permission.ROLES_MANAGE)
    public ResponseEntity<RoleResponse> create(@Valid @RequestBody RoleRequest req) {
        var role = permissionService.createRole(
            req.getName(), req.getDescription(), req.getPermissions());
        return ResponseEntity.ok(RoleResponse.from(role));
    }

    @PutMapping("/roles/{id}")
    @RequirePermission(Permission.ROLES_MANAGE)
    public ResponseEntity<RoleResponse> update(
            @PathVariable UUID id, @Valid @RequestBody RoleRequest req) {
        var role = permissionService.updateRole(
            id, req.getName(), req.getDescription(), req.getPermissions());
        return ResponseEntity.ok(RoleResponse.from(role));
    }

    @DeleteMapping("/roles/{id}")
    @RequirePermission(Permission.ROLES_MANAGE)
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        permissionService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    // ── User role/permission assignment ────────────────────────────────
    @PutMapping("/users/{userId}/custom-role")
    @RequirePermission(Permission.USER_PERMISSIONS_MANAGE)
    public ResponseEntity<Void> assignRole(
            @PathVariable UUID userId,
            @RequestBody Map<String, UUID> body) {
        permissionService.assignCustomRoleToUser(userId, body.get("roleId"));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{userId}/permissions")
    @RequirePermission(Permission.USER_PERMISSIONS_MANAGE)
    public ResponseEntity<Void> setUserPermissions(
            @PathVariable UUID userId,
            @RequestBody Map<String, Set<String>> body) {
        permissionService.setUserPermissions(
            userId, body.getOrDefault("permissions", Set.of()));
        return ResponseEntity.noContent().build();
    }
}
