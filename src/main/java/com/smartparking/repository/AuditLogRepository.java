package com.smartparking.repository;

import com.smartparking.entity.AuditLog;
import java.util.List;
import java.util.Optional;

public interface AuditLogRepository {
    Optional<AuditLog> findById(Long id);
    List<AuditLog> findAll();
    AuditLog save(AuditLog log);
    List<AuditLog> findAllByOrderByTimestampDesc();
    long count();
}
