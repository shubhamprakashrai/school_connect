-- V12__Create_fee_tables.sql
-- Fee management tables: fee_types, fee_structures, fee_payments

-- Fee Types table
CREATE TABLE IF NOT EXISTS fee_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_recurring BOOLEAN DEFAULT FALSE,
    frequency VARCHAR(20),
    is_mandatory BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_fee_type_tenant ON fee_types(tenant_id);

-- Fee Structures table
CREATE TABLE IF NOT EXISTS fee_structures (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    fee_type_id UUID NOT NULL REFERENCES fee_types(id),
    class_id UUID NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    due_date DATE,
    late_fee DECIMAL(10, 2) DEFAULT 0.00,
    discount_percentage DOUBLE PRECISION DEFAULT 0.0,
    academic_year VARCHAR(20),
    term VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_fee_struct_tenant ON fee_structures(tenant_id);
CREATE INDEX IF NOT EXISTS idx_fee_struct_class ON fee_structures(class_id);

-- Fee Payments table
CREATE TABLE IF NOT EXISTS fee_payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    fee_structure_id UUID NOT NULL REFERENCES fee_structures(id),
    student_id UUID NOT NULL,
    student_name VARCHAR(200),
    amount_paid DECIMAL(10, 2) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) DEFAULT 0.00,
    late_fee_amount DECIMAL(10, 2) DEFAULT 0.00,
    balance_amount DECIMAL(10, 2) DEFAULT 0.00,
    payment_date DATE,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_mode VARCHAR(20),
    transaction_id VARCHAR(100),
    receipt_number VARCHAR(50),
    remarks VARCHAR(500),
    collected_by VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_fee_payment_tenant ON fee_payments(tenant_id);
CREATE INDEX IF NOT EXISTS idx_fee_payment_student ON fee_payments(student_id);
CREATE INDEX IF NOT EXISTS idx_fee_payment_date ON fee_payments(payment_date);
CREATE INDEX IF NOT EXISTS idx_fee_payment_status ON fee_payments(payment_status);

-- Comments
COMMENT ON TABLE fee_types IS 'Types of fees (Tuition, Transport, Library, etc.)';
COMMENT ON TABLE fee_structures IS 'Fee structures defining amounts per class and fee type';
COMMENT ON TABLE fee_payments IS 'Student fee payment records';
