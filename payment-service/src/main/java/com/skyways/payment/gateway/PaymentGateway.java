package com.skyways.payment.gateway;

import java.math.BigDecimal;

public interface PaymentGateway {

    PaymentGatewayResult charge(BigDecimal amount, String currency, String paymentMethodToken);

    PaymentGatewayResult refund(String providerReference);
}
