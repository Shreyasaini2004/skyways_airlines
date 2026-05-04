package com.skyways.payment.service;

import com.skyways.payment.client.BookingStatusClient;
import com.skyways.payment.domain.Payment;
import com.skyways.payment.domain.PaymentStatus;
import com.skyways.payment.dto.CreatePaymentRequest;
import com.skyways.payment.exception.BookingStatusUpdateException;
import com.skyways.payment.exception.PaymentNotFoundException;
import com.skyways.payment.exception.PaymentStateException;
import com.skyways.payment.event.PaymentEventPublisher;
import com.skyways.payment.gateway.PaymentGateway;
import com.skyways.payment.gateway.PaymentGatewayResult;
import com.skyways.payment.repository.PaymentRepository;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final BookingStatusClient bookingStatusClient;
    private final PaymentEventPublisher paymentEventPublisher;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentGateway paymentGateway,
            BookingStatusClient bookingStatusClient,
            PaymentEventPublisher paymentEventPublisher
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.bookingStatusClient = bookingStatusClient;
        this.paymentEventPublisher = paymentEventPublisher;
    }

    @Transactional
    public Payment createPayment(CreatePaymentRequest request) {
        Payment payment = new Payment(
                request.bookingId(),
                request.userId(),
                request.amount(),
                request.currency(),
                maskPaymentMethod(request.paymentMethodToken())
        );

        PaymentGatewayResult gatewayResult = paymentGateway.charge(
                request.amount(),
                request.currency(),
                request.paymentMethodToken()
        );

        if (gatewayResult.successful()) {
            payment.succeed(gatewayResult.providerReference());
        } else {
            payment.fail(gatewayResult.failureReason());
        }

        Payment savedPayment = paymentRepository.save(payment);
        updateBookingStatus(savedPayment);
        paymentEventPublisher.publish(payment.getStatus() == PaymentStatus.SUCCEEDED ? "PAYMENT_SUCCEEDED" : "PAYMENT_FAILED", savedPayment);
        return savedPayment;
    }

    @Transactional(readOnly = true)
    public Payment getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsForBooking(UUID bookingId) {
        return paymentRepository.findByBookingIdOrderByCreatedAtDesc(bookingId);
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsForUser(String userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Payment refundPayment(UUID paymentId) {
        Payment payment = getPayment(paymentId);
        if (payment.getStatus() != PaymentStatus.SUCCEEDED) {
            throw new PaymentStateException("Only succeeded payments can be refunded");
        }

        PaymentGatewayResult gatewayResult = paymentGateway.refund(payment.getProviderReference());
        if (!gatewayResult.successful()) {
            throw new PaymentStateException(gatewayResult.failureReason());
        }

        payment.refund(gatewayResult.providerReference());
        paymentEventPublisher.publish("PAYMENT_REFUNDED", payment);
        return payment;
    }

    private void updateBookingStatus(Payment payment) {
        try {
            if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
                bookingStatusClient.markPaymentSucceeded(payment.getBookingId());
            } else if (payment.getStatus() == PaymentStatus.FAILED) {
                bookingStatusClient.markPaymentFailed(payment.getBookingId());
            }
        } catch (BookingStatusUpdateException ex) {
            log.warn("Payment {} was saved, but booking {} status update failed: {}",
                    payment.getId(),
                    payment.getBookingId(),
                    ex.getMessage()
            );
        }
    }

    private String maskPaymentMethod(String paymentMethodToken) {
        String trimmed = paymentMethodToken.trim();
        if (trimmed.length() <= 4) {
            return "****";
        }
        return "****" + trimmed.substring(trimmed.length() - 4);
    }
}
