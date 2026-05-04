package com.skyways.flight.dto;

import com.skyways.flight.domain.Flight;
import com.skyways.flight.domain.FlightStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FlightResponse(
        UUID id,
        String flightNumber,
        String airline,
        String origin,
        String destination,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        BigDecimal baseFare,
        String currency,
        int totalSeats,
        int availableSeats,
        FlightStatus status
) {
    public static FlightResponse from(Flight flight) {
        return new FlightResponse(
                flight.getId(),
                flight.getFlightNumber(),
                flight.getAirline(),
                flight.getOrigin(),
                flight.getDestination(),
                flight.getDepartureTime(),
                flight.getArrivalTime(),
                flight.getBaseFare(),
                flight.getCurrency(),
                flight.getTotalSeats(),
                flight.getAvailableSeats(),
                flight.getStatus()
        );
    }
}
