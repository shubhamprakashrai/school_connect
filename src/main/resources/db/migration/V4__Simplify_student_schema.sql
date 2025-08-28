-- V4__Simplify_student_schema.sql
-- Remove unwanted fields from students table

-- Remove admission number column and related index
DROP INDEX IF EXISTS idx_student_admission;
ALTER TABLE students DROP COLUMN IF EXISTS admission_number;

-- Remove blood group column
ALTER TABLE students DROP COLUMN IF EXISTS blood_group;

-- Remove transport related columns
ALTER TABLE students DROP COLUMN IF EXISTS transport_mode;
ALTER TABLE students DROP COLUMN IF EXISTS transport_route_id;
ALTER TABLE students DROP COLUMN IF EXISTS pickup_point;

-- Remove fee related columns
ALTER TABLE students DROP COLUMN IF EXISTS fee_category;
ALTER TABLE students DROP COLUMN IF EXISTS scholarship_applicable;
ALTER TABLE students DROP COLUMN IF EXISTS scholarship_details;

-- Remove additional medical fields we don't want
ALTER TABLE students DROP COLUMN IF EXISTS allergies;
ALTER TABLE students DROP COLUMN IF EXISTS emergency_medication;
ALTER TABLE students DROP COLUMN IF EXISTS doctor_phone;

-- Remove transfer certificate column
ALTER TABLE students DROP COLUMN IF EXISTS transfer_certificate_number;

-- Remove promotion status column
ALTER TABLE students DROP COLUMN IF EXISTS promotion_status;
