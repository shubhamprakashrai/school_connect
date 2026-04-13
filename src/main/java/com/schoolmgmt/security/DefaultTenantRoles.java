package com.schoolmgmt.security;

import java.util.List;
import java.util.Set;

/**
 * Starter roles that every newly onboarded tenant gets, so a school admin
 * doesn't have to build RBAC from scratch. Admins can still edit the
 * permissions of any of these roles and can delete nothing — {@code isSystem}
 * prevents deletion but not modification.
 */
public final class DefaultTenantRoles {

    public record SeedRole(String name, String description, Set<String> permissions) {}

    public static List<SeedRole> seeds() {
        return List.of(
            new SeedRole(
                "Principal",
                "Full school oversight: people, academics, finance, communication.",
                Set.of(
                    Permission.ROLES_VIEW,
                    Permission.STUDENT_VIEW, Permission.STUDENT_CREATE,
                        Permission.STUDENT_EDIT, Permission.STUDENT_DELETE,
                    Permission.TEACHER_VIEW, Permission.TEACHER_CREATE,
                        Permission.TEACHER_EDIT, Permission.TEACHER_DELETE,
                    Permission.PARENT_VIEW, Permission.PARENT_MANAGE,
                    Permission.CLASS_VIEW, Permission.CLASS_MANAGE,
                    Permission.SUBJECT_MANAGE, Permission.TIMETABLE_MANAGE,
                    Permission.EXAM_MANAGE, Permission.ASSIGNMENT_MANAGE,
                    Permission.ATTENDANCE_MARK, Permission.ATTENDANCE_VIEW,
                    Permission.FEES_VIEW, Permission.FEES_COLLECT,
                        Permission.FEES_APPROVE, Permission.PAYMENT_CONFIG,
                    Permission.ANNOUNCEMENT_CREATE, Permission.MESSAGING_USE,
                        Permission.COMPLAINT_MANAGE,
                    Permission.LEAVE_APPROVE, Permission.TRANSPORT_MANAGE,
                        Permission.LIBRARY_MANAGE, Permission.GALLERY_MANAGE,
                        Permission.HEALTH_MANAGE, Permission.DISCIPLINE_MANAGE,
                        Permission.PERFORMANCE_MANAGE, Permission.REPORTS_VIEW
                )
            ),
            new SeedRole(
                "Teacher",
                "Classroom ops: attendance, assignments, exams, announcements.",
                Set.of(
                    Permission.STUDENT_VIEW,
                    Permission.CLASS_VIEW,
                    Permission.ATTENDANCE_MARK, Permission.ATTENDANCE_VIEW,
                    Permission.ASSIGNMENT_MANAGE, Permission.EXAM_MANAGE,
                    Permission.ANNOUNCEMENT_CREATE, Permission.MESSAGING_USE,
                    Permission.GALLERY_MANAGE, Permission.LIBRARY_MANAGE,
                    Permission.LEAVE_APPROVE
                )
            ),
            new SeedRole(
                "Accountant",
                "Fees, payment approvals, financial reports.",
                Set.of(
                    Permission.FEES_VIEW, Permission.FEES_COLLECT,
                        Permission.FEES_APPROVE, Permission.PAYMENT_CONFIG,
                    Permission.REPORTS_VIEW, Permission.STUDENT_VIEW
                )
            ),
            new SeedRole(
                "Front Office",
                "Admissions desk: admit students, view parents, respond to complaints.",
                Set.of(
                    Permission.STUDENT_VIEW, Permission.STUDENT_CREATE,
                        Permission.STUDENT_EDIT,
                    Permission.PARENT_VIEW, Permission.PARENT_MANAGE,
                    Permission.MESSAGING_USE, Permission.COMPLAINT_MANAGE,
                    Permission.ANNOUNCEMENT_CREATE
                )
            ),
            new SeedRole(
                "Librarian",
                "Books, issues, library management.",
                Set.of(
                    Permission.STUDENT_VIEW,
                    Permission.LIBRARY_MANAGE,
                    Permission.MESSAGING_USE
                )
            ),
            new SeedRole(
                "Discipline Officer",
                "Discipline records, behaviour cases, complaints.",
                Set.of(
                    Permission.STUDENT_VIEW,
                    Permission.DISCIPLINE_MANAGE, Permission.COMPLAINT_MANAGE,
                    Permission.MESSAGING_USE
                )
            )
        );
    }

    private DefaultTenantRoles() {}
}
