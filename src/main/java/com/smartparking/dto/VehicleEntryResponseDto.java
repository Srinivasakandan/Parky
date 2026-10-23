package com.smartparking.dto;

import java.time.LocalDateTime;

/**
 * DTO returned after a successful vehicle entry registration.
 * Contains the minimal fields the frontend needs to display a confirmation.
 */
public class VehicleEntryResponseDto {

    private Long recordId;
    private String vehicleNumber;
    private String vehicleType;
    private String slotNumber;
    private LocalDateTime entryTime;
    private String status;

    public VehicleEntryResponseDto() {}

    public VehicleEntryResponseDto(Long recordId, String vehicleNumber, String vehicleType,
                                   String slotNumber, LocalDateTime entryTime, String status) {
        this.recordId = recordId;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.slotNumber = slotNumber;
        this.entryTime = entryTime;
        this.status = status;
    }

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }

    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
