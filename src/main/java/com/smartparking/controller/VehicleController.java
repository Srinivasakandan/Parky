package com.smartparking.controller;

import com.smartparking.dto.ApiResponse;
import com.smartparking.dto.BillDto;
import com.smartparking.dto.CustomerSearchResultDto;
import com.smartparking.dto.VehicleEntryRequestDto;
import com.smartparking.dto.VehicleExitRequestDto;
import com.smartparking.entity.ParkingRecord;
import com.smartparking.security.CustomUserDetails;
import com.smartparking.service.ParkingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    @Autowired
    private ParkingService parkingService;

    @PostMapping("/entry")
    public ResponseEntity<ApiResponse<ParkingRecord>> registerEntry(
            @Valid @RequestBody VehicleEntryRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : "system";
        ParkingRecord record = parkingService.registerVehicleEntry(request, email);
        return ResponseEntity.ok(ApiResponse.success("Vehicle entry registered successfully", record));
    }

    @PostMapping("/exit")
    public ResponseEntity<ApiResponse<BillDto>> processExit(
            @Valid @RequestBody VehicleExitRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : "system";
        BillDto bill = parkingService.processVehicleExit(request, email);
        return ResponseEntity.ok(ApiResponse.success("Vehicle exit processed successfully", bill));
    }

    @GetMapping("/parked")
    public ResponseEntity<ApiResponse<List<ParkingRecord>>> getParkedVehicles() {
        return ResponseEntity.ok(ApiResponse.success("Currently parked vehicles", parkingService.getCurrentlyParkedVehicles()));
    }

    @GetMapping("/exited")
    public ResponseEntity<ApiResponse<List<ParkingRecord>>> getExitedVehicles() {
        return ResponseEntity.ok(ApiResponse.success("Exited vehicles history", parkingService.getExitedVehicles()));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<ParkingRecord>>> getAllHistory() {
        return ResponseEntity.ok(ApiResponse.success("All parking history", parkingService.getAllParkingHistory()));
    }

    @GetMapping("/{vehicleNumber}")
    public ResponseEntity<ApiResponse<CustomerSearchResultDto>> searchVehicle(@PathVariable String vehicleNumber) {
        CustomerSearchResultDto result = parkingService.searchVehicleByCustomer(vehicleNumber);
        return ResponseEntity.ok(ApiResponse.success("Vehicle record found", result));
    }
}
