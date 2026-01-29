-- V8: Create safety-related tables (incidents, counseling, emergency alerts)

-- Incident Reports Table
CREATE TABLE IF NOT EXISTS incident_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    location VARCHAR(200),
    occurred_at TIMESTAMP,
    reported_by VARCHAR(100),
    reported_by_role VARCHAR(50),
    status VARCHAR(30) DEFAULT 'REPORTED',
    assigned_to VARCHAR(100),
    resolution_notes TEXT,
    resolved_at TIMESTAMP,
    resolved_by VARCHAR(100),
    attachments TEXT,
    witnesses TEXT,
    students_involved TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    version BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_incident_tenant ON incident_reports(tenant_id);
CREATE INDEX IF NOT EXISTS idx_incident_status ON incident_reports(status);
CREATE INDEX IF NOT EXISTS idx_incident_severity ON incident_reports(severity);
CREATE INDEX IF NOT EXISTS idx_incident_occurred ON incident_reports(occurred_at);

-- Counseling Referrals Table
CREATE TABLE IF NOT EXISTS counseling_referrals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    student_id VARCHAR(50) NOT NULL,
    student_name VARCHAR(200),
    class_info VARCHAR(100),
    reason TEXT NOT NULL,
    urgency VARCHAR(20) NOT NULL,
    referred_by VARCHAR(100),
    referred_by_role VARCHAR(50),
    status VARCHAR(30) DEFAULT 'PENDING',
    counselor_assigned VARCHAR(100),
    scheduled_at TIMESTAMP,
    session_notes TEXT,
    follow_up_required BOOLEAN DEFAULT FALSE,
    follow_up_date TIMESTAMP,
    parent_notified BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    version BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_counseling_tenant ON counseling_referrals(tenant_id);
CREATE INDEX IF NOT EXISTS idx_counseling_student ON counseling_referrals(student_id);
CREATE INDEX IF NOT EXISTS idx_counseling_status ON counseling_referrals(status);
CREATE INDEX IF NOT EXISTS idx_counseling_urgency ON counseling_referrals(urgency);

-- Emergency Alerts Table
CREATE TABLE IF NOT EXISTS emergency_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT,
    alert_type VARCHAR(30) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    triggered_by VARCHAR(100),
    triggered_by_role VARCHAR(50),
    location VARCHAR(200),
    is_active BOOLEAN DEFAULT TRUE,
    acknowledged BOOLEAN DEFAULT FALSE,
    acknowledged_by VARCHAR(100),
    acknowledged_at TIMESTAMP,
    resolved BOOLEAN DEFAULT FALSE,
    resolved_by VARCHAR(100),
    resolved_at TIMESTAMP,
    resolution_notes TEXT,
    notifications_sent INTEGER DEFAULT 0,
    target_audience VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    version BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_alert_tenant ON emergency_alerts(tenant_id);
CREATE INDEX IF NOT EXISTS idx_alert_type ON emergency_alerts(alert_type);
CREATE INDEX IF NOT EXISTS idx_alert_active ON emergency_alerts(is_active) WHERE is_active = TRUE;
CREATE INDEX IF NOT EXISTS idx_alert_severity ON emergency_alerts(severity);

-- Add comments
COMMENT ON TABLE incident_reports IS 'Safety incident reports in schools';
COMMENT ON TABLE counseling_referrals IS 'Student counseling referrals';
COMMENT ON TABLE emergency_alerts IS 'School emergency alerts';
