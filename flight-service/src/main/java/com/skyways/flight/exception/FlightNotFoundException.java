package com.skyways.flight.exception;

import java.util.UUID;

public class FlightNotFoundException extends RuntimeException {

    public FlightNotFoundException(UUID flightId) {
        super("Flight not found with id: " + flightId);
    }

    public FlightNotFoundException(String flightNumber) {
        super("Flight not found with flight number: " + flightNumber);
    }
}
