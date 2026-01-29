-- V6: Create FCM tokens table for push notifications
-- This table stores Firebase Cloud Messaging tokens for mobile devices

CREATE TABLE IF NOT EXISTS fcm_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    device_type VARCHAR(20),
    device_id VARCHAR(100),
    device_name VARCHAR(100),
    app_version VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP
);

-- Indexes for efficient querying
CREATE INDEX IF NOT EXISTS idx_fcm_token_user_id ON fcm_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_fcm_token_tenant_id ON fcm_tokens(tenant_id);
CREATE INDEX IF NOT EXISTS idx_fcm_token_device_id ON fcm_tokens(user_id, device_id);
CREATE INDEX IF NOT EXISTS idx_fcm_token_active ON fcm_tokens(is_active) WHERE is_active = TRUE;

-- Add comment
COMMENT ON TABLE fcm_tokens IS 'Stores FCM tokens for push notifications to mobile devices';
