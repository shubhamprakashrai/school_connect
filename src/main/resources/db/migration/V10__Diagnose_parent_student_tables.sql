-- V10__Diagnose_parent_student_tables.sql
-- Diagnostic migration to check if parent-student tables exist

-- Create a temporary table to store diagnostic information
CREATE TABLE IF NOT EXISTS migration_diagnostics (
    table_name VARCHAR(100),
    column_name VARCHAR(100),
    exists_flag BOOLEAN,
    diagnostic_info TEXT
);

-- Check if student_parents table exists
INSERT INTO migration_diagnostics (table_name, column_name, exists_flag, diagnostic_info)
SELECT 
    'student_parents' as table_name,
    'table_exists' as column_name,
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'student_parents') as exists_flag,
    CASE 
        WHEN EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'student_parents') 
        THEN 'Table exists'
        ELSE 'Table does not exist'
    END as diagnostic_info;

-- Check if student_guardians table exists
INSERT INTO migration_diagnostics (table_name, column_name, exists_flag, diagnostic_info)
SELECT 
    'student_guardians' as table_name,
    'table_exists' as column_name,
    EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'student_guardians') as exists_flag,
    CASE 
        WHEN EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'student_guardians') 
        THEN 'Table exists'
        ELSE 'Table does not exist'
    END as diagnostic_info;
