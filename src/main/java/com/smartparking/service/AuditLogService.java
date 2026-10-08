package com.smartparking.service;

import com.smartparking.entity.AuditLog;
import com.smartparking.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void logAction(Long userId, String userEmail, String action, String description) {
        AuditLog log = new AuditLog(userId, userEmail, action, description);
        auditLogRepository.save(log);
    }

    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }
}
