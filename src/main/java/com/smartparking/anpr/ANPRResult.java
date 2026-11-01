package com.smartparking.anpr;

public class ANPRResult {
    private String vehicleNumber;
    private String confidence;
    private String status;
    private String source;
    private boolean validFormat;
    private String formatType;
    private String validationMessage;

    public ANPRResult() {}

    public ANPRResult(String vehicleNumber, String confidence, String status, String source) {
        this.vehicleNumber = vehicleNumber;
        this.confidence = confidence;
        this.status = status;
        this.source = source;
    }

    public ANPRResult(String vehicleNumber, String confidence, String status, String source,
                      boolean validFormat, String formatType, String validationMessage) {
        this.vehicleNumber = vehicleNumber;
        this.confidence = confidence;
        this.status = status;
        this.source = source;
        this.validFormat = validFormat;
        this.formatType = formatType;
        this.validationMessage = validationMessage;
    }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public boolean isValidFormat() { return validFormat; }
    public void setValidFormat(boolean validFormat) { this.validFormat = validFormat; }

    public String getFormatType() { return formatType; }
    public void setFormatType(String formatType) { this.formatType = formatType; }

    public String getValidationMessage() { return validationMessage; }
    public void setValidationMessage(String validationMessage) { this.validationMessage = validationMessage; }
}
