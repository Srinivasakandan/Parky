-- ====================================================================
-- Smart Parking System - Oracle SQL DDL Schema
-- Compatible with Oracle Database (12c, 18c, 19c, 21c, 23c)
-- ====================================================================

-- 1. Users Table
CREATE TABLE users (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR2(100) NOT NULL,
    email VARCHAR2(100) NOT NULL UNIQUE,
    password VARCHAR2(255) NOT NULL,
    role VARCHAR2(20) NOT NULL,
    status VARCHAR2(20) DEFAULT 'ACTIVE' NOT NULL,
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);

-- 2. Vehicles Table
CREATE TABLE vehicles (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vehicle_number VARCHAR2(30) NOT NULL UNIQUE,
    vehicle_type VARCHAR2(20) NOT NULL,
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);

-- 3. Parking Slots Table
CREATE TABLE parking_slots (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    slot_number VARCHAR2(20) NOT NULL UNIQUE,
    vehicle_type_allowed VARCHAR2(20) NOT NULL,
    status VARCHAR2(20) DEFAULT 'AVAILABLE' NOT NULL
);

-- 4. Parking Rates Table
CREATE TABLE parking_rates (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vehicle_type VARCHAR2(20) NOT NULL UNIQUE,
    rate_per_hour NUMBER(10, 2) NOT NULL,
    status VARCHAR2(20) DEFAULT 'ACTIVE' NOT NULL,
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT SYSTIMESTAMP
);

-- 5. Parking Records Table
CREATE TABLE parking_records (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vehicle_id NUMBER NOT NULL REFERENCES vehicles(id),
    slot_id NUMBER NOT NULL REFERENCES parking_slots(id),
    entry_time TIMESTAMP NOT NULL,
    exit_time TIMESTAMP,
    duration_minutes NUMBER,
    parking_fee NUMBER(10, 2),
    status VARCHAR2(20) DEFAULT 'PARKED' NOT NULL,
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);

-- 6. Payments Table
CREATE TABLE payments (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    parking_record_id NUMBER NOT NULL REFERENCES parking_records(id),
    amount NUMBER(10, 2) NOT NULL,
    payment_status VARCHAR2(20) DEFAULT 'PAID' NOT NULL,
    payment_method VARCHAR2(30) NOT NULL,
    paid_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    transaction_id VARCHAR2(100) NOT NULL UNIQUE
);

-- 7. Audit Logs Table
CREATE TABLE audit_logs (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id NUMBER,
    user_email VARCHAR2(100),
    action VARCHAR2(100) NOT NULL,
    description VARCHAR2(1000),
    timestamp TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);

-- 8. ANPR Records Table
CREATE TABLE anpr_records (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vehicle_number VARCHAR2(30) NOT NULL,
    confidence VARCHAR2(20) NOT NULL,
    recognition_status VARCHAR2(30) NOT NULL,
    source VARCHAR2(50) DEFAULT 'MOCK' NOT NULL,
    captured_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);

-- Indexes for performance
CREATE INDEX idx_pr_entry_time ON parking_records(entry_time);
CREATE INDEX idx_pr_status ON parking_records(status);
CREATE INDEX idx_payments_paid_at ON payments(paid_at);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_anpr_captured_at ON anpr_records(captured_at);
