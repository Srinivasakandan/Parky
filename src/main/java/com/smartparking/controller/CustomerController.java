package com.smartparking.controller;

import com.smartparking.dto.ApiResponse;
import com.smartparking.dto.CustomerSearchResultDto;
import com.smartparking.service.ParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private ParkingService parkingService;

    @GetMapping("/search/{vehicleNumber}")
    public ResponseEntity<ApiResponse<CustomerSearchResultDto>> searchVehicle(@PathVariable String vehicleNumber) {
        CustomerSearchResultDto result = parkingService.searchVehicleByCustomer(vehicleNumber);
        return ResponseEntity.ok(ApiResponse.success("Vehicle search successful", result));
    }
}
