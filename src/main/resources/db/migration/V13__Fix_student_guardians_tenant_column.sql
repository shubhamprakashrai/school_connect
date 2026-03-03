-- V13__Fix_student_guardians_tenant_column.sql
-- Ensure tenant_id columns exist in parent-student relationship tables

-- Add tenant_id column to student_parents if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'student_parents' 
        AND column_name = 'tenant_id'
    ) THEN
        ALTER TABLE student_parents ADD COLUMN tenant_id VARCHAR(50) NOT NULL DEFAULT 'SYSTEM';
        CREATE INDEX IF NOT EXISTS idx_student_parents_tenant ON student_parents(tenant_id);
    END IF;
END $$;

-- Add tenant_id column to student_guardians if it doesn't exist  
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'student_guardians' 
        AND column_name = 'tenant_id'
    ) THEN
        ALTER TABLE student_guardians ADD COLUMN tenant_id VARCHAR(50) NOT NULL DEFAULT 'SYSTEM';
        CREATE INDEX IF NOT EXISTS idx_student_guardians_tenant ON student_guardians(tenant_id);
    END IF;
END $$;

-- Update existing records to use parent's tenant_id instead of SYSTEM
UPDATE student_parents sp
SET tenant_id = p.tenant_id
FROM parents p
WHERE sp.parent_id = p.id AND sp.tenant_id = 'SYSTEM';

UPDATE student_guardians sg  
SET tenant_id = p.tenant_id
FROM parents p
WHERE sg.guardian_id = p.id AND sg.tenant_id = 'SYSTEM';

-- Add unique constraints to prevent duplicate relationships within same tenant
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
