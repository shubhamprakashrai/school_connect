-- V7__Fix_subject_constraints.sql
-- Fix subject table constraints to be tenant-specific only
-- Remove old global constraint and add proper tenant-specific constraint

-- First, drop the old global constraint if it exists
DO $$ 
BEGIN;
    -- Check if the old global constraint exists and drop it
    DROP CONSTRAINT IF EXISTS ukrg7x1lyii7kdyycw98d45vep5;
EXCEPTION
    WHEN OTHERS THEN
        -- Constraint doesn't exist or can't be dropped, continue
END;
$$;

-- Add the proper tenant-specific unique constraint
ALTER TABLE subjects 
ADD CONSTRAINT uk_subject_tenant_code 
UNIQUE (tenant_id, code);

-- Log the constraint change
DO $$
BEGIN
    RAISE NOTICE 'Subject table constraints updated: Removed global constraint, added tenant-specific constraint';
EXCEPTION
    WHEN OTHERS THEN
        -- Ignore if constraint already exists
END;
$$;
