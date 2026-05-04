package com.skyways.booking.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "skyways.flight-service.client", havingValue = "mock", matchIfMissing = true)
public class MockFlightAvailabilityClient implements FlightAvailabilityClient {

    @Override
    public boolean hasAvailableSeats(String flightId, int requestedSeats) {
        return !"FULL".equalsIgnoreCase(flightId) && requestedSeats <= 9;
    }
}
