package com.smartparking.service;

import com.smartparking.dto.BillDto;
import com.smartparking.dto.CustomerSearchResultDto;
import com.smartparking.dto.PaymentRequestDto;
import com.smartparking.dto.VehicleEntryRequestDto;
import com.smartparking.dto.VehicleExitRequestDto;
import com.smartparking.entity.ParkingRecord;
import com.smartparking.entity.ParkingSlot;
import com.smartparking.entity.Payment;
import com.smartparking.entity.Vehicle;
import com.smartparking.exception.VehicleAlreadyParkedException;
import com.smartparking.exception.VehicleNotFoundException;
import com.smartparking.repository.ParkingRecordRepository;
import com.smartparking.util.ParkingRecordStatus;
import com.smartparking.util.SlotStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ParkingService {

    @Autowired
    private ParkingRecordRepository parkingRecordRepository;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private ParkingSlotService parkingSlotService;

    @Autowired
    private BillingService billingService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    public ParkingRecord registerVehicleEntry(VehicleEntryRequestDto request, String performedByEmail) {
        String normalizedNumber = request.getVehicleNumber().trim().toUpperCase();

        // 1. Business Rule: Check if vehicle is already parked
        Optional<ParkingRecord> existingActive = parkingRecordRepository.findByVehicleVehicleNumberAndStatus(
                normalizedNumber, ParkingRecordStatus.PARKED);
        if (existingActive.isPresent()) {
            throw new VehicleAlreadyParkedException("Vehicle " + normalizedNumber + " is already currently parked in slot "
                    + existingActive.get().getSlot().getSlotNumber());
        }

        // 2. Find or create vehicle
        Vehicle vehicle = vehicleService.findOrCreateVehicle(normalizedNumber, request.getVehicleType());

        // 3. Find available slot
        ParkingSlot slot = parkingSlotService.findAvailableSlot(request.getVehicleType(), request.getPreferredSlotNumber());

        // 4. Create record with automatically generated entry time
        LocalDateTime entryTime = LocalDateTime.now();
        ParkingRecord record = new ParkingRecord(vehicle, slot, entryTime, ParkingRecordStatus.PARKED);
        ParkingRecord savedRecord = parkingRecordRepository.save(record);

        // 5. Slot status update (AVAILABLE -> OCCUPIED) is handled automatically by DB trigger 'trg_after_parking_entry'
        slot.setStatus(SlotStatus.OCCUPIED);

        // 6. Log Audit
        auditLogService.logAction(null, performedByEmail, "VEHICLE_ENTRY",
                "Vehicle " + normalizedNumber + " entered and assigned slot " + slot.getSlotNumber());

        return savedRecord;
    }

    @Transactional
    public BillDto processVehicleExit(VehicleExitRequestDto request, String performedByEmail) {
        String normalizedNumber = request.getVehicleNumber().trim().toUpperCase();

        // 1. Search active parking record
        ParkingRecord record = parkingRecordRepository.findByVehicleVehicleNumberAndStatus(
                normalizedNumber, ParkingRecordStatus.PARKED)
                .orElseThrow(() -> new VehicleNotFoundException("No active parking record found for vehicle: " + normalizedNumber));

        // 2. Generate exit time automatically
        LocalDateTime exitTime = LocalDateTime.now();
        record.setExitTime(exitTime);

        // 3. Calculate duration & fee
        long durationMins = billingService.calculateDurationMinutes(record.getEntryTime(), exitTime);
        record.setDurationMinutes(durationMins);

        BigDecimal fee = billingService.calculateFee(record.getVehicle().getVehicleType(), durationMins);
        record.setParkingFee(fee);

        // 4. Update status = EXITED
        record.setStatus(ParkingRecordStatus.EXITED);
        ParkingRecord updatedRecord = parkingRecordRepository.save(record);

        // 5. Process payment simulation
        PaymentRequestDto paymentRequest = new PaymentRequestDto(updatedRecord.getId(), fee, request.getPaymentMethod());
        Payment payment = paymentService.processPayment(paymentRequest, performedByEmail);

        // 6. Slot release (OCCUPIED -> AVAILABLE) is handled automatically by DB trigger 'trg_after_parking_exit'
        record.getSlot().setStatus(SlotStatus.AVAILABLE);

        // 7. Log Audit
        auditLogService.logAction(null, performedByEmail, "VEHICLE_EXIT",
                "Vehicle " + normalizedNumber + " exited from slot " + record.getSlot().getSlotNumber() + ". Fee: ₹" + fee);

        return billingService.generateBillDto(updatedRecord);
    }

    public List<ParkingRecord> getCurrentlyParkedVehicles() {
        return parkingRecordRepository.findByStatusOrderByEntryTimeDesc(ParkingRecordStatus.PARKED);
    }

    public List<ParkingRecord> getExitedVehicles() {
        return parkingRecordRepository.findByStatusOrderByEntryTimeDesc(ParkingRecordStatus.EXITED);
    }

    public List<ParkingRecord> getAllParkingHistory() {
        return parkingRecordRepository.findAllByOrderByEntryTimeDesc();
    }

    public CustomerSearchResultDto searchVehicleByCustomer(String vehicleNumber) {
        String normalizedNumber = vehicleNumber.trim().toUpperCase();

        List<ParkingRecord> records = parkingRecordRepository.findByVehicleVehicleNumberOrderByEntryTimeDesc(normalizedNumber);
        if (records.isEmpty()) {
            throw new VehicleNotFoundException("No parking records found for vehicle: " + normalizedNumber);
        }

        ParkingRecord latest = records.get(0);
        CustomerSearchResultDto dto = new CustomerSearchResultDto();
        dto.setVehicleNumber(latest.getVehicle().getVehicleNumber());
        dto.setVehicleType(latest.getVehicle().getVehicleType());
        dto.setParkingSlot(latest.getSlot().getSlotNumber());
        dto.setEntryTime(latest.getEntryTime());
        dto.setStatus(latest.getStatus());

        if (latest.getStatus() == ParkingRecordStatus.PARKED) {
            LocalDateTime now = LocalDateTime.now();
            long mins = billingService.calculateDurationMinutes(latest.getEntryTime(), now);
            dto.setDurationMinutes(mins);
            long hours = mins / 60;
            long m = mins % 60;
            dto.setDurationFormatted(hours > 0 ? hours + " hr " + m + " mins" : m + " mins");
            dto.setParkingFee(billingService.calculateFee(latest.getVehicle().getVehicleType(), mins));
        } else {
            dto.setExitTime(latest.getExitTime());
            dto.setDurationMinutes(latest.getDurationMinutes() != null ? latest.getDurationMinutes() : 0);
            long mins = dto.getDurationMinutes();
            long hours = mins / 60;
            long m = mins % 60;
            dto.setDurationFormatted(hours > 0 ? hours + " hr " + m + " mins" : m + " mins");
            dto.setParkingFee(latest.getParkingFee());

            billingService.generateBillDto(latest); // pull payment details
            BillDto bill = billingService.generateBillDto(latest);
            dto.setPaymentStatus(bill.getPaymentStatus());
            dto.setPaymentMethod(bill.getPaymentMethod());
            dto.setTransactionId(bill.getTransactionId());
        }

        return dto;
    }
}
