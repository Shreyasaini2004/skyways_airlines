package com.skyways.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public record PassengerRequest(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @NotBlank(message = "Passport number is required")
        @Pattern(regexp = "^[A-Z0-9]{6,12}$", message = "Passport number must be 6 to 12 uppercase letters or digits")
        String passportNumber,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email
) {
}
