# QoreLabs Shortlet Booking API Documentation

## Overview
This document describes the full capabilities of the QoreLabs Shortlet Booking system, including REST and GraphQL endpoints, booking/availability logic, and integration details. All endpoints are accessible via the NGINX proxy as configured in your production environment.

---

## NGINX Proxy Configuration Reference
All API endpoints are exposed via the following base URL:

```
https://qorelabs.xyz
```

Relevant proxy paths:
- `/api/shortlets/` → Shortlet availability & bookings (proxied to backend)
- `/api/bookings/` → Booking history
- `/api/calendar/` → Calendar controller
- `/graphql` → GraphQL endpoint

All requests require the following header:
- `Authorization: Bearer DAEC6812-7A31-40DE-832D-D1167D15A77F`

---

## REST API Endpoints

### 1. Create Shortlet Availability
**POST** `/api/shortlets/availability/{landlordId}`

**Request Body:**
```json
{
  "propertyId": "<property-uuid>",
  "startDate": "YYYY-MM-DD",
  "endDate": "YYYY-MM-DD"
}
```

**Response:**
```json
{
  "id": "<availability-uuid>",
  "startDate": "YYYY-MM-DD",
  "endDate": "YYYY-MM-DD",
  "propertyId": "<property-uuid>",
  "landlordId": "<landlord-uuid>"
}
```

---

### 2. Create Shortlet Booking
**POST** `/api/shortlets/bookings`

**Request Body:**
```json
{
  "tenantId": "<tenant-uuid>",
  "landlordId": "<landlord-uuid>",
  "propertyId": "<property-uuid>",
  "startDate": "YYYY-MM-DD",
  "endDate": "YYYY-MM-DD",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890"
}
```

**Response:**
```json
{
  "id": "<booking-uuid>",
  "tenantId": "<tenant-uuid>",
  "landlordId": "<landlord-uuid>",
  "propertyId": "<property-uuid>",
  "startDate": "YYYY-MM-DD",
  "endDate": "YYYY-MM-DD",
  "status": "PENDING",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "paymentStatus": "PENDING",
  "paymentReference": null,
  "paymentAmount": null
}
```

---

### 3. Update Booking Payment (REST)
**POST** `/api/shortlets/bookings/{bookingId}/payment`

**Request Body:**
```json
{
  "paymentStatus": "PAID", // or PENDING, FAILED
  "paymentReference": "TXN-123456",
  "paymentAmount": 100.00
}
```

**Response:**
Returns the updated booking object (see above).

---

### 4. Accept/Reject/Cancel a Booking
**Accept:**
- `POST /api/shortlets/bookings/{bookingId}/accept`

**Reject:**
- `POST /api/shortlets/bookings/{bookingId}/reject`

**Cancel:**
- `POST /api/shortlets/bookings/{bookingId}/cancel`

**Response:**
```json
{
  "id": "<booking-uuid>",
  ...
  "status": "ACCEPTED" | "REJECTED" | "CANCELLED"
}
```

---

### 5. Get Bookings (History)
**GET** `/api/bookings/{landlordId}`

**Query Params:**
- `page` (default: 0)
- `size` (default: 20)

**Response:**
```json
[
  {
    "id": "<booking-uuid>",
    ...
  },
  ...
]
```

---

### 6. Calendar View
**GET** `/api/calendar/{propertyId}?from=YYYY-MM-DD&to=YYYY-MM-DD`

Returns available slots and bookings for a property in a date range.

**Response Example:**
```json
{
  "slots": [
    {
      "id": "<slot-uuid>",
      "startTime": "2025-11-17T09:00:00+01:00",
      "endTime": "2025-11-17T10:00:00+01:00",
      "booked": false
    }
  ],
  "bookings": [
    {
      "id": "<booking-uuid>",
      "startDate": "2025-11-17T00:00:00+01:00",
      "endDate": "2025-11-19T00:00:00+01:00",
      "status": "PENDING"
    }
  ]
}
```

---

## GraphQL API
**Endpoint:** `/graphql`

### Example Mutations
- `createBooking(input: Map<String, String>): ShortletBookingDTO`
- `acceptBooking(id: String): ShortletBookingDTO`
- `rejectBooking(id: String): ShortletBookingDTO`
- `cancelBooking(id: String): ShortletBookingDTO`
- `rescheduleBooking(id: String, startDate: String, endDate: String): ShortletBookingDTO`
- `updateBookingPayment(bookingId: String, paymentStatus: String, paymentReference: String, paymentAmount: Float): ShortletBookingDTO`

### Example Queries
- `booking(id: String): ShortletBookingDTO`
- `bookings(filter: BookingFilterDTO): [ShortletBookingDTO]`
- `calendar(propertyId: String, from: String, to: String): CalendarViewDTO`

---

## Booking & Availability Logic
- **Availability is tracked per property and landlord.**
- **Multiple non-overlapping availability slots** can exist for a property.
- **Multiple PENDING bookings** can exist for the same slot (optimistic concurrency).
- **Only one booking can be ACCEPTED** for a slot; all overlapping PENDING bookings are rejected when one is accepted.
- **Reminders** are sent at 24h and 1h before booking start, based on Africa/Lagos time, and only once per booking per window.

---

## Timezone
- All date/time logic (including reminders) is based on `Africa/Lagos` timezone.

---

## Security
- All endpoints require the `Authorization` header as configured in NGINX.
- All requests and responses use JSON.

---

## Example cURL: Create Booking
```sh
curl -X POST "https://qorelabs.xyz/api/shortlets/bookings" \
  -H "Authorization: Bearer DAEC6812-7A31-40DE-832D-D1167D15A77F" \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "0d508b66-2175-457d-846b-915905f4cbcd",
    "landlordId": "eb22925c-1201-48b0-894d-373c825e2778",
    "propertyId": "1880e4c0-5bd6-4ab9-b06d-2e422298ef30",
    "startDate": "2025-11-17",
    "endDate": "2025-11-19",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890"
  }'
```

---

## Notes for Frontend Integration
- Always use the NGINX-proxied URLs (e.g., `https://qorelabs.xyz/api/shortlets/...`).
- Always include the required `Authorization` header.
- All date/times should be in `YYYY-MM-DD` format and interpreted as Africa/Lagos time.
- For booking status transitions, use the appropriate endpoint (accept, reject, cancel).
- For calendar and booking history, use the provided endpoints for efficient data retrieval.
- Include `firstName`, `lastName`, and `phoneNumber` in all booking requests.
- Handle payment updates via the `/api/shortlets/bookings/{bookingId}/payment` endpoint or the `updateBookingPayment` GraphQL mutation.

---

For further details, see the backend source code or contact the backend team.
