package com.skyways.notification.provider;

public interface EmailProvider {

    EmailProviderResult send(String recipient, String subject, String body);
}
