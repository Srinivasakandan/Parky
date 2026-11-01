package com.smartparking;

import com.smartparking.anpr.ANPRResult;
import com.smartparking.anpr.NumberPlateRecognitionService;
import com.smartparking.dto.BillDto;
import com.smartparking.dto.CustomerSearchResultDto;
import com.smartparking.dto.VehicleEntryRequestDto;
import com.smartparking.dto.VehicleExitRequestDto;
import com.smartparking.entity.ParkingRecord;
import com.smartparking.entity.ParkingSlot;
import com.smartparking.exception.VehicleAlreadyParkedException;
import com.smartparking.exception.VehicleNotFoundException;
import com.smartparking.repository.ParkingRecordRepository;
import com.smartparking.repository.ParkingSlotRepository;
import com.smartparking.service.ParkingService;
import com.smartparking.util.ParkingRecordStatus;
import com.smartparking.util.PaymentMethod;
import com.smartparking.util.SlotStatus;
import com.smartparking.util.VehicleNumberValidator;
import com.smartparking.util.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
public class SmartParkingApplicationTests {

    @Autowired
    private NumberPlateRecognitionService anprService;

    @Autowired
    private ParkingService parkingService;

    @Autowired
    private ParkingSlotRepository parkingSlotRepository;

    @Autowired
    private ParkingRecordRepository parkingRecordRepository;

    @Test
    @DisplayName("Test 1: Indian Vehicle Number Regex Format Validation")
    void testVehicleNumberValidation() {
        // Standard State Registrations (Valid)
        assertTrue(VehicleNumberValidator.isValid("TN01AB1234"));
        assertTrue(VehicleNumberValidator.isValid("MH12DE5678"));
        assertTrue(VehicleNumberValidator.isValid("DL04C1000"));
        assertTrue(VehicleNumberValidator.isValid("KA05MB9999"));

        // With spaces and hyphens (Valid after normalization)
        assertTrue(VehicleNumberValidator.isValid("TN-01-AB-1234"));
        assertTrue(VehicleNumberValidator.isValid("ka 05 mb 9999"));

        // Bharat Series (Valid)
        assertTrue(VehicleNumberValidator.isValid("21BH1234AA"));
        assertTrue(VehicleNumberValidator.isValid("22BH9999B"));

        // Invalid Formats (Must be rejected)
        assertFalse(VehicleNumberValidator.isValid(""));
        assertFalse(VehicleNumberValidator.isValid("INVALID"));
        assertFalse(VehicleNumberValidator.isValid("ZZ99AB1234")); // Non-existent state code ZZ
        assertFalse(VehicleNumberValidator.isValid("12345678"));    // Digits only
        assertFalse(VehicleNumberValidator.isValid("TN01"));        // Too short
    }

    @Test
    @DisplayName("Test 2: ANPR Service Plate Recognition")
    void testANPRRecognition() {
        ANPRResult result = anprService.recognize("TN01AB1234");
        assertNotNull(result);
        assertEquals("TN01AB1234", result.getVehicleNumber());
        assertTrue(result.isValidFormat());
        assertEquals("SUCCESS", result.getStatus());
    }

    @Test
    @DisplayName("Test 3: Prevent Duplicate Parking of Already Parked Vehicle")
    void testPreventDuplicateParking() {
        String testPlate = "KA01AB7777";
        VehicleEntryRequestDto entry1 = new VehicleEntryRequestDto(testPlate, VehicleType.CAR, null);
        parkingService.registerVehicleEntry(entry1, "emp@parking.com");

        // Attempting to park the same vehicle again while active should throw VehicleAlreadyParkedException
        VehicleEntryRequestDto entry2 = new VehicleEntryRequestDto(testPlate, VehicleType.CAR, null);
        assertThrows(VehicleAlreadyParkedException.class, () -> {
            parkingService.registerVehicleEntry(entry2, "emp@parking.com");
        });

        // Clean up: exit the vehicle
        parkingService.processVehicleExit(new VehicleExitRequestDto(testPlate, PaymentMethod.CASH), "emp@parking.com");
    }

    @Test
    @DisplayName("Test 4: Reject Exit for Vehicle That Is Not Parked")
    void testRejectExitForUnparkedVehicle() {
        String unparkedPlate = "DL09ZZ1111";
        VehicleExitRequestDto exitReq = new VehicleExitRequestDto(unparkedPlate, PaymentMethod.UPI);

        assertThrows(VehicleNotFoundException.class, () -> {
            parkingService.processVehicleExit(exitReq, "emp@parking.com");
        });
    }

    @Test
    @DisplayName("Test 5: Complete End-to-End Parking Workflow with Employee Activity Tracking")
    void testEndToEndParkingWorkflowWithEmployeeTracking() {
        String testPlate = "TN99XY8888";
        String employeeEmail = "employee@parking.com";

        // Step 1: Vehicle Entry Registration by Employee
        VehicleEntryRequestDto entryReq = new VehicleEntryRequestDto(testPlate, VehicleType.CAR, null);
        ParkingRecord record = parkingService.registerVehicleEntry(entryReq, employeeEmail);

        assertNotNull(record);
        assertNotNull(record.getId());
        assertEquals(testPlate, record.getVehicle().getVehicleNumber());
        assertEquals(ParkingRecordStatus.PARKED, record.getStatus());
        assertEquals(SlotStatus.OCCUPIED, record.getSlot().getStatus());
        assertEquals(employeeEmail, record.getEnteredBy());

        // Step 2: Customer Portal Vehicle Search
        CustomerSearchResultDto customerResult = parkingService.searchVehicleByCustomer(testPlate);
        assertNotNull(customerResult);
        assertEquals(testPlate, customerResult.getVehicleNumber());
        assertEquals(ParkingRecordStatus.PARKED, customerResult.getStatus());

        // Step 3: Vehicle Exit & Payment Processing by Employee
        VehicleExitRequestDto exitReq = new VehicleExitRequestDto(testPlate, PaymentMethod.UPI);
        BillDto bill = parkingService.processVehicleExit(exitReq, employeeEmail);

        assertNotNull(bill);
        assertEquals(testPlate, bill.getVehicleNumber());
        assertNotNull(bill.getParkingFee());
        assertTrue(bill.getParkingFee().doubleValue() > 0);

        // Step 4: Verify record reflects exit and employee tracking
        ParkingRecord exitedRecord = parkingRecordRepository.findById(record.getId()).orElseThrow();
        assertEquals(ParkingRecordStatus.EXITED, exitedRecord.getStatus());
        assertEquals(employeeEmail, exitedRecord.getExitedBy());
        assertNotNull(exitedRecord.getExitTime());

        // Step 5: Verify Parking Slot is Freed (AVAILABLE)
        ParkingSlot freedSlot = parkingSlotRepository.findById(record.getSlot().getId()).orElseThrow();
        assertEquals(SlotStatus.AVAILABLE, freedSlot.getStatus());
    }
}
