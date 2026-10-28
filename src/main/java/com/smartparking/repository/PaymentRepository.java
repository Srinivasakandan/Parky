package com.smartparking.repository;

import com.smartparking.entity.Payment;
import com.smartparking.util.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Optional<Payment> findById(Long id);
    List<Payment> findAll();
    Payment save(Payment payment);
    Optional<Payment> findByParkingRecordId(Long parkingRecordId);
    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);
    BigDecimal calculateRevenueBetween(LocalDateTime start, LocalDateTime end);
    List<Payment> findPaidPaymentsBetween(LocalDateTime start, LocalDateTime end);
    long count();
}
