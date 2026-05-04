package com.skyways.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.skyways.booking.domain.Booking;
import com.skyways.booking.domain.BookingStatus;
import com.skyways.booking.dto.CreateBookingRequest;
import com.skyways.booking.dto.PassengerRequest;
import com.skyways.booking.exception.FlightOverbookedException;
import com.skyways.booking.exception.InvalidPassengerDetailsException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BookingServiceTests {

    @Autowired
    private BookingService bookingService;

    @Test
    void createsPendingBooking() {
        Booking booking = bookingService.createBooking(validRequest("SK101"));

        assertThat(booking.getId()).isNotNull();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(booking.getPassengers()).hasSize(1);
    }

    @Test
    void rejectsOverbookedFlight() {
        assertThatThrownBy(() -> bookingService.createBooking(validRequest("FULL")))
                .isInstanceOf(FlightOverbookedException.class);
    }

    @Test
    void rejectsDuplicatePassengerPassport() {
        PassengerRequest passenger = validPassenger("A1234567");
        CreateBookingRequest request = new CreateBookingRequest(
                "user-1",
                "SK101",
                BigDecimal.valueOf(300),
                "USD",
                List.of(passenger, passenger)
        );

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(InvalidPassengerDetailsException.class);
    }

    private CreateBookingRequest validRequest(String flightId) {
        return new CreateBookingRequest(
                "user-1",
                flightId,
                BigDecimal.valueOf(150),
                "USD",
                List.of(validPassenger("A1234567"))
        );
    }

    private PassengerRequest validPassenger(String passportNumber) {
        return new PassengerRequest(
                "Asha",
                "Rao",
                LocalDate.of(1998, 3, 15),
                passportNumber,
                "asha.rao@example.com"
        );
    }
}
