-- V15__Add_performance_indexes.sql
-- Performance optimization: Add missing composite indexes for frequently queried columns
-- Phase 9.3.2 - Database Performance Indexes

-- =====================================================
-- STUDENTS TABLE - Missing composite indexes
-- =====================================================

-- Composite index for tenant + status (used by findByStatusAndTenantId, countByTenantIdAndStatus)
CREATE INDEX IF NOT EXISTS idx_student_tenant_status ON students(tenant_id, status);

-- Composite index for tenant + class + status (used by countByCurrentClassIdAndTenantIdAndStatus)
CREATE INDEX IF NOT EXISTS idx_student_tenant_class_status ON students(tenant_id, current_class_id, status);

-- Composite index for tenant + class + section (used by findByCurrentClassIdAndCurrentSectionIdAndTenantId)
CREATE INDEX IF NOT EXISTS idx_student_tenant_class_section ON students(tenant_id, current_class_id, current_section_id);

-- Index for date_of_birth month/day extraction queries (birthday lookups)
CREATE INDEX IF NOT EXISTS idx_student_dob ON students(date_of_birth);

-- Composite for admission date range queries within a tenant
CREATE INDEX IF NOT EXISTS idx_student_tenant_admission ON students(tenant_id, admission_date);

-- =====================================================
-- TEACHERS TABLE - Missing composite indexes
-- =====================================================

-- Composite index for tenant + status (used by findByStatusAndTenantId, countByTenantIdAndStatus)
CREATE INDEX IF NOT EXISTS idx_teacher_tenant_status ON teachers(tenant_id, status);

-- Composite index for tenant + department (used by findByDepartmentAndTenantId)
CREATE INDEX IF NOT EXISTS idx_teacher_tenant_department ON teachers(tenant_id, department);

-- Composite index for tenant + employee_type (used by findByEmployeeTypeAndTenantId)
CREATE INDEX IF NOT EXISTS idx_teacher_tenant_emptype ON teachers(tenant_id, employee_type);

-- Index for teacher evaluation queries
CREATE INDEX IF NOT EXISTS idx_teacher_tenant_eval ON teachers(tenant_id, last_evaluation_date);

-- =====================================================
-- PARENTS TABLE - Missing composite indexes
-- =====================================================

-- Composite index for tenant + status (used by findByStatusAndTenantId, countByTenantIdAndStatus)
CREATE INDEX IF NOT EXISTS idx_parent_tenant_status ON parents(tenant_id, status);

-- Composite for tenant + parent_type (used by findByParentTypeAndTenantId)
CREATE INDEX IF NOT EXISTS idx_parent_tenant_type ON parents(tenant_id, parent_type);

-- =====================================================
-- USERS TABLE - Missing composite indexes
-- =====================================================

-- Composite for tenant + role (used by findByRoleAndTenantId, countActiveUsersByRoleAndTenant)
CREATE INDEX IF NOT EXISTS idx_user_tenant_role ON users(tenant_id, primary_role);

-- Composite for tenant + status (used by findByStatusAndTenantId)
CREATE INDEX IF NOT EXISTS idx_user_tenant_status ON users(tenant_id, status);

-- Composite for tenant + role + status (used by countActiveUsersByRoleAndTenant)
CREATE INDEX IF NOT EXISTS idx_user_tenant_role_status ON users(tenant_id, primary_role, status);

-- =====================================================
-- NOTIFICATION_LOGS TABLE - Missing composite indexes
-- =====================================================

-- Composite for tenant + recipient + created_at (used by paginated notification queries)
CREATE INDEX IF NOT EXISTS idx_notif_log_tenant_recipient_created ON notification_logs(tenant_id, recipient_user_id, created_at DESC);

-- Composite for tenant + recipient + is_read (used by unread notification queries)
CREATE INDEX IF NOT EXISTS idx_notif_log_tenant_recipient_unread ON notification_logs(tenant_id, recipient_user_id, is_read) WHERE is_read = false;

-- =====================================================
-- FEE_PAYMENTS TABLE - Missing composite indexes
-- =====================================================

-- Composite for tenant + student (used by findByTenantIdAndStudentId)
CREATE INDEX IF NOT EXISTS idx_fee_payment_tenant_student ON fee_payments(tenant_id, student_id);

-- Composite for tenant + payment_status (used by findByTenantIdAndPaymentStatus, aggregate queries)
CREATE INDEX IF NOT EXISTS idx_fee_payment_tenant_status ON fee_payments(tenant_id, payment_status);

-- Composite for tenant + date range (used by getCollectionBetweenDates)
CREATE INDEX IF NOT EXISTS idx_fee_payment_tenant_date ON fee_payments(tenant_id, payment_date);

-- =====================================================
-- FEE_STRUCTURES TABLE - Missing composite indexes
-- =====================================================

-- Composite for tenant + class + active (used by findByTenantIdAndClassIdAndIsActiveTrue)
CREATE INDEX IF NOT EXISTS idx_fee_struct_tenant_class_active ON fee_structures(tenant_id, class_id, is_active);

-- =====================================================
-- EXAMS TABLE - Missing composite indexes
-- =====================================================

-- Composite for tenant + class (used by findByTenantIdAndClassId)
CREATE INDEX IF NOT EXISTS idx_exam_tenant_class ON exams(tenant_id, class_id);

-- Composite for tenant + status (used by findByTenantIdAndStatus)
CREATE INDEX IF NOT EXISTS idx_exam_tenant_status ON exams(tenant_id, status);

-- Composite for tenant + exam_date (used by findUpcomingExams with ORDER BY)
CREATE INDEX IF NOT EXISTS idx_exam_tenant_date ON exams(tenant_id, exam_date);

-- =====================================================
-- EXAM_RESULTS TABLE - Missing composite indexes
-- =====================================================

-- Composite for tenant + student (used by findByTenantIdAndStudentId)
CREATE INDEX IF NOT EXISTS idx_result_tenant_student ON exam_results(tenant_id, student_id);

-- Index for result_status (used by pass/fail count queries)
CREATE INDEX IF NOT EXISTS idx_result_status ON exam_results(result_status);

-- =====================================================
-- LEAVE_REQUESTS TABLE - Missing composite indexes
-- =====================================================

-- Composite for tenant + user + created_at (used by findByTenantIdAndUserIdOrderByCreatedAtDesc)
CREATE INDEX IF NOT EXISTS idx_leave_req_tenant_user_created ON leave_requests(tenant_id, user_id, created_at DESC);

-- Composite for tenant + status + created_at (used by findByTenantIdAndStatusOrderByCreatedAtDesc)
CREATE INDEX IF NOT EXISTS idx_leave_req_tenant_status_created ON leave_requests(tenant_id, status, created_at DESC);

-- =====================================================
-- LEAVE_BALANCES TABLE - Missing composite indexes
-- =====================================================

-- Composite for tenant + user + academic_year (used by findByTenantIdAndUserIdAndAcademicYear)
CREATE INDEX IF NOT EXISTS idx_leave_bal_tenant_user_year ON leave_balances(tenant_id, user_id, academic_year);

-- =====================================================
-- INCIDENT_REPORTS TABLE - Missing composite indexes
-- =====================================================

-- Composite for tenant + status + is_deleted (used by findByTenantIdAndStatusAndIsDeletedFalse)
CREATE INDEX IF NOT EXISTS idx_incident_tenant_status_del ON incident_reports(tenant_id, status, is_deleted);

-- Composite for tenant + severity + is_deleted (used by findByTenantIdAndSeverityAndIsDeletedFalse)
CREATE INDEX IF NOT EXISTS idx_incident_tenant_severity_del ON incident_reports(tenant_id, severity, is_deleted);

-- =====================================================
-- COUNSELING_REFERRALS TABLE - Missing composite indexes
-- =====================================================

-- Composite for tenant + status + is_deleted (used by findByTenantIdAndStatusAndIsDeletedFalse)
CREATE INDEX IF NOT EXISTS idx_counseling_tenant_status_del ON counseling_referrals(tenant_id, status, is_deleted);

-- Composite for tenant + student + is_deleted (used by findByTenantIdAndStudentIdAndIsDeletedFalse)
CREATE INDEX IF NOT EXISTS idx_counseling_tenant_student_del ON counseling_referrals(tenant_id, student_id, is_deleted);

-- =====================================================
-- EMERGENCY_ALERTS TABLE - Missing composite indexes
-- =====================================================

-- Composite for tenant + is_active + is_deleted (used by findByTenantIdAndIsActiveAndIsDeletedFalse)
CREATE INDEX IF NOT EXISTS idx_alert_tenant_active_del ON emergency_alerts(tenant_id, is_active, is_deleted);

-- =====================================================
-- TIMETABLE_ENTRIES TABLE - Missing composite indexes
-- =====================================================

-- Composite for tenant + class + active (used by findByTenantIdAndClassIdAndIsActiveTrue)
CREATE INDEX IF NOT EXISTS idx_tt_tenant_class_active ON timetable_entries(tenant_id, class_id, is_active);

-- Composite for tenant + teacher + active (used by findByTenantIdAndTeacherIdAndIsActiveTrue)
CREATE INDEX IF NOT EXISTS idx_tt_tenant_teacher_active ON timetable_entries(tenant_id, teacher_id, is_active);

-- Composite for tenant + class + day + active (used by findByTenantIdAndClassIdAndDayOfWeekAndIsActiveTrue)
CREATE INDEX IF NOT EXISTS idx_tt_tenant_class_day_active ON timetable_entries(tenant_id, class_id, day_of_week, is_active);

-- =====================================================
-- TEACHER_CLASSES (assignments) TABLE - Missing indexes
-- =====================================================

-- Composite for tenant + teacher + active (used by findByTeacherIdAndIsActiveTrueAndTenantId)
CREATE INDEX IF NOT EXISTS idx_tc_tenant_teacher_active ON teacher_classes(tenant_id, teacher_id, is_active);

-- Composite for tenant + section + active (used by findBySectionIdAndIsActiveTrueAndTenantId)
CREATE INDEX IF NOT EXISTS idx_tc_tenant_section_active ON teacher_classes(tenant_id, section_id, is_active);

-- Composite for tenant + subject + active (used by findBySubjectIdAndIsActiveTrueAndTenantId)
CREATE INDEX IF NOT EXISTS idx_tc_tenant_subject_active ON teacher_classes(tenant_id, subject_id, is_active);

-- Composite for academic year + active + tenant (used by findByAcademicYearIdAndIsActiveTrueAndTenantId)
CREATE INDEX IF NOT EXISTS idx_tc_tenant_year_active ON teacher_classes(tenant_id, academic_year_id, is_active);

-- Comments
COMMENT ON INDEX idx_student_tenant_status IS 'Performance: composite index for tenant-scoped student status queries';
COMMENT ON INDEX idx_notif_log_tenant_recipient_unread IS 'Performance: partial index for unread notification count (frequently polled)';
COMMENT ON INDEX idx_fee_payment_tenant_status IS 'Performance: composite index for fee collection aggregate queries';
