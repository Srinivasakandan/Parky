package com.smartparking.repository;

import com.smartparking.entity.ANPRRecord;
import java.util.List;
import java.util.Optional;

public interface ANPRRecordRepository {
    Optional<ANPRRecord> findById(Long id);
    List<ANPRRecord> findAll();
    ANPRRecord save(ANPRRecord record);
    List<ANPRRecord> findAllByOrderByCapturedAtDesc();
    long count();
}
