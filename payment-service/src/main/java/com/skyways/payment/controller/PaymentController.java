package com.skyways.payment.controller;

import com.skyways.payment.dto.CreatePaymentRequest;
import com.skyways.payment.dto.PaymentResponse;
import com.skyways.payment.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = PaymentResponse.from(paymentService.createPayment(request));
        return ResponseEntity
                .created(URI.create("/api/payments/" + response.id()))
                .body(response);
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse getPayment(@PathVariable UUID paymentId) {
        return PaymentResponse.from(paymentService.getPayment(paymentId));
    }

    @GetMapping(params = "bookingId")
    public List<PaymentResponse> getPaymentsForBooking(@RequestParam UUID bookingId) {
        return paymentService.getPaymentsForBooking(bookingId).stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @GetMapping(params = "userId")
    public List<PaymentResponse> getPaymentsForUser(@RequestParam @NotBlank String userId) {
        return paymentService.getPaymentsForUser(userId).stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @PatchMapping("/{paymentId}/refund")
    public PaymentResponse refundPayment(@PathVariable UUID paymentId) {
        return PaymentResponse.from(paymentService.refundPayment(paymentId));
    }
}
