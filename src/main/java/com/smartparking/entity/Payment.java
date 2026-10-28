package com.smartparking.entity;

import com.smartparking.util.PaymentMethod;
import com.smartparking.util.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {

    private Long id;
    private ParkingRecord parkingRecord;
    private BigDecimal amount;
    private PaymentStatus paymentStatus = PaymentStatus.PAID;
    private PaymentMethod paymentMethod;
    private LocalDateTime paidAt = LocalDateTime.now();
    private String transactionId;

    public Payment() {}

    public Payment(ParkingRecord parkingRecord, BigDecimal amount, PaymentStatus paymentStatus, PaymentMethod paymentMethod, String transactionId) {
        this.parkingRecord = parkingRecord;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.paidAt = LocalDateTime.now();
        this.transactionId = transactionId;
    }

    public Payment(Long id, ParkingRecord parkingRecord, BigDecimal amount, PaymentStatus paymentStatus,
                   PaymentMethod paymentMethod, LocalDateTime paidAt, String transactionId) {
        this.id = id;
        this.parkingRecord = parkingRecord;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.paidAt = paidAt != null ? paidAt : LocalDateTime.now();
        this.transactionId = transactionId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ParkingRecord getParkingRecord() { return parkingRecord; }
    public void setParkingRecord(ParkingRecord parkingRecord) { this.parkingRecord = parkingRecord; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}
