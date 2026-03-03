-- V12__Create_parent_student_tables_if_missing.sql
-- Create parent-student relationship tables if they don't exist

-- Create student_parents table if it doesn't exist
CREATE TABLE IF NOT EXISTS student_parents (
    student_id UUID NOT NULL,
    parent_id UUID NOT NULL,
    tenant_id VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    PRIMARY KEY (student_id, parent_id, tenant_id),
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES parents(id) ON DELETE CASCADE
);

-- Create student_guardians table if it doesn't exist  
CREATE TABLE IF NOT EXISTS student_guardians (
    student_id UUID NOT NULL,
    guardian_id UUID NOT NULL,
    tenant_id VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    PRIMARY KEY (student_id, guardian_id, tenant_id),
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (guardian_id) REFERENCES parents(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_student_parents_tenant ON student_parents(tenant_id);
CREATE INDEX IF NOT EXISTS idx_student_guardians_tenant ON student_guardians(tenant_id);

-- Create unique constraints to prevent duplicate relationships within same tenant
-- Note: PostgreSQL doesn't support IF NOT EXISTS for constraints, so we handle it manually
DO $$
BEGIN
    -- Add constraint for student_parents if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                  WHERE table_name = 'student_parents' AND constraint_name = 'uk_student_parents_tenant_unique') THEN
        ALTER TABLE student_parents 
        ADD CONSTRAINT uk_student_parents_tenant_unique 
        UNIQUE (student_id, parent_id, tenant_id);
    END IF;
    
    -- Add constraint for student_guardians if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                  WHERE table_name = 'student_guardians' AND constraint_name = 'uk_student_guardians_tenant_unique') THEN
        ALTER TABLE student_guardians 
        ADD CONSTRAINT uk_student_guardians_tenant_unique 
        UNIQUE (student_id, guardian_id, tenant_id);
    END IF;
END $$;
