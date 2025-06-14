# Frontend Implementation Guide for Visiting Service

## Table of Contents
1. [Overview](#overview)
2. [System Architecture](#system-architecture)
3. [API Reference](#api-reference)
4. [Landlord Integration Guide](#landlord-integration-guide)
5. [Tenant Integration Guide](#tenant-integration-guide)
6. [Design Suggestions](#design-suggestions)
7. [Common Workflows](#common-workflows)
8. [Error Handling](#error-handling)

## Overview

The Visiting Service allows tenants to schedule visits to properties listed by landlords. It supports the complete visit lifecycle including:

- Availability management
- Visit requests
- Approval/rejection workflows
- Visit cancellation and rescheduling
- Feedback collection
- Notifications and reminders

This guide provides detailed instructions for frontend developers to integrate with the Visiting Service API.

## System Architecture

The Visiting Service is a Spring Boot application that exposes RESTful endpoints for managing property visits. It integrates with other services:

- **User Service**: For user authentication and profile information
- **Listing Service**: For property details
- **Notification Service**: For sending notifications and reminders

The system is built around three main entities:

1. **AvailabilitySlot**: Time periods when a property is available for viewing
2. **Visit**: Property visit requests and their lifecycle
3. **VisitFeedback**: Feedback submitted after visits

## API Reference

### Availability Management

#### Get Available Slots for a Property

```
GET /api/availability/{propertyId}
```

**Response:**
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "propertyId": "123e4567-e89b-12d3-a456-426614174001",
    "landlordId": "123e4567-e89b-12d3-a456-426614174002",
    "startTime": "2023-07-15T14:00:00+01:00",
    "endTime": "2023-07-15T15:00:00+01:00",
    "booked": false,
    "createdAt": "2023-07-10T09:30:00+01:00",
    "updatedAt": "2023-07-10T09:30:00+01:00"
  }
]
```

#### Create Availability Slot

```
POST /api/availability
```

**Request:**
```json
{
  "propertyId": "123e4567-e89b-12d3-a456-426614174001",
  "landlordId": "123e4567-e89b-12d3-a456-426614174002",
  "startTime": "2023-07-15T14:00:00+01:00",
  "endTime": "2023-07-15T15:00:00+01:00"
}
```

**Response:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "propertyId": "123e4567-e89b-12d3-a456-426614174001",
  "landlordId": "123e4567-e89b-12d3-a456-426614174002",
  "startTime": "2023-07-15T14:00:00+01:00",
  "endTime": "2023-07-15T15:00:00+01:00",
  "booked": false,
  "createdAt": "2023-07-10T09:30:00+01:00",
  "updatedAt": "2023-07-10T09:30:00+01:00"
}
```

#### Check Slot Availability

```
GET /api/availability/{propertyId}/check?start={startTime}&end={endTime}
```

**Response:**
```json
true
```

### Visit Management

#### Request a Visit

```
POST /api/visits
```

**Request:**
```json
{
  "propertyId": "123e4567-e89b-12d3-a456-426614174001",
  "landlordId": "123e4567-e89b-12d3-a456-426614174002",
  "visitorId": "123e4567-e89b-12d3-a456-426614174003",
  "slotId": "123e4567-e89b-12d3-a456-426614174000",
  "notes": "I'm interested in this property and would like to see the kitchen in particular."
}
```

**Response:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174004",
  "propertyId": "123e4567-e89b-12d3-a456-426614174001",
  "visitorId": "123e4567-e89b-12d3-a456-426614174003",
  "landlordId": "123e4567-e89b-12d3-a456-426614174002",
  "scheduledAt": "2023-07-15T14:00:00+01:00",
  "durationMinutes": 60,
  "status": "PENDING",
  "rescheduledFromId": null,
  "createdAt": "2023-07-10T10:15:00+01:00",
  "updatedAt": "2023-07-10T10:15:00+01:00",
  "notes": "I'm interested in this property and would like to see the kitchen in particular.",
  "slotId": "123e4567-e89b-12d3-a456-426614174000"
}
```

#### Approve a Visit

```
PUT /api/visits/{id}/approve
```

**Response:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174004",
  "propertyId": "123e4567-e89b-12d3-a456-426614174001",
  "visitorId": "123e4567-e89b-12d3-a456-426614174003",
  "landlordId": "123e4567-e89b-12d3-a456-426614174002",
  "scheduledAt": "2023-07-15T14:00:00+01:00",
  "durationMinutes": 60,
  "status": "APPROVED",
  "rescheduledFromId": null,
  "createdAt": "2023-07-10T10:15:00+01:00",
  "updatedAt": "2023-07-10T10:20:00+01:00",
  "notes": "I'm interested in this property and would like to see the kitchen in particular.",
  "slotId": "123e4567-e89b-12d3-a456-426614174000"
}
```

#### Reject a Visit

```
PUT /api/visits/{id}/reject
```

**Response:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174004",
  "propertyId": "123e4567-e89b-12d3-a456-426614174001",
  "visitorId": "123e4567-e89b-12d3-a456-426614174003",
  "landlordId": "123e4567-e89b-12d3-a456-426614174002",
  "scheduledAt": "2023-07-15T14:00:00+01:00",
  "durationMinutes": 60,
  "status": "REJECTED",
  "rescheduledFromId": null,
  "createdAt": "2023-07-10T10:15:00+01:00",
  "updatedAt": "2023-07-10T10:20:00+01:00",
  "notes": "I'm interested in this property and would like to see the kitchen in particular.",
  "slotId": "123e4567-e89b-12d3-a456-426614174000"
}
```

#### Cancel a Visit

```
PUT /api/visits/{id}/cancel
```

**Response:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174004",
  "propertyId": "123e4567-e89b-12d3-a456-426614174001",
  "visitorId": "123e4567-e89b-12d3-a456-426614174003",
  "landlordId": "123e4567-e89b-12d3-a456-426614174002",
  "scheduledAt": "2023-07-15T14:00:00+01:00",
  "durationMinutes": 60,
  "status": "CANCELLED",
  "rescheduledFromId": null,
  "createdAt": "2023-07-10T10:15:00+01:00",
  "updatedAt": "2023-07-10T10:25:00+01:00",
  "notes": "I'm interested in this property and would like to see the kitchen in particular.",
  "slotId": "123e4567-e89b-12d3-a456-426614174000"
}
```

#### Complete a Visit

```
PUT /api/visits/{id}/complete
```

**Response:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174004",
  "propertyId": "123e4567-e89b-12d3-a456-426614174001",
  "visitorId": "123e4567-e89b-12d3-a456-426614174003",
  "landlordId": "123e4567-e89b-12d3-a456-426614174002",
  "scheduledAt": "2023-07-15T14:00:00+01:00",
  "durationMinutes": 60,
  "status": "COMPLETED",
  "rescheduledFromId": null,
  "createdAt": "2023-07-10T10:15:00+01:00",
  "updatedAt": "2023-07-15T15:05:00+01:00",
  "notes": "I'm interested in this property and would like to see the kitchen in particular.",
  "slotId": "123e4567-e89b-12d3-a456-426614174000"
}
```

#### Get Visits for a Property

```
GET /api/visits/property/{propertyId}
```

**Response:**
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174004",
    "propertyId": "123e4567-e89b-12d3-a456-426614174001",
    "visitorId": "123e4567-e89b-12d3-a456-426614174003",
    "landlordId": "123e4567-e89b-12d3-a456-426614174002",
    "scheduledAt": "2023-07-15T14:00:00+01:00",
    "durationMinutes": 60,
    "status": "APPROVED",
    "rescheduledFromId": null,
    "createdAt": "2023-07-10T10:15:00+01:00",
    "updatedAt": "2023-07-10T10:20:00+01:00",
    "notes": "I'm interested in this property and would like to see the kitchen in particular.",
    "slotId": "123e4567-e89b-12d3-a456-426614174000"
  }
]
```

### Feedback Management

#### Submit Feedback

```
POST /api/feedback
```

**Request:**
```json
{
  "visitId": "123e4567-e89b-12d3-a456-426614174004",
  "visitorId": "123e4567-e89b-12d3-a456-426614174003",
  "rating": 4,
  "comment": "The property was nice and the landlord was very helpful. The kitchen was smaller than I expected though."
}
```

**Response:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174005",
  "visitId": "123e4567-e89b-12d3-a456-426614174004",
  "visitorId": "123e4567-e89b-12d3-a456-426614174003",
  "rating": 4,
  "comment": "The property was nice and the landlord was very helpful. The kitchen was smaller than I expected though.",
  "followUpNeeded": false,
  "followUpReason": null,
  "followUpStatus": null,
  "handlerId": null,
  "createdAt": "2023-07-15T16:30:00+01:00"
}
```

#### Get Feedback for a Visit

```
GET /api/feedback/{visitId}
```

**Response:**
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174005",
    "visitId": "123e4567-e89b-12d3-a456-426614174004",
    "visitorId": "123e4567-e89b-12d3-a456-426614174003",
    "rating": 4,
    "comment": "The property was nice and the landlord was very helpful. The kitchen was smaller than I expected though.",
    "followUpNeeded": false,
    "followUpReason": null,
    "followUpStatus": null,
    "handlerId": null,
    "createdAt": "2023-07-15T16:30:00+01:00"
  }
]
```

## Landlord Integration Guide

### Authentication

Before making any API calls, ensure the landlord is authenticated. The authentication token should be included in the Authorization header of all requests.

### Workflow

1. **Create Availability Slots**
   - Landlords need to define when their properties are available for viewing
   - Use the `POST /api/availability` endpoint to create slots
   - Ensure slots don't overlap and are in the future

2. **Manage Visit Requests**
   - Retrieve pending visits using `GET /api/visits/property/{propertyId}`
   - Filter visits by status (PENDING) on the frontend
   - Approve visits using `PUT /api/visits/{id}/approve`
   - Reject visits using `PUT /api/visits/{id}/reject`

3. **View Upcoming Visits**
   - Retrieve all visits using `GET /api/visits/property/{propertyId}`
   - Filter visits by status (APPROVED) on the frontend
   - Display in a calendar or list view

4. **View Visit History**
   - Retrieve all visits using `GET /api/visits/property/{propertyId}`
   - Filter visits by status (COMPLETED, CANCELLED, REJECTED) on the frontend
   - Allow sorting by date

5. **View Feedback**
   - For each completed visit, retrieve feedback using `GET /api/feedback/{visitId}`
   - Display ratings and comments

### UI Components

1. **Availability Calendar**
   - Interactive calendar showing available and booked slots
   - Allow creating new slots by selecting time ranges
   - Show visit details on booked slots

2. **Visit Request Dashboard**
   - List of pending visit requests with approve/reject actions
   - Show visitor details, requested time, and notes

3. **Visit Schedule**
   - Calendar or timeline view of upcoming approved visits
   - Include visitor details and property information

4. **Feedback Dashboard**
   - Summary of ratings across properties
   - List of recent feedback with detailed comments

## Tenant Integration Guide

### Authentication

Before making any API calls, ensure the tenant is authenticated. The authentication token should be included in the Authorization header of all requests.

### Workflow

1. **View Available Slots**
   - Use the `GET /api/availability/{propertyId}` endpoint to retrieve available slots
   - Display slots in a calendar or list view

2. **Request a Visit**
   - Select an available slot
   - Use the `POST /api/visits` endpoint to request a visit
   - Include any notes or questions for the landlord

3. **Manage Visit Requests**
   - Retrieve all visits using `GET /api/visits/property/{propertyId}` (filter by visitorId on frontend)
   - View status (PENDING, APPROVED, REJECTED)
   - Cancel if needed using `PUT /api/visits/{id}/cancel`

4. **Attend Visit**
   - Receive reminders (handled by backend)
   - View visit details before attending

5. **Submit Feedback**
   - After a visit is completed, use `POST /api/feedback` to submit feedback
   - Include rating and comments

### UI Components

1. **Property Availability Viewer**
   - Calendar showing available slots for a property
   - Allow selecting a slot to request a visit

2. **Visit Request Form**
   - Form to submit visit request with selected slot
   - Fields for notes or special requests

3. **My Visits Dashboard**
   - List of all visits with status indicators
   - Actions based on status (cancel, reschedule)
   - Countdown to upcoming visits

4. **Feedback Form**
   - Star rating component
   - Text area for comments
   - Submit button

## Design Suggestions

### Color Coding

Use consistent color coding for visit statuses:
- PENDING: Yellow/Orange
- APPROVED: Green
- REJECTED: Red
- CANCELLED: Gray
- COMPLETED: Blue
- RESCHEDULED: Purple

### Mobile Responsiveness

Ensure all UI components work well on mobile devices:
- Use responsive calendars that adapt to screen size
- Implement collapsible sections for visit details
- Ensure touch-friendly UI elements for mobile users

### Notifications

Implement frontend notifications to complement backend notifications:
- Toast messages for status changes
- Browser notifications for reminders
- Email notifications (handled by backend)

### User Experience

1. **Landlord Dashboard**
   - Group visits by property
   - Show statistics (approval rate, average rating)
   - Highlight urgent actions (pending requests)

2. **Tenant Interface**
   - Show property images alongside availability slots
   - Provide clear visit status tracking
   - Include maps and directions for approved visits

## Common Workflows

### Visit Request to Completion (Tenant Perspective)

1. Tenant views available slots for a property
2. Tenant selects a slot and submits a visit request
3. Tenant receives notification when request is approved/rejected
4. If approved, tenant receives reminders before the visit
5. Tenant attends the visit
6. Visit is automatically marked as completed
7. Tenant receives prompt to submit feedback
8. Tenant submits feedback

### Visit Management (Landlord Perspective)

1. Landlord creates availability slots for a property
2. Landlord receives notification when a visit is requested
3. Landlord approves or rejects the visit
4. If approved, landlord receives reminders before the visit
5. Landlord conducts the visit
6. Visit is automatically marked as completed
7. Landlord can view feedback submitted by the tenant

## Error Handling

The API returns standard HTTP status codes:

- 200 OK: Request successful
- 400 Bad Request: Invalid input (e.g., booking a past slot)
- 401 Unauthorized: Authentication required
- 403 Forbidden: Insufficient permissions
- 404 Not Found: Resource not found
- 409 Conflict: Resource conflict (e.g., double booking)
- 500 Internal Server Error: Server error

Frontend implementations should handle these errors gracefully:

1. Display user-friendly error messages
2. Provide recovery options where possible
3. Log errors for debugging
4. Implement retry logic for network failures

Common error scenarios:
- Slot already booked: Show alternative available slots
- Visit in terminal state: Disable modification actions
- Invalid time selection: Highlight valid time ranges