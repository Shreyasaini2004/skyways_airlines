package com.skyways.booking.client;

public interface FlightAvailabilityClient {

    boolean hasAvailableSeats(String flightId, int requestedSeats);
}
