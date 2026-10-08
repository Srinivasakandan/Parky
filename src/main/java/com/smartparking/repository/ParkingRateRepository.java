package com.smartparking.repository;

import com.smartparking.entity.ParkingRate;
import com.smartparking.util.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParkingRateRepository extends JpaRepository<ParkingRate, Long> {
    Optional<ParkingRate> findByVehicleType(VehicleType vehicleType);
}
