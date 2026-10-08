package com.smartparking.service;

import com.smartparking.dto.PaymentRequestDto;
import com.smartparking.entity.ParkingRecord;
import com.smartparking.entity.Payment;
import com.smartparking.exception.PaymentException;
import com.smartparking.repository.ParkingRecordRepository;
import com.smartparking.repository.PaymentRepository;
import com.smartparking.util.PaymentStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ParkingRecordRepository parkingRecordRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    public Payment processPayment(PaymentRequestDto requestDto, String performedByEmail) {
        ParkingRecord record = parkingRecordRepository.findById(requestDto.getParkingRecordId())
                .orElseThrow(() -> new PaymentException("Parking record not found with ID: " + requestDto.getParkingRecordId()));

        if (paymentRepository.findByParkingRecordId(record.getId()).isPresent()) {
            return paymentRepository.findByParkingRecordId(record.getId()).get();
        }

        String transactionId = "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Payment payment = new Payment();
        payment.setParkingRecord(record);
        payment.setAmount(requestDto.getAmount());
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaymentMethod(requestDto.getPaymentMethod());
        payment.setPaidAt(LocalDateTime.now());
        payment.setTransactionId(transactionId);

        Payment saved = paymentRepository.save(payment);
        auditLogService.logAction(null, performedByEmail, "PAYMENT", "Payment of ₹" + saved.getAmount() + " processed for vehicle " + record.getVehicle().getVehicleNumber() + " via " + saved.getPaymentMethod());

        return saved;
    }
}
