-- V9__Create_tenant_settings.sql
-- Tenant settings table for per-school configuration

CREATE TABLE IF NOT EXISTS tenant_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL UNIQUE REFERENCES tenants(id) ON DELETE CASCADE,

    -- Branding Settings
    display_name VARCHAR(200),
    tagline VARCHAR(500),
    logo_url VARCHAR(500),
    favicon_url VARCHAR(500),
    primary_color VARCHAR(7) DEFAULT '#1E3A5F',
    secondary_color VARCHAR(7) DEFAULT '#4CAF50',
    accent_color VARCHAR(7) DEFAULT '#FFC107',

    -- Academic Settings
    academic_year_start DATE,
    academic_year_end DATE,
    grading_system VARCHAR(20) DEFAULT 'PERCENTAGE',
    passing_percentage INTEGER DEFAULT 33,
    default_working_days VARCHAR(50) DEFAULT 'MON,TUE,WED,THU,FRI',
    school_start_time VARCHAR(10) DEFAULT '08:00',
    school_end_time VARCHAR(10) DEFAULT '14:00',

    -- Feature Flags
    attendance_enabled BOOLEAN DEFAULT TRUE,
    fees_enabled BOOLEAN DEFAULT TRUE,
    exams_enabled BOOLEAN DEFAULT TRUE,
    timetable_enabled BOOLEAN DEFAULT TRUE,
    library_enabled BOOLEAN DEFAULT FALSE,
    transport_enabled BOOLEAN DEFAULT FALSE,
    hostel_enabled BOOLEAN DEFAULT FALSE,
    parent_portal_enabled BOOLEAN DEFAULT TRUE,
    student_portal_enabled BOOLEAN DEFAULT TRUE,
    sms_notifications_enabled BOOLEAN DEFAULT FALSE,
    email_notifications_enabled BOOLEAN DEFAULT TRUE,
    push_notifications_enabled BOOLEAN DEFAULT TRUE,

    -- Locale Settings
    timezone VARCHAR(50) DEFAULT 'Asia/Kolkata',
    date_format VARCHAR(20) DEFAULT 'dd/MM/yyyy',
    time_format VARCHAR(10) DEFAULT 'HH:mm',
    currency VARCHAR(3) DEFAULT 'INR',
    language VARCHAR(10) DEFAULT 'en',

    -- Contact Settings
    support_email VARCHAR(100),
    support_phone VARCHAR(20),
    emergency_contact VARCHAR(20),

    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0
);

-- Index on tenant_id for fast lookups
CREATE INDEX IF NOT EXISTS idx_tenant_settings_tenant_id ON tenant_settings(tenant_id);

-- Comment
COMMENT ON TABLE tenant_settings IS 'School-specific configuration and settings';
