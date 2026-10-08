package com.smartparking.service;

import com.smartparking.anpr.ANPRResult;
import com.smartparking.anpr.NumberPlateRecognitionService;
import com.smartparking.entity.ANPRRecord;
import com.smartparking.repository.ANPRRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ANPRService {

    @Autowired
    private NumberPlateRecognitionService recognitionService;

    @Autowired
    private ANPRRecordRepository anprRecordRepository;

    public ANPRResult processMockANPR(String input) {
        ANPRResult result = recognitionService.recognize(input);

        // Save ANPR audit log record
        ANPRRecord record = new ANPRRecord();
        record.setVehicleNumber(result.getVehicleNumber());
        record.setConfidence(result.getConfidence());
        record.setRecognitionStatus(result.getStatus());
        record.setSource(result.getSource());
        record.setCapturedAt(LocalDateTime.now());

        anprRecordRepository.save(record);

        return result;
    }

    public List<ANPRRecord> getANPRLogs() {
        return anprRecordRepository.findAllByOrderByCapturedAtDesc();
    }
}
