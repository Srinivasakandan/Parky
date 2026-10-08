package com.smartparking.repository;

import com.smartparking.entity.ParkingSlot;
import com.smartparking.util.SlotStatus;
import com.smartparking.util.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {
    Optional<ParkingSlot> findBySlotNumber(String slotNumber);
    List<ParkingSlot> findByStatus(SlotStatus status);
    List<ParkingSlot> findByVehicleTypeAllowedAndStatus(VehicleType vehicleTypeAllowed, SlotStatus status);
    List<ParkingSlot> findAllByOrderBySlotNumberAsc();
    long countByStatus(SlotStatus status);
}
