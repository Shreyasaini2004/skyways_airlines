package com.skyways.flight.dto;

import com.skyways.flight.domain.SeatHold;
import com.skyways.flight.domain.SeatHoldStatus;
import java.time.Instant;
import java.util.UUID;

public record SeatHoldResponse(
        UUID id,
        UUID flightId,
        String flightNumber,
        int passengerCount,
        Instant expiresAt,
        SeatHoldStatus status
) {
    public static SeatHoldResponse from(SeatHold seatHold) {
        return new SeatHoldResponse(
                seatHold.getId(),
                seatHold.getFlight().getId(),
                seatHold.getFlight().getFlightNumber(),
                seatHold.getPassengerCount(),
                seatHold.getExpiresAt(),
                seatHold.getStatus()
        );
    }
}
