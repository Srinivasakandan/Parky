package com.smartparking.repository;

import com.smartparking.entity.ANPRRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ANPRRecordRepository extends JpaRepository<ANPRRecord, Long> {
    List<ANPRRecord> findAllByOrderByCapturedAtDesc();
}
