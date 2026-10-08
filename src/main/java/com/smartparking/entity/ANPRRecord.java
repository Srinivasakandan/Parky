package com.smartparking.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "anpr_records")
public class ANPRRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_number", nullable = false)
    private String vehicleNumber;

    @Column(nullable = false)
    private String confidence;

    @Column(name = "recognition_status", nullable = false)
    private String recognitionStatus;

    @Column(nullable = false)
    private String source = "MOCK";

    @Column(name = "captured_at", nullable = false)
    private LocalDateTime capturedAt = LocalDateTime.now();

    public ANPRRecord() {}

    public ANPRRecord(String vehicleNumber, String confidence, String recognitionStatus, String source) {
        this.vehicleNumber = vehicleNumber != null ? vehicleNumber.toUpperCase().trim() : null;
        this.confidence = confidence;
        this.recognitionStatus = recognitionStatus;
        this.source = source;
        this.capturedAt = LocalDateTime.now();
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
