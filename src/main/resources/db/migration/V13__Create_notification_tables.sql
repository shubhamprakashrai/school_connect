-- V13__Create_notification_tables.sql
-- Notification templates and notification logs tables

-- Notification Templates table
CREATE TABLE IF NOT EXISTS notification_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(100) NOT NULL,
    type VARCHAR(30) NOT NULL,
    title_template VARCHAR(200) NOT NULL,
    body_template VARCHAR(1000) NOT NULL,
    channel VARCHAR(20) DEFAULT 'PUSH',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notif_template_tenant ON notification_templates(tenant_id);
CREATE INDEX IF NOT EXISTS idx_notif_template_type ON notification_templates(type);

-- Notification Logs table
CREATE TABLE IF NOT EXISTS notification_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    title VARCHAR(200) NOT NULL,
    body VARCHAR(1000) NOT NULL,
    notification_type VARCHAR(30) NOT NULL,
    recipient_user_id VARCHAR(50),
    recipient_role VARCHAR(30),
    recipient_class_id UUID,
    sender_user_id VARCHAR(50),
    sender_name VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP WITH TIME ZONE,
    data_payload VARCHAR(2000),
    error_message VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notif_log_tenant ON notification_logs(tenant_id);
CREATE INDEX IF NOT EXISTS idx_notif_log_recipient ON notification_logs(recipient_user_id);
CREATE INDEX IF NOT EXISTS idx_notif_log_read ON notification_logs(is_read);
CREATE INDEX IF NOT EXISTS idx_notif_log_created ON notification_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_notif_log_status ON notification_logs(status);

-- Comments
COMMENT ON TABLE notification_templates IS 'Templates for notifications (fee reminders, attendance alerts, etc.)';
COMMENT ON TABLE notification_logs IS 'Log of all sent notifications with delivery status';
