-- V3__Create_student_teacher_parent_schema.sql
-- Create student, teacher, and parent tables

-- Create students table
CREATE TABLE IF NOT EXISTS students (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    admission_number VARCHAR(50) NOT NULL,
    roll_number VARCHAR(20) NOT NULL,
    
    -- Personal Information
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    blood_group VARCHAR(10),
    nationality VARCHAR(50),
    religion VARCHAR(50),
    caste VARCHAR(50),
    category VARCHAR(20),
    
    -- Contact Information
    email VARCHAR(100),
    phone VARCHAR(20),
    alternate_phone VARCHAR(20),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    
    -- Academic Information
    current_class_id VARCHAR(255),
    current_section_id VARCHAR(255),
    admission_date DATE NOT NULL,
    admission_class VARCHAR(50),
    previous_school VARCHAR(200),
    transfer_certificate_number VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    promotion_status VARCHAR(20),
    
    -- Health Information
    medical_conditions TEXT,
    allergies TEXT,
    emergency_medication TEXT,
    doctor_name VARCHAR(100),
    doctor_phone VARCHAR(20),
    
    -- Parent/Guardian Information
    father_name VARCHAR(200),
    father_occupation VARCHAR(100),
    father_phone VARCHAR(20),
    father_email VARCHAR(100),
    mother_name VARCHAR(200),
    mother_occupation VARCHAR(100),
    mother_phone VARCHAR(20),
    mother_email VARCHAR(100),
    guardian_name VARCHAR(200),
    guardian_relation VARCHAR(50),
    guardian_phone VARCHAR(20),
    guardian_email VARCHAR(100),
    
    -- Emergency Contact
    emergency_contact_name VARCHAR(200),
    emergency_contact_relation VARCHAR(50),
    emergency_contact_phone VARCHAR(20) NOT NULL,
    
    -- Transportation
    transport_mode VARCHAR(50),
    transport_route_id VARCHAR(255),
    pickup_point VARCHAR(200),
    
    -- Documents
    photo_url VARCHAR(500),
    birth_certificate_url VARCHAR(500),
    aadhar_number VARCHAR(20),
    documents TEXT,
    
    -- Fee Information
    fee_category VARCHAR(50),
    scholarship_applicable BOOLEAN DEFAULT false,
    scholarship_details TEXT,
    
    -- System User Link
    user_id UUID UNIQUE,
    
    -- Metadata
    remarks TEXT,
    graduation_date DATE,
    leaving_date DATE,
    leaving_reason TEXT,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT false,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    
    -- Constraints
    CONSTRAINT uk_student_admission_tenant UNIQUE (admission_number, tenant_id),
    CONSTRAINT uk_student_roll_class UNIQUE (roll_number, current_class_id, tenant_id),
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create teachers table
CREATE TABLE IF NOT EXISTS teachers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    employee_id VARCHAR(50) NOT NULL,
    
    -- Personal Information
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    blood_group VARCHAR(10),
    nationality VARCHAR(50),
    religion VARCHAR(50),
    marital_status VARCHAR(20),
    
    -- Contact Information
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    alternate_phone VARCHAR(20),
    address TEXT,
    permanent_address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    
    -- Professional Information
    joining_date DATE NOT NULL,
    employee_type VARCHAR(20) NOT NULL DEFAULT 'PERMANENT',
    department VARCHAR(100),
    designation VARCHAR(100) NOT NULL,
    is_class_teacher BOOLEAN DEFAULT false,
    class_teacher_for VARCHAR(50),
    
    -- Qualifications
    highest_qualification VARCHAR(100),
    professional_qualification VARCHAR(100),
    experience_years INTEGER,
    previous_school VARCHAR(200),
    
    -- Salary Information
    basic_salary DECIMAL(10,2),
    gross_salary DECIMAL(10,2),
    bank_name VARCHAR(100),
    bank_account_number VARCHAR(50),
    bank_branch VARCHAR(100),
    ifsc_code VARCHAR(20),
    pan_number VARCHAR(20),
    aadhar_number VARCHAR(20),
    pf_number VARCHAR(50),
    esi_number VARCHAR(50),
    
    -- Status and Availability
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    leaving_date DATE,
    leaving_reason TEXT,
    
    -- Emergency Contact
    emergency_contact_name VARCHAR(200),
    emergency_contact_relation VARCHAR(50),
    emergency_contact_phone VARCHAR(20),
    
    -- Documents
    photo_url VARCHAR(500),
    resume_url VARCHAR(500),
    documents TEXT,
    
    -- System User Link
    user_id UUID UNIQUE,
    
    -- Additional Information
    bio TEXT,
    achievements TEXT,
    publications TEXT,
    research_interests TEXT,
    notes TEXT,
    
    -- Performance Metrics
    rating DOUBLE PRECISION,
    attendance_percentage DOUBLE PRECISION,
    last_evaluation_date DATE,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT false,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    
    -- Constraints
    CONSTRAINT uk_teacher_employee_tenant UNIQUE (employee_id, tenant_id),
    CONSTRAINT uk_teacher_email_tenant UNIQUE (email, tenant_id),
    CONSTRAINT fk_teacher_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create parents table
CREATE TABLE IF NOT EXISTS parents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    
    -- Personal Information
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    parent_type VARCHAR(20) NOT NULL,
    gender VARCHAR(10),
    date_of_birth DATE,
    
    -- Contact Information
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    alternate_phone VARCHAR(20),
    work_phone VARCHAR(20),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    
    -- Professional Information
    occupation VARCHAR(100),
    employer VARCHAR(200),
    work_address TEXT,
    annual_income VARCHAR(50),
    education_level VARCHAR(100),
    
    -- Identification
    aadhar_number VARCHAR(20),
    pan_number VARCHAR(20),
    voter_id VARCHAR(20),
    
    -- Relationship Information
    relationship_to_student VARCHAR(50),
    is_primary_contact BOOLEAN DEFAULT false,
    is_emergency_contact BOOLEAN DEFAULT false,
    can_pickup_child BOOLEAN DEFAULT true,
    
    -- Communication Preferences
    preferred_language VARCHAR(50) DEFAULT 'English',
    receive_sms BOOLEAN DEFAULT true,
    receive_email BOOLEAN DEFAULT true,
    receive_app_notifications BOOLEAN DEFAULT true,
    
    -- Portal Access
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    portal_access_enabled BOOLEAN DEFAULT true,
    last_portal_login TIMESTAMP,
    
    -- Documents
    photo_url VARCHAR(500),
    id_proof_url VARCHAR(500),
    documents TEXT,
    
    -- System User Link
    user_id UUID UNIQUE,
    
    -- Notes and Remarks
    notes TEXT,
    special_instructions TEXT,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT false,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    
    -- Constraints
    CONSTRAINT uk_parent_email_tenant UNIQUE (email, tenant_id),
    CONSTRAINT fk_parent_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create relationship tables
CREATE TABLE IF NOT EXISTS student_parents (
    student_id UUID NOT NULL,
    parent_id UUID NOT NULL,
    PRIMARY KEY (student_id, parent_id),
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES parents(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS student_guardians (
    student_id UUID NOT NULL,
    guardian_id UUID NOT NULL,
    PRIMARY KEY (student_id, guardian_id),
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (guardian_id) REFERENCES parents(id) ON DELETE CASCADE
);

-- Create teacher subject and class tables
CREATE TABLE IF NOT EXISTS teacher_subjects (
    teacher_id UUID NOT NULL,
    subject VARCHAR(100) NOT NULL,
    PRIMARY KEY (teacher_id, subject),
    FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS teacher_classes (
    teacher_id UUID NOT NULL,
    class_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (teacher_id, class_id),
    FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS teacher_qualifications (
    teacher_id UUID NOT NULL,
    qualification VARCHAR(200) NOT NULL,
    FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS teacher_specializations (
    teacher_id UUID NOT NULL,
    specialization VARCHAR(200) NOT NULL,
    FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_student_tenant ON students(tenant_id);
CREATE INDEX idx_student_status ON students(status);
CREATE INDEX idx_student_class ON students(current_class_id);
CREATE INDEX idx_student_section ON students(current_section_id);
CREATE INDEX idx_student_admission ON students(admission_date);

CREATE INDEX idx_teacher_tenant ON teachers(tenant_id);
CREATE INDEX idx_teacher_status ON teachers(status);
CREATE INDEX idx_teacher_department ON teachers(department);
CREATE INDEX idx_teacher_joining ON teachers(joining_date);

CREATE INDEX idx_parent_tenant ON parents(tenant_id);
CREATE INDEX idx_parent_status ON parents(status);
CREATE INDEX idx_parent_type ON parents(parent_type);

-- Create triggers for updated_at
CREATE TRIGGER update_students_updated_at 
    BEFORE UPDATE ON students
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_teachers_updated_at 
    BEFORE UPDATE ON teachers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_parents_updated_at 
    BEFORE UPDATE ON parents
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
