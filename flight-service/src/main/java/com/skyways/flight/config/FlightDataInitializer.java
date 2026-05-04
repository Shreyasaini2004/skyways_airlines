package com.skyways.flight.config;

import com.skyways.flight.domain.Flight;
import com.skyways.flight.domain.FlightStatus;
import com.skyways.flight.repository.FlightRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class FlightDataInitializer implements CommandLineRunner {

    private final FlightRepository flightRepository;

    public FlightDataInitializer(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Override
    public void run(String... args) {
        seed("SK101", "SkyWays Airlines", "DEL", "BOM", LocalTime.of(8, 30), LocalTime.of(10, 40), "6500.00", 180, 42);
        seed("SK205", "SkyWays Airlines", "DEL", "BOM", LocalTime.of(13, 15), LocalTime.of(15, 25), "7200.00", 180, 12);
        seed("SK309", "SkyWays Airlines", "BOM", "BLR", LocalTime.of(9, 0), LocalTime.of(10, 45), "5400.00", 160, 30);
        seed("SK412", "SkyWays Airlines", "DEL", "DXB", LocalTime.of(22, 10), LocalTime.of(0, 50), "18500.00", 220, 8);
    }

    private void seed(
            String flightNumber,
            String airline,
            String origin,
            String destination,
            LocalTime departure,
            LocalTime arrival,
            String baseFare,
            int totalSeats,
            int availableSeats
    ) {
        if (flightRepository.existsByFlightNumber(flightNumber)) {
            return;
        }

        LocalDate travelDate = LocalDate.now().plusDays(7);
        flightRepository.save(new Flight(
                flightNumber,
                airline,
                origin,
                destination,
                travelDate.atTime(departure),
                travelDate.atTime(arrival),
                new BigDecimal(baseFare),
                "INR",
                totalSeats,
                availableSeats,
                FlightStatus.SCHEDULED
        ));
    }
}
