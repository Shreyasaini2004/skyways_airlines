package com.skyways.flight.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.skyways.flight.domain.Flight;
import com.skyways.flight.dto.CreateSeatHoldRequest;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FlightServiceTests {

    @Autowired
    private FlightService flightService;

    @Test
    void searchesAvailableFlights() {
        List<Flight> flights = flightService.searchFlights("DEL", "BOM", LocalDate.now().plusDays(7), 1);

        assertThat(flights).isNotEmpty();
        assertThat(flights).allMatch(flight -> flight.getAvailableSeats() >= 1);
    }

    @Test
    void createsSeatHoldAndReducesAvailability() {
        Flight flight = flightService.searchFlights("DEL", "BOM", LocalDate.now().plusDays(7), 1).get(0);
        int before = flight.getAvailableSeats();

        flightService.createSeatHold(flight.getId(), new CreateSeatHoldRequest(2));

        Flight updatedFlight = flightService.getFlight(flight.getId());
        assertThat(updatedFlight.getAvailableSeats()).isEqualTo(before - 2);
    }
}
