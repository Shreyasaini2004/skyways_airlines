package com.skyways.payment.gateway;

import com.skyways.payment.exception.PaymentGatewayUnavailableException;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public PaymentGatewayResult charge(BigDecimal amount, String currency, String paymentMethodToken) {
        String normalizedToken = paymentMethodToken.trim().toLowerCase();

        return switch (normalizedToken) {
            case "pm_card_declined", "pm_fail" -> PaymentGatewayResult.failed("Card was declined by the mock provider");
            case "pm_insufficient_funds" -> PaymentGatewayResult.failed("Insufficient funds");
            case "pm_timeout" -> throw new PaymentGatewayUnavailableException("Mock payment provider timed out");
            default -> PaymentGatewayResult.succeeded("mock_txn_" + UUID.randomUUID());
        };
    }

    @Override
    public PaymentGatewayResult refund(String providerReference) {
        if (providerReference == null || providerReference.isBlank()) {
            return PaymentGatewayResult.failed("Missing provider reference for refund");
        }
        return PaymentGatewayResult.succeeded("mock_refund_" + UUID.randomUUID());
    }
}
