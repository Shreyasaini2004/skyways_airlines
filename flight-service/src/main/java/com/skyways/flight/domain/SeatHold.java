package com.skyways.flight.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seat_holds")
public class SeatHold {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @Column(nullable = false)
    private int passengerCount;

    @Column(nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatHoldStatus status = SeatHoldStatus.ACTIVE;

    @Column(nullable = false)
    private Instant createdAt;

    protected SeatHold() {
    }

    public SeatHold(Flight flight, int passengerCount, Instant expiresAt) {
        this.flight = flight;
        this.passengerCount = passengerCount;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public boolean isActive() {
        return status == SeatHoldStatus.ACTIVE && expiresAt.isAfter(Instant.now());
    }

    public void release() {
        this.status = SeatHoldStatus.RELEASED;
    }

    public UUID getId() {
        return id;
    }

    public Flight getFlight() {
        return flight;
    }

    public int getPassengerCount() {
        return passengerCount;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public SeatHoldStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
