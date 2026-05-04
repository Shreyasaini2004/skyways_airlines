package com.skyways.flight.controller;

import com.skyways.flight.dto.AvailabilityResponse;
import com.skyways.flight.dto.CreateSeatHoldRequest;
import com.skyways.flight.dto.FlightResponse;
import com.skyways.flight.dto.ProviderFlightResponse;
import com.skyways.flight.dto.SeatHoldResponse;
import com.skyways.flight.service.FlightService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping("/search")
    public List<FlightResponse> searchFlights(
            @RequestParam @NotBlank @Pattern(regexp = "^[A-Za-z]{3}$", message = "Origin must be a 3-letter airport code") String origin,
            @RequestParam @NotBlank @Pattern(regexp = "^[A-Za-z]{3}$", message = "Destination must be a 3-letter airport code") String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
            @RequestParam(defaultValue = "1") @Min(1) @Max(9) int passengers
    ) {
        return flightService.searchFlights(origin.toUpperCase(), destination.toUpperCase(), departureDate, passengers)
                .stream()
                .map(FlightResponse::from)
                .toList();
    }

    @GetMapping("/provider-search")
    public List<ProviderFlightResponse> searchExternalProviderFlights(
            @RequestParam @NotBlank @Pattern(regexp = "^[A-Za-z]{3}$", message = "Origin must be a 3-letter airport code") String origin,
            @RequestParam @NotBlank @Pattern(regexp = "^[A-Za-z]{3}$", message = "Destination must be a 3-letter airport code") String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
            @RequestParam(defaultValue = "1") @Min(1) @Max(9) int passengers
    ) {
        return flightService.searchProviderFlights(origin.toUpperCase(), destination.toUpperCase(), departureDate, passengers);
    }

    @GetMapping("/{flightId}")
    public FlightResponse getFlight(@PathVariable UUID flightId) {
        return FlightResponse.from(flightService.getFlight(flightId));
    }

    @GetMapping("/numbers/{flightNumber}")
    public FlightResponse getFlightByNumber(@PathVariable String flightNumber) {
        return FlightResponse.from(flightService.getFlightByNumber(flightNumber.toUpperCase()));
    }

    @GetMapping("/{flightId}/availability")
    public AvailabilityResponse checkAvailability(
            @PathVariable UUID flightId,
            @RequestParam(defaultValue = "1") @Min(1) @Max(9) int passengers
    ) {
        return flightService.checkAvailability(flightId, passengers);
    }

    @GetMapping("/numbers/{flightNumber}/availability")
    public AvailabilityResponse checkAvailabilityByFlightNumber(
            @PathVariable String flightNumber,
            @RequestParam(defaultValue = "1") @Min(1) @Max(9) int passengers
    ) {
        return flightService.checkAvailabilityByFlightNumber(flightNumber.toUpperCase(), passengers);
    }

    @PostMapping("/{flightId}/seat-holds")
    public ResponseEntity<SeatHoldResponse> createSeatHold(
            @PathVariable UUID flightId,
            @Valid @RequestBody CreateSeatHoldRequest request
    ) {
        SeatHoldResponse response = SeatHoldResponse.from(flightService.createSeatHold(flightId, request));
        return ResponseEntity
                .created(URI.create("/api/flights/" + flightId + "/seat-holds/" + response.id()))
                .body(response);
    }

    @DeleteMapping("/seat-holds/{seatHoldId}")
    public SeatHoldResponse releaseSeatHold(@PathVariable UUID seatHoldId) {
        return SeatHoldResponse.from(flightService.releaseSeatHold(seatHoldId));
    }
}
