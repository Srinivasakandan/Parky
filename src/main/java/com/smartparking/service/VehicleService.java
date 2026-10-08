package com.smartparking.service;

import com.smartparking.entity.Vehicle;
import com.smartparking.exception.VehicleNotFoundException;
import com.smartparking.repository.VehicleRepository;
import com.smartparking.util.VehicleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    public Vehicle findOrCreateVehicle(String vehicleNumber, VehicleType vehicleType) {
        String normalizedNumber = vehicleNumber.trim().toUpperCase();

        return vehicleRepository.findByVehicleNumber(normalizedNumber)
                .map(existing -> {
                    if (vehicleType != null && existing.getVehicleType() != vehicleType) {
                        existing.setVehicleType(vehicleType);
                        return vehicleRepository.save(existing);
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    Vehicle newVehicle = new Vehicle();
                    newVehicle.setVehicleNumber(normalizedNumber);
                    newVehicle.setVehicleType(vehicleType != null ? vehicleType : VehicleType.CAR);
                    newVehicle.setCreatedAt(LocalDateTime.now());
                    return vehicleRepository.save(newVehicle);
                });
    }

    public Vehicle getVehicleByNumber(String vehicleNumber) {
        String normalizedNumber = vehicleNumber.trim().toUpperCase();
        return vehicleRepository.findByVehicleNumber(normalizedNumber)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle with number " + normalizedNumber + " not found"));
    }
}
