package com.smartparking.repository;

import com.smartparking.entity.ParkingSlot;
import com.smartparking.util.SlotStatus;
import com.smartparking.util.VehicleType;
import java.util.List;
import java.util.Optional;

public interface ParkingSlotRepository {
    Optional<ParkingSlot> findById(Long id);
    List<ParkingSlot> findAll();
    ParkingSlot save(ParkingSlot slot);
    Optional<ParkingSlot> findBySlotNumber(String slotNumber);
    List<ParkingSlot> findByStatus(SlotStatus status);
    List<ParkingSlot> findByVehicleTypeAllowedAndStatus(VehicleType vehicleTypeAllowed, SlotStatus status);
    List<ParkingSlot> findAllByOrderBySlotNumberAsc();
    long countByStatus(SlotStatus status);
    long count();
    void delete(ParkingSlot slot);
}
