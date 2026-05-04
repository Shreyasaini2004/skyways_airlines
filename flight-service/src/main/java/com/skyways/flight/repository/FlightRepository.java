package com.skyways.flight.repository;

import com.skyways.flight.domain.Flight;
import com.skyways.flight.domain.FlightStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightRepository extends JpaRepository<Flight, UUID> {

    boolean existsByFlightNumber(String flightNumber);

    Optional<Flight> findByFlightNumber(String flightNumber);

    List<Flight> findByOriginIgnoreCaseAndDestinationIgnoreCaseAndDepartureTimeBetweenAndStatus(
            String origin,
            String destination,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            FlightStatus status
    );
}
