package com.skyways.payment.exception;

import com.skyways.payment.domain.ErrorLog;
import com.skyways.payment.dto.ErrorResponse;
import com.skyways.payment.dto.FieldErrorResponse;
import com.skyways.payment.repository.ErrorLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String SERVICE_NAME = "payment-service";

    private final ErrorLogRepository errorLogRepository;

    public GlobalExceptionHandler(ErrorLogRepository errorLogRepository) {
        this.errorLogRepository = errorLogRepository;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldErrorResponse> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
                .toList();

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                "Request validation failed",
                request.getRequestURI(),
                fieldErrors
        );
        persistError(response);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                ex.getMessage(),
                request.getRequestURI()
        );
        persistError(response);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "MISSING_REQUEST_PARAMETER",
                ex.getParameterName() + " is required",
                request.getRequestURI()
        );
        persistError(response);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFound(PaymentNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", ex, request);
    }

    @ExceptionHandler(PaymentStateException.class)
    public ResponseEntity<ErrorResponse> handlePaymentState(PaymentStateException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "INVALID_PAYMENT_STATE", ex, request);
    }

    @ExceptionHandler(PaymentGatewayUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleGatewayUnavailable(PaymentGatewayUnavailableException ex, HttpServletRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "PAYMENT_PROVIDER_UNAVAILABLE", ex, request);
    }

    @ExceptionHandler(BookingStatusUpdateException.class)
    public ResponseEntity<ErrorResponse> handleBookingStatusUpdate(BookingStatusUpdateException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, "BOOKING_STATUS_UPDATE_FAILED", ex, request);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleConcurrentUpdate(OptimisticLockingFailureException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "CONCURRENT_PAYMENT_UPDATE", ex, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", ex, request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String errorCode, Exception ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(status.value(), errorCode, ex.getMessage(), request.getRequestURI());
        persistError(response);
        if (status.is5xxServerError()) {
            log.error("{} at {}", errorCode, request.getRequestURI(), ex);
        } else {
            log.warn("{} at {}: {}", errorCode, request.getRequestURI(), ex.getMessage());
        }
        return ResponseEntity.status(status).body(response);
    }

    private void persistError(ErrorResponse response) {
        try {
            errorLogRepository.save(new ErrorLog(
                    SERVICE_NAME,
                    response.error(),
                    response.status(),
                    response.path(),
                    response.message()
            ));
        } catch (Exception persistenceFailure) {
            log.warn("Failed to persist error log: {}", persistenceFailure.getMessage());
        }
    }
}
