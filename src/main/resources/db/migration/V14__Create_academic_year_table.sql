-- V14__Create_academic_year_table.sql
-- Create academic_years table with proper tenant isolation

-- Create academic_years table
CREATE TABLE IF NOT EXISTS academic_years (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    name VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    
    -- Unique constraint for tenant + name
    CONSTRAINT uk_academic_year_tenant_name UNIQUE (tenant_id, name),
    
    -- Check constraints
    CONSTRAINT chk_academic_year_dates CHECK (end_date > start_date),
    CONSTRAINT chk_academic_year_single_active CHECK (
        is_active = FALSE OR 
        (tenant_id, is_active) IN (
            SELECT tenant_id, is_active 
            FROM academic_years 
            WHERE is_active = TRUE 
            GROUP BY tenant_id, is_active 
            HAVING COUNT(*) = 1
        )
    )
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_academic_years_tenant_id ON academic_years(tenant_id);
CREATE INDEX IF NOT EXISTS idx_academic_years_is_active ON academic_years(is_active);
CREATE INDEX IF NOT EXISTS idx_academic_years_dates ON academic_years(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_academic_years_deleted ON academic_years(is_deleted);

-- Create trigger for updated_at (PostgreSQL)
CREATE OR REPLACE FUNCTION update_academic_years_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

DROP TRIGGER IF EXISTS trigger_update_academic_years_updated_at ON academic_years;
CREATE TRIGGER trigger_update_academic_years_updated_at
    BEFORE UPDATE ON academic_years
    FOR EACH ROW
    EXECUTE FUNCTION update_academic_years_updated_at();
