package com.skyways.booking.client;

import com.skyways.booking.exception.FlightAvailabilityException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@ConditionalOnProperty(name = "skyways.flight-service.client", havingValue = "http")
public class FlightServiceAvailabilityClient implements FlightAvailabilityClient {

    private final RestClient restClient;

    public FlightServiceAvailabilityClient(
            RestClient.Builder restClientBuilder,
            @Value("${skyways.flight-service.base-url}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public boolean hasAvailableSeats(String flightId, int requestedSeats) {
        try {
            AvailabilityResponse response = restClient.get()
                    .uri("/api/flights/numbers/{flightNumber}/availability?passengers={passengers}", flightId, requestedSeats)
                    .retrieve()
                    .body(AvailabilityResponse.class);

            return response != null && response.available();
        } catch (RestClientException ex) {
            throw new FlightAvailabilityException("Unable to verify availability for flight " + flightId, ex);
        }
    }

    private record AvailabilityResponse(boolean available) {
    }
}
