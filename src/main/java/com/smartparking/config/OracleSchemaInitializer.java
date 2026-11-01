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
        log.info("Checking and initializing database tables and triggers...");
        try {
            String dbName = "";
            try (Connection conn = dataSource.getConnection()) {
                DatabaseMetaData meta = conn.getMetaData();
                dbName = meta.getDatabaseProductName();
            }

            log.info("Detected database engine: {}", dbName);

            if (dbName != null && dbName.toLowerCase().contains("mysql")) {
                initMySQLSchema();
            } else {
                initOracleOrH2Schema();
            }

            log.info("Database schema verification and initialization completed successfully.");
        } catch (Exception e) {
            log.warn("Database schema auto-creation encountered notice: {}", e.getMessage());
        }
    }

    private void initMySQLSchema() {
        createTableIfNotExists("users",
                "CREATE TABLE IF NOT EXISTS users (" +
                "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "  name VARCHAR(100) NOT NULL," +
                "  email VARCHAR(100) NOT NULL UNIQUE," +
                "  password VARCHAR(255) NOT NULL," +
                "  role VARCHAR(20) NOT NULL," +
                "  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'," +
                "  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ")");

        createTableIfNotExists("vehicles",
                "CREATE TABLE IF NOT EXISTS vehicles (" +
                "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "  vehicle_number VARCHAR(30) NOT NULL UNIQUE," +
                "  vehicle_type VARCHAR(20) NOT NULL," +
                "  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ")");

        createTableIfNotExists("parking_slots",
                "CREATE TABLE IF NOT EXISTS parking_slots (" +
                "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "  slot_number VARCHAR(20) NOT NULL UNIQUE," +
                "  vehicle_type_allowed VARCHAR(20) NOT NULL," +
                "  status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'" +
                ")");

        createTableIfNotExists("parking_rates",
                "CREATE TABLE IF NOT EXISTS parking_rates (" +
                "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "  vehicle_type VARCHAR(20) NOT NULL UNIQUE," +
                "  rate_per_hour DECIMAL(10, 2) NOT NULL," +
                "  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'," +
                "  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  updated_at DATETIME DEFAULT NULL" +
                ")");

        createTableIfNotExists("parking_records",
                "CREATE TABLE IF NOT EXISTS parking_records (" +
                "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "  vehicle_id BIGINT NOT NULL," +
                "  slot_id BIGINT NOT NULL," +
                "  entry_time DATETIME NOT NULL," +
                "  exit_time DATETIME DEFAULT NULL," +
                "  duration_minutes BIGINT DEFAULT NULL," +
                "  parking_fee DECIMAL(10, 2) DEFAULT NULL," +
                "  status VARCHAR(20) NOT NULL DEFAULT 'PARKED'," +
                "  entered_by VARCHAR(100) DEFAULT NULL," +
                "  exited_by VARCHAR(100) DEFAULT NULL," +
                "  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  CONSTRAINT fk_pr_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)," +
                "  CONSTRAINT fk_pr_slot FOREIGN KEY (slot_id) REFERENCES parking_slots(id)" +
                ")");

        createTableIfNotExists("payments",
                "CREATE TABLE IF NOT EXISTS payments (" +
                "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "  parking_record_id BIGINT NOT NULL," +
                "  amount DECIMAL(10, 2) NOT NULL," +
                "  payment_status VARCHAR(20) NOT NULL DEFAULT 'PAID'," +
                "  payment_method VARCHAR(30) NOT NULL," +
                "  paid_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  transaction_id VARCHAR(100) NOT NULL UNIQUE," +
                "  CONSTRAINT fk_pay_record FOREIGN KEY (parking_record_id) REFERENCES parking_records(id)" +
                ")");

        createTableIfNotExists("audit_logs",
                "CREATE TABLE IF NOT EXISTS audit_logs (" +
                "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "  user_id BIGINT DEFAULT NULL," +
                "  user_email VARCHAR(100) DEFAULT NULL," +
                "  action VARCHAR(100) NOT NULL," +
                "  description VARCHAR(1000) DEFAULT NULL," +
                "  timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ")");

        createTableIfNotExists("anpr_records",
                "CREATE TABLE IF NOT EXISTS anpr_records (" +
                "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "  vehicle_number VARCHAR(30) NOT NULL," +
                "  confidence VARCHAR(20) NOT NULL," +
                "  recognition_status VARCHAR(30) NOT NULL," +
                "  source VARCHAR(50) NOT NULL DEFAULT 'MOCK'," +
                "  captured_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ")");

        // Initialize triggers in MySQL
        initMySQLTriggers();
    }

    private void initMySQLTriggers() {
        try {
            jdbcTemplate.execute("DROP TRIGGER IF EXISTS trg_after_parking_entry");
            jdbcTemplate.execute(
                    "CREATE TRIGGER trg_after_parking_entry AFTER INSERT ON parking_records " +
                    "FOR EACH ROW BEGIN " +
                    "  IF NEW.status = 'PARKED' THEN " +
                    "    UPDATE parking_slots SET status = 'OCCUPIED' WHERE id = NEW.slot_id; " +
                    "  END IF; " +
                    "END"
            );

            jdbcTemplate.execute("DROP TRIGGER IF EXISTS trg_after_parking_exit");
            jdbcTemplate.execute(
                    "CREATE TRIGGER trg_after_parking_exit AFTER UPDATE ON parking_records " +
                    "FOR EACH ROW BEGIN " +
                    "  IF NEW.status IN ('EXITED', 'COMPLETED') AND OLD.status = 'PARKED' THEN " +
                    "    UPDATE parking_slots SET status = 'AVAILABLE' WHERE id = NEW.slot_id; " +
                    "  END IF; " +
                    "END"
            );
            log.info("MySQL automated triggers initialized.");
        } catch (Exception ex) {
            log.warn("Notice while initializing MySQL triggers: {}", ex.getMessage());
        }
    }

    private void initOracleOrH2Schema() {
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
                "  entered_by VARCHAR2(100)," +
                "  exited_by VARCHAR2(100)," +
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
