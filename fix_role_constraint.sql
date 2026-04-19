-- Fix the role constraint to support multiple roles

-- Step 1: Drop the existing constraint that prevents multiple roles
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;

-- Step 2: (Optional) Add a new constraint that allows multiple roles
-- This constraint ensures the role field is not empty and contains valid role values
    ALTER TABLE users
    ADD CONSTRAINT users_role_check
    CHECK (
        role IS NOT NULL
        AND role != ''
        AND (
            role LIKE '%TEACHER%' OR
            role LIKE '%ADMIN%' OR
            role LIKE '%STUDENT%' OR
            role LIKE '%PARENT%' OR
            role LIKE '%STAFF%' OR
            role LIKE '%SUPER_ADMIN%'
        )
    );

-- Step 3: Update the column type to TEXT if it's not already
ALTER TABLE users ALTER COLUMN role TYPE TEXT;

-- Step 4: Test the constraint with a sample update
-- This should work now:
-- UPDATE users SET role = 'TEACHER,ADMIN' WHERE id = 'your-user-id';
