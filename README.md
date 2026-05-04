# SkyWays Online Flight Booking System

SkyWays is a Spring Boot microservices project for searching flights, creating bookings, processing mock payments, sending notifications, and handling failures cleanly.

The project is intentionally built in two modes:

- **Simple learning mode:** run each service with H2 in-memory database, no Kafka required.
- **Full platform mode:** run services with PostgreSQL, Kafka, Docker Compose, and Kubernetes manifests.

## Tech Stack Used

| Requirement | Where it is used |
| --- | --- |
| Java | All backend services |
| Spring Boot | All microservices |
| Spring Cloud | `api-gateway` uses Spring Cloud Gateway |
| Kubernetes | `k8s/skyways-platform.yaml` |
| Kafka | Booking/payment events and notification listener |
| Skyscanner/GDS style API | `flight-service` has a mock external provider adapter |
| Stripe style API | `payment-service` has a mock payment gateway |
| SendGrid style API | `notification-service` has a mock SendGrid provider |
| Java exception handling | Global exception handlers in every service |

## Services

| Service | Port | Main responsibility |
| --- | ---: | --- |
| `api-gateway` | `8080` | Single entry point for all APIs |
| `flight-service` | `8081` | Flight search, mock GDS/Skyscanner search, availability, seat holds |
| `booking-service` | `8082` | Passenger validation, booking creation, status changes |
| `payment-service` | `8083` | Mock Stripe-style payments and refunds |
| `notification-service` | `8084` | Mock SendGrid-style email notifications |

## Architecture

```text
Client / Swagger / Frontend
        |
        v
Spring Cloud API Gateway :8080
        |
        +--> Flight Service :8081  ---- Mock GDS / Skyscanner adapter
        |
        +--> Booking Service :8082 ---- passenger validation + error handling
        |
        +--> Payment Service :8083 ---- Mock Stripe gateway
        |
        +--> Notification Service :8084 ---- Mock SendGrid provider

Kafka topics:
  booking-events
  payment-events
```

## Project Structure

```text
skyways-flight-booking/
  api-gateway/
  flight-service/
  booking-service/
  payment-service/
  notification-service/
  docs/
  k8s/
  docker-compose.yml
  pom.xml
```

## Prerequisites

Install:

- Java 17 or later
- Maven 3.9 or later
- Docker Desktop, optional but recommended
- Kubernetes or Minikube, optional

Check:

```powershell
java -version
mvn -version
docker --version
```

## Run In Simple Learning Mode

This mode uses H2 databases and disables Kafka by default.

Open separate terminals.

```powershell
cd "C:\Users\DELL\Documents\New project\flight-service"
mvn spring-boot:run
```

```powershell
cd "C:\Users\DELL\Documents\New project\booking-service"
mvn spring-boot:run
```

```powershell
cd "C:\Users\DELL\Documents\New project\payment-service"
mvn spring-boot:run
```

```powershell
cd "C:\Users\DELL\Documents\New project\notification-service"
mvn spring-boot:run
```

```powershell
cd "C:\Users\DELL\Documents\New project\api-gateway"
mvn spring-boot:run
```

Swagger URLs:

- Gateway routes APIs through `http://localhost:8080`
- Flight Swagger: `http://localhost:8081/swagger-ui.html`
- Booking Swagger: `http://localhost:8082/swagger-ui.html`
- Payment Swagger: `http://localhost:8083/swagger-ui.html`
- Notification Swagger: `http://localhost:8084/swagger-ui.html`

## Run With Service Integration

To make `booking-service` call `flight-service` for real availability:

```powershell
cd "C:\Users\DELL\Documents\New project\booking-service"
mvn spring-boot:run "-Dspring-boot.run.arguments=--skyways.flight-service.client=http"
```

To make `payment-service` call `booking-service` after success/failure:

```powershell
cd "C:\Users\DELL\Documents\New project\payment-service"
mvn spring-boot:run "-Dspring-boot.run.arguments=--skyways.booking-service.client=http"
```

## Run Full Docker Compose Mode

This starts Kafka, PostgreSQL databases, all services, and the gateway.

```powershell
cd "C:\Users\DELL\Documents\New project"
docker compose up --build
```

Gateway:

```text
http://localhost:8080
```

## Main User Flow

### 1. Search Flights

```text
GET http://localhost:8080/api/flights/search?origin=DEL&destination=BOM&departureDate=2026-05-07&passengers=1
```

The sample flights are created for **7 days after the day you start `flight-service`**. Adjust `departureDate` accordingly.

### 2. Search Mock GDS / Skyscanner Results

```text
GET http://localhost:8080/api/flights/provider-search?origin=DEL&destination=BOM&departureDate=2026-05-07&passengers=1
```

### 3. Create Booking

```http
POST http://localhost:8080/api/bookings
Content-Type: application/json

{
  "userId": "asha@example.com",
  "flightId": "SK101",
  "totalAmount": 1500.00,
  "currency": "INR",
  "passengers": [
    {
      "firstName": "Asha",
      "lastName": "Rao",
      "dateOfBirth": "1998-03-15",
      "passportNumber": "A1234567",
      "email": "asha@example.com"
    }
  ]
}
```

The booking starts as:

```text
PENDING
```

### 4. Pay For Booking

Use the `id` from the booking response as `bookingId`.

```http
POST http://localhost:8080/api/payments
Content-Type: application/json

{
  "bookingId": "PUT_BOOKING_ID_HERE",
  "userId": "asha@example.com",
  "amount": 1500.00,
  "currency": "INR",
  "paymentMethodToken": "pm_success"
}
```

If payment integration is enabled, successful payment confirms the booking.

Mock payment tokens:

| Token | Result |
| --- | --- |
| `pm_success` | Payment succeeds |
| `pm_card_declined` | Payment fails |
| `pm_fail` | Payment fails |
| `pm_insufficient_funds` | Payment fails |
| `pm_timeout` | Provider unavailable error |

### 5. Send Notification

```http
POST http://localhost:8080/api/notifications
Content-Type: application/json

{
  "bookingId": "PUT_BOOKING_ID_HERE",
  "userId": "asha@example.com",
  "recipient": "asha@example.com",
  "subject": "SkyWays booking confirmed",
  "body": "Your SkyWays booking has been confirmed."
}
```

When Kafka is enabled, `notification-service` can also listen to `payment-events` and send payment success/failure emails automatically.

## Error Handling Features

Every service has:

- Custom exceptions
- `GlobalExceptionHandler`
- Standard JSON error response
- Field validation errors
- Error log persistence in `error_logs`

Example:

```json
{
  "timestamp": "2026-04-30T10:15:30Z",
  "status": 409,
  "error": "FLIGHT_OVERBOOKED",
  "message": "Flight FULL does not have enough available seats",
  "path": "/api/bookings",
  "fieldErrors": []
}
```

## Failure Scenarios To Demonstrate

| Scenario | How to test |
| --- | --- |
| Invalid passenger details | Send empty name, bad email, future date of birth |
| Duplicate passenger | Use the same passport number twice |
| Overbooked flight | In mock mode, use `flightId: "FULL"` |
| Payment declined | Use `paymentMethodToken: "pm_card_declined"` |
| Payment provider timeout | Use `paymentMethodToken: "pm_timeout"` |
| Email provider rejection | Send notification to `customer@fail.test` |

## Kafka Events

Kafka is disabled by default for easy local learning.

Enable it:

```powershell
--skyways.kafka.enabled=true --spring.kafka.bootstrap-servers=localhost:9092
```

Topics:

```text
booking-events
payment-events
```

Events:

- `BOOKING_CREATED`
- `BOOKING_CONFIRMED`
- `BOOKING_PAYMENT_FAILED`
- `BOOKING_CANCELLED`
- `PAYMENT_SUCCEEDED`
- `PAYMENT_FAILED`
- `PAYMENT_REFUNDED`

## Kubernetes

Build images:

```powershell
docker build -t skyways/flight-service:latest ./flight-service
docker build -t skyways/booking-service:latest ./booking-service
docker build -t skyways/payment-service:latest ./payment-service
docker build -t skyways/notification-service:latest ./notification-service
docker build -t skyways/api-gateway:latest ./api-gateway
```

Apply manifests:

```powershell
kubectl apply -f k8s/skyways-platform.yaml
```

Access gateway:

```powershell
kubectl get svc -n skyways api-gateway
```

The manifest exposes gateway using NodePort `30080`.

## Files To Study First

Start here:

```text
flight-service/src/main/java/com/skyways/flight/controller/FlightController.java
booking-service/src/main/java/com/skyways/booking/controller/BookingController.java
payment-service/src/main/java/com/skyways/payment/controller/PaymentController.java
notification-service/src/main/java/com/skyways/notification/controller/NotificationController.java
api-gateway/src/main/resources/application.yml
```

Then study service logic:

```text
flight-service/src/main/java/com/skyways/flight/service/FlightService.java
booking-service/src/main/java/com/skyways/booking/service/BookingService.java
payment-service/src/main/java/com/skyways/payment/service/PaymentService.java
notification-service/src/main/java/com/skyways/notification/service/NotificationService.java
```

## What To Build Next

Good next improvements:

- Add a frontend using React or Angular
- Replace mock payment gateway with Stripe test mode
- Replace mock email provider with SendGrid sandbox
- Add JWT authentication
- Add service discovery with Eureka or Kubernetes DNS only
- Add distributed tracing with OpenTelemetry
- Add real Kafka retry/dead-letter topics
