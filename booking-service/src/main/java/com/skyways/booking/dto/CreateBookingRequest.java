package com.skyways.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record CreateBookingRequest(
        @NotBlank(message = "User id is required")
        String userId,

        @NotBlank(message = "Flight id is required")
        String flightId,

        @NotNull(message = "Total amount is required")
        @DecimalMin(value = "1.00", message = "Total amount must be greater than zero")
        BigDecimal totalAmount,

        @NotBlank(message = "Currency is required")
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code")
        String currency,

        @Valid
        @NotEmpty(message = "At least one passenger is required")
        @Size(max = 9, message = "A single booking can contain at most 9 passengers")
        List<PassengerRequest> passengers
) {
}
