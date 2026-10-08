package com.smartparking.service;

import com.smartparking.dto.DashboardStatsDto;
import com.smartparking.entity.ParkingRecord;
import com.smartparking.repository.ParkingRecordRepository;
import com.smartparking.repository.ParkingSlotRepository;
import com.smartparking.repository.PaymentRepository;
import com.smartparking.util.ParkingRecordStatus;
import com.smartparking.util.SlotStatus;
import com.smartparking.util.VehicleType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class RevenueService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ParkingRecordRepository parkingRecordRepository;

    @Autowired
    private ParkingSlotRepository parkingSlotRepository;

    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        LocalDateTime startOfWeek = LocalDate.now().minusDays(6).atStartOfDay();
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        stats.setCurrentlyParked(parkingRecordRepository.countByStatus(ParkingRecordStatus.PARKED));
        stats.setAvailableSlots(parkingSlotRepository.countByStatus(SlotStatus.AVAILABLE));
        stats.setOccupiedSlots(parkingSlotRepository.countByStatus(SlotStatus.OCCUPIED));
        stats.setReservedSlots(parkingSlotRepository.countByStatus(SlotStatus.RESERVED));
        stats.setMaintenanceSlots(parkingSlotRepository.countByStatus(SlotStatus.MAINTENANCE));

        stats.setVehiclesToday(parkingRecordRepository.countByEntryTimeBetween(startOfDay, endOfDay));
        stats.setExitedVehiclesToday(parkingRecordRepository.countByExitTimeBetween(startOfDay, endOfDay));

        BigDecimal todayRev = paymentRepository.calculateRevenueBetween(startOfDay, endOfDay);
        BigDecimal weekRev = paymentRepository.calculateRevenueBetween(startOfWeek, endOfDay);
        BigDecimal monthRev = paymentRepository.calculateRevenueBetween(startOfMonth, endOfDay);

        stats.setTodayRevenue(todayRev != null ? todayRev : BigDecimal.ZERO);
        stats.setWeekRevenue(weekRev != null ? weekRev : BigDecimal.ZERO);
        stats.setMonthRevenue(monthRev != null ? monthRev : BigDecimal.ZERO);

        stats.setPeakParkingHour(calculatePeakHour());

        return stats;
    }

    public Map<String, Object> getDailyRevenueChartData() {
        Map<String, Object> chartData = new LinkedHashMap<>();
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            BigDecimal rev = paymentRepository.calculateRevenueBetween(start, end);

            labels.add(date.getMonthValue() + "/" + date.getDayOfMonth());
            data.add(rev != null ? rev : BigDecimal.ZERO);
        }

        chartData.put("labels", labels);
        chartData.put("data", data);
        return chartData;
    }

    public Map<String, Object> getMonthlyRevenueChartData() {
        Map<String, Object> chartData = new LinkedHashMap<>();
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = LocalDate.now().minusMonths(i);
            LocalDateTime start = monthDate.withDayOfMonth(1).atStartOfDay();
            LocalDateTime end = monthDate.withDayOfMonth(monthDate.lengthOfMonth()).atTime(LocalTime.MAX);
            BigDecimal rev = paymentRepository.calculateRevenueBetween(start, end);

            labels.add(monthDate.getMonth().name().substring(0, 3));
            data.add(rev != null ? rev : BigDecimal.ZERO);
        }

        chartData.put("labels", labels);
        chartData.put("data", data);
        return chartData;
    }

    public Map<String, Object> getVehicleTypeDistributionChartData() {
        Map<String, Object> chartData = new LinkedHashMap<>();
        List<String> labels = Arrays.asList("BIKE", "CAR", "SUV", "TRUCK");
        List<Long> counts = new ArrayList<>();

        List<ParkingRecord> allRecords = parkingRecordRepository.findAll();
        Map<VehicleType, Long> typeCountMap = new EnumMap<>(VehicleType.class);

        for (ParkingRecord record : allRecords) {
            VehicleType type = record.getVehicle().getVehicleType();
            typeCountMap.put(type, typeCountMap.getOrDefault(type, 0L) + 1);
        }

        counts.add(typeCountMap.getOrDefault(VehicleType.BIKE, 0L));
        counts.add(typeCountMap.getOrDefault(VehicleType.CAR, 0L));
        counts.add(typeCountMap.getOrDefault(VehicleType.SUV, 0L));
        counts.add(typeCountMap.getOrDefault(VehicleType.TRUCK, 0L));

        chartData.put("labels", labels);
        chartData.put("data", counts);
        return chartData;
    }

    public int calculatePeakHour() {
        List<ParkingRecord> allRecords = parkingRecordRepository.findAll();
        int[] hourlyCounts = new int[24];

        for (ParkingRecord record : allRecords) {
            if (record.getEntryTime() != null) {
                int hour = record.getEntryTime().getHour();
                hourlyCounts[hour]++;
            }
        }

        int peakHour = 9; // Default 9 AM if empty
        int maxCount = -1;
        for (int i = 0; i < 24; i++) {
            if (hourlyCounts[i] > maxCount) {
                maxCount = hourlyCounts[i];
                peakHour = i;
            }
        }

        return peakHour;
    }
}
