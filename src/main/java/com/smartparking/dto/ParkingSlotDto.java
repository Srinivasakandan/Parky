package com.smartparking.dto;

import com.smartparking.util.SlotStatus;
import com.smartparking.util.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ParkingSlotDto {

    private Long id;

    @NotBlank(message = "Slot number is required")
    private String slotNumber;

    @NotNull(message = "Allowed vehicle type is required")
    private VehicleType vehicleTypeAllowed;

    private SlotStatus status;

    public ParkingSlotDto() {}

    public ParkingSlotDto(Long id, String slotNumber, VehicleType vehicleTypeAllowed, SlotStatus status) {
        this.id = id;
        this.slotNumber = slotNumber;
        this.vehicleTypeAllowed = vehicleTypeAllowed;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }

    public VehicleType getVehicleTypeAllowed() { return vehicleTypeAllowed; }
    public void setVehicleTypeAllowed(VehicleType vehicleTypeAllowed) { this.vehicleTypeAllowed = vehicleTypeAllowed; }

    public SlotStatus getStatus() { return status; }
    public void setStatus(SlotStatus status) { this.status = status; }
}
