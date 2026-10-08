package com.smartparking.dto;

import com.smartparking.util.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PaymentRequestDto {

    @NotNull(message = "Parking record ID is required")
    private Long parkingRecordId;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    public PaymentRequestDto() {}

    public PaymentRequestDto(Long parkingRecordId, BigDecimal amount, PaymentMethod paymentMethod) {
        this.parkingRecordId = parkingRecordId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    public Long getParkingRecordId() { return parkingRecordId; }
    public void setParkingRecordId(Long parkingRecordId) { this.parkingRecordId = parkingRecordId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
}
