package com.smartparking.service;

import com.smartparking.dto.ParkingSlotDto;
import com.smartparking.entity.ParkingSlot;
import com.smartparking.exception.ParkingSlotUnavailableException;
import com.smartparking.repository.ParkingSlotRepository;
import com.smartparking.util.SlotStatus;
import com.smartparking.util.VehicleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParkingSlotService {

    @Autowired
    private ParkingSlotRepository parkingSlotRepository;

    @Autowired
    private AuditLogService auditLogService;

    public List<ParkingSlotDto> getAllSlots() {
        return parkingSlotRepository.findAllByOrderBySlotNumberAsc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ParkingSlot findAvailableSlot(VehicleType vehicleType, String preferredSlotNumber) {
        if (preferredSlotNumber != null && !preferredSlotNumber.trim().isEmpty()) {
            ParkingSlot slot = parkingSlotRepository.findBySlotNumber(preferredSlotNumber.trim().toUpperCase())
                    .orElseThrow(() -> new ParkingSlotUnavailableException("Slot " + preferredSlotNumber + " does not exist"));
            if (slot.getStatus() != SlotStatus.AVAILABLE) {
                throw new ParkingSlotUnavailableException("Slot " + preferredSlotNumber + " is currently " + slot.getStatus());
            }
            return slot;
        }

        // Try exact vehicle type match first
        List<ParkingSlot> matchingSlots = parkingSlotRepository.findByVehicleTypeAllowedAndStatus(vehicleType, SlotStatus.AVAILABLE);
        if (!matchingSlots.isEmpty()) {
            return matchingSlots.get(0);
        }

        // Try slots designated for ALL vehicle types
        List<ParkingSlot> allTypeSlots = parkingSlotRepository.findByVehicleTypeAllowedAndStatus(VehicleType.ALL, SlotStatus.AVAILABLE);
        if (!allTypeSlots.isEmpty()) {
            return allTypeSlots.get(0);
        }

        // Fallback to any available slot if matching type not restricted
        List<ParkingSlot> anyAvailable = parkingSlotRepository.findByStatus(SlotStatus.AVAILABLE);
        if (!anyAvailable.isEmpty()) {
            return anyAvailable.get(0);
        }

        throw new ParkingSlotUnavailableException("No available parking slot found for vehicle type: " + vehicleType);
    }

    public void updateSlotStatus(ParkingSlot slot, SlotStatus newStatus) {
        slot.setStatus(newStatus);
        parkingSlotRepository.save(slot);
    }

    public ParkingSlotDto createSlot(ParkingSlotDto dto, String performedByEmail) {
        if (parkingSlotRepository.findBySlotNumber(dto.getSlotNumber().toUpperCase()).isPresent()) {
            throw new IllegalArgumentException("Parking slot " + dto.getSlotNumber() + " already exists");
        }

        ParkingSlot slot = new ParkingSlot();
        slot.setSlotNumber(dto.getSlotNumber().toUpperCase().trim());
        slot.setVehicleTypeAllowed(dto.getVehicleTypeAllowed());
        slot.setStatus(dto.getStatus() != null ? dto.getStatus() : SlotStatus.AVAILABLE);

        ParkingSlot saved = parkingSlotRepository.save(slot);
        auditLogService.logAction(null, performedByEmail, "SLOT_UPDATE", "Created slot " + saved.getSlotNumber());
        return convertToDto(saved);
    }

    public ParkingSlotDto updateSlot(Long id, ParkingSlotDto dto, String performedByEmail) {
        ParkingSlot slot = parkingSlotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found with id: " + id));

        slot.setSlotNumber(dto.getSlotNumber().toUpperCase().trim());
        slot.setVehicleTypeAllowed(dto.getVehicleTypeAllowed());
        if (dto.getStatus() != null) {
            slot.setStatus(dto.getStatus());
        }

        ParkingSlot updated = parkingSlotRepository.save(slot);
        auditLogService.logAction(null, performedByEmail, "SLOT_UPDATE", "Updated slot " + updated.getSlotNumber() + " status to " + updated.getStatus());
        return convertToDto(updated);
    }

    public void deleteSlot(Long id, String performedByEmail) {
        ParkingSlot slot = parkingSlotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found with id: " + id));
        if (slot.getStatus() == SlotStatus.OCCUPIED) {
            throw new ParkingSlotUnavailableException("Cannot delete occupied parking slot " + slot.getSlotNumber());
        }
        parkingSlotRepository.delete(slot);
        auditLogService.logAction(null, performedByEmail, "SLOT_UPDATE", "Deleted slot " + slot.getSlotNumber());
    }

    private ParkingSlotDto convertToDto(ParkingSlot slot) {
        return new ParkingSlotDto(slot.getId(), slot.getSlotNumber(), slot.getVehicleTypeAllowed(), slot.getStatus());
    }
}
