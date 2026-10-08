package com.smartparking.service;

import com.smartparking.dto.UserDto;
import com.smartparking.entity.User;
import com.smartparking.exception.UnauthorizedAccessException;
import com.smartparking.repository.UserRepository;
import com.smartparking.util.UserRole;
import com.smartparking.util.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLogService auditLogService;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public UserDto createUser(UserDto dto, String performedByEmail) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("User with email " + dto.getEmail() + " already exists");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword() != null ? dto.getPassword() : "emp123"));
        user.setRole(dto.getRole() != null ? dto.getRole() : UserRole.EMPLOYEE);
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : UserStatus.ACTIVE);

        User saved = userRepository.save(user);
        auditLogService.logAction(null, performedByEmail, "CREATE_USER", "Created user " + saved.getEmail() + " with role " + saved.getRole());
        return convertToDto(saved);
    }

    public UserDto updateUser(Long id, UserDto dto, String performedByEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        user.setName(dto.getName());
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }

        User updated = userRepository.save(user);
        auditLogService.logAction(null, performedByEmail, "UPDATE_USER", "Updated user " + updated.getEmail());
        return convertToDto(updated);
    }

    public void deleteUser(Long id, String performedByEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        if (user.getRole() == UserRole.ADMIN) {
            throw new UnauthorizedAccessException("Cannot delete an Admin account");
        }
        userRepository.delete(user);
        auditLogService.logAction(null, performedByEmail, "DELETE_USER", "Deleted user " + user.getEmail());
    }

    private UserDto convertToDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getStatus());
    }
}
