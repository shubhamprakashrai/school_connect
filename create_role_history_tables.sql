-- Create user_role_history table for tracking role assignments with expiry support

CREATE TABLE user_role_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    assigned_by VARCHAR(100),
    reason TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    removed_at TIMESTAMP NULL,
    removed_by VARCHAR(100),
    removal_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_role_history_user_id (user_id),
    INDEX idx_user_role_history_role (role),
    INDEX idx_user_role_history_tenant (tenant_id),
    INDEX idx_user_role_history_active (is_active),
    INDEX idx_user_role_history_expires (expires_at),
    INDEX idx_user_role_history_user_active (user_id, is_active),
    INDEX idx_user_role_history_user_tenant (user_id, tenant_id),
    INDEX idx_user_role_history_user_tenant_active (user_id, tenant_id, is_active)
);

-- Insert existing roles into history table for audit trail
INSERT INTO user_role_history (user_id, role, tenant_id, assigned_at, assigned_by, reason, is_active)
SELECT 
    id as user_id,
    -- Extract first role from comma-separated list
    CASE 
        WHEN role LIKE '%,%' THEN SUBSTRING(role, 1, LOCATE(',', role) - 1)
        ELSE role
    END as role,
    tenant_id,
    created_at as assigned_at,
    created_by as assigned_by,
    'Initial role assignment' as reason,
    TRUE as is_active
FROM users 
WHERE role IS NOT NULL AND role != '';

-- If users have multiple roles, insert additional entries
-- This is a simplified approach - you might want to handle this more carefully
INSERT INTO user_role_history (user_id, role, tenant_id, assigned_at, assigned_by, reason, is_active)
SELECT 
    id as user_id,
    TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(role, ',', n), ',', -1)) as role,
    tenant_id,
    created_at as assigned_at,
    created_by as assigned_by,
    'Initial role assignment' as reason,
    TRUE as is_active
FROM users 
CROSS JOIN (
    SELECT 1 as n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
) numbers
WHERE role LIKE '%,%' 
  AND n <= LENGTH(role) - LENGTH(REPLACE(role, ',', '')) + 1
  AND TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(role, ',', n), ',', -1)) != '';
