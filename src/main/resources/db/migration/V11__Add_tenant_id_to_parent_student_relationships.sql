-- V11__Add_tenant_id_to_parent_student_relationships.sql
-- Add tenant_id columns to student relationship tables for better data isolation

-- Add tenant_id to student_parents table if it doesn't exist
-- This will only work if the table exists, so we use a safe approach
ALTER TABLE student_parents 
ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50) NOT NULL DEFAULT 'SYSTEM';

-- Add tenant_id to student_guardians table if it doesn't exist
ALTER TABLE student_guardians 
ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50) NOT NULL DEFAULT 'SYSTEM';

-- Create indexes for tenant_id to improve query performance
CREATE INDEX IF NOT EXISTS idx_student_parents_tenant ON student_parents(tenant_id);
CREATE INDEX IF NOT EXISTS idx_student_guardians_tenant ON student_guardians(tenant_id);

-- Note: Primary key updates and constraint additions are handled in V12
-- because ALTER TABLE ... DROP CONSTRAINT may fail if constraints don't exist
