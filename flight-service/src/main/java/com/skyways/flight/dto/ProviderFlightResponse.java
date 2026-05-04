package com.skyways.flight.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProviderFlightResponse(
        String provider,
        String flightNumber,
        String airline,
        String origin,
        String destination,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        BigDecimal fare,
        String currency,
        int availableSeats
) {
}
