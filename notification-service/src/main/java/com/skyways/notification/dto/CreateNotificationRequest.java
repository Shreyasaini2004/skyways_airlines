package com.skyways.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateNotificationRequest(
        UUID bookingId,

        @NotBlank(message = "User id is required")
        String userId,

        @NotBlank(message = "Recipient email is required")
        @Email(message = "Recipient email must be valid")
        String recipient,

        @NotBlank(message = "Subject is required")
        @Size(max = 120, message = "Subject can contain at most 120 characters")
        String subject,

        @NotBlank(message = "Body is required")
        String body
) {
}
