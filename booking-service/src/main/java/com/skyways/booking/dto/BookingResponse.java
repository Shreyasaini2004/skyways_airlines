package com.skyways.booking.dto;

import com.skyways.booking.domain.Booking;
import com.skyways.booking.domain.BookingStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        String userId,
        String flightId,
        BookingStatus status,
        BigDecimal totalAmount,
        String currency,
        List<PassengerResponse> passengers,
        Instant createdAt,
        Instant updatedAt
) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUserId(),
                booking.getFlightId(),
                booking.getStatus(),
                booking.getTotalAmount(),
                booking.getCurrency(),
                booking.getPassengers().stream().map(PassengerResponse::from).toList(),
                booking.getCreatedAt(),
                booking.getUpdatedAt()
        );
    }
}
