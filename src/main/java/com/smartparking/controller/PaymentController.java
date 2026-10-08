package com.smartparking.controller;

import com.smartparking.dto.ApiResponse;
import com.smartparking.dto.PaymentRequestDto;
import com.smartparking.entity.Payment;
import com.smartparking.security.CustomUserDetails;
import com.smartparking.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<Payment>> processPayment(
            @Valid @RequestBody PaymentRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : "system";
        Payment payment = paymentService.processPayment(request, email);
        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", payment));
    }
}
