-- V2__Create_user_schema.sql
-- Create users and related tables for authentication and authorization

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    avatar_url VARCHAR(500),
    
    -- Status and role
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    primary_role VARCHAR(20) NOT NULL,
    
    -- Security fields
    is_account_non_expired BOOLEAN DEFAULT true,
    is_account_non_locked BOOLEAN DEFAULT true,
    is_credentials_non_expired BOOLEAN DEFAULT true,
    is_enabled BOOLEAN DEFAULT false,
    
    -- MFA fields
    is_mfa_enabled BOOLEAN DEFAULT false,
    mfa_secret VARCHAR(255),
    
    -- Additional security fields
    last_login_at TIMESTAMP,
    last_password_change_at TIMESTAMP,
    password_reset_token VARCHAR(255),
    password_reset_token_expiry TIMESTAMP,
    email_verification_token VARCHAR(255),
    email_verified BOOLEAN DEFAULT false,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    
    -- Reference fields
    reference_id VARCHAR(255),
    reference_type VARCHAR(50),
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0,
    
    -- Soft delete
    is_deleted BOOLEAN DEFAULT false,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    
    -- Unique constraints per tenant
    CONSTRAINT uk_user_email_tenant UNIQUE (email, tenant_id),
    CONSTRAINT uk_user_username_tenant UNIQUE (username, tenant_id)
);

-- Create user_roles table for role assignments
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_user_tenant ON users(tenant_id);
CREATE INDEX idx_user_status ON users(status);
CREATE INDEX idx_user_primary_role ON users(primary_role);
CREATE INDEX idx_user_reference ON users(reference_id, reference_type);
CREATE INDEX idx_user_password_reset_token ON users(password_reset_token);
CREATE INDEX idx_user_email_verification_token ON users(email_verification_token);
CREATE INDEX idx_user_deleted ON users(is_deleted);

-- Create trigger for users table
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Create default admin user for public tenant
INSERT INTO users (
    tenant_id,
    username,
    email,
    password,
    first_name,
    last_name,
    status,
    primary_role,
    is_enabled,
    email_verified,
    created_by
) VALUES (
    'public',
    'admin',
    'admin@publicschool.com',
    '$2a$10$EOs8VROb14e7ZnydvXECA.4LoIhPOoFHKvVF/iBZ/ker17Eocz4Vi', -- password: admin123
    'System',
    'Administrator',
    'ACTIVE',
    'ADMIN',
    true,
    true,
    'system'
) ON CONFLICT (username, tenant_id) DO NOTHING;

-- Add ADMIN role to default admin user
INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE username = 'admin' AND tenant_id = 'public'
ON CONFLICT DO NOTHING;
