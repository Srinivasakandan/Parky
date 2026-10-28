package com.smartparking.repository;

import com.smartparking.entity.Vehicle;
import java.util.List;
import java.util.Optional;

public interface VehicleRepository {
    Optional<Vehicle> findById(Long id);
    List<Vehicle> findAll();
    Vehicle save(Vehicle vehicle);
    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);
    Optional<Vehicle> findByVehicleNumberIgnoreCase(String vehicleNumber);
    boolean existsByVehicleNumber(String vehicleNumber);
    long count();
}
