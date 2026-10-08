package com.smartparking.exception;

public class ParkingSlotUnavailableException extends RuntimeException {
    public ParkingSlotUnavailableException(String message) {
        super(message);
    }
}
