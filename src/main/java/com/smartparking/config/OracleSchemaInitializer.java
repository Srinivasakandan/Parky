package com.smartparking.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

@Component
@Order(1)
public class OracleSchemaInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(OracleSchemaInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) {
        log.info("Checking and initializing database tables...");
        try {
            createTableIfNotExists("USERS",
                    "CREATE TABLE users (" +
                    "  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                    "  name VARCHAR2(100) NOT NULL," +
                    "  email VARCHAR2(100) NOT NULL UNIQUE," +
                    "  password VARCHAR2(255) NOT NULL," +
                    "  role VARCHAR2(20) NOT NULL," +
                    "  status VARCHAR2(20) DEFAULT 'ACTIVE' NOT NULL," +
                    "  created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL" +
                    ")");

            createTableIfNotExists("VEHICLES",
                    "CREATE TABLE vehicles (" +
                    "  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                    "  vehicle_number VARCHAR2(30) NOT NULL UNIQUE," +
                    "  vehicle_type VARCHAR2(20) NOT NULL," +
                    "  created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL" +
                    ")");

            createTableIfNotExists("PARKING_SLOTS",
                    "CREATE TABLE parking_slots (" +
                    "  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                    "  slot_number VARCHAR2(20) NOT NULL UNIQUE," +
                    "  vehicle_type_allowed VARCHAR2(20) NOT NULL," +
                    "  status VARCHAR2(20) DEFAULT 'AVAILABLE' NOT NULL" +
                    ")");

            createTableIfNotExists("PARKING_RATES",
                    "CREATE TABLE parking_rates (" +
                    "  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                    "  vehicle_type VARCHAR2(20) NOT NULL UNIQUE," +
                    "  rate_per_hour NUMBER(10, 2) NOT NULL," +
                    "  status VARCHAR2(20) DEFAULT 'ACTIVE' NOT NULL," +
                    "  created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL," +
                    "  updated_at TIMESTAMP DEFAULT SYSTIMESTAMP" +
                    ")");

            createTableIfNotExists("PARKING_RECORDS",
                    "CREATE TABLE parking_records (" +
                    "  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                    "  vehicle_id NUMBER NOT NULL REFERENCES vehicles(id)," +
                    "  slot_id NUMBER NOT NULL REFERENCES parking_slots(id)," +
                    "  entry_time TIMESTAMP NOT NULL," +
                    "  exit_time TIMESTAMP," +
                    "  duration_minutes NUMBER," +
                    "  parking_fee NUMBER(10, 2)," +
                    "  status VARCHAR2(20) DEFAULT 'PARKED' NOT NULL," +
                    "  created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL" +
                    ")");

            createTableIfNotExists("PAYMENTS",
                    "CREATE TABLE payments (" +
                    "  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                    "  parking_record_id NUMBER NOT NULL REFERENCES parking_records(id)," +
                    "  amount NUMBER(10, 2) NOT NULL," +
                    "  payment_status VARCHAR2(20) DEFAULT 'PAID' NOT NULL," +
                    "  payment_method VARCHAR2(30) NOT NULL," +
                    "  paid_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL," +
                    "  transaction_id VARCHAR2(100) NOT NULL UNIQUE" +
                    ")");

            createTableIfNotExists("AUDIT_LOGS",
                    "CREATE TABLE audit_logs (" +
                    "  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                    "  user_id NUMBER," +
                    "  user_email VARCHAR2(100)," +
                    "  action VARCHAR2(100) NOT NULL," +
                    "  description VARCHAR2(1000)," +
                    "  timestamp TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL" +
                    ")");

            createTableIfNotExists("ANPR_RECORDS",
                    "CREATE TABLE anpr_records (" +
                    "  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                    "  vehicle_number VARCHAR2(30) NOT NULL," +
                    "  confidence VARCHAR2(20) NOT NULL," +
                    "  recognition_status VARCHAR2(30) NOT NULL," +
                    "  source VARCHAR2(50) DEFAULT 'MOCK' NOT NULL," +
                    "  captured_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL" +
                    ")");

            log.info("Database schema verification and initialization completed successfully.");
        } catch (Exception e) {
            log.warn("Database schema auto-creation encountered notice (tables may already exist): {}", e.getMessage());
        }
    }

    private void createTableIfNotExists(String tableName, String ddl) {
        if (!tableExists(tableName)) {
            log.info("Creating table: {}", tableName);
            jdbcTemplate.execute(ddl);
        } else {
            log.debug("Table already exists: {}", tableName);
        }
    }

    private boolean tableExists(String tableName) {
        try {
            jdbcTemplate.execute("SELECT 1 FROM " + tableName + " WHERE 1=0");
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
