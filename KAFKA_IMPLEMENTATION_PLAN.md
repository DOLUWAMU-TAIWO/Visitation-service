# Kafka Producer-Consumer Implementation Plan for Booking Events

## Overview
This document outlines the plan to transform the VisitingService into a comprehensive Kafka-based event-driven booking system. The service will produce booking events for every booking lifecycle change and consume events for processing and analytics.

## Current State Analysis

### ✅ Already Implemented
- **Kafka Dependencies**: Already added to `pom.xml`
- **Basic Kafka Setup**: 
  - `BookingTopicConfig.java` exists with basic topic configuration
  - `VisitingServiceApplication.java` has test producer sending 100 messages
  - Basic KafkaTemplate is working

### 🎯 Implementation Goals
1. **Event-Driven Architecture**: Every booking action triggers a Kafka event
2. **Idempotency**: Use booking ID as message key for consistent processing
3. **Rich Event Payloads**: Include extended booking context and metadata
4. **Reliable Processing**: Implement proper error handling and retry mechanisms
5. **Consumer Processing**: Handle events for notifications, analytics, and integrations

## Detailed Implementation Plan

### Phase 1: Event Structure & Configuration

#### 1.1 Enhanced Topic Configuration
**File**: `BookingTopicConfig.java`
```java
@Bean
public NewTopic bookingEventsTopic(){
    return TopicBuilder.name("booking-events")
            .partitions(6) // For scaling
            .replicas(1) // Adjust based on Kafka cluster
            .config("retention.ms", "604800000") // 7 days retention
            .config("cleanup.policy", "delete")
            .build();
}
```

#### 1.2 Event DTOs Creation
**New Files**:
- `dto/BookingEventDTO.java` - Main event wrapper
- `dto/BookingEventPayload.java` - Rich event payload
- `dto/BookingEventType.java` - Event type enumeration

**Event Types**:
```
BOOKING_INITIATED    - User clicks book button
BOOKING_CREATED      - Booking saved to database
BOOKING_ACCEPTED     - Landlord accepts booking
BOOKING_REJECTED     - Landlord rejects booking  
BOOKING_CANCELLED    - Booking cancelled
BOOKING_RESCHEDULED  - Dates changed
BOOKING_PAYMENT_UPDATED - Payment status changed
```

#### 1.3 Enhanced Kafka Configuration
**File**: `config/KafkaConfig.java`
- Producer configuration with idempotency
- Consumer configuration with manual acknowledgment
- JSON serialization/deserialization
- Error handling and retry policies

### Phase 2: Producer Service Implementation

#### 2.1 Booking Event Producer Service
**New File**: `service/BookingEventProducerService.java`

**Key Methods**:
```java
// For user initiating booking (before DB save)
sendBookingInitiated(UUID tenantId, UUID landlordId, UUID propertyId, 
                    LocalDate startDate, LocalDate endDate, 
                    String sessionId, String userAgent, String sourceIP)

// For successful booking creation
sendBookingCreated(ShortletBookingDTO booking)

// For status changes
sendBookingAccepted(ShortletBookingDTO booking, String previousStatus)
sendBookingRejected(ShortletBookingDTO booking, String previousStatus)
sendBookingCancelled(ShortletBookingDTO booking, String previousStatus)
sendBookingRescheduled(ShortletBookingDTO booking, LocalDate previousStartDate, LocalDate previousEndDate)

// For payment updates
sendBookingPaymentUpdated(ShortletBookingDTO booking, String previousPaymentStatus)
```

#### 2.2 Enhanced Event Payload
**Rich Context Data**:
- Core booking info (tenant, landlord, property, dates)
- Guest information (name, phone, guest count)
- Session tracking (sessionId, userAgent, sourceIP)
- Property context (title, location, type)
- Calculated fields (total nights, estimated amount)
- Previous state (for change events)
- Timestamps and metadata

### Phase 3: Service Layer Integration

#### 3.1 Update Booking Service Implementation
**File**: `service/impl/ShortletBookingServiceImpl.java`

**Integration Points**:
```java
@Transactional
public ShortletBookingDTO createBooking(...) {
    // Existing validation logic...
    
    // NEW: Send BOOKING_INITIATED event first
    eventProducer.sendBookingInitiated(tenantId, landlordId, propertyId, 
                                      startDate, endDate, sessionId, userAgent, sourceIP);
    
    // Existing booking creation logic...
    ShortletBooking saved = bookingRepository.save(booking);
    
    // UPDATED: Send BOOKING_CREATED event instead of basic notification
    eventProducer.sendBookingCreated(toDTO(saved));
    
    return toDTO(saved);
}

@Transactional  
public ShortletBookingDTO acceptBooking(UUID bookingId) {
    ShortletBooking booking = // ... existing logic
    String previousStatus = booking.getStatus().name();
    
    // ... existing acceptance logic
    
    // UPDATED: Send rich event instead of basic notification
    eventProducer.sendBookingAccepted(toDTO(booking), previousStatus);
}
```

#### 3.2 Controller Layer Enhancements
**Files**: `controller/ShortletUnifiedController.java`, `controller/BookingGraphQLController.java`

**Add Context Extraction**:
```java
@PostMapping("/bookings")
public ResponseEntity<?> createBooking(@RequestBody Map<String, String> body,
                                     HttpServletRequest request) {
    // Extract context information
    String sessionId = request.getSession().getId();
    String userAgent = request.getHeader("User-Agent");
    String sourceIP = request.getRemoteAddr();
    
    // Pass context to service
    ShortletBookingDTO dto = bookingService.createBooking(
        tenantId, landlordId, propertyId, startDate, endDate, 
        firstName, lastName, phoneNumber, guestNumber,
        sessionId, userAgent, sourceIP);
}
```

### Phase 4: Consumer Implementation

#### 4.1 Booking Event Consumer Service
**New File**: `service/BookingEventConsumerService.java`

**Consumer Methods**:
```java
@KafkaListener(topics = "booking-events", groupId = "visiting-service-group")
public void handleBookingEvent(@Payload BookingEventDTO event, 
                              @Header Map<String, Object> headers,
                              Acknowledgment ack) {
    // Process based on event type
    switch (event.getEventType()) {
        case BOOKING_INITIATED -> handleBookingInitiated(event);
        case BOOKING_CREATED -> handleBookingCreated(event);
        case BOOKING_ACCEPTED -> handleBookingAccepted(event);
        // ... other event types
    }
    ack.acknowledge(); // Manual acknowledgment
}
```

#### 4.2 Event Processing Logic
**Processing Examples**:
- **BOOKING_INITIATED**: Log user behavior, update analytics, fraud detection
- **BOOKING_CREATED**: Send notifications, update dashboards, trigger external integrations
- **BOOKING_ACCEPTED**: Send confirmation emails, update availability, sync with external calendars
- **BOOKING_REJECTED**: Send rejection notifications, release held inventory
- **BOOKING_CANCELLED**: Process refunds, update availability, send notifications

### Phase 5: Configuration & Properties

#### 5.1 Application Properties
**File**: `application.properties`
```properties
# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=visiting-service-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.enable-auto-commit=false

# Custom Topic Names
kafka.topic.booking-events=booking-events
kafka.topic.notification-events=notification-events

# Producer Settings
spring.kafka.producer.acks=all
spring.kafka.producer.retries=3
spring.kafka.producer.enable-idempotence=true
```

### Phase 6: Error Handling & Monitoring

#### 6.1 Dead Letter Queue Setup
**New File**: `config/KafkaErrorConfig.java`
- Configure dead letter topics for failed messages
- Implement retry mechanisms
- Set up error handling strategies

#### 6.2 Event Monitoring
**New File**: `service/BookingEventMonitoringService.java`
- Track event processing metrics
- Monitor consumer lag
- Alert on processing failures

## Implementation Benefits

### 🎯 Business Benefits
1. **Real-time Analytics**: Track booking funnel in real-time
2. **Improved Notifications**: Rich context for personalized messages
3. **Fraud Detection**: Monitor suspicious booking patterns
4. **Integration Ready**: Easy integration with external systems
5. **Audit Trail**: Complete booking lifecycle tracking

### 🔧 Technical Benefits
1. **Loose Coupling**: Services communicate via events
2. **Scalability**: Horizontal scaling with Kafka partitions
3. **Reliability**: Exactly-once processing with idempotency
4. **Flexibility**: Easy to add new event consumers
5. **Debugging**: Rich event logs for troubleshooting

## Event Message Example

```json
{
  "eventId": "123e4567-e89b-12d3-a456-426614174000",
  "eventType": "BOOKING_CREATED",
  "bookingId": "987fcdeb-51a2-43d1-b123-456789012345",
  "timestamp": "2024-12-25T10:30:00Z",
  "sourceService": "VisitingService",
  "version": "1.0",
  "payload": {
    "tenantId": "0d508b66-2175-457d-846b-915905f4cbcd",
    "landlordId": "eb22925c-1201-48b0-894d-373c825e2778",
    "propertyId": "1880e4c0-5bd6-4ab9-b06d-2e422298ef30",
    "startDate": "2025-11-17",
    "endDate": "2025-11-19",
    "status": "PENDING",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "guestNumber": 2,
    "totalNights": 2,
    "currency": "NGN",
    "bookingChannel": "web",
    "sessionId": "A1B2C3D4E5F6",
    "userAgent": "Mozilla/5.0...",
    "sourceIP": "192.168.1.100",
    "propertyTitle": "Luxury Apartment in Victoria Island",
    "propertyLocation": "Lagos, Nigeria",
    "paymentStatus": "PENDING",
    "createdAt": "2024-12-25T10:30:00Z"
  }
}
```

## Implementation Order

1. **Phase 1**: Create event DTOs and enhanced Kafka config
2. **Phase 2**: Implement producer service
3. **Phase 3**: Integrate producer into booking service
4. **Phase 4**: Implement basic consumer
5. **Phase 5**: Add configuration and properties
6. **Phase 6**: Implement error handling and monitoring

## Next Steps

1. Review and approve this implementation plan
2. Start with Phase 1 - create the event structure
3. Test with existing Kafka setup
4. Gradually integrate into booking workflow
5. Add consumers for specific business logic

This implementation will transform your booking service into a robust, event-driven system that provides real-time insights and enables seamless integrations.
