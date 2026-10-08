package com.smartparking.controller;

import com.smartparking.dto.ApiResponse;
import com.smartparking.dto.RateUpdateDto;
import com.smartparking.entity.ParkingRate;
import com.smartparking.repository.ParkingRateRepository;
import com.smartparking.security.CustomUserDetails;
import com.smartparking.service.AuditLogService;
import com.smartparking.util.VehicleType;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rates")
public class BillingController {

    @Autowired
    private ParkingRateRepository parkingRateRepository;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ParkingRate>>> getAllRates() {
        return ResponseEntity.ok(ApiResponse.success("Parking rates fetched", parkingRateRepository.findAll()));
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<ParkingRate>> updateRate(
            @Valid @RequestBody RateUpdateDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : "system";

        ParkingRate rate = parkingRateRepository.findByVehicleType(dto.getVehicleType())
                .orElseGet(() -> new ParkingRate(dto.getVehicleType(), dto.getRatePerHour()));

        rate.setRatePerHour(dto.getRatePerHour());
        rate.setUpdatedAt(LocalDateTime.now());
        ParkingRate saved = parkingRateRepository.save(rate);

        auditLogService.logAction(null, email, "RATE_UPDATE", "Updated rate for " + dto.getVehicleType() + " to ₹" + dto.getRatePerHour() + "/hr");

        return ResponseEntity.ok(ApiResponse.success("Parking rate updated successfully", saved));
    }
}
