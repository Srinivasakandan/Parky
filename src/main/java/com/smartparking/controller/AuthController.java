package com.smartparking.controller;

import com.smartparking.dto.ApiResponse;
import com.smartparking.dto.LoginRequestDto;
import com.smartparking.dto.UserDto;
import com.smartparking.security.CustomUserDetails;
import com.smartparking.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuditLogService auditLogService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDto>> login(@Valid @RequestBody LoginRequestDto loginDto, HttpServletRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        UserDto userDto = new UserDto(
                userDetails.getUser().getId(),
                userDetails.getUser().getName(),
                userDetails.getUser().getEmail(),
                userDetails.getUser().getRole(),
                userDetails.getUser().getStatus()
        );

        auditLogService.logAction(userDetails.getUser().getId(), userDetails.getUser().getEmail(), "LOGIN", "User logged in successfully");

        return ResponseEntity.ok(ApiResponse.success("Login successful", userDto));
    }

    @GetMapping("/current-user")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.ok(ApiResponse.error("Unauthenticated"));
        }

        UserDto userDto = new UserDto(
                userDetails.getUser().getId(),
                userDetails.getUser().getName(),
                userDetails.getUser().getEmail(),
                userDetails.getUser().getRole(),
                userDetails.getUser().getStatus()
        );

        return ResponseEntity.ok(ApiResponse.success("User retrieved", userDto));
    }
}
