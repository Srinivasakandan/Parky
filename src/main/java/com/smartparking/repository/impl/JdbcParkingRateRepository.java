package com.smartparking.repository.impl;

import com.smartparking.entity.ParkingRate;
import com.smartparking.repository.ParkingRateRepository;
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
public class JdbcParkingRateRepository implements ParkingRateRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<ParkingRate> rateRowMapper = (rs, rowNum) -> {
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        return new ParkingRate(
                rs.getLong("id"),
                VehicleType.valueOf(rs.getString("vehicle_type")),
                rs.getBigDecimal("rate_per_hour"),
                rs.getString("status"),
                createdTs != null ? createdTs.toLocalDateTime() : null,
                updatedTs != null ? updatedTs.toLocalDateTime() : null
        );
    };

    @Override
    public Optional<ParkingRate> findById(Long id) {
        String sql = "SELECT id, vehicle_type, rate_per_hour, status, created_at, updated_at FROM parking_rates WHERE id = ?";
        try {
            ParkingRate rate = jdbcTemplate.queryForObject(sql, rateRowMapper, id);
            return Optional.ofNullable(rate);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ParkingRate> findAll() {
        String sql = "SELECT id, vehicle_type, rate_per_hour, status, created_at, updated_at FROM parking_rates ORDER BY id ASC";
        return jdbcTemplate.query(sql, rateRowMapper);
    }

    @Override
    public ParkingRate save(ParkingRate parkingRate) {
        LocalDateTime now = LocalDateTime.now();
        if (parkingRate.getId() == null) {
            String sql = "INSERT INTO parking_rates (vehicle_type, rate_per_hour, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            LocalDateTime created = parkingRate.getCreatedAt() != null ? parkingRate.getCreatedAt() : now;
            parkingRate.setCreatedAt(created);
            parkingRate.setUpdatedAt(now);

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, parkingRate.getVehicleType().name());
                ps.setBigDecimal(2, parkingRate.getRatePerHour());
                ps.setString(3, parkingRate.getStatus());
                ps.setTimestamp(4, Timestamp.valueOf(created));
                ps.setTimestamp(5, Timestamp.valueOf(now));
                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            if (key != null) {
                parkingRate.setId(key.longValue());
            } else if (keyHolder.getKeys() != null && !keyHolder.getKeys().isEmpty()) {
                Object val = keyHolder.getKeys().values().iterator().next();
                if (val instanceof Number num) {
                    parkingRate.setId(num.longValue());
                }
            }
            return parkingRate;
        } else {
            String sql = "UPDATE parking_rates SET vehicle_type = ?, rate_per_hour = ?, status = ?, updated_at = ? WHERE id = ?";
            parkingRate.setUpdatedAt(now);
            jdbcTemplate.update(sql, parkingRate.getVehicleType().name(), parkingRate.getRatePerHour(),
                    parkingRate.getStatus(), Timestamp.valueOf(now), parkingRate.getId());
            return parkingRate;
        }
    }

    @Override
    public Optional<ParkingRate> findByVehicleType(VehicleType vehicleType) {
        String sql = "SELECT id, vehicle_type, rate_per_hour, status, created_at, updated_at FROM parking_rates WHERE vehicle_type = ?";
        try {
            ParkingRate rate = jdbcTemplate.queryForObject(sql, rateRowMapper, vehicleType.name());
            return Optional.ofNullable(rate);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(1) FROM parking_rates";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}
