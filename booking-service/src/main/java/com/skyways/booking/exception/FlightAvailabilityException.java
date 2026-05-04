package com.skyways.booking.exception;

public class FlightAvailabilityException extends RuntimeException {

    public FlightAvailabilityException(String message, Throwable cause) {
        super(message, cause);
    }
}
