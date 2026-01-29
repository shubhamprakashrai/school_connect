-- V11__Create_timetable_tables.sql
-- Timetable management tables: periods, timetable_entries

-- Periods table (time slots for the school day)
CREATE TABLE IF NOT EXISTS periods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    period_number INTEGER NOT NULL,
    name VARCHAR(50) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_break BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_period_tenant_number UNIQUE (tenant_id, period_number)
);

CREATE INDEX IF NOT EXISTS idx_period_tenant ON periods(tenant_id);

-- Timetable Entries table
CREATE TABLE IF NOT EXISTS timetable_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    day_of_week VARCHAR(10) NOT NULL,
    period_id UUID NOT NULL REFERENCES periods(id),
    class_id UUID NOT NULL,
    section VARCHAR(10),
    subject_id UUID,
    subject_name VARCHAR(100),
    teacher_id UUID,
    teacher_name VARCHAR(200),
    room VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    academic_year VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    CONSTRAINT uk_tt_class_day_period UNIQUE (tenant_id, class_id, section, day_of_week, period_id),
    CONSTRAINT uk_tt_teacher_day_period UNIQUE (tenant_id, teacher_id, day_of_week, period_id)
);

CREATE INDEX IF NOT EXISTS idx_tt_tenant ON timetable_entries(tenant_id);
CREATE INDEX IF NOT EXISTS idx_tt_class ON timetable_entries(class_id);
CREATE INDEX IF NOT EXISTS idx_tt_teacher ON timetable_entries(teacher_id);
CREATE INDEX IF NOT EXISTS idx_tt_day_period ON timetable_entries(day_of_week, period_id);

-- Comments
COMMENT ON TABLE periods IS 'Period time slots for the school day';
COMMENT ON TABLE timetable_entries IS 'Weekly timetable entries mapping classes, periods, subjects and teachers';
