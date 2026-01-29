-- V14__Create_leave_tables.sql
-- Leave management tables: leave_types, leave_requests, leave_balances

-- Leave Types table
CREATE TABLE IF NOT EXISTS leave_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    max_days_per_year INTEGER DEFAULT 12,
    is_paid BOOLEAN DEFAULT TRUE,
    requires_approval BOOLEAN DEFAULT TRUE,
    applicable_roles VARCHAR(200),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_leave_type_tenant ON leave_types(tenant_id);

-- Leave Requests table
CREATE TABLE IF NOT EXISTS leave_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    leave_type_id UUID NOT NULL REFERENCES leave_types(id),
    user_id VARCHAR(50) NOT NULL,
    user_name VARCHAR(200),
    user_role VARCHAR(30),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days INTEGER,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by VARCHAR(50),
    approved_by_name VARCHAR(200),
    approval_remarks VARCHAR(500),
    approved_at TIMESTAMP WITH TIME ZONE,
    is_half_day BOOLEAN DEFAULT FALSE,
    attachment_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_leave_req_tenant ON leave_requests(tenant_id);
CREATE INDEX IF NOT EXISTS idx_leave_req_user ON leave_requests(user_id);
CREATE INDEX IF NOT EXISTS idx_leave_req_status ON leave_requests(status);
CREATE INDEX IF NOT EXISTS idx_leave_req_dates ON leave_requests(start_date, end_date);

-- Leave Balances table
CREATE TABLE IF NOT EXISTS leave_balances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    user_id VARCHAR(50) NOT NULL,
    leave_type_id UUID NOT NULL REFERENCES leave_types(id),
    academic_year VARCHAR(20) NOT NULL,
    total_allocated INTEGER DEFAULT 0,
    used INTEGER DEFAULT 0,
    pending INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_leave_balance UNIQUE (tenant_id, user_id, leave_type_id, academic_year)
);

CREATE INDEX IF NOT EXISTS idx_leave_bal_tenant ON leave_balances(tenant_id);
CREATE INDEX IF NOT EXISTS idx_leave_bal_user ON leave_balances(user_id);

-- Comments
COMMENT ON TABLE leave_types IS 'Types of leaves (Sick, Casual, Earned, etc.)';
COMMENT ON TABLE leave_requests IS 'Leave applications with approval workflow';
COMMENT ON TABLE leave_balances IS 'Leave balance per user per leave type per academic year';
