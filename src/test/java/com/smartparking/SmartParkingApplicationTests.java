package com.smartparking;

import com.smartparking.anpr.ANPRResult;
import com.smartparking.anpr.NumberPlateRecognitionService;
import com.smartparking.dto.BillDto;
import com.smartparking.dto.CustomerSearchResultDto;
import com.smartparking.dto.VehicleEntryRequestDto;
import com.smartparking.dto.VehicleExitRequestDto;
import com.smartparking.entity.ParkingRecord;
import com.smartparking.entity.ParkingSlot;
import com.smartparking.repository.ParkingSlotRepository;
import com.smartparking.service.ParkingService;
import com.smartparking.util.ParkingRecordStatus;
import com.smartparking.util.PaymentMethod;
import com.smartparking.util.SlotStatus;
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

    @Test
    @DisplayName("Test 1: Mock ANPR Plate Recognition")
    void testMockANPR() {
        ANPRResult result = anprService.recognize("tn01ab1234");
        assertNotNull(result);
        assertEquals("TN01AB1234", result.getVehicleNumber());
        assertEquals("98%", result.getConfidence());
        assertEquals("SUCCESS", result.getStatus());
        assertEquals("MOCK", result.getSource());
    }

    @Test
    @DisplayName("Test 2: Complete End-to-End Parking Workflow (Entry -> Search -> Exit -> Slot Release)")
    void testEndToEndParkingWorkflow() {
        String testPlate = "TN99XY8888";

        // Step 1: Vehicle Entry Registration
        VehicleEntryRequestDto entryReq = new VehicleEntryRequestDto(testPlate, VehicleType.CAR, null);
        ParkingRecord record = parkingService.registerVehicleEntry(entryReq, "test@parking.com");

        assertNotNull(record);
        assertNotNull(record.getId());
        assertEquals(testPlate, record.getVehicle().getVehicleNumber());
        assertEquals(ParkingRecordStatus.PARKED, record.getStatus());
        assertEquals(SlotStatus.OCCUPIED, record.getSlot().getStatus());

        // Step 2: Customer Search active parked vehicle
        CustomerSearchResultDto customerResult = parkingService.searchVehicleByCustomer(testPlate);
        assertNotNull(customerResult);
        assertEquals(testPlate, customerResult.getVehicleNumber());
        assertEquals(ParkingRecordStatus.PARKED, customerResult.getStatus());

        // Step 3: Vehicle Exit & Payment Processing
        VehicleExitRequestDto exitReq = new VehicleExitRequestDto(testPlate, PaymentMethod.UPI);
        BillDto bill = parkingService.processVehicleExit(exitReq, "test@parking.com");

        assertNotNull(bill);
        assertEquals(testPlate, bill.getVehicleNumber());
        assertNotNull(bill.getParkingFee());
        assertTrue(bill.getParkingFee().doubleValue() > 0);

        // Step 4: Verify Parking Slot is Freed (AVAILABLE)
        ParkingSlot freedSlot = parkingSlotRepository.findById(record.getSlot().getId()).orElseThrow();
        assertEquals(SlotStatus.AVAILABLE, freedSlot.getStatus());
    }
}
