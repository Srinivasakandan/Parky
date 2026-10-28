package com.smartparking.repository.impl;

import com.smartparking.entity.ANPRRecord;
import com.smartparking.repository.ANPRRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcANPRRecordRepository implements ANPRRecordRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<ANPRRecord> recordRowMapper = (rs, rowNum) -> {
        Timestamp ts = rs.getTimestamp("captured_at");
        return new ANPRRecord(
                rs.getLong("id"),
                rs.getString("vehicle_number"),
                rs.getString("confidence"),
                rs.getString("recognition_status"),
                rs.getString("source"),
                ts != null ? ts.toLocalDateTime() : null
        );
    };

    @Override
    public Optional<ANPRRecord> findById(Long id) {
        String sql = "SELECT id, vehicle_number, confidence, recognition_status, source, captured_at FROM anpr_records WHERE id = ?";
        try {
            ANPRRecord record = jdbcTemplate.queryForObject(sql, recordRowMapper, id);
            return Optional.ofNullable(record);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ANPRRecord> findAll() {
        String sql = "SELECT id, vehicle_number, confidence, recognition_status, source, captured_at FROM anpr_records ORDER BY id ASC";
        return jdbcTemplate.query(sql, recordRowMapper);
    }

    @Override
    public ANPRRecord save(ANPRRecord record) {
        if (record.getId() == null) {
            String sql = "INSERT INTO anpr_records (vehicle_number, confidence, recognition_status, source, captured_at) VALUES (?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            LocalDateTime now = record.getCapturedAt() != null ? record.getCapturedAt() : LocalDateTime.now();
            record.setCapturedAt(now);

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, record.getVehicleNumber());
                ps.setString(2, record.getConfidence());
                ps.setString(3, record.getRecognitionStatus());
                ps.setString(4, record.getSource());
                ps.setTimestamp(5, Timestamp.valueOf(now));
                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            if (key != null) {
                record.setId(key.longValue());
            } else if (keyHolder.getKeys() != null && !keyHolder.getKeys().isEmpty()) {
                Object val = keyHolder.getKeys().values().iterator().next();
                if (val instanceof Number num) {
                    record.setId(num.longValue());
                }
            }
            return record;
        } else {
            String sql = "UPDATE anpr_records SET vehicle_number = ?, confidence = ?, recognition_status = ?, source = ? WHERE id = ?";
            jdbcTemplate.update(sql, record.getVehicleNumber(), record.getConfidence(),
                    record.getRecognitionStatus(), record.getSource(), record.getId());
            return record;
        }
    }

    @Override
    public List<ANPRRecord> findAllByOrderByCapturedAtDesc() {
        String sql = "SELECT id, vehicle_number, confidence, recognition_status, source, captured_at FROM anpr_records ORDER BY captured_at DESC";
        return jdbcTemplate.query(sql, recordRowMapper);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(1) FROM anpr_records";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}
