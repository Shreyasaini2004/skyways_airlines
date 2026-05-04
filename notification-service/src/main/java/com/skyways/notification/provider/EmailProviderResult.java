package com.skyways.notification.provider;

public record EmailProviderResult(
        boolean successful,
        String providerReference,
        String failureReason
) {
    public static EmailProviderResult sent(String providerReference) {
        return new EmailProviderResult(true, providerReference, null);
    }

    public static EmailProviderResult failed(String failureReason) {
        return new EmailProviderResult(false, null, failureReason);
    }
}
