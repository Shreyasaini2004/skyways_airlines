# SkyWays AngularJS Frontend

This frontend is a small AngularJS app served by Node.js. The Node server proxies `/api/*` to the existing SkyWays API gateway, so the browser uses one origin while the backend remains on `http://localhost:8080`.

## Run

Start the backend services first, then run:

```powershell
cd "C:\Users\DELL\Documents\New project\frontend"
npm start
```

Open:

```text
http://localhost:4200
```

To point the frontend to a different gateway:

```powershell
$env:API_BASE_URL="http://localhost:30080"; npm start
```

## Integrated Backend APIs

- `GET /api/flights/search`
- `GET /api/flights/provider-search`
- `POST /api/bookings`
- `GET /api/bookings?userId=...`
- `PATCH /api/bookings/{id}/confirm`
- `PATCH /api/bookings/{id}/cancel`
- `POST /api/payments`
- `GET /api/payments?userId=...`
- `POST /api/notifications`
- `GET /api/notifications?userId=...`
