package com.skyways.booking.controller;

import com.skyways.booking.dto.BookingResponse;
import com.skyways.booking.dto.CreateBookingRequest;
import com.skyways.booking.service.BookingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = BookingResponse.from(bookingService.createBooking(request));
        return ResponseEntity
                .created(URI.create("/api/bookings/" + response.id()))
                .body(response);
    }

    @GetMapping("/{bookingId}")
    public BookingResponse getBooking(@PathVariable UUID bookingId) {
        return BookingResponse.from(bookingService.getBooking(bookingId));
    }

    @GetMapping
    public List<BookingResponse> getBookingsForUser(@RequestParam @NotBlank String userId) {
        return bookingService.getBookingsForUser(userId).stream()
                .map(BookingResponse::from)
                .toList();
    }

    @PatchMapping("/{bookingId}/confirm")
    public BookingResponse confirmBooking(@PathVariable UUID bookingId) {
        return BookingResponse.from(bookingService.confirmBooking(bookingId));
    }

    @PatchMapping("/{bookingId}/payment-failed")
    public BookingResponse markPaymentFailed(@PathVariable UUID bookingId) {
        return BookingResponse.from(bookingService.markPaymentFailed(bookingId));
    }

    @PatchMapping("/{bookingId}/cancel")
    public BookingResponse cancelBooking(@PathVariable UUID bookingId) {
        return BookingResponse.from(bookingService.cancelBooking(bookingId));
    }
}
