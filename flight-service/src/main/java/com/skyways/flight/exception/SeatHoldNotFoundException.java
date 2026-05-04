package com.skyways.flight.exception;

import java.util.UUID;

public class SeatHoldNotFoundException extends RuntimeException {

    public SeatHoldNotFoundException(UUID seatHoldId) {
        super("Seat hold not found with id: " + seatHoldId);
    }
}
