package com.smartparking.dto;

import com.smartparking.util.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class VehicleEntryRequestDto {

    @NotBlank(message = "Vehicle number is required")
    @Pattern(regexp = "^[A-Za-z0-9]{4,15}$", message = "Vehicle number must be 4 to 15 alphanumeric characters")
    private String vehicleNumber;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    private String preferredSlotNumber;

    public VehicleEntryRequestDto() {}

    public VehicleEntryRequestDto(String vehicleNumber, VehicleType vehicleType, String preferredSlotNumber) {
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.preferredSlotNumber = preferredSlotNumber;
    }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }

    public String getPreferredSlotNumber() { return preferredSlotNumber; }
    public void setPreferredSlotNumber(String preferredSlotNumber) { this.preferredSlotNumber = preferredSlotNumber; }
}
