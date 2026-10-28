package com.smartparking.config;

import com.smartparking.entity.*;
import com.smartparking.repository.*;
import com.smartparking.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import org.springframework.core.annotation.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Order(2)
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParkingRateRepository parkingRateRepository;

    @Autowired
    private ParkingSlotRepository parkingSlotRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ParkingRecordRepository parkingRecordRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initUsers();
        initRates();
        initSlots();
        initSampleParkingData();
    }

    private void initUsers() {
        if (!userRepository.existsByEmail("admin@parking.com")) {
            User admin = new User("System Admin", "admin@parking.com", passwordEncoder.encode("admin123"), UserRole.ADMIN, UserStatus.ACTIVE);
            userRepository.save(admin);
        }

        if (!userRepository.existsByEmail("employee@parking.com")) {
            User employee = new User("John Employee", "employee@parking.com", passwordEncoder.encode("emp123"), UserRole.EMPLOYEE, UserStatus.ACTIVE);
            userRepository.save(employee);
        }
    }

    private void initRates() {
        if (parkingRateRepository.count() == 0) {
            parkingRateRepository.save(new ParkingRate(VehicleType.BIKE, BigDecimal.valueOf(20.00)));
            parkingRateRepository.save(new ParkingRate(VehicleType.CAR, BigDecimal.valueOf(30.00)));
            parkingRateRepository.save(new ParkingRate(VehicleType.SUV, BigDecimal.valueOf(40.00)));
            parkingRateRepository.save(new ParkingRate(VehicleType.TRUCK, BigDecimal.valueOf(50.00)));
        }
    }

    private void initSlots() {
        if (parkingSlotRepository.count() == 0) {
            // Zone A - Car / General Slots (A01 - A08)
            for (int i = 1; i <= 8; i++) {
                String slotNum = String.format("A%02d", i);
                parkingSlotRepository.save(new ParkingSlot(slotNum, VehicleType.CAR, SlotStatus.AVAILABLE));
            }

            // Zone B - Bike / SUV / Heavy Slots (B01 - B08)
            for (int i = 1; i <= 4; i++) {
                String slotNum = String.format("B%02d", i);
                parkingSlotRepository.save(new ParkingSlot(slotNum, VehicleType.BIKE, SlotStatus.AVAILABLE));
            }
            for (int i = 5; i <= 8; i++) {
                String slotNum = String.format("B%02d", i);
                parkingSlotRepository.save(new ParkingSlot(slotNum, VehicleType.SUV, SlotStatus.AVAILABLE));
            }
        }
    }

    private void initSampleParkingData() {
        if (parkingRecordRepository.count() == 0) {
            // Vehicle 1: Currently Parked CAR in slot A01
            Vehicle v1 = vehicleRepository.save(new Vehicle("TN01AB1234", VehicleType.CAR));
            ParkingSlot s1 = parkingSlotRepository.findBySlotNumber("A01").orElseThrow();
            s1.setStatus(SlotStatus.OCCUPIED);
            parkingSlotRepository.save(s1);

            ParkingRecord pr1 = new ParkingRecord(v1, s1, LocalDateTime.now().minusHours(2), ParkingRecordStatus.PARKED);
            parkingRecordRepository.save(pr1);

            // Vehicle 2: Exited BIKE in slot B01 with payment
            Vehicle v2 = vehicleRepository.save(new Vehicle("TN02CD5678", VehicleType.BIKE));
            ParkingSlot s2 = parkingSlotRepository.findBySlotNumber("B01").orElseThrow();

            ParkingRecord pr2 = new ParkingRecord(v2, s2, LocalDateTime.now().minusHours(4), ParkingRecordStatus.EXITED);
            pr2.setExitTime(LocalDateTime.now().minusHours(1));
            pr2.setDurationMinutes(180L); // 3 hours
            pr2.setParkingFee(BigDecimal.valueOf(60.00)); // 3 * ₹20
            parkingRecordRepository.save(pr2);

            Payment payment2 = new Payment(pr2, BigDecimal.valueOf(60.00), PaymentStatus.PAID, PaymentMethod.UPI, "TXN" + System.currentTimeMillis() + "101");
            paymentRepository.save(payment2);

            // Vehicle 3: Currently Parked SUV in slot B05
            Vehicle v3 = vehicleRepository.save(new Vehicle("TN03EF9012", VehicleType.SUV));
            ParkingSlot s3 = parkingSlotRepository.findBySlotNumber("B05").orElseThrow();
            s3.setStatus(SlotStatus.OCCUPIED);
            parkingSlotRepository.save(s3);

            ParkingRecord pr3 = new ParkingRecord(v3, s3, LocalDateTime.now().minusHours(1), ParkingRecordStatus.PARKED);
            parkingRecordRepository.save(pr3);
        }
    }
}
