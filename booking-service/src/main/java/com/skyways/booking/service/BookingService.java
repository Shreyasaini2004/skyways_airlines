package com.skyways.booking.service;

import com.skyways.booking.client.FlightAvailabilityClient;
import com.skyways.booking.domain.Booking;
import com.skyways.booking.domain.BookingStatus;
import com.skyways.booking.domain.Passenger;
import com.skyways.booking.dto.CreateBookingRequest;
import com.skyways.booking.dto.PassengerRequest;
import com.skyways.booking.event.BookingEventPublisher;
import com.skyways.booking.exception.BookingNotFoundException;
import com.skyways.booking.exception.BookingStateException;
import com.skyways.booking.exception.FlightOverbookedException;
import com.skyways.booking.exception.InvalidPassengerDetailsException;
import com.skyways.booking.repository.BookingRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final FlightAvailabilityClient flightAvailabilityClient;
    private final BookingEventPublisher bookingEventPublisher;

    public BookingService(
            BookingRepository bookingRepository,
            FlightAvailabilityClient flightAvailabilityClient,
            BookingEventPublisher bookingEventPublisher
    ) {
        this.bookingRepository = bookingRepository;
        this.flightAvailabilityClient = flightAvailabilityClient;
        this.bookingEventPublisher = bookingEventPublisher;
    }

    @Transactional
    public Booking createBooking(CreateBookingRequest request) {
        validateDuplicatePassengers(request.passengers());

        if (!flightAvailabilityClient.hasAvailableSeats(request.flightId(), request.passengers().size())) {
            throw new FlightOverbookedException(request.flightId());
        }

        Booking booking = new Booking(
                request.userId(),
                request.flightId(),
                request.totalAmount(),
                request.currency(),
                toPassengers(request.passengers())
        );

        Booking savedBooking = bookingRepository.save(booking);
        bookingEventPublisher.publish("BOOKING_CREATED", savedBooking);
        return savedBooking;
    }

    @Transactional(readOnly = true)
    public Booking getBooking(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsForUser(String userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Booking confirmBooking(UUID bookingId) {
        Booking booking = getBooking(bookingId);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BookingStateException("Only pending bookings can be confirmed");
        }
        booking.confirm();
        bookingEventPublisher.publish("BOOKING_CONFIRMED", booking);
        return booking;
    }

    @Transactional
    public Booking markPaymentFailed(UUID bookingId) {
        Booking booking = getBooking(bookingId);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingStateException("Cancelled bookings cannot be marked as payment failed");
        }
        booking.markPaymentFailed();
        bookingEventPublisher.publish("BOOKING_PAYMENT_FAILED", booking);
        return booking;
    }

    @Transactional
    public Booking cancelBooking(UUID bookingId) {
        Booking booking = getBooking(bookingId);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return booking;
        }
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new BookingStateException("Confirmed bookings require refund processing before cancellation");
        }
        booking.cancel();
        bookingEventPublisher.publish("BOOKING_CANCELLED", booking);
        return booking;
    }

    private void validateDuplicatePassengers(List<PassengerRequest> passengers) {
        Set<String> passportNumbers = new HashSet<>();
        for (PassengerRequest passenger : passengers) {
            String normalizedPassport = passenger.passportNumber().toUpperCase();
            if (!passportNumbers.add(normalizedPassport)) {
                throw new InvalidPassengerDetailsException("Duplicate passenger passport number: " + normalizedPassport);
            }
        }
    }

    private List<Passenger> toPassengers(List<PassengerRequest> requests) {
        return requests.stream()
                .map(request -> new Passenger(
                        request.firstName(),
                        request.lastName(),
                        request.dateOfBirth(),
                        request.passportNumber().toUpperCase(),
                        request.email()
                ))
                .toList();
    }
}
