-- V10__Create_exam_tables.sql
-- Exam management tables: exam_types, exams, exam_results

-- Exam Types table
CREATE TABLE IF NOT EXISTS exam_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    weightage DOUBLE PRECISION DEFAULT 0.0,
    max_marks INTEGER DEFAULT 100,
    passing_marks INTEGER DEFAULT 33,
    is_active BOOLEAN DEFAULT TRUE,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_exam_type_tenant ON exam_types(tenant_id);

-- Exams table
CREATE TABLE IF NOT EXISTS exams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    exam_type_id UUID NOT NULL REFERENCES exam_types(id),
    class_id UUID NOT NULL,
    section VARCHAR(10),
    subject_id UUID,
    subject_name VARCHAR(100),
    exam_date DATE NOT NULL,
    start_time TIME,
    end_time TIME,
    max_marks INTEGER NOT NULL DEFAULT 100,
    passing_marks INTEGER DEFAULT 33,
    room VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    academic_year VARCHAR(20),
    instructions TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_exam_tenant ON exams(tenant_id);
CREATE INDEX IF NOT EXISTS idx_exam_class ON exams(class_id);
CREATE INDEX IF NOT EXISTS idx_exam_type ON exams(exam_type_id);
CREATE INDEX IF NOT EXISTS idx_exam_date ON exams(exam_date);

-- Exam Results table
CREATE TABLE IF NOT EXISTS exam_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    exam_id UUID NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    student_id UUID NOT NULL,
    student_name VARCHAR(200),
    marks_obtained DOUBLE PRECISION NOT NULL,
    max_marks INTEGER NOT NULL,
    percentage DOUBLE PRECISION,
    grade VARCHAR(5),
    rank INTEGER,
    result_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    remarks VARCHAR(500),
    is_absent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    entered_by VARCHAR(100),
    CONSTRAINT uk_exam_student UNIQUE (exam_id, student_id)
);

CREATE INDEX IF NOT EXISTS idx_result_tenant ON exam_results(tenant_id);
CREATE INDEX IF NOT EXISTS idx_result_exam ON exam_results(exam_id);
CREATE INDEX IF NOT EXISTS idx_result_student ON exam_results(student_id);

-- Comments
COMMENT ON TABLE exam_types IS 'Types of examinations (Unit Test, Mid-Term, Final, etc.)';
COMMENT ON TABLE exams IS 'Scheduled examinations';
COMMENT ON TABLE exam_results IS 'Student results for each exam';
