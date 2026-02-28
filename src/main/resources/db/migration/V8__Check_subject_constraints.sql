-- V8__Check_subject_constraints.sql
-- Check current constraints on subjects table

-- List all constraints on the subjects table
DO $$
BEGIN
    RAISE NOTICE 'Checking current constraints on subjects table...';
EXCEPTION
    WHEN OTHERS THEN
        -- Ignore errors
END;
$$;

-- This will help us see what constraints currently exist
SELECT 
    tc.constraint_name,
    tc.constraint_type,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name 
FROM 
    information_schema.table_constraints tc
    JOIN information_schema.key_column_usage kcu 
        ON tc.constraint_name = kcu.constraint_name
    JOIN information_schema.constraint_column_usage ccu 
        ON ccu.constraint_name = tc.constraint_name
WHERE 
    tc.table_name = 'subjects' 
    AND tc.constraint_schema = 'public'
ORDER BY 
    tc.constraint_name, 
    kcu.ordinal_position;
