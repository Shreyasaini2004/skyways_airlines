package com.skyways.flight.provider;

import com.skyways.flight.dto.ProviderFlightResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MockGdsFlightProviderClient implements ExternalFlightProviderClient {

    @Override
    public List<ProviderFlightResponse> search(String origin, String destination, LocalDate departureDate, int passengers) {
        return List.of(
                new ProviderFlightResponse(
                        "MockGDS",
                        "GDS901",
                        "Partner Air",
                        origin,
                        destination,
                        departureDate.atTime(LocalTime.of(7, 45)),
                        departureDate.atTime(LocalTime.of(10, 5)),
                        BigDecimal.valueOf(6900),
                        "INR",
                        Math.max(0, 18 - passengers)
                ),
                new ProviderFlightResponse(
                        "MockSkyscanner",
                        "SKY702",
                        "SkyWays Codeshare",
                        origin,
                        destination,
                        departureDate.atTime(LocalTime.of(18, 20)),
                        departureDate.atTime(LocalTime.of(20, 35)),
                        BigDecimal.valueOf(7350),
                        "INR",
                        Math.max(0, 9 - passengers)
                )
        );
    }
}
