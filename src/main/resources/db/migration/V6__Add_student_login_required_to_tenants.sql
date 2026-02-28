-- V6__Add_student_login_required_to_tenants.sql
-- Add student_login_required column to tenants table

ALTER TABLE tenants 
ADD COLUMN student_login_required BOOLEAN NOT NULL DEFAULT false;

-- Add comment to the new column
COMMENT ON COLUMN tenants.student_login_required IS 'Whether student login is required for this tenant';
