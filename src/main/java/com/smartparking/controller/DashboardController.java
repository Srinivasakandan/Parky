package com.smartparking.controller;

import com.smartparking.dto.ApiResponse;
import com.smartparking.dto.DashboardStatsDto;
import com.smartparking.service.RevenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DashboardController {

    @Autowired
    private RevenueService revenueService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics fetched", revenueService.getDashboardStats()));
    }

    @GetMapping("/revenue/daily")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDailyRevenue() {
        return ResponseEntity.ok(ApiResponse.success("Daily revenue data", revenueService.getDailyRevenueChartData()));
    }

    @GetMapping("/revenue/monthly")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlyRevenue() {
        return ResponseEntity.ok(ApiResponse.success("Monthly revenue data", revenueService.getMonthlyRevenueChartData()));
    }

    @GetMapping("/revenue/distribution")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVehicleDistribution() {
        return ResponseEntity.ok(ApiResponse.success("Vehicle distribution data", revenueService.getVehicleTypeDistributionChartData()));
    }
}
