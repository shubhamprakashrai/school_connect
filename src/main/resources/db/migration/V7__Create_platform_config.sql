-- V7: Create platform configuration table
-- This table stores platform-wide settings managed by Super Admin

CREATE TABLE IF NOT EXISTS platform_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    config_key VARCHAR(100) NOT NULL UNIQUE,

    -- Branding
    app_name VARCHAR(100) DEFAULT 'School Connect',
    logo_url VARCHAR(500),
    favicon_url VARCHAR(500),
    primary_color VARCHAR(20) DEFAULT '#1976D2',
    secondary_color VARCHAR(20) DEFAULT '#424242',
    accent_color VARCHAR(20) DEFAULT '#FF9800',

    -- Contact
    support_email VARCHAR(100),
    support_phone VARCHAR(20),
    website_url VARCHAR(200),

    -- Legal Links
    terms_url VARCHAR(500),
    privacy_url VARCHAR(500),

    -- Feature Flags (JSON)
    feature_flags TEXT,

    -- Social Links (JSON)
    social_links TEXT,

    -- Maintenance Mode
    maintenance_mode BOOLEAN DEFAULT FALSE,
    maintenance_message VARCHAR(500),
    maintenance_end_time TIMESTAMP,

    -- App Versions
    min_android_version VARCHAR(20),
    min_ios_version VARCHAR(20),
    latest_android_version VARCHAR(20),
    latest_ios_version VARCHAR(20),
    force_update BOOLEAN DEFAULT FALSE,
    update_message VARCHAR(500),

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

-- Create index on config key
CREATE INDEX IF NOT EXISTS idx_platform_config_key ON platform_config(config_key);

-- Insert default configuration
INSERT INTO platform_config (config_key, app_name, feature_flags)
VALUES (
    'default',
    'School Connect',
    '{"attendance":true,"fees":true,"exams":true,"timetable":true,"notifications":true,"parentPortal":true,"library":false,"transport":false,"hostel":false,"onlineClasses":false,"smsNotifications":false}'
) ON CONFLICT (config_key) DO NOTHING;

-- Add comment
COMMENT ON TABLE platform_config IS 'Platform-wide configuration managed by Super Admin';
