package com.skyways.flight.service;

import com.skyways.flight.domain.Flight;
import com.skyways.flight.domain.FlightStatus;
import com.skyways.flight.domain.SeatHold;
import com.skyways.flight.dto.AvailabilityResponse;
import com.skyways.flight.dto.CreateSeatHoldRequest;
import com.skyways.flight.dto.ProviderFlightResponse;
import com.skyways.flight.exception.FlightNotFoundException;
import com.skyways.flight.exception.InsufficientSeatsException;
import com.skyways.flight.exception.SeatHoldNotFoundException;
import com.skyways.flight.exception.SeatHoldStateException;
import com.skyways.flight.provider.ExternalFlightProviderClient;
import com.skyways.flight.repository.FlightRepository;
import com.skyways.flight.repository.SeatHoldRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FlightService {

    private static final Duration DEFAULT_HOLD_DURATION = Duration.ofMinutes(10);

    private final FlightRepository flightRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final ExternalFlightProviderClient externalFlightProviderClient;

    public FlightService(
            FlightRepository flightRepository,
            SeatHoldRepository seatHoldRepository,
            ExternalFlightProviderClient externalFlightProviderClient
    ) {
        this.flightRepository = flightRepository;
        this.seatHoldRepository = seatHoldRepository;
        this.externalFlightProviderClient = externalFlightProviderClient;
    }

    @Transactional(readOnly = true)
    public List<Flight> searchFlights(String origin, String destination, LocalDate departureDate, int passengers) {
        return flightRepository.findByOriginIgnoreCaseAndDestinationIgnoreCaseAndDepartureTimeBetweenAndStatus(
                        origin,
                        destination,
                        departureDate.atStartOfDay(),
                        departureDate.plusDays(1).atStartOfDay().minusNanos(1),
                        FlightStatus.SCHEDULED
                )
                .stream()
                .filter(flight -> flight.getAvailableSeats() >= passengers)
                .sorted(Comparator.comparing(Flight::getDepartureTime).thenComparing(Flight::getBaseFare))
                .toList();
    }

    public List<ProviderFlightResponse> searchProviderFlights(String origin, String destination, LocalDate departureDate, int passengers) {
        return externalFlightProviderClient.search(origin, destination, departureDate, passengers);
    }

    @Transactional(readOnly = true)
    public Flight getFlight(UUID flightId) {
        return flightRepository.findById(flightId)
                .orElseThrow(() -> new FlightNotFoundException(flightId));
    }

    @Transactional(readOnly = true)
    public Flight getFlightByNumber(String flightNumber) {
        return flightRepository.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new FlightNotFoundException(flightNumber));
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse checkAvailability(UUID flightId, int passengers) {
        Flight flight = getFlight(flightId);
        return toAvailabilityResponse(flight, passengers);
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse checkAvailabilityByFlightNumber(String flightNumber, int passengers) {
        Flight flight = getFlightByNumber(flightNumber);
        return toAvailabilityResponse(flight, passengers);
    }

    private AvailabilityResponse toAvailabilityResponse(Flight flight, int passengers) {
        return new AvailabilityResponse(
                flight.getId(),
                flight.getFlightNumber(),
                passengers,
                flight.getAvailableSeats(),
                flight.canHoldSeats(passengers)
        );
    }

    @Transactional
    public SeatHold createSeatHold(UUID flightId, CreateSeatHoldRequest request) {
        Flight flight = getFlight(flightId);
        if (!flight.canHoldSeats(request.passengerCount())) {
            throw new InsufficientSeatsException(flight.getFlightNumber());
        }

        flight.holdSeats(request.passengerCount());
        SeatHold seatHold = new SeatHold(
                flight,
                request.passengerCount(),
                Instant.now().plus(DEFAULT_HOLD_DURATION)
        );
        return seatHoldRepository.save(seatHold);
    }

    @Transactional
    public SeatHold releaseSeatHold(UUID seatHoldId) {
        SeatHold seatHold = seatHoldRepository.findById(seatHoldId)
                .orElseThrow(() -> new SeatHoldNotFoundException(seatHoldId));

        if (!seatHold.isActive()) {
            throw new SeatHoldStateException("Only active seat holds can be released");
        }

        seatHold.getFlight().releaseSeats(seatHold.getPassengerCount());
        seatHold.release();
        return seatHold;
    }
}
