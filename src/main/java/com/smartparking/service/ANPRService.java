package com.smartparking.service;

import com.smartparking.anpr.ANPRResult;
import com.smartparking.anpr.AnprImageProcessor;
import com.smartparking.anpr.NumberPlateRecognitionService;
import com.smartparking.entity.ANPRRecord;
import com.smartparking.repository.ANPRRecordRepository;
import com.smartparking.util.VehicleNumberValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ANPRService {

    @Autowired
    private NumberPlateRecognitionService recognitionService;

    @Autowired
    private AnprImageProcessor imageProcessor;

    @Autowired
    private ANPRRecordRepository anprRecordRepository;

    public ANPRResult processImageANPR(MultipartFile file) throws IOException {
        imageProcessor.validateImage(file);

        ANPRResult result = recognitionService.recognizeImage(file.getBytes());

        // Save ANPR audit log record
        ANPRRecord record = new ANPRRecord();
        record.setVehicleNumber(result.getVehicleNumber() != null && !result.getVehicleNumber().isEmpty() ?
                result.getVehicleNumber() : "UNKNOWN");
        record.setConfidence(result.getConfidence());
        record.setRecognitionStatus(result.getStatus());
        record.setSource(result.getSource() != null ? result.getSource() : "CAMERA_OCR");
        record.setCapturedAt(LocalDateTime.now());

        anprRecordRepository.save(record);

        return result;
    }

    public VehicleNumberValidator.ValidationResult validateNumber(String vehicleNumber) {
        return VehicleNumberValidator.validate(vehicleNumber);
    }

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
