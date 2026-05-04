package com.skyways.payment.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.skyways.payment.domain.Payment;
import com.skyways.payment.domain.PaymentStatus;
import com.skyways.payment.dto.CreatePaymentRequest;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PaymentServiceTests {

    @Autowired
    private PaymentService paymentService;

    @Test
    void createsSucceededPaymentForSuccessToken() {
        Payment payment = paymentService.createPayment(request("pm_success"));

        assertThat(payment.getId()).isNotNull();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(payment.getProviderReference()).startsWith("mock_txn_");
    }

    @Test
    void createsFailedPaymentForDeclinedToken() {
        Payment payment = paymentService.createPayment(request("pm_card_declined"));

        assertThat(payment.getId()).isNotNull();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.getFailureReason()).contains("declined");
    }

    private CreatePaymentRequest request(String token) {
        return new CreatePaymentRequest(
                UUID.randomUUID(),
                "user-1",
                BigDecimal.valueOf(1500),
                "INR",
                token
        );
    }
}
