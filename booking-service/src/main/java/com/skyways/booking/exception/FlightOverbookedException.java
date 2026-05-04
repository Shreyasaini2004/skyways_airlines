package com.skyways.booking.exception;

public class FlightOverbookedException extends RuntimeException {

    public FlightOverbookedException(String flightId) {
        super("Flight " + flightId + " does not have enough available seats");
    }
}
