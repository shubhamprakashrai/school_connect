-- Test script to verify tenant_id functionality in student-parent relationships
-- Run this script directly in PostgreSQL to test the fix

-- Test 1: Verify tables exist with tenant_id column
SELECT table_name, column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name IN ('student_parents', 'student_guardians') 
AND column_name = 'tenant_id'
ORDER BY table_name;

-- Test 2: Check primary key constraints include tenant_id
SELECT tc.table_name, tc.constraint_name, tc.constraint_type,
       kcu.column_name
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu 
     ON tc.constraint_name = kcu.constraint_name
WHERE tc.table_name IN ('student_parents', 'student_guardians')
AND tc.constraint_type = 'PRIMARY KEY'
ORDER BY tc.table_name, kcu.ordinal_position;

-- Test 3: Check unique constraints include tenant_id
SELECT tc.table_name, tc.constraint_name, tc.constraint_type,
       kcu.column_name
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu 
     ON tc.constraint_name = kcu.constraint_name
WHERE tc.table_name IN ('student_parents', 'student_guardians')
AND tc.constraint_type = 'UNIQUE'
ORDER BY tc.table_name, kcu.ordinal_position;

-- Test 4: Check indexes for tenant_id
SELECT indexname, tablename, indexdef 
FROM pg_indexes 
WHERE tablename IN ('student_parents', 'student_guardians')
AND indexdef LIKE '%tenant_id%'
ORDER BY tablename, indexname;

-- Test 5: Sample data verification (if data exists)
SELECT 'student_parents' as table_name, COUNT(*) as total_records,
       COUNT(DISTINCT tenant_id) as unique_tenants
FROM student_parents
UNION ALL
SELECT 'student_guardians' as table_name, COUNT(*) as total_records,
       COUNT(DISTINCT tenant_id) as unique_tenants
FROM student_guardians;
