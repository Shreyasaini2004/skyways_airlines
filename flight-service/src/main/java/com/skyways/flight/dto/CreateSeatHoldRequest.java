package com.skyways.flight.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CreateSeatHoldRequest(
        @Min(value = 1, message = "Passenger count must be at least 1")
        @Max(value = 9, message = "Passenger count cannot be more than 9")
        int passengerCount
) {
}
