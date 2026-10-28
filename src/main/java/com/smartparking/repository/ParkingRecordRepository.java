package com.smartparking.repository;

import com.smartparking.entity.ParkingRecord;
import com.smartparking.util.ParkingRecordStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ParkingRecordRepository {
    Optional<ParkingRecord> findById(Long id);
    List<ParkingRecord> findAll();
    ParkingRecord save(ParkingRecord record);
    Optional<ParkingRecord> findByVehicleVehicleNumberAndStatus(String vehicleNumber, ParkingRecordStatus status);
    List<ParkingRecord> findByStatus(ParkingRecordStatus status);
    List<ParkingRecord> findByStatusOrderByEntryTimeDesc(ParkingRecordStatus status);
    List<ParkingRecord> findAllByOrderByEntryTimeDesc();
    List<ParkingRecord> findByVehicleVehicleNumberOrderByEntryTimeDesc(String vehicleNumber);
    long countByStatus(ParkingRecordStatus status);
    long countByEntryTimeBetween(LocalDateTime start, LocalDateTime end);
    long countByExitTimeBetween(LocalDateTime start, LocalDateTime end);
    List<ParkingRecord> findRecordsAfter(LocalDateTime startTime);
    List<ParkingRecord> findExitedBetween(LocalDateTime start, LocalDateTime end);
    long count();
}
