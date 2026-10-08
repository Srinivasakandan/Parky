package com.smartparking.controller;

import com.smartparking.dto.ApiResponse;
import com.smartparking.dto.UserDto;
import com.smartparking.entity.AuditLog;
import com.smartparking.security.CustomUserDetails;
import com.smartparking.service.AuditLogService;
import com.smartparking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/employees")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllEmployees() {
        return ResponseEntity.ok(ApiResponse.success("Users fetched", userService.getAllUsers()));
    }

    @PostMapping("/employees")
    public ResponseEntity<ApiResponse<UserDto>> createEmployee(
            @Valid @RequestBody UserDto userDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : "admin";
        UserDto created = userService.createUser(userDto, email);
        return ResponseEntity.ok(ApiResponse.success("Employee created successfully", created));
    }

    @PutMapping("/employees/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody UserDto userDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : "admin";
        UserDto updated = userService.updateUser(id, userDto, email);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", updated));
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : "admin";
        userService.deleteUser(id, email);
        return ResponseEntity.ok(ApiResponse.success("Employee deleted successfully", null));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs() {
        return ResponseEntity.ok(ApiResponse.success("Audit logs fetched", auditLogService.getAllAuditLogs()));
    }
}
