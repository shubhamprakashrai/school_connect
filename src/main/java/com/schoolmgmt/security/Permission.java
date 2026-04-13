package com.schoolmgmt.security;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Static catalog of all permission keys recognised by the backend.
 *
 * Permission keys follow {@code MODULE_ACTION} convention and are shared
 * with the mobile client via the {@code /permissions/catalog} endpoint so
 * both sides agree on the vocabulary.
 *
 * NOTE: Adding a key here alone does not enforce anything — you must:
 *   1. guard the relevant endpoint with {@code @RequirePermission(KEY)}, and
 *   2. include the key in the default grants for appropriate system roles
 *      (see {@link com.schoolmgmt.service.PermissionService#defaultsFor}).
 */
public final class Permission {

    // ── ROLES & ACCESS CONTROL ─────────────────────────────────────────
    public static final String ROLES_VIEW = "ROLES_VIEW";
    public static final String ROLES_MANAGE = "ROLES_MANAGE";
    public static final String USER_PERMISSIONS_MANAGE = "USER_PERMISSIONS_MANAGE";

    // ── STUDENT ────────────────────────────────────────────────────────
    public static final String STUDENT_VIEW = "STUDENT_VIEW";
    public static final String STUDENT_CREATE = "STUDENT_CREATE";
    public static final String STUDENT_EDIT = "STUDENT_EDIT";
    public static final String STUDENT_DELETE = "STUDENT_DELETE";

    // ── TEACHER ────────────────────────────────────────────────────────
    public static final String TEACHER_VIEW = "TEACHER_VIEW";
    public static final String TEACHER_CREATE = "TEACHER_CREATE";
    public static final String TEACHER_EDIT = "TEACHER_EDIT";
    public static final String TEACHER_DELETE = "TEACHER_DELETE";

    // ── PARENT ─────────────────────────────────────────────────────────
    public static final String PARENT_VIEW = "PARENT_VIEW";
    public static final String PARENT_MANAGE = "PARENT_MANAGE";

    // ── ACADEMIC ───────────────────────────────────────────────────────
    public static final String CLASS_VIEW = "CLASS_VIEW";
    public static final String CLASS_MANAGE = "CLASS_MANAGE";
    public static final String SUBJECT_MANAGE = "SUBJECT_MANAGE";
    public static final String TIMETABLE_MANAGE = "TIMETABLE_MANAGE";
    public static final String EXAM_MANAGE = "EXAM_MANAGE";
    public static final String ASSIGNMENT_MANAGE = "ASSIGNMENT_MANAGE";
    public static final String ATTENDANCE_MARK = "ATTENDANCE_MARK";
    public static final String ATTENDANCE_VIEW = "ATTENDANCE_VIEW";

    // ── FEES & PAYMENTS ────────────────────────────────────────────────
    public static final String FEES_VIEW = "FEES_VIEW";
    public static final String FEES_COLLECT = "FEES_COLLECT";
    public static final String FEES_APPROVE = "FEES_APPROVE";
    public static final String PAYMENT_CONFIG = "PAYMENT_CONFIG";
    public static final String PAYMENT_SUBMIT = "PAYMENT_SUBMIT";

    // ── COMMUNICATION ──────────────────────────────────────────────────
    public static final String ANNOUNCEMENT_CREATE = "ANNOUNCEMENT_CREATE";
    public static final String MESSAGING_USE = "MESSAGING_USE";
    public static final String COMPLAINT_MANAGE = "COMPLAINT_MANAGE";

    // ── OPERATIONS ─────────────────────────────────────────────────────
    public static final String LEAVE_APPROVE = "LEAVE_APPROVE";
    public static final String TRANSPORT_MANAGE = "TRANSPORT_MANAGE";
    public static final String LIBRARY_MANAGE = "LIBRARY_MANAGE";
    public static final String GALLERY_MANAGE = "GALLERY_MANAGE";
    public static final String HEALTH_MANAGE = "HEALTH_MANAGE";
    public static final String HOSTEL_MANAGE = "HOSTEL_MANAGE";
    public static final String DISCIPLINE_MANAGE = "DISCIPLINE_MANAGE";
    public static final String PERFORMANCE_MANAGE = "PERFORMANCE_MANAGE";
    public static final String REPORTS_VIEW = "REPORTS_VIEW";
    public static final String SETTINGS_MANAGE = "SETTINGS_MANAGE";
    public static final String MASTER_DATA_MANAGE = "MASTER_DATA_MANAGE";
    public static final String AUDIT_VIEW = "AUDIT_VIEW";

    /** Module grouping for UI (role editor shows grouped checkboxes). */
    public enum Module {
        ACCESS_CONTROL("Access control"),
        STUDENT("Student"),
        TEACHER("Teacher"),
        PARENT("Parent"),
        ACADEMIC("Academic"),
        FEES("Fees"),
        COMMUNICATION("Communication"),
        OPERATIONS("Operations");

        public final String displayName;
        Module(String displayName) { this.displayName = displayName; }
    }

    /**
     * Catalog item — exposed to the mobile client so the UI can render a
     * grouped permission chooser without hardcoding the vocabulary.
     */
    public record Entry(String key, String label, Module module) {}

    public static List<Entry> catalog() {
        return List.of(
            new Entry(ROLES_VIEW,              "View roles",                 Module.ACCESS_CONTROL),
            new Entry(ROLES_MANAGE,            "Create / edit roles",        Module.ACCESS_CONTROL),
            new Entry(USER_PERMISSIONS_MANAGE, "Override user permissions",  Module.ACCESS_CONTROL),

            new Entry(STUDENT_VIEW,   "View students",   Module.STUDENT),
            new Entry(STUDENT_CREATE, "Admit students",  Module.STUDENT),
            new Entry(STUDENT_EDIT,   "Edit students",   Module.STUDENT),
            new Entry(STUDENT_DELETE, "Delete students", Module.STUDENT),

            new Entry(TEACHER_VIEW,   "View teachers",   Module.TEACHER),
            new Entry(TEACHER_CREATE, "Add teachers",    Module.TEACHER),
            new Entry(TEACHER_EDIT,   "Edit teachers",   Module.TEACHER),
            new Entry(TEACHER_DELETE, "Delete teachers", Module.TEACHER),

            new Entry(PARENT_VIEW,   "View parents",   Module.PARENT),
            new Entry(PARENT_MANAGE, "Manage parents", Module.PARENT),

            new Entry(CLASS_VIEW,         "View classes",            Module.ACADEMIC),
            new Entry(CLASS_MANAGE,       "Manage classes",          Module.ACADEMIC),
            new Entry(SUBJECT_MANAGE,     "Manage subjects",         Module.ACADEMIC),
            new Entry(TIMETABLE_MANAGE,   "Manage timetable",        Module.ACADEMIC),
            new Entry(EXAM_MANAGE,        "Manage exams",            Module.ACADEMIC),
            new Entry(ASSIGNMENT_MANAGE,  "Manage assignments",      Module.ACADEMIC),
            new Entry(ATTENDANCE_MARK,    "Mark attendance",         Module.ACADEMIC),
            new Entry(ATTENDANCE_VIEW,    "View attendance reports", Module.ACADEMIC),

            new Entry(FEES_VIEW,       "View fees",                Module.FEES),
            new Entry(FEES_COLLECT,    "Collect fees",             Module.FEES),
            new Entry(FEES_APPROVE,    "Approve payment submissions", Module.FEES),
            new Entry(PAYMENT_CONFIG,  "Configure payment channels", Module.FEES),
            new Entry(PAYMENT_SUBMIT,  "Submit a payment (student/parent)", Module.FEES),

            new Entry(ANNOUNCEMENT_CREATE, "Post announcements",  Module.COMMUNICATION),
            new Entry(MESSAGING_USE,       "Use messaging",       Module.COMMUNICATION),
            new Entry(COMPLAINT_MANAGE,    "Manage complaints",   Module.COMMUNICATION),

            new Entry(LEAVE_APPROVE,      "Approve leaves",       Module.OPERATIONS),
            new Entry(TRANSPORT_MANAGE,   "Manage transport",     Module.OPERATIONS),
            new Entry(LIBRARY_MANAGE,     "Manage library",       Module.OPERATIONS),
            new Entry(GALLERY_MANAGE,     "Manage gallery",       Module.OPERATIONS),
            new Entry(HEALTH_MANAGE,      "Manage health records", Module.OPERATIONS),
            new Entry(HOSTEL_MANAGE,      "Manage hostel",        Module.OPERATIONS),
            new Entry(DISCIPLINE_MANAGE,  "Manage discipline",    Module.OPERATIONS),
            new Entry(PERFORMANCE_MANAGE, "Manage performance",   Module.OPERATIONS),
            new Entry(REPORTS_VIEW,       "View reports",         Module.OPERATIONS),
            new Entry(SETTINGS_MANAGE,    "Manage settings",      Module.OPERATIONS),
            new Entry(MASTER_DATA_MANAGE, "Manage master data",   Module.OPERATIONS),
            new Entry(AUDIT_VIEW,         "View audit logs",      Module.OPERATIONS)
        );
    }

    /** All valid permission keys — used for validation. */
    public static Set<String> allKeys() {
        return catalog().stream().map(Entry::key).collect(Collectors.toSet());
    }

    /** Fail if any supplied key is not in the catalog. */
    public static void validate(Set<String> keys) {
        Set<String> valid = allKeys();
        var unknown = keys.stream().filter(k -> !valid.contains(k))
            .collect(Collectors.toSet());
        if (!unknown.isEmpty()) {
            throw new IllegalArgumentException("Unknown permission keys: " + unknown);
        }
    }

    private Permission() {}
}
