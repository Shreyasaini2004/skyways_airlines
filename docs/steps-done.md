# Steps Completed

This file explains the build sequence used for the SkyWays project.

## 1. Created The Root Maven Project

The root `pom.xml` lists each microservice as a module:

- `flight-service`
- `booking-service`
- `payment-service`
- `notification-service`
- `api-gateway`

## 2. Built Flight Service

Purpose:

- Search local SkyWays flights
- Search mock GDS/Skyscanner results
- Check availability
- Create and release seat holds

Important classes:

- `FlightController`
- `FlightService`
- `MockGdsFlightProviderClient`
- `FlightDataInitializer`

## 3. Built Booking Service

Purpose:

- Validate passenger details
- Reject duplicate passport numbers
- Create pending bookings
- Confirm, fail, or cancel bookings
- Optionally call `flight-service` for availability

Important classes:

- `BookingController`
- `BookingService`
- `FlightServiceAvailabilityClient`
- `GlobalExceptionHandler`

## 4. Built Payment Service

Purpose:

- Simulate Stripe-style payment success and failure
- Store payment attempts
- Refund succeeded payments
- Optionally update booking status
- Publish Kafka payment events when enabled

Important classes:

- `PaymentController`
- `PaymentService`
- `MockPaymentGateway`
- `HttpBookingStatusClient`

## 5. Built Notification Service

Purpose:

- Simulate SendGrid-style email sending
- Store notification history
- Listen to payment events when Kafka is enabled

Important classes:

- `NotificationController`
- `NotificationService`
- `MockSendGridEmailProvider`
- `PaymentEventListener`

## 6. Built API Gateway

Purpose:

- Provide one entry point on port `8080`
- Route requests to all backend services

Important file:

- `api-gateway/src/main/resources/application.yml`

## 7. Added Infrastructure

Added:

- `docker-compose.yml`
- service Dockerfiles
- `k8s/skyways-platform.yaml`
- API request examples in `docs/*.http`

## 8. Added Error Handling Pattern

Each service has:

- Custom exceptions
- DTO-level validation
- Global exception handler
- Consistent error JSON
- `error_logs` table
