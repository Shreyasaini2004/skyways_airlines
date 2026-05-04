package com.skyways.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID bookingId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.INITIATED;

    @Column(nullable = false)
    private String paymentMethodLabel;

    private String providerReference;

    @Lob
    private String failureReason;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected Payment() {
    }

    public Payment(UUID bookingId, String userId, BigDecimal amount, String currency, String paymentMethodLabel) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethodLabel = paymentMethodLabel;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void succeed(String providerReference) {
        this.status = PaymentStatus.SUCCEEDED;
        this.providerReference = providerReference;
        this.failureReason = null;
    }

    public void fail(String failureReason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
    }

    public void refund(String providerReference) {
        this.status = PaymentStatus.REFUNDED;
        this.providerReference = providerReference;
        this.failureReason = null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public String getUserId() {
        return userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getPaymentMethodLabel() {
        return paymentMethodLabel;
    }

    public String getProviderReference() {
        return providerReference;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
