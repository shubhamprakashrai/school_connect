-- Insert super admin user for tenant creation and system management
-- Password: Admin@123 (BCrypt hashed)

INSERT INTO users (
    id,
    tenant_id,
    username,
    email,
    password,
    first_name,
    last_name,
    phone,
    status,
    primary_role,
    is_enabled,
    email_verified,
    created_at,
    updated_at,
    created_by,
    updated_by
) VALUES (
    gen_random_uuid(),
    'SYSTEM',
    'superadmin@system.com',
    'superadmin@system.com',
    '$2a$10$FgtVz7LbRGH8x1iGZ8p1Feh4.3.UnC5SHf5b7OhE8QZ2KWZ2SYr2m', -- Admin@123
    'System',
    'Administrator',
    '+1000000000',
    'ACTIVE',
    'SUPER_ADMIN',
    true,
    true,
    NOW(),
    NOW(),
    'SYSTEM_INIT',
    'SYSTEM_INIT'
) ON CONFLICT (username, tenant_id) DO NOTHING;

-- Add SUPER_ADMIN role to super admin user
INSERT INTO user_roles (user_id, role)
SELECT id, 'SUPER_ADMIN' FROM users WHERE username = 'superadmin@system.com' AND tenant_id = 'SYSTEM'
ON CONFLICT DO NOTHING;