package com.smartparking.repository;

import com.smartparking.entity.ParkingRate;
import com.smartparking.util.VehicleType;
import java.util.List;
import java.util.Optional;

public interface ParkingRateRepository {
    Optional<ParkingRate> findById(Long id);
    List<ParkingRate> findAll();
    ParkingRate save(ParkingRate parkingRate);
    Optional<ParkingRate> findByVehicleType(VehicleType vehicleType);
    long count();
}
