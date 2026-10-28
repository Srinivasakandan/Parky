package com.smartparking.repository.impl;

import com.smartparking.entity.ParkingSlot;
import com.smartparking.repository.ParkingSlotRepository;
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
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcParkingSlotRepository implements ParkingSlotRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<ParkingSlot> slotRowMapper = (rs, rowNum) -> new ParkingSlot(
            rs.getLong("id"),
            rs.getString("slot_number"),
            VehicleType.valueOf(rs.getString("vehicle_type_allowed")),
            SlotStatus.valueOf(rs.getString("status"))
    );

    @Override
    public Optional<ParkingSlot> findById(Long id) {
        String sql = "SELECT id, slot_number, vehicle_type_allowed, status FROM parking_slots WHERE id = ?";
        try {
            ParkingSlot slot = jdbcTemplate.queryForObject(sql, slotRowMapper, id);
            return Optional.ofNullable(slot);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ParkingSlot> findAll() {
        String sql = "SELECT id, slot_number, vehicle_type_allowed, status FROM parking_slots ORDER BY id ASC";
        return jdbcTemplate.query(sql, slotRowMapper);
    }

    @Override
    public ParkingSlot save(ParkingSlot slot) {
        if (slot.getId() == null) {
            String sql = "INSERT INTO parking_slots (slot_number, vehicle_type_allowed, status) VALUES (?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, slot.getSlotNumber());
                ps.setString(2, slot.getVehicleTypeAllowed().name());
                ps.setString(3, slot.getStatus().name());
                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            if (key != null) {
                slot.setId(key.longValue());
            } else if (keyHolder.getKeys() != null && !keyHolder.getKeys().isEmpty()) {
                Object val = keyHolder.getKeys().values().iterator().next();
                if (val instanceof Number num) {
                    slot.setId(num.longValue());
                }
            }
            return slot;
        } else {
            String sql = "UPDATE parking_slots SET slot_number = ?, vehicle_type_allowed = ?, status = ? WHERE id = ?";
            jdbcTemplate.update(sql, slot.getSlotNumber(), slot.getVehicleTypeAllowed().name(), slot.getStatus().name(), slot.getId());
            return slot;
        }
    }

    @Override
    public Optional<ParkingSlot> findBySlotNumber(String slotNumber) {
        String sql = "SELECT id, slot_number, vehicle_type_allowed, status FROM parking_slots WHERE UPPER(slot_number) = UPPER(?)";
        try {
            ParkingSlot slot = jdbcTemplate.queryForObject(sql, slotRowMapper, slotNumber);
            return Optional.ofNullable(slot);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ParkingSlot> findByStatus(SlotStatus status) {
        String sql = "SELECT id, slot_number, vehicle_type_allowed, status FROM parking_slots WHERE status = ?";
        return jdbcTemplate.query(sql, slotRowMapper, status.name());
    }

    @Override
    public List<ParkingSlot> findByVehicleTypeAllowedAndStatus(VehicleType vehicleTypeAllowed, SlotStatus status) {
        String sql = "SELECT id, slot_number, vehicle_type_allowed, status FROM parking_slots WHERE vehicle_type_allowed = ? AND status = ?";
        return jdbcTemplate.query(sql, slotRowMapper, vehicleTypeAllowed.name(), status.name());
    }

    @Override
    public List<ParkingSlot> findAllByOrderBySlotNumberAsc() {
        String sql = "SELECT id, slot_number, vehicle_type_allowed, status FROM parking_slots ORDER BY slot_number ASC";
        return jdbcTemplate.query(sql, slotRowMapper);
    }

    @Override
    public long countByStatus(SlotStatus status) {
        String sql = "SELECT COUNT(1) FROM parking_slots WHERE status = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, status.name());
        return count != null ? count : 0L;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(1) FROM parking_slots";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public void delete(ParkingSlot slot) {
        if (slot != null && slot.getId() != null) {
            String sql = "DELETE FROM parking_slots WHERE id = ?";
            jdbcTemplate.update(sql, slot.getId());
        }
    }
}
