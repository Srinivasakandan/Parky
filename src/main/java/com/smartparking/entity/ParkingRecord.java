package com.smartparking.entity;

import com.smartparking.util.ParkingRecordStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ParkingRecord {

    private Long id;
    private Vehicle vehicle;
    private ParkingSlot slot;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Long durationMinutes;
    private BigDecimal parkingFee;
    private ParkingRecordStatus status = ParkingRecordStatus.PARKED;
    private LocalDateTime createdAt = LocalDateTime.now();

    public ParkingRecord() {}

    public ParkingRecord(Vehicle vehicle, ParkingSlot slot, LocalDateTime entryTime, ParkingRecordStatus status) {
        this.vehicle = vehicle;
        this.slot = slot;
        this.entryTime = entryTime;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public ParkingRecord(Long id, Vehicle vehicle, ParkingSlot slot, LocalDateTime entryTime, LocalDateTime exitTime,
                         Long durationMinutes, BigDecimal parkingFee, ParkingRecordStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.vehicle = vehicle;
        this.slot = slot;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.durationMinutes = durationMinutes;
        this.parkingFee = parkingFee;
        this.status = status;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public ParkingSlot getSlot() { return slot; }
    public void setSlot(ParkingSlot slot) { this.slot = slot; }

    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }

    public LocalDateTime getExitTime() { return exitTime; }
    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }

    public Long getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Long durationMinutes) { this.durationMinutes = durationMinutes; }

    public BigDecimal getParkingFee() { return parkingFee; }
    public void setParkingFee(BigDecimal parkingFee) { this.parkingFee = parkingFee; }

    public ParkingRecordStatus getStatus() { return status; }
    public void setStatus(ParkingRecordStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
