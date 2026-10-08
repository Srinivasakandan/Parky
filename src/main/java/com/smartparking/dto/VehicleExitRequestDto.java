package com.smartparking.dto;

import com.smartparking.util.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class VehicleExitRequestDto {

    @NotBlank(message = "Vehicle number is required")
    private String vehicleNumber;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    public VehicleExitRequestDto() {}

    public VehicleExitRequestDto(String vehicleNumber, PaymentMethod paymentMethod) {
        this.vehicleNumber = vehicleNumber;
        this.paymentMethod = paymentMethod;
    }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
}
