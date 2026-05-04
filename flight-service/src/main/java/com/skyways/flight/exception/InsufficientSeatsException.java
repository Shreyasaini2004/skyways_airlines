package com.skyways.flight.exception;

public class InsufficientSeatsException extends RuntimeException {

    public InsufficientSeatsException(String flightNumber) {
        super("Flight " + flightNumber + " does not have enough available seats");
    }
}
