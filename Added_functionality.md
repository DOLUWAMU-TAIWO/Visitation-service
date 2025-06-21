# New Feature Documentation (Since June 20, 2025)

This document outlines the new features, REST endpoints, and GraphQL API capabilities added to the Visiting Service. This guide is intended for frontend developers to understand how to integrate with the new functionality.

---

## 1. Shortlet Booking & Availability System (REST API)

A complete system for managing shortlet bookings and landlord availability has been implemented.

### Base URL
All REST endpoints are prefixed with `/api`.

### Endpoints

#### Availability Management
Managed by `ShortletUnifiedController`.

*   **Set Availability for a Landlord**
    *   **Endpoint:** `POST /api/shortlets/availability/{landlordId}`
    *   **Description:** Allows a landlord to set a date range when their property is available.
    *   **Request Body:**
        ```json
        {
          "startDate": "2025-07-10",
          "endDate": "2025-07-20"
        }
        ```
    *   **Success Response (200 OK):**
        ```json
        {
          "id": "generated-uuid",
          "startDate": "2025-07-10",
          "endDate": "2025-07-20"
        }
        ```

*   **Get Availability for a Landlord**
    *   **Endpoint:** `GET /api/shortlets/availability/{landlordId}`
    *   **Description:** Retrieves all availability ranges for a given landlord.
    *   **Success Response (200 OK):**
        ```json
        [
          {
            "id": "uuid-1",
            "startDate": "2025-07-10",
            "endDate": "2025-07-20"
          }
        ]
        ```

*   **Delete an Availability Range**
    *   **Endpoint:** `DELETE /api/shortlets/availability/{availabilityId}`
    *   **Description:** Removes a specific availability range.
    *   **Success Response (200 OK):**
        ```json
        {
          "message": "Availability deleted successfully."
        }
        ```

#### Booking Management
Managed by `ShortletUnifiedController`.

*   **Create a Booking**
    *   **Endpoint:** `POST /api/shortlets/bookings`
    *   **Description:** Allows a tenant to book a property for a specific date range. The system checks if the dates are available.
    *   **Request Body:**
        ```json
        {
          "tenantId": "tenant-uuid",
          "landlordId": "landlord-uuid",
          "propertyId": "property-uuid",
          "startDate": "2025-07-12",
          "endDate": "2025-07-15"
        }
        ```
    *   **Success Response (200 OK):** Returns the created booking with `PENDING` status.

*   **Get Bookings for a Landlord**
    *   **Endpoint:** `GET /api/shortlets/bookings/{landlordId}`
    *   **Description:** Retrieves all bookings for a landlord. Supports pagination.
    *   **Query Parameters:** `page` (e.g., 0), `size` (e.g., 20).

*   **Accept, Reject, Cancel, Reschedule a Booking**
    *   **Accept:** `POST /api/shortlets/bookings/{bookingId}/accept`
    *   **Reject:** `POST /api/shortlets/bookings/{bookingId}/reject`
    *   **Cancel:** `POST /api/shortlets/bookings/{bookingId}/cancel`
    *   **Reschedule:** `POST /api/shortlets/bookings/{bookingId}/reschedule`
        *   **Request Body for Reschedule:**
            ```json
            {
              "startDate": "2025-08-01",
              "endDate": "2025-08-05"
            }
            ```
    *   **Success Response (200 OK):** A confirmation message.

---

## 2. Dynamic Visiting Slot Creation (REST API)

Landlords can now generate multiple visiting slots for a day within a specified time range.

*   **Endpoint:** `POST /api/slots/range`
*   **Description:** Creates multiple `AvailabilitySlot`s based on a date, a start/end time, and an interval.
*   **Request Body:**
    ```json
    {
      "propertyId": "property-uuid",
      "landlordId": "landlord-uuid",
      "date": "2025-07-10",
      "startTime": "09:00",
      "endTime": "17:00",
      "intervalMinutes": 60
    }
    ```
*   **Success Response (200 OK):** A list of the created `AvailabilitySlot` objects.

---

## 3. Calendar and Booking History (REST API)

New endpoints to support rich frontend views.

*   **Get Calendar View**
    *   **Endpoint:** `GET /api/calendar/{propertyId}`
    *   **Description:** Fetches all availability slots and bookings for a property within a given date range.
    *   **Query Parameters:** `from` (ISO 8601 DateTime), `to` (ISO 8601 DateTime).
    *   **Example:** `?from=2025-07-01T00:00:00Z&to=2025-07-31T23:59:59Z`
    *   **Success Response (200 OK):**
        ```json
        {
          "slots": [ ... ],
          "bookings": [ ... ]
        }
        ```

*   **Search Booking History**
    *   **Endpoint:** `POST /api/bookings/history/search`
    *   **Description:** Provides advanced, paginated filtering for booking history.
    *   **Request Body:**
        ```json
        {
          "landlordId": "landlord-uuid",
          "status": "ACCEPTED",
          "fromDate": "2025-06-01",
          "toDate": "2025-06-30",
          "page": 0,
          "size": 10
        }
        ```

---

## 4. GraphQL API

A comprehensive GraphQL API has been added to provide a flexible and efficient way to interact with the system.

*   **Endpoint:** `POST /graphql`
*   **Web Interface for Testing:** `http://localhost:8080/graphiql`

### Key Queries

*   **Get Calendar View:**
    ```graphql
    query GetCalendar {
      calendar(propertyId: "...", from: "2025-07-01T00:00:00Z", to: "2025-07-31T23:59:59Z") {
        slots { id, startTime, endTime, booked }
        bookings { id, startDate, endDate, status }
      }
    }
    ```

*   **Get Filtered Bookings:**
    ```graphql
    query GetBookings {
      bookings(filter: { landlordId: "...", status: ACCEPTED }) {
        id
        status
        startDate
        endDate
      }
    }
    ```

*   **Get Filtered Visits:**
    ```graphql
    query GetVisits {
      visits(filter: { visitorId: "...", status: APPROVED }) {
        id
        status
        scheduledDate
      }
    }
    ```

### Key Mutations

*   **Create a Booking:**
    ```graphql
    mutation CreateBooking {
      createBooking(input: {
        tenantId: "...",
        landlordId: "...",
        propertyId: "...",
        startDate: "2025-07-15",
        endDate: "2025-07-20"
      }) {
        id
        status
      }
    }
    ```

*   **Approve a Visit:**
    ```graphql
    mutation ApproveVisit {
      approveVisit(id: "...") {
        id
        status
      }
    }
    ```

*   **Request a Visit:**
    ```graphql
    mutation RequestVisit {
      requestVisit(input: {
        visitorId: "...",
        landlordId: "...",
        propertyId: "...",
        slotId: "..."
      }) {
        id
        status
      }
    }
    ```

This documentation covers all major additions. For detailed field information on each type, please refer to the GraphQL schema located at `src/main/resources/graphql/schema.graphqls`.

