package com.skyways.booking.exception;

import com.skyways.booking.domain.ErrorLog;
import com.skyways.booking.dto.ErrorResponse;
import com.skyways.booking.dto.FieldErrorResponse;
import com.skyways.booking.repository.ErrorLogRepository;
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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String SERVICE_NAME = "booking-service";

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

    @ExceptionHandler(InvalidPassengerDetailsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPassenger(InvalidPassengerDetailsException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_PASSENGER_DETAILS", ex, request);
    }

    @ExceptionHandler(FlightOverbookedException.class)
    public ResponseEntity<ErrorResponse> handleOverbooked(FlightOverbookedException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "FLIGHT_OVERBOOKED", ex, request);
    }

    @ExceptionHandler(FlightAvailabilityException.class)
    public ResponseEntity<ErrorResponse> handleFlightAvailability(FlightAvailabilityException ex, HttpServletRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "FLIGHT_AVAILABILITY_UNAVAILABLE", ex, request);
    }

    @ExceptionHandler(BookingStateException.class)
    public ResponseEntity<ErrorResponse> handleBookingState(BookingStateException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "INVALID_BOOKING_STATE", ex, request);
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(BookingNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND", ex, request);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleConcurrentUpdate(OptimisticLockingFailureException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "CONCURRENT_BOOKING_UPDATE", ex, request);
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
