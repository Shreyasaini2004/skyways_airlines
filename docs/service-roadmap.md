# SkyWays Service Roadmap

Build the platform in this order so each service has a clear reason to exist.

## 1. Booking Service

Status: implemented.

Responsibilities:

- Create bookings
- Validate passengers
- Track booking status
- Handle overbooked flights
- Log recoverable and unexpected errors

## 2. Flight Service

Status: implemented.

Responsibilities:

- Search flights
- Compare prices
- Hold seat inventory
- Provide real availability APIs that can replace the current booking mock
- Later integrate Skyscanner or a GDS provider

Main endpoints:

- `GET /api/flights/search`
- `GET /api/flights/{flightId}`
- `POST /api/flights/{flightId}/seat-holds`
- `DELETE /api/flights/{flightId}/seat-holds/{holdId}`

## 3. Payment Service

Status: implemented as a mock provider.

Responsibilities:

- Create payment intents
- Handle mock Stripe-style success and failure
- Optionally update booking status through HTTP
- Later publish payment events to Kafka
- Trigger booking confirmation or failure

Main endpoints:

- `POST /api/payments`
- `POST /api/payments/webhook`
- `GET /api/payments/{paymentId}`

## 4. Notification Service

Status: implemented with mock SendGrid behavior.

Responsibilities:

- Listen for booking and payment events
- Send confirmation and failure emails
- Store notification history
- Later integrate SendGrid

## 5. API Gateway

Status: implemented with Spring Cloud Gateway.

Responsibilities:

- Route requests to services
- Add authentication filters
- Centralize CORS and request logging
- Hide internal service ports from clients

## 6. Kafka Event Flow

Status: implemented as optional event publishing/listening. It is disabled by default and enabled with `skyways.kafka.enabled=true`.

Initial topics:

- `booking-events`
- `payment-events`
- `notification-events`
- `error-events`

Useful events:

- `BookingCreatedEvent`
- `PaymentSucceededEvent`
- `PaymentFailedEvent`
- `BookingConfirmedEvent`
- `BookingCancelledEvent`
- `NotificationSentEvent`
