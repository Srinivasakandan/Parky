package com.smartparking.controller;

import com.smartparking.dto.ApiResponse;
import com.smartparking.dto.ParkingSlotDto;
import com.smartparking.security.CustomUserDetails;
import com.smartparking.service.ParkingSlotService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking-slots")
public class ParkingSlotController {

    @Autowired
    private ParkingSlotService parkingSlotService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ParkingSlotDto>>> getAllSlots() {
        return ResponseEntity.ok(ApiResponse.success("Parking slots fetched", parkingSlotService.getAllSlots()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ParkingSlotDto>> createSlot(
            @Valid @RequestBody ParkingSlotDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : "system";
        ParkingSlotDto created = parkingSlotService.createSlot(dto, email);
        return ResponseEntity.ok(ApiResponse.success("Parking slot created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ParkingSlotDto>> updateSlot(
            @PathVariable Long id,
            @Valid @RequestBody ParkingSlotDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : "system";
        ParkingSlotDto updated = parkingSlotService.updateSlot(id, dto, email);
        return ResponseEntity.ok(ApiResponse.success("Parking slot updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSlot(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : "system";
        parkingSlotService.deleteSlot(id, email);
        return ResponseEntity.ok(ApiResponse.success("Parking slot deleted", null));
    }
}
