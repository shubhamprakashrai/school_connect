-- Migration script to change user.role column from single role to multiple roles
-- This will store roles as comma-separated values in the same column

-- Step 1: Create backup of existing data
CREATE TABLE users_role_backup AS SELECT * FROM users;

-- Step 2: Update the column to TEXT type to store multiple roles
-- This works for most databases. For PostgreSQL, you might want to use TEXT array type
ALTER TABLE users ALTER COLUMN role TYPE TEXT;

-- Step 3: The UserRoleSetConverter will handle the conversion automatically
-- Existing single roles will be converted to Set with one element
-- New multi-roles will be stored as comma-separated values

-- Example data transformation:
-- Before: "TEACHER"
-- After: "TEACHER" (single role) or "TEACHER,ADMIN" (multiple roles)

-- Step 4: Verify the migration
SELECT id, email, role FROM users LIMIT 10;

-- Rollback script if needed:
-- ALTER TABLE users ALTER COLUMN role TYPE VARCHAR(20);
-- UPDATE users SET role = 'TEACHER' WHERE role LIKE '%TEACHER%';
-- UPDATE users SET role = 'ADMIN' WHERE role LIKE '%ADMIN%';
-- UPDATE users SET role = 'STUDENT' WHERE role LIKE '%STUDENT%';
-- UPDATE users SET role = 'PARENT' WHERE role LIKE '%PARENT%';
-- UPDATE users SET role = 'SUPER_ADMIN' WHERE role LIKE '%SUPER_ADMIN%';
