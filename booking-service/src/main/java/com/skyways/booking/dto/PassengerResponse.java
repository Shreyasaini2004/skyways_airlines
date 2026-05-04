package com.skyways.booking.dto;

import com.skyways.booking.domain.Passenger;
import java.time.LocalDate;

public record PassengerResponse(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String passportNumber,
        String email
) {
    public static PassengerResponse from(Passenger passenger) {
        return new PassengerResponse(
                passenger.getFirstName(),
                passenger.getLastName(),
                passenger.getDateOfBirth(),
                passenger.getPassportNumber(),
                passenger.getEmail()
        );
    }
}
