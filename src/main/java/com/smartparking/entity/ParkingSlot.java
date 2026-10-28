package com.smartparking.entity;

import com.smartparking.util.SlotStatus;
import com.smartparking.util.VehicleType;

public class ParkingSlot {

    private Long id;
    private String slotNumber;
    private VehicleType vehicleTypeAllowed;
    private SlotStatus status = SlotStatus.AVAILABLE;

    public ParkingSlot() {}

    public ParkingSlot(String slotNumber, VehicleType vehicleTypeAllowed, SlotStatus status) {
        this.slotNumber = slotNumber;
        this.vehicleTypeAllowed = vehicleTypeAllowed;
        this.status = status;
    }

    public ParkingSlot(Long id, String slotNumber, VehicleType vehicleTypeAllowed, SlotStatus status) {
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
