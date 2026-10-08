package com.smartparking.dto;

import java.math.BigDecimal;

public class DashboardStatsDto {
    private long currentlyParked;
    private long availableSlots;
    private long occupiedSlots;
    private long reservedSlots;
    private long maintenanceSlots;
    private long vehiclesToday;
    private long exitedVehiclesToday;
    private BigDecimal todayRevenue;
    private BigDecimal weekRevenue;
    private BigDecimal monthRevenue;
    private int peakParkingHour;

    public DashboardStatsDto() {}

    public long getCurrentlyParked() { return currentlyParked; }
    public void setCurrentlyParked(long currentlyParked) { this.currentlyParked = currentlyParked; }

    public long getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(long availableSlots) { this.availableSlots = availableSlots; }

    public long getOccupiedSlots() { return occupiedSlots; }
    public void setOccupiedSlots(long occupiedSlots) { this.occupiedSlots = occupiedSlots; }

    public long getReservedSlots() { return reservedSlots; }
    public void setReservedSlots(long reservedSlots) { this.reservedSlots = reservedSlots; }

    public long getMaintenanceSlots() { return maintenanceSlots; }
    public void setMaintenanceSlots(long maintenanceSlots) { this.maintenanceSlots = maintenanceSlots; }

    public long getVehiclesToday() { return vehiclesToday; }
    public void setVehiclesToday(long vehiclesToday) { this.vehiclesToday = vehiclesToday; }

    public long getExitedVehiclesToday() { return exitedVehiclesToday; }
    public void setExitedVehiclesToday(long exitedVehiclesToday) { this.exitedVehiclesToday = exitedVehiclesToday; }

    public BigDecimal getTodayRevenue() { return todayRevenue; }
    public void setTodayRevenue(BigDecimal todayRevenue) { this.todayRevenue = todayRevenue; }

    public BigDecimal getWeekRevenue() { return weekRevenue; }
    public void setWeekRevenue(BigDecimal weekRevenue) { this.weekRevenue = weekRevenue; }

    public BigDecimal getMonthRevenue() { return monthRevenue; }
    public void setMonthRevenue(BigDecimal monthRevenue) { this.monthRevenue = monthRevenue; }

    public int getPeakParkingHour() { return peakParkingHour; }
    public void setPeakParkingHour(int peakParkingHour) { this.peakParkingHour = peakParkingHour; }
}
