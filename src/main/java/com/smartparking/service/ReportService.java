package com.smartparking.service;

import com.smartparking.entity.ParkingRecord;
import com.smartparking.entity.Payment;
import com.smartparking.repository.ParkingRecordRepository;
import com.smartparking.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class ReportService {

    @Autowired
    private ParkingRecordRepository parkingRecordRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    public Map<String, Object> generateSummaryReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<ParkingRecord> exitedRecords = parkingRecordRepository.findExitedBetween(start, end);
        List<Payment> paidPayments = paymentRepository.findPaidPaymentsBetween(start, end);

        BigDecimal totalRevenue = paidPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> report = new HashMap<>();
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalTransactions", paidPayments.size());
        report.put("totalExitedVehicles", exitedRecords.size());
        report.put("totalRevenue", totalRevenue);
        report.put("generatedAt", LocalDateTime.now());

        return report;
    }
}
