package com.smartparking.repository.impl;

import com.smartparking.entity.ParkingRecord;
import com.smartparking.entity.ParkingSlot;
import com.smartparking.entity.Payment;
import com.smartparking.entity.Vehicle;
import com.smartparking.repository.PaymentRepository;
import com.smartparking.util.ParkingRecordStatus;
import com.smartparking.util.PaymentMethod;
import com.smartparking.util.PaymentStatus;
import com.smartparking.util.SlotStatus;
import com.smartparking.util.VehicleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcPaymentRepository implements PaymentRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String BASE_SELECT =
            "SELECT p.id, p.amount, p.payment_status, p.payment_method, p.paid_at, p.transaction_id, " +
            "pr.id AS pr_id, pr.entry_time AS pr_entry_time, pr.exit_time AS pr_exit_time, " +
            "pr.duration_minutes AS pr_duration, pr.parking_fee AS pr_fee, pr.status AS pr_status, pr.created_at AS pr_created_at, " +
            "v.id AS v_id, v.vehicle_number AS v_number, v.vehicle_type AS v_type, v.created_at AS v_created_at, " +
            "ps.id AS ps_id, ps.slot_number AS ps_number, ps.vehicle_type_allowed AS ps_type, ps.status AS ps_status " +
            "FROM payments p " +
            "JOIN parking_records pr ON p.parking_record_id = pr.id " +
            "JOIN vehicles v ON pr.vehicle_id = v.id " +
            "JOIN parking_slots ps ON pr.slot_id = ps.id ";

    private final RowMapper<Payment> paymentRowMapper = (rs, rowNum) -> {
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

        Timestamp entryTs = rs.getTimestamp("pr_entry_time");
        Timestamp exitTs = rs.getTimestamp("pr_exit_time");
        Timestamp prCreatedTs = rs.getTimestamp("pr_created_at");
        long dur = rs.getLong("pr_duration");
        Long durationMinutes = rs.wasNull() ? null : dur;

        ParkingRecord pr = new ParkingRecord(
                rs.getLong("pr_id"),
                v,
                ps,
                entryTs != null ? entryTs.toLocalDateTime() : null,
                exitTs != null ? exitTs.toLocalDateTime() : null,
                durationMinutes,
                rs.getBigDecimal("pr_fee"),
                ParkingRecordStatus.valueOf(rs.getString("pr_status")),
                prCreatedTs != null ? prCreatedTs.toLocalDateTime() : null
        );

        Timestamp paidAtTs = rs.getTimestamp("paid_at");
        return new Payment(
                rs.getLong("id"),
                pr,
                rs.getBigDecimal("amount"),
                PaymentStatus.valueOf(rs.getString("payment_status")),
                PaymentMethod.valueOf(rs.getString("payment_method")),
                paidAtTs != null ? paidAtTs.toLocalDateTime() : null,
                rs.getString("transaction_id")
        );
    };

    @Override
    public Optional<Payment> findById(Long id) {
        String sql = BASE_SELECT + "WHERE p.id = ?";
        try {
            Payment payment = jdbcTemplate.queryForObject(sql, paymentRowMapper, id);
            return Optional.ofNullable(payment);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Payment> findAll() {
        String sql = BASE_SELECT + "ORDER BY p.id ASC";
        return jdbcTemplate.query(sql, paymentRowMapper);
    }

    @Override
    public Payment save(Payment payment) {
        if (payment.getId() == null) {
            String sql = "INSERT INTO payments (parking_record_id, amount, payment_status, payment_method, paid_at, transaction_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            LocalDateTime now = payment.getPaidAt() != null ? payment.getPaidAt() : LocalDateTime.now();
            payment.setPaidAt(now);

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, payment.getParkingRecord().getId());
                ps.setBigDecimal(2, payment.getAmount());
                ps.setString(3, payment.getPaymentStatus().name());
                ps.setString(4, payment.getPaymentMethod().name());
                ps.setTimestamp(5, Timestamp.valueOf(now));
                ps.setString(6, payment.getTransactionId());
                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            if (key != null) {
                payment.setId(key.longValue());
            } else if (keyHolder.getKeys() != null && !keyHolder.getKeys().isEmpty()) {
                Object val = keyHolder.getKeys().values().iterator().next();
                if (val instanceof Number num) {
                    payment.setId(num.longValue());
                }
            }
            return payment;
        } else {
            String sql = "UPDATE payments SET parking_record_id = ?, amount = ?, payment_status = ?, payment_method = ?, " +
                    "paid_at = ?, transaction_id = ? WHERE id = ?";
            jdbcTemplate.update(sql, payment.getParkingRecord().getId(), payment.getAmount(),
                    payment.getPaymentStatus().name(), payment.getPaymentMethod().name(),
                    Timestamp.valueOf(payment.getPaidAt()), payment.getTransactionId(), payment.getId());
            return payment;
        }
    }

    @Override
    public Optional<Payment> findByParkingRecordId(Long parkingRecordId) {
        String sql = BASE_SELECT + "WHERE p.parking_record_id = ?";
        try {
            Payment payment = jdbcTemplate.queryForObject(sql, paymentRowMapper, parkingRecordId);
            return Optional.ofNullable(payment);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Payment> findByPaymentStatus(PaymentStatus paymentStatus) {
        String sql = BASE_SELECT + "WHERE p.payment_status = ?";
        return jdbcTemplate.query(sql, paymentRowMapper, paymentStatus.name());
    }

    @Override
    public BigDecimal calculateRevenueBetween(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE payment_status = 'PAID' AND paid_at BETWEEN ? AND ?";
        BigDecimal total = jdbcTemplate.queryForObject(sql, BigDecimal.class, Timestamp.valueOf(start), Timestamp.valueOf(end));
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public List<Payment> findPaidPaymentsBetween(LocalDateTime start, LocalDateTime end) {
        String sql = BASE_SELECT + "WHERE p.payment_status = 'PAID' AND p.paid_at BETWEEN ? AND ? ORDER BY p.paid_at DESC";
        return jdbcTemplate.query(sql, paymentRowMapper, Timestamp.valueOf(start), Timestamp.valueOf(end));
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(1) FROM payments";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}
