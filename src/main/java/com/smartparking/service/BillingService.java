package com.smartparking.service;

import com.smartparking.dto.BillDto;
import com.smartparking.entity.ParkingRate;
import com.smartparking.entity.ParkingRecord;
import com.smartparking.entity.Payment;
import com.smartparking.exception.InvalidParkingRateException;
import com.smartparking.repository.ParkingRateRepository;
import com.smartparking.repository.PaymentRepository;
import com.smartparking.util.VehicleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class BillingService {

    @Autowired
    private ParkingRateRepository parkingRateRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    public BigDecimal getRateForVehicleType(VehicleType vehicleType) {
        return parkingRateRepository.findByVehicleType(vehicleType)
                .map(ParkingRate::getRatePerHour)
                .orElseGet(() -> {
                    // Fallback rates if not initialized
                    switch (vehicleType) {
                        case BIKE: return BigDecimal.valueOf(20.00);
                        case CAR: return BigDecimal.valueOf(30.00);
                        case SUV: return BigDecimal.valueOf(40.00);
                        case TRUCK: return BigDecimal.valueOf(50.00);
                        default: return BigDecimal.valueOf(30.00);
                    }
                });
    }

    public long calculateDurationMinutes(LocalDateTime entryTime, LocalDateTime exitTime) {
        if (entryTime == null || exitTime == null) return 0;
        Duration duration = Duration.between(entryTime, exitTime);
        long minutes = duration.toMinutes();
        return Math.max(minutes, 1); // Minimum 1 minute
    }

    public BigDecimal calculateFee(VehicleType vehicleType, long durationMinutes) {
        BigDecimal rate = getRateForVehicleType(vehicleType);
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidParkingRateException("Invalid rate configured for vehicle type: " + vehicleType);
        }

        // Standard billing rule: minimum 1 hour, ceil(minutes / 60)
        long hours = (long) Math.ceil(durationMinutes / 60.0);
        if (hours < 1) {
            hours = 1;
        }

        return rate.multiply(BigDecimal.valueOf(hours)).setScale(2, RoundingMode.HALF_UP);
    }

    public BillDto generateBillDto(ParkingRecord record) {
        LocalDateTime exitTime = record.getExitTime() != null ? record.getExitTime() : LocalDateTime.now();
        long durationMins = calculateDurationMinutes(record.getEntryTime(), exitTime);
        BigDecimal ratePerHour = getRateForVehicleType(record.getVehicle().getVehicleType());
        BigDecimal fee = record.getParkingFee() != null ? record.getParkingFee() : calculateFee(record.getVehicle().getVehicleType(), durationMins);

        BillDto dto = new BillDto();
        dto.setParkingRecordId(record.getId());
        dto.setVehicleNumber(record.getVehicle().getVehicleNumber());
        dto.setVehicleType(record.getVehicle().getVehicleType());
        dto.setSlotNumber(record.getSlot().getSlotNumber());
        dto.setEntryTime(record.getEntryTime());
        dto.setExitTime(exitTime);
        dto.setDurationMinutes(durationMins);

        long hours = durationMins / 60;
        long mins = durationMins % 60;
        dto.setDurationFormatted(hours > 0 ? hours + " hr " + mins + " mins" : mins + " mins");
        dto.setRatePerHour(ratePerHour);
        dto.setParkingFee(fee);

        paymentRepository.findByParkingRecordId(record.getId()).ifPresent(payment -> {
            dto.setPaymentStatus(payment.getPaymentStatus());
            dto.setPaymentMethod(payment.getPaymentMethod());
            dto.setTransactionId(payment.getTransactionId());
        });

        return dto;
    }
}
