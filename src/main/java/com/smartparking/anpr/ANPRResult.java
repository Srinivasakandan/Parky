package com.smartparking.anpr;

public class ANPRResult {
    private String vehicleNumber;
    private String confidence;
    private String status;
    private String source;

    public ANPRResult() {}

    public ANPRResult(String vehicleNumber, String confidence, String status, String source) {
        this.vehicleNumber = vehicleNumber;
        this.confidence = confidence;
        this.status = status;
        this.source = source;
    }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
