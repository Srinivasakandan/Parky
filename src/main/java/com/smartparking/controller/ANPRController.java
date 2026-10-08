package com.smartparking.controller;

import com.smartparking.anpr.ANPRResult;
import com.smartparking.dto.ANPRRequestDto;
import com.smartparking.dto.ApiResponse;
import com.smartparking.entity.ANPRRecord;
import com.smartparking.service.ANPRService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anpr")
public class ANPRController {

    @Autowired
    private ANPRService anprService;

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
