package com.skyways.notification.provider;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MockSendGridEmailProvider implements EmailProvider {

    @Override
    public EmailProviderResult send(String recipient, String subject, String body) {
        if (recipient.endsWith("@fail.test")) {
            return EmailProviderResult.failed("Mock SendGrid rejected the recipient");
        }
        return EmailProviderResult.sent("mock_sendgrid_" + UUID.randomUUID());
    }
}
