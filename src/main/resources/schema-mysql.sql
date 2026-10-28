-- ====================================================================
-- Smart Parking System - MySQL DDL Schema
-- Compatible with MySQL 8.x
-- Run this once to create all tables in the 'smart_parking' database.
-- ====================================================================

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Vehicles Table
CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_number VARCHAR(30) NOT NULL UNIQUE,
    vehicle_type VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. Parking Slots Table
CREATE TABLE IF NOT EXISTS parking_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    slot_number VARCHAR(20) NOT NULL UNIQUE,
    vehicle_type_allowed VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
);

-- 4. Parking Rates Table
CREATE TABLE IF NOT EXISTS parking_rates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_type VARCHAR(20) NOT NULL UNIQUE,
    rate_per_hour DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL
);

-- 5. Parking Records Table
CREATE TABLE IF NOT EXISTS parking_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    slot_id BIGINT NOT NULL,
    entry_time DATETIME NOT NULL,
    exit_time DATETIME DEFAULT NULL,
    duration_minutes BIGINT DEFAULT NULL,
    parking_fee DECIMAL(10, 2) DEFAULT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PARKED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pr_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    CONSTRAINT fk_pr_slot FOREIGN KEY (slot_id) REFERENCES parking_slots(id)
);

-- 6. Payments Table
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parking_record_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PAID',
    payment_method VARCHAR(30) NOT NULL,
    paid_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    transaction_id VARCHAR(100) NOT NULL UNIQUE,
    CONSTRAINT fk_pay_record FOREIGN KEY (parking_record_id) REFERENCES parking_records(id)
);

-- 7. Audit Logs Table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT DEFAULT NULL,
    user_email VARCHAR(100) DEFAULT NULL,
    action VARCHAR(100) NOT NULL,
    description VARCHAR(1000) DEFAULT NULL,
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 8. ANPR Records Table
CREATE TABLE IF NOT EXISTS anpr_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_number VARCHAR(30) NOT NULL,
    confidence VARCHAR(20) NOT NULL,
    recognition_status VARCHAR(30) NOT NULL,
    source VARCHAR(50) NOT NULL DEFAULT 'MOCK',
    captured_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_pr_entry_time ON parking_records(entry_time);
CREATE INDEX idx_pr_status ON parking_records(status);
CREATE INDEX idx_payments_paid_at ON payments(paid_at);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_anpr_captured_at ON anpr_records(captured_at);
