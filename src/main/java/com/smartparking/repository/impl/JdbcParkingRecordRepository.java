package com.smartparking.repository.impl;

import com.smartparking.entity.ParkingRecord;
import com.smartparking.entity.ParkingSlot;
import com.smartparking.entity.Vehicle;
import com.smartparking.repository.ParkingRecordRepository;
import com.smartparking.util.ParkingRecordStatus;
import com.smartparking.util.SlotStatus;
import com.smartparking.util.VehicleType;
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
public class JdbcParkingRecordRepository implements ParkingRecordRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String BASE_SELECT =
            "SELECT pr.id, pr.entry_time, pr.exit_time, pr.duration_minutes, pr.parking_fee, pr.status, pr.created_at, " +
            "v.id AS v_id, v.vehicle_number AS v_number, v.vehicle_type AS v_type, v.created_at AS v_created_at, " +
            "ps.id AS ps_id, ps.slot_number AS ps_number, ps.vehicle_type_allowed AS ps_type, ps.status AS ps_status " +
            "FROM parking_records pr " +
            "JOIN vehicles v ON pr.vehicle_id = v.id " +
            "JOIN parking_slots ps ON pr.slot_id = ps.id ";

    private final RowMapper<ParkingRecord> recordRowMapper = (rs, rowNum) -> {
        Timestamp vCreatedTs = rs.getTimestamp("v_created_at");
        Vehicle v = new Vehicle(
                rs.getLong("v_id"),
                rs.getString("v_number"),
                VehicleType.valueOf(rs.getString("v_type")),
                vCreatedTs != null ? vCreatedTs.toLocalDateTime() : null
        );

        ParkingSlot ps = new ParkingSlot(
                rs.getLong("ps_id"),
                rs.getString("ps_number"),
                VehicleType.valueOf(rs.getString("ps_type")),
                SlotStatus.valueOf(rs.getString("ps_status"))
        );

        Timestamp entryTs = rs.getTimestamp("entry_time");
        Timestamp exitTs = rs.getTimestamp("exit_time");
        Timestamp createdTs = rs.getTimestamp("created_at");
        long dur = rs.getLong("duration_minutes");
        Long durationMinutes = rs.wasNull() ? null : dur;

        return new ParkingRecord(
                rs.getLong("id"),
                v,
                ps,
                entryTs != null ? entryTs.toLocalDateTime() : null,
                exitTs != null ? exitTs.toLocalDateTime() : null,
                durationMinutes,
                rs.getBigDecimal("parking_fee"),
                ParkingRecordStatus.valueOf(rs.getString("status")),
                createdTs != null ? createdTs.toLocalDateTime() : null
        );
    };

    @Override
    public Optional<ParkingRecord> findById(Long id) {
        String sql = BASE_SELECT + "WHERE pr.id = ?";
        try {
            ParkingRecord record = jdbcTemplate.queryForObject(sql, recordRowMapper, id);
            return Optional.ofNullable(record);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ParkingRecord> findAll() {
        String sql = BASE_SELECT + "ORDER BY pr.id ASC";
        return jdbcTemplate.query(sql, recordRowMapper);
    }

    @Override
    public ParkingRecord save(ParkingRecord record) {
        if (record.getId() == null) {
            String sql = "INSERT INTO parking_records (vehicle_id, slot_id, entry_time, exit_time, duration_minutes, parking_fee, status, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            LocalDateTime now = record.getCreatedAt() != null ? record.getCreatedAt() : LocalDateTime.now();
            record.setCreatedAt(now);

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, record.getVehicle().getId());
                ps.setLong(2, record.getSlot().getId());
                ps.setTimestamp(3, Timestamp.valueOf(record.getEntryTime()));
                ps.setTimestamp(4, record.getExitTime() != null ? Timestamp.valueOf(record.getExitTime()) : null);
                if (record.getDurationMinutes() != null) {
                    ps.setLong(5, record.getDurationMinutes());
                } else {
                    ps.setNull(5, java.sql.Types.NUMERIC);
                }
                ps.setBigDecimal(6, record.getParkingFee());
                ps.setString(7, record.getStatus().name());
                ps.setTimestamp(8, Timestamp.valueOf(now));
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
            String sql = "UPDATE parking_records SET vehicle_id = ?, slot_id = ?, entry_time = ?, exit_time = ?, " +
                    "duration_minutes = ?, parking_fee = ?, status = ? WHERE id = ?";
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setLong(1, record.getVehicle().getId());
                ps.setLong(2, record.getSlot().getId());
                ps.setTimestamp(3, Timestamp.valueOf(record.getEntryTime()));
                ps.setTimestamp(4, record.getExitTime() != null ? Timestamp.valueOf(record.getExitTime()) : null);
                if (record.getDurationMinutes() != null) {
                    ps.setLong(5, record.getDurationMinutes());
                } else {
                    ps.setNull(5, java.sql.Types.NUMERIC);
                }
                ps.setBigDecimal(6, record.getParkingFee());
                ps.setString(7, record.getStatus().name());
                ps.setLong(8, record.getId());
                return ps;
            });
            return record;
        }
    }

    @Override
    public Optional<ParkingRecord> findByVehicleVehicleNumberAndStatus(String vehicleNumber, ParkingRecordStatus status) {
        String sql = BASE_SELECT + "WHERE UPPER(v.vehicle_number) = UPPER(?) AND pr.status = ?";
        try {
            ParkingRecord record = jdbcTemplate.queryForObject(sql, recordRowMapper, vehicleNumber, status.name());
            return Optional.ofNullable(record);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ParkingRecord> findByStatus(ParkingRecordStatus status) {
        String sql = BASE_SELECT + "WHERE pr.status = ?";
        return jdbcTemplate.query(sql, recordRowMapper, status.name());
    }

    @Override
    public List<ParkingRecord> findByStatusOrderByEntryTimeDesc(ParkingRecordStatus status) {
        String sql = BASE_SELECT + "WHERE pr.status = ? ORDER BY pr.entry_time DESC";
        return jdbcTemplate.query(sql, recordRowMapper, status.name());
    }

    @Override
    public List<ParkingRecord> findAllByOrderByEntryTimeDesc() {
        String sql = BASE_SELECT + "ORDER BY pr.entry_time DESC";
        return jdbcTemplate.query(sql, recordRowMapper);
    }

    @Override
    public List<ParkingRecord> findByVehicleVehicleNumberOrderByEntryTimeDesc(String vehicleNumber) {
        String sql = BASE_SELECT + "WHERE UPPER(v.vehicle_number) = UPPER(?) ORDER BY pr.entry_time DESC";
        return jdbcTemplate.query(sql, recordRowMapper, vehicleNumber);
    }

    @Override
    public long countByStatus(ParkingRecordStatus status) {
        String sql = "SELECT COUNT(1) FROM parking_records WHERE status = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, status.name());
        return count != null ? count : 0L;
    }

    @Override
    public long countByEntryTimeBetween(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT COUNT(1) FROM parking_records WHERE entry_time BETWEEN ? AND ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, Timestamp.valueOf(start), Timestamp.valueOf(end));
        return count != null ? count : 0L;
    }

    @Override
    public long countByExitTimeBetween(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT COUNT(1) FROM parking_records WHERE exit_time BETWEEN ? AND ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, Timestamp.valueOf(start), Timestamp.valueOf(end));
        return count != null ? count : 0L;
    }

    @Override
    public List<ParkingRecord> findRecordsAfter(LocalDateTime startTime) {
        String sql = BASE_SELECT + "WHERE pr.entry_time >= ? ORDER BY pr.entry_time DESC";
        return jdbcTemplate.query(sql, recordRowMapper, Timestamp.valueOf(startTime));
    }

    @Override
    public List<ParkingRecord> findExitedBetween(LocalDateTime start, LocalDateTime end) {
        String sql = BASE_SELECT + "WHERE pr.status = 'EXITED' AND pr.exit_time BETWEEN ? AND ? ORDER BY pr.exit_time DESC";
        return jdbcTemplate.query(sql, recordRowMapper, Timestamp.valueOf(start), Timestamp.valueOf(end));
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(1) FROM parking_records";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}
