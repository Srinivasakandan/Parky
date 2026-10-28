package com.smartparking.entity;

import com.smartparking.util.VehicleType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ParkingRate {

    private Long id;
    private VehicleType vehicleType;
    private BigDecimal ratePerHour;
    private String status = "ACTIVE";
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public ParkingRate() {}

    public ParkingRate(VehicleType vehicleType, BigDecimal ratePerHour) {
        this.vehicleType = vehicleType;
        this.ratePerHour = ratePerHour;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public ParkingRate(Long id, VehicleType vehicleType, BigDecimal ratePerHour, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.vehicleType = vehicleType;
        this.ratePerHour = ratePerHour;
        this.status = status;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }

    public BigDecimal getRatePerHour() { return ratePerHour; }
    public void setRatePerHour(BigDecimal ratePerHour) { this.ratePerHour = ratePerHour; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
