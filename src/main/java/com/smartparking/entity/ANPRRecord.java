package com.smartparking.entity;

import java.time.LocalDateTime;

public class ANPRRecord {

    private Long id;
    private String vehicleNumber;
    private String confidence;
    private String recognitionStatus;
    private String source = "MOCK";
    private LocalDateTime capturedAt = LocalDateTime.now();

    public ANPRRecord() {}

    public ANPRRecord(String vehicleNumber, String confidence, String recognitionStatus, String source) {
        this.vehicleNumber = vehicleNumber != null ? vehicleNumber.toUpperCase().trim() : null;
        this.confidence = confidence;
        this.recognitionStatus = recognitionStatus;
        this.source = source;
        this.capturedAt = LocalDateTime.now();
    }

    public ANPRRecord(Long id, String vehicleNumber, String confidence, String recognitionStatus, String source, LocalDateTime capturedAt) {
        this.id = id;
        this.vehicleNumber = vehicleNumber != null ? vehicleNumber.toUpperCase().trim() : null;
        this.confidence = confidence;
        this.recognitionStatus = recognitionStatus;
        this.source = source;
        this.capturedAt = capturedAt != null ? capturedAt : LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }

    public String getRecognitionStatus() { return recognitionStatus; }
    public void setRecognitionStatus(String recognitionStatus) { this.recognitionStatus = recognitionStatus; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDateTime getCapturedAt() { return capturedAt; }
    public void setCapturedAt(LocalDateTime capturedAt) { this.capturedAt = capturedAt; }
}
