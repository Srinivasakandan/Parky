package com.smartparking.exception;

public class InvalidParkingRateException extends RuntimeException {
    public InvalidParkingRateException(String message) {
        super(message);
    }
}
