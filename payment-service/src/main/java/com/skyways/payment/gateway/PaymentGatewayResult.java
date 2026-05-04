package com.skyways.payment.gateway;

public record PaymentGatewayResult(
        boolean successful,
        String providerReference,
        String failureReason
) {
    public static PaymentGatewayResult succeeded(String providerReference) {
        return new PaymentGatewayResult(true, providerReference, null);
    }

    public static PaymentGatewayResult failed(String failureReason) {
        return new PaymentGatewayResult(false, null, failureReason);
    }
}
