-- V1__Create_tenant_schema.sql
-- Initial migration to create tenant management tables

-- Create tenants table
CREATE TABLE IF NOT EXISTS tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    identifier VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    subdomain VARCHAR(50) UNIQUE NOT NULL,
    schema_name VARCHAR(63) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    subscription_plan VARCHAR(20) NOT NULL DEFAULT 'BASIC',
    
    -- Contact Information
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    
    -- Configuration
    config TEXT,
    logo_url VARCHAR(500),
    website VARCHAR(200),
    
    -- Limits and Quotas
    max_students INTEGER DEFAULT 500,
    max_teachers INTEGER DEFAULT 50,
    max_storage_gb INTEGER DEFAULT 10,
    current_students INTEGER DEFAULT 0,
    current_teachers INTEGER DEFAULT 0,
    current_storage_mb INTEGER DEFAULT 0,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activated_at TIMESTAMP,
    suspended_at TIMESTAMP,
    deleted_at TIMESTAMP,
    
    -- Metadata
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0
);

-- Create indexes for tenants table
CREATE INDEX idx_tenant_identifier ON tenants(identifier);
CREATE INDEX idx_tenant_subdomain ON tenants(subdomain);
CREATE INDEX idx_tenant_status ON tenants(status);
CREATE INDEX idx_tenant_email ON tenants(email);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for tenants table
CREATE TRIGGER update_tenants_updated_at 
    BEFORE UPDATE ON tenants
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Create a default public tenant for development
INSERT INTO tenants (
    identifier, 
    name, 
    subdomain, 
    schema_name, 
    status, 
    subscription_plan,
    email,
    created_by
) VALUES (
    'public',
    'Public School (Default)',
    'public',
    'public',
    'ACTIVE',
    'ENTERPRISE',
    'admin@publicschool.com',
    'system'
) ON CONFLICT (identifier) DO NOTHING;
