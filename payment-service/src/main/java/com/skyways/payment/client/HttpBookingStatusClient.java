package com.skyways.payment.client;

import com.skyways.payment.exception.BookingStatusUpdateException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@ConditionalOnProperty(name = "skyways.booking-service.client", havingValue = "http")
public class HttpBookingStatusClient implements BookingStatusClient {

    private final RestClient restClient;

    public HttpBookingStatusClient(
            RestClient.Builder restClientBuilder,
            @Value("${skyways.booking-service.base-url}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public void markPaymentSucceeded(UUID bookingId) {
        patchBookingStatus(bookingId, "confirm");
    }

    @Override
    public void markPaymentFailed(UUID bookingId) {
        patchBookingStatus(bookingId, "payment-failed");
    }

    private void patchBookingStatus(UUID bookingId, String action) {
        try {
            restClient.patch()
                    .uri("/api/bookings/{bookingId}/{action}", bookingId, action)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            throw new BookingStatusUpdateException("Unable to update booking " + bookingId + " after payment", ex);
        }
    }
}
