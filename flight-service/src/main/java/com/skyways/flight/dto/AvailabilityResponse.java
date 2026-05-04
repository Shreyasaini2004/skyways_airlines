package com.skyways.flight.dto;

import java.util.UUID;

public record AvailabilityResponse(
        UUID flightId,
        String flightNumber,
        int requestedSeats,
        int availableSeats,
        boolean available
) {
}
