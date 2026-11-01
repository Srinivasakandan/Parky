package com.smartparking.controller;

import com.smartparking.anpr.ANPRResult;
import com.smartparking.dto.ANPRRequestDto;
import com.smartparking.dto.ApiResponse;
import com.smartparking.entity.ANPRRecord;
import com.smartparking.service.ANPRService;
import com.smartparking.util.VehicleNumberValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/anpr")
public class ANPRController {

    @Autowired
    private ANPRService anprService;

    @PostMapping("/recognize")
    public ResponseEntity<ApiResponse<ANPRResult>> recognizeImage(
            @RequestParam("image") MultipartFile image) throws IOException {
        ANPRResult result = anprService.processImageANPR(image);
        return ResponseEntity.ok(ApiResponse.success("ANPR image recognition completed", result));
    }

    @PostMapping("/validate-number")
    public ResponseEntity<ApiResponse<VehicleNumberValidator.ValidationResult>> validateVehicleNumber(
            @RequestParam("vehicleNumber") String vehicleNumber) {
        VehicleNumberValidator.ValidationResult result = anprService.validateNumber(vehicleNumber);
        return ResponseEntity.ok(ApiResponse.success("Vehicle number format validation completed", result));
    }

    @PostMapping("/mock-recognize")
    public ResponseEntity<ApiResponse<ANPRResult>> mockRecognize(@Valid @RequestBody ANPRRequestDto requestDto) {
        ANPRResult result = anprService.processMockANPR(requestDto.getInput());
        return ResponseEntity.ok(ApiResponse.success("ANPR Recognition completed", result));
    }

    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<List<ANPRRecord>>> getANPRLogs() {
        return ResponseEntity.ok(ApiResponse.success("ANPR Logs fetched", anprService.getANPRLogs()));
    }
}
