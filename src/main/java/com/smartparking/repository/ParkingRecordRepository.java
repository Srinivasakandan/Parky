package com.smartparking.repository;

import com.smartparking.entity.ParkingRecord;
import com.smartparking.util.ParkingRecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingRecordRepository extends JpaRepository<ParkingRecord, Long> {

    Optional<ParkingRecord> findByVehicleVehicleNumberAndStatus(String vehicleNumber, ParkingRecordStatus status);

    List<ParkingRecord> findByStatus(ParkingRecordStatus status);

    List<ParkingRecord> findByStatusOrderByEntryTimeDesc(ParkingRecordStatus status);

    List<ParkingRecord> findAllByOrderByEntryTimeDesc();

    List<ParkingRecord> findByVehicleVehicleNumberOrderByEntryTimeDesc(String vehicleNumber);

    long countByStatus(ParkingRecordStatus status);

    long countByEntryTimeBetween(LocalDateTime start, LocalDateTime end);

    long countByExitTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT pr FROM ParkingRecord pr WHERE pr.entryTime >= :startTime")
    List<ParkingRecord> findRecordsAfter(@Param("startTime") LocalDateTime startTime);

    @Query("SELECT pr FROM ParkingRecord pr WHERE pr.status = 'EXITED' AND pr.exitTime BETWEEN :start AND :end")
    List<ParkingRecord> findExitedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
