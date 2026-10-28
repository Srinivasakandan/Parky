package com.smartparking.repository.impl;

import com.smartparking.entity.Vehicle;
import com.smartparking.repository.VehicleRepository;
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
public class JdbcVehicleRepository implements VehicleRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Vehicle> vehicleRowMapper = (rs, rowNum) -> {
        Timestamp ts = rs.getTimestamp("created_at");
        return new Vehicle(
                rs.getLong("id"),
                rs.getString("vehicle_number"),
                VehicleType.valueOf(rs.getString("vehicle_type")),
                ts != null ? ts.toLocalDateTime() : null
        );
    };

    @Override
    public Optional<Vehicle> findById(Long id) {
        String sql = "SELECT id, vehicle_number, vehicle_type, created_at FROM vehicles WHERE id = ?";
        try {
            Vehicle vehicle = jdbcTemplate.queryForObject(sql, vehicleRowMapper, id);
            return Optional.ofNullable(vehicle);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Vehicle> findAll() {
        String sql = "SELECT id, vehicle_number, vehicle_type, created_at FROM vehicles ORDER BY id ASC";
        return jdbcTemplate.query(sql, vehicleRowMapper);
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        if (vehicle.getId() == null) {
            String sql = "INSERT INTO vehicles (vehicle_number, vehicle_type, created_at) VALUES (?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            LocalDateTime now = vehicle.getCreatedAt() != null ? vehicle.getCreatedAt() : LocalDateTime.now();
            vehicle.setCreatedAt(now);

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, vehicle.getVehicleNumber());
                ps.setString(2, vehicle.getVehicleType().name());
                ps.setTimestamp(3, Timestamp.valueOf(now));
                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            if (key != null) {
                vehicle.setId(key.longValue());
            } else if (keyHolder.getKeys() != null && !keyHolder.getKeys().isEmpty()) {
                Object val = keyHolder.getKeys().values().iterator().next();
                if (val instanceof Number num) {
                    vehicle.setId(num.longValue());
                }
            }
            return vehicle;
        } else {
            String sql = "UPDATE vehicles SET vehicle_number = ?, vehicle_type = ? WHERE id = ?";
            jdbcTemplate.update(sql, vehicle.getVehicleNumber(), vehicle.getVehicleType().name(), vehicle.getId());
            return vehicle;
        }
    }

    @Override
    public Optional<Vehicle> findByVehicleNumber(String vehicleNumber) {
        String sql = "SELECT id, vehicle_number, vehicle_type, created_at FROM vehicles WHERE vehicle_number = ?";
        try {
            Vehicle vehicle = jdbcTemplate.queryForObject(sql, vehicleRowMapper, vehicleNumber);
            return Optional.ofNullable(vehicle);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Vehicle> findByVehicleNumberIgnoreCase(String vehicleNumber) {
        String sql = "SELECT id, vehicle_number, vehicle_type, created_at FROM vehicles WHERE UPPER(vehicle_number) = UPPER(?)";
        try {
            Vehicle vehicle = jdbcTemplate.queryForObject(sql, vehicleRowMapper, vehicleNumber);
            return Optional.ofNullable(vehicle);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByVehicleNumber(String vehicleNumber) {
        String sql = "SELECT COUNT(1) FROM vehicles WHERE UPPER(vehicle_number) = UPPER(?)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, vehicleNumber);
        return count != null && count > 0;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(1) FROM vehicles";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}
