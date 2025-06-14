# VisitingService Architecture

This document provides an overview of the VisitingService architecture, explaining how each component works and interacts with others.

## System Overview

VisitingService is a Spring Boot application that manages property visits. It allows landlords to set availability slots for their properties, visitors to request visits, and both parties to manage the visit lifecycle. The system also collects feedback after visits and sends notifications for various events.

## Architecture Components

### Domain Model

The system is built around three main entities:

1. **AvailabilitySlot**: Represents a time period when a property is available for viewing
   - Contains property and landlord IDs
   - Tracks start and end times
   - Has a boolean flag to indicate if the slot is booked

2. **Visit**: Represents a property visit request and its lifecycle
   - Contains property, visitor, and landlord IDs
   - Tracks scheduling information (time, duration)
   - Has a status (PENDING, APPROVED, REJECTED, CANCELLED, RESCHEDULED, COMPLETED)
   - Contains metadata like creation time, update time, and notes

3. **VisitFeedback**: Represents feedback submitted after a visit
   - Links to a visit and visitor
   - Contains rating and comments
   - Tracks follow-up information (if needed, reason, status)

### Data Access Layer

The data access layer uses Spring Data JPA repositories:

1. **AvailabilitySlotRepository**: Manages availability slot data
   - Finds available slots for a property
   - Checks for overlapping slots

2. **VisitRepository**: Manages visit data
   - Finds visits by property, status, and time ranges
   - Used for both user-initiated queries and scheduled tasks

3. **VisitFeedbackRepository**: Manages feedback data
   - Finds feedback for specific visits

### Service Layer

The service layer contains the business logic:

1. **AvailabilitySlotService**: Manages availability slots
   - Creates new slots with validation (no past slots, no overlaps)
   - Retrieves available slots for a property

2. **VisitService**: Manages the visit lifecycle
   - Handles visit requests, approvals, rejections, cancellations, and completions
   - Retrieves visits by property and status
   - Sends notifications for visit status changes

3. **VisitFeedbackService**: Manages feedback
   - Submits feedback with automatic flagging for low ratings
   - Retrieves feedback for visits

4. **NotificationPublisher**: Handles sending notifications
   - Sends notifications for visit status changes
   - Sends reminders for upcoming visits
   - Prompts for feedback after completed visits

### API Layer

The API layer exposes RESTful endpoints:

1. **AvailabilityController**: Endpoints for availability slots
   - GET /api/availability/{propertyId}: Gets available slots for a property
   - POST /api/availability: Creates a new availability slot

2. **VisitController**: Endpoints for visit management
   - POST /api/visits: Creates a visit request
   - PUT /api/visits/{id}/approve: Approves a visit
   - PUT /api/visits/{id}/reject: Rejects a visit
   - PUT /api/visits/{id}/cancel: Cancels a visit
   - PUT /api/visits/{id}/complete: Completes a visit
   - GET /api/visits/property/{propertyId}: Gets visits for a property

3. **VisitFeedbackController**: Endpoints for feedback
   - POST /api/feedback: Submits feedback
   - GET /api/feedback/{visitId}: Gets feedback for a visit

### Background Processing

The system uses scheduled tasks for automation:

1. **VisitScheduler**: Runs scheduled tasks
   - Sends reminders 24 hours and 1 hour before visits
   - Automatically completes past visits

### External Integration

The system integrates with an external notification service:

1. **NotificationPublisherImpl**: Sends HTTP requests to an external notification service
   - Uses RestTemplate to send event payloads
   - Configured with a URL from application properties

## Data Flow

1. **Availability Management**:
   - Landlords create availability slots through the API
   - The service validates the slots (no past slots, no overlaps)
   - Visitors can query available slots for a property

2. **Visit Lifecycle**:
   - Visitors request visits through the API
   - The service creates a visit with PENDING status and notifies the landlord
   - Landlords approve or reject visits
   - Visitors can cancel visits
   - Visits are automatically completed after their scheduled time
   - Notifications are sent at each step

3. **Feedback Collection**:
   - After a visit is completed, a feedback prompt is sent
   - Visitors submit feedback through the API
   - Low ratings are automatically flagged for follow-up

## Exception Handling

The system uses custom exceptions like `InvalidRequestException` to handle invalid requests. Service methods validate inputs and throw appropriate exceptions, which are then handled by Spring's exception handling mechanism.

## Configuration

The system uses Spring's configuration mechanisms:

1. **RestTemplateConfig**: Configures the RestTemplate bean used for HTTP requests
2. **Application Properties**: Contains configuration like the notification service URL

## Conclusion

VisitingService follows a layered architecture with clear separation of concerns:
- Domain model defines the core entities
- Repositories handle data access
- Services contain business logic
- Controllers expose the API
- Schedulers handle background tasks

This architecture makes the system modular, maintainable, and extensible.