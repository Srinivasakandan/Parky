package com.smartparking.entity;

import com.smartparking.util.SlotStatus;
import com.smartparking.util.VehicleType;
import jakarta.persistence.*;

@Entity
@Table(name = "parking_slots")
public class ParkingSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slot_number", nullable = false, unique = true)
    private String slotNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type_allowed", nullable = false)
    private VehicleType vehicleTypeAllowed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status = SlotStatus.AVAILABLE;

    public ParkingSlot() {}

    public ParkingSlot(String slotNumber, VehicleType vehicleTypeAllowed, SlotStatus status) {
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
