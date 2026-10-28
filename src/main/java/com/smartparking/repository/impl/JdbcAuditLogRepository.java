package com.smartparking.repository.impl;

import com.smartparking.entity.AuditLog;
import com.smartparking.repository.AuditLogRepository;
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
public class JdbcAuditLogRepository implements AuditLogRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<AuditLog> logRowMapper = (rs, rowNum) -> {
        long uId = rs.getLong("user_id");
        Long userId = rs.wasNull() ? null : uId;
        Timestamp ts = rs.getTimestamp("timestamp");
        return new AuditLog(
                rs.getLong("id"),
                userId,
                rs.getString("user_email"),
                rs.getString("action"),
                rs.getString("description"),
                ts != null ? ts.toLocalDateTime() : null
        );
    };

    @Override
    public Optional<AuditLog> findById(Long id) {
        String sql = "SELECT id, user_id, user_email, action, description, timestamp FROM audit_logs WHERE id = ?";
        try {
            AuditLog log = jdbcTemplate.queryForObject(sql, logRowMapper, id);
            return Optional.ofNullable(log);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<AuditLog> findAll() {
        String sql = "SELECT id, user_id, user_email, action, description, timestamp FROM audit_logs ORDER BY id ASC";
        return jdbcTemplate.query(sql, logRowMapper);
    }

    @Override
    public AuditLog save(AuditLog log) {
        if (log.getId() == null) {
            String sql = "INSERT INTO audit_logs (user_id, user_email, action, description, timestamp) VALUES (?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            LocalDateTime now = log.getTimestamp() != null ? log.getTimestamp() : LocalDateTime.now();
            log.setTimestamp(now);

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                if (log.getUserId() != null) {
                    ps.setLong(1, log.getUserId());
                } else {
                    ps.setNull(1, java.sql.Types.NUMERIC);
                }
                ps.setString(2, log.getUserEmail());
                ps.setString(3, log.getAction());
                ps.setString(4, log.getDescription());
                ps.setTimestamp(5, Timestamp.valueOf(now));
                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            if (key != null) {
                log.setId(key.longValue());
            } else if (keyHolder.getKeys() != null && !keyHolder.getKeys().isEmpty()) {
                Object val = keyHolder.getKeys().values().iterator().next();
                if (val instanceof Number num) {
                    log.setId(num.longValue());
                }
            }
            return log;
        } else {
            String sql = "UPDATE audit_logs SET user_id = ?, user_email = ?, action = ?, description = ? WHERE id = ?";
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql);
                if (log.getUserId() != null) {
                    ps.setLong(1, log.getUserId());
                } else {
                    ps.setNull(1, java.sql.Types.NUMERIC);
                }
                ps.setString(2, log.getUserEmail());
                ps.setString(3, log.getAction());
                ps.setString(4, log.getDescription());
                ps.setLong(5, log.getId());
                return ps;
            });
            return log;
        }
    }

    @Override
    public List<AuditLog> findAllByOrderByTimestampDesc() {
        String sql = "SELECT id, user_id, user_email, action, description, timestamp FROM audit_logs ORDER BY timestamp DESC";
        return jdbcTemplate.query(sql, logRowMapper);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(1) FROM audit_logs";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}
