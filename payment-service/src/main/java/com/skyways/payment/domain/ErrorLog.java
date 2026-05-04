package com.skyways.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "error_logs")
public class ErrorLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private String serviceName;

    @Column(nullable = false)
    private String errorCode;

    @Column(nullable = false)
    private int httpStatus;

    @Column(nullable = false)
    private String path;

    @Lob
    @Column(nullable = false)
    private String message;

    protected ErrorLog() {
    }

    public ErrorLog(String serviceName, String errorCode, int httpStatus, String path, String message) {
        this.serviceName = serviceName;
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.path = path;
        this.message = message;
    }

    @PrePersist
    void onCreate() {
        this.timestamp = Instant.now();
    }
}
