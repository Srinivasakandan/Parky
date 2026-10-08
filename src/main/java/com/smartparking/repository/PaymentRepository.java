package com.smartparking.repository;

import com.smartparking.entity.Payment;
import com.smartparking.util.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByParkingRecordId(Long parkingRecordId);

    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = 'PAID' AND p.paidAt BETWEEN :start AND :end")
    BigDecimal calculateRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'PAID' AND p.paidAt BETWEEN :start AND :end")
    List<Payment> findPaidPaymentsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
