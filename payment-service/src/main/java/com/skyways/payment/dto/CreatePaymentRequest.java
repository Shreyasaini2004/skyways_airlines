package com.skyways.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentRequest(
        @NotNull(message = "Booking id is required")
        UUID bookingId,

        @NotBlank(message = "User id is required")
        String userId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "1.00", message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code")
        String currency,

        @NotBlank(message = "Payment method token is required")
        String paymentMethodToken
) {
}
