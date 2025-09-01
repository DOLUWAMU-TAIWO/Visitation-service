package dev.visitingservice.service;

import dev.visitingservice.dto.BookingEventDTO;
import dev.visitingservice.dto.BookingEventPayload;
import dev.visitingservice.dto.BookingEventType;
import dev.visitingservice.dto.ShortletBookingDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class BookingEventProducerService {

    private static final Logger logger = LoggerFactory.getLogger(BookingEventProducerService.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate; // FIX: Use Object type to match config

    @Value("${kafka.topic.booking-events:booking-events}")
    private String bookingEventsTopic;

    /**
     * Send BOOKING_INITIATED event - triggered when user clicks book button (before DB save)
     */
    public void sendBookingInitiated(UUID tenantId, UUID landlordId, UUID propertyId,
                                     LocalDate startDate, LocalDate endDate,
                                     String sessionId, String userAgent, String sourceIP) {
        try {
            BookingEventPayload payload = BookingEventPayload.forBookingInitiated(
                    tenantId, landlordId, propertyId, startDate, endDate);

            // Add session tracking info
            payload.setSessionId(sessionId);
            payload.setUserAgent(userAgent);
            payload.setSourceIP(sourceIP);
            payload.setBookingChannel("web"); // Can be enhanced to detect channel

            // Generate a temporary booking ID for tracking (since no DB booking exists yet)
            UUID tempBookingId = UUID.randomUUID();

            BookingEventDTO event = new BookingEventDTO(BookingEventType.BOOKING_INITIATED, tempBookingId, payload);

            sendEvent(event, "BOOKING_INITIATED");

        } catch (Exception e) {
            logger.error("Failed to send BOOKING_INITIATED event for tenant {} and property {}",
                    tenantId, propertyId, e);
        }
    }

    /**
     * Send BOOKING_CREATED event - triggered when booking is successfully saved to database
     */
    public void sendBookingCreated(ShortletBookingDTO booking) {
        try {
            BookingEventPayload payload = BookingEventPayload.fromBookingDTO(booking);
            payload.setCreatedAt(OffsetDateTime.now());

            BookingEventDTO event = new BookingEventDTO(BookingEventType.BOOKING_CREATED, booking.getId(), payload);

            sendEvent(event, "BOOKING_CREATED");

        } catch (Exception e) {
            logger.error("Failed to send BOOKING_CREATED event for booking {}", booking.getId(), e);
        }
    }

    /**
     * Send BOOKING_ACCEPTED event - triggered when landlord accepts booking
     */
    public void sendBookingAccepted(ShortletBookingDTO booking, String previousStatus) {
        try {
            BookingEventPayload payload = BookingEventPayload.fromBookingDTO(booking);
            payload.setPreviousStatus(previousStatus);
            payload.setUpdatedAt(OffsetDateTime.now());

            BookingEventDTO event = new BookingEventDTO(BookingEventType.BOOKING_ACCEPTED, booking.getId(), payload);

            sendEvent(event, "BOOKING_ACCEPTED");

        } catch (Exception e) {
            logger.error("Failed to send BOOKING_ACCEPTED event for booking {}", booking.getId(), e);
        }
    }

    /**
     * Enhanced BOOKING_ACCEPTED event - captures ALL rich context data
     * Use this when you have access to property context, session data, and admin info
     * All context fields can be null - method handles null values gracefully
     */
    public void sendBookingAcceptedWithRichContext(ShortletBookingDTO booking, String previousStatus,
            String propertyTitle, String propertyLocation, String propertyType,
            String adminUserId, String sessionId, String userAgent,
            String sourceIP, String acceptanceNotes) {
        try {
            BookingEventPayload payload = BookingEventPayload.fromBookingDTO(booking);

            // Previous state tracking
            payload.setPreviousStatus(previousStatus);

            // Property context (only set if not null)
            if (propertyTitle != null) {
                payload.setPropertyTitle(propertyTitle);
            }
            if (propertyLocation != null) {
                payload.setPropertyLocation(propertyLocation);
            }
            if (propertyType != null) {
                payload.setPropertyType(propertyType);
            }

            // Admin/landlord context (can be null)
            if (adminUserId != null) {
                payload.setAdminUserId(adminUserId);
            }
            if (acceptanceNotes != null && !acceptanceNotes.trim().isEmpty()) {
                payload.setChangeReason(acceptanceNotes);
            }

            // Session tracking (handle nulls gracefully)
            if (sessionId != null) {
                payload.setSessionId(sessionId);
            }
            if (userAgent != null) {
                payload.setUserAgent(userAgent);
                payload.setBookingChannel(detectBookingChannel(userAgent));
            }
            if (sourceIP != null) {
                payload.setSourceIP(sourceIP);
                payload.setIpAddress(sourceIP);
            }

            // Timestamps
            payload.setUpdatedAt(OffsetDateTime.now());

            // Log comprehensive acceptance details (handle nulls in logging)
            logger.info("✅ Booking {} ACCEPTED by admin {}: Property '{}' at {}, Previous status: {}, Notes: '{}', Session: {}",
                    booking.getId(),
                    adminUserId != null ? adminUserId : "unknown",
                    propertyTitle != null ? propertyTitle : "unknown",
                    propertyLocation != null ? propertyLocation : "unknown",
                    previousStatus != null ? previousStatus : "unknown",
                    acceptanceNotes != null ? acceptanceNotes : "none",
                    sessionId != null ? sessionId : "unknown");

            BookingEventDTO event = new BookingEventDTO(BookingEventType.BOOKING_ACCEPTED, booking.getId(), payload);

            sendEvent(event, "BOOKING_ACCEPTED_RICH_CONTEXT");

        } catch (Exception e) {
            logger.error("Failed to send enhanced BOOKING_ACCEPTED event for booking {}", booking.getId(), e);
        }
    }

    /**
     * Send BOOKING_REJECTED event - triggered when landlord rejects booking
     */
    public void sendBookingRejected(ShortletBookingDTO booking, String previousStatus) {
        try {
            BookingEventPayload payload = BookingEventPayload.fromBookingDTO(booking);
            payload.setPreviousStatus(previousStatus);
            payload.setUpdatedAt(OffsetDateTime.now());

            BookingEventDTO event = new BookingEventDTO(BookingEventType.BOOKING_REJECTED, booking.getId(), payload);

            sendEvent(event, "BOOKING_REJECTED");

        } catch (Exception e) {
            logger.error("Failed to send BOOKING_REJECTED event for booking {}", booking.getId(), e);
        }
    }

    /**
     * Send BOOKING_CANCELLED event - triggered when booking is cancelled
     */
    public void sendBookingCancelled(ShortletBookingDTO booking, String previousStatus) {
        try {
            BookingEventPayload payload = BookingEventPayload.fromBookingDTO(booking);
            payload.setPreviousStatus(previousStatus);
            payload.setUpdatedAt(OffsetDateTime.now());

            BookingEventDTO event = new BookingEventDTO(BookingEventType.BOOKING_CANCELLED, booking.getId(), payload);

            sendEvent(event, "BOOKING_CANCELLED");

        } catch (Exception e) {
            logger.error("Failed to send BOOKING_CANCELLED event for booking {}", booking.getId(), e);
        }
    }

    /**
     * Send BOOKING_RESCHEDULED event - triggered when booking dates are changed
     */
    public void sendBookingRescheduled(ShortletBookingDTO booking, LocalDate previousStartDate, LocalDate previousEndDate) {
        try {
            BookingEventPayload payload = BookingEventPayload.fromBookingDTO(booking);
            payload.setPreviousStartDate(previousStartDate);
            payload.setPreviousEndDate(previousEndDate);
            payload.setUpdatedAt(OffsetDateTime.now());

            BookingEventDTO event = new BookingEventDTO(BookingEventType.BOOKING_RESCHEDULED, booking.getId(), payload);

            sendEvent(event, "BOOKING_RESCHEDULED");

        } catch (Exception e) {
            logger.error("Failed to send BOOKING_RESCHEDULED event for booking {}", booking.getId(), e);
        }
    }

    /**
     * Enhanced BOOKING_RESCHEDULED event - captures ALL frontend context data
     * Use this when frontend provides rich context like amount changes, currency, nights, etc.
     */
    public void sendBookingRescheduledWithFullContext(ShortletBookingDTO booking,
            LocalDate previousStartDate, LocalDate previousEndDate,
            Double previousAmount, Double newTotalAmount,
            String currency, Integer totalNights,
            String changeReason, String sessionId,
            String userAgent, String sourceIP) {
        try {
            BookingEventPayload payload = BookingEventPayload.fromBookingDTO(booking);

            // Previous state tracking (what changed)
            payload.setPreviousStartDate(previousStartDate);
            payload.setPreviousEndDate(previousEndDate);
            payload.setPreviousAmount(previousAmount);

            // Frontend-provided current state
            if (totalNights != null) {
                payload.setTotalNights(totalNights); // Use frontend-calculated nights
            }
            if (newTotalAmount != null) {
                payload.setTotalAmount(newTotalAmount); // Use frontend-calculated amount
            }
            if (currency != null) {
                payload.setCurrency(currency); // Use frontend-specified currency
            }

            // Context and tracking information
            payload.setChangeReason(changeReason);
            payload.setSessionId(sessionId);
            payload.setUserAgent(userAgent);
            payload.setSourceIP(sourceIP);
            payload.setIpAddress(sourceIP); // Duplicate for enhanced tracking
            payload.setBookingChannel(detectBookingChannel(userAgent));
            payload.setUpdatedAt(OffsetDateTime.now());

            // Log the comprehensive change details
            logger.info("📅 Booking {} rescheduled: Dates [{}→{}] to [{}→{}], Amount [{}→{}], Nights: {}, Reason: '{}', Session: {}",
                    booking.getId(),
                    previousStartDate, previousEndDate,
                    booking.getStartDate(), booking.getEndDate(),
                    previousAmount, newTotalAmount, totalNights,
                    changeReason, sessionId);

            BookingEventDTO event = new BookingEventDTO(BookingEventType.BOOKING_RESCHEDULED, booking.getId(), payload);

            sendEvent(event, "BOOKING_RESCHEDULED_FULL_CONTEXT");

        } catch (Exception e) {
            logger.error("Failed to send enhanced BOOKING_RESCHEDULED event for booking {}", booking.getId(), e);
        }
    }

    /**
     * Send BOOKING_PAYMENT_UPDATED event - triggered when payment status changes
     */
    public void sendBookingPaymentUpdated(ShortletBookingDTO booking, String previousPaymentStatus) {
        try {
            BookingEventPayload payload = BookingEventPayload.fromBookingDTO(booking);
            payload.setUpdatedAt(OffsetDateTime.now());

            // Store previous payment status in a custom field if needed
            // For now, we'll log it
            logger.info("Payment status changed from {} to {} for booking {}",
                    previousPaymentStatus, booking.getPaymentStatus(), booking.getId());

            BookingEventDTO event = new BookingEventDTO(BookingEventType.BOOKING_PAYMENT_UPDATED, booking.getId(), payload);

            sendEvent(event, "BOOKING_PAYMENT_UPDATED");

        } catch (Exception e) {
            logger.error("Failed to send BOOKING_PAYMENT_UPDATED event for booking {}", booking.getId(), e);
        }
    }

    /**
     * Generic method to send events with consistent error handling and logging
     * Always uses transactional sending to match producer config
     */

    private void sendEvent(BookingEventDTO event, String eventTypeStr) {
        try {
            String key = event.getBookingId().toString();

            logger.info("📤 Sending {} event transactionally for booking {} to topic {}",
                    eventTypeStr, event.getBookingId(), bookingEventsTopic);

            kafkaTemplate.executeInTransaction(ops -> {
                ops.send(bookingEventsTopic, key, event)
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                logger.info("✅ Transactionally sent {} event for booking {} to partition {} at offset {}",
                                        eventTypeStr, event.getBookingId(),
                                        result.getRecordMetadata().partition(),
                                        result.getRecordMetadata().offset());
                            } else {
                                logger.error("❌ Failed to send {} event transactionally for booking {}",
                                        eventTypeStr, event.getBookingId(), ex);
                            }
                        });
                return true; // signals tx success
            });

        } catch (Exception e) {
            logger.error("❌ Transaction failed while sending {} event for booking {}",
                    eventTypeStr, event.getBookingId(), e);
            throw new RuntimeException("Failed to send event transactionally", e);
        }
    }
    /**
     * Send single event with transactional guarantee (production-ready)
     */
    private void sendEventTransactional(BookingEventDTO event, String eventTypeStr) {
        try {
            String messageKey = event.getBookingId().toString();

            logger.info("📤 Sending {} event transactionally for booking {} to topic {}",
                    eventTypeStr, event.getBookingId(), bookingEventsTopic);

            // Execute in transaction for exactly-once semantics
            kafkaTemplate.executeInTransaction(operations -> {
                CompletableFuture<SendResult<String, Object>> future =
                        operations.send(bookingEventsTopic, messageKey, event);

                future.whenComplete((result, ex) -> {
                    if (ex == null) {
                        logger.info("✅ Transactionally sent {} event for booking {} to partition {} at offset {}",
                                eventTypeStr, event.getBookingId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        logger.error("❌ Failed to send {} event transactionally for booking {}",
                                eventTypeStr, event.getBookingId(), ex);
                    }
                });

                return true; // Transaction success
            });

        } catch (Exception e) {
            logger.error("❌ Transaction failed while sending {} event for booking {}",
                    eventTypeStr, event.getBookingId(), e);
            throw new RuntimeException("Failed to send event transactionally", e);
        }
    }

    /**
     * Send multiple related events atomically in a single transaction
     * Ensures all events are sent or none are sent (atomic guarantee)
     */
    public void sendEventsAtomically(BookingEventDTO... events) {
        if (events == null || events.length == 0) {
            logger.warn("No events provided for atomic sending");
            return;
        }

        try {
            logger.info("📦 Sending {} events atomically in transaction", events.length);

            // Execute all sends in a single transaction
            kafkaTemplate.executeInTransaction(operations -> {
                for (BookingEventDTO event : events) {
                    String messageKey = event.getBookingId().toString();

                    logger.debug("🔄 Adding {} event for booking {} to transaction",
                            event.getEventType(), event.getBookingId());

                    operations.send(bookingEventsTopic, messageKey, event);
                }

                logger.info("✅ All {} events committed atomically", events.length);
                return true; // Commit transaction
            });

        } catch (Exception e) {
            logger.error("❌ Atomic transaction failed for {} events", events.length, e);
            throw new RuntimeException("Failed to send events atomically", e);
        }
    }

    /**
     * Enhanced booking flow - sends both BOOKING_CREATED and follow-up events atomically
     * Example: When accepting a booking, send both ACCEPTED event + any related events
     */
    public void sendBookingAcceptedWithFollowUpEvents(ShortletBookingDTO booking, String previousStatus,
            String propertyTitle, String propertyLocation, String propertyType,
            String adminUserId, String sessionId, String userAgent,
            String sourceIP, String acceptanceNotes,
            BookingEventDTO... additionalEvents) {
        try {
            // Create the main acceptance event
            BookingEventPayload acceptPayload = BookingEventPayload.fromBookingDTO(booking);

            // Add rich context with null safety
            acceptPayload.setPreviousStatus(previousStatus);
            if (propertyTitle != null) acceptPayload.setPropertyTitle(propertyTitle);
            if (propertyLocation != null) acceptPayload.setPropertyLocation(propertyLocation);
            if (propertyType != null) acceptPayload.setPropertyType(propertyType);
            if (adminUserId != null) acceptPayload.setAdminUserId(adminUserId);
            if (acceptanceNotes != null && !acceptanceNotes.trim().isEmpty()) {
                acceptPayload.setChangeReason(acceptanceNotes);
            }
            if (sessionId != null) acceptPayload.setSessionId(sessionId);
            if (userAgent != null) {
                acceptPayload.setUserAgent(userAgent);
                acceptPayload.setBookingChannel(detectBookingChannel(userAgent));
            }
            if (sourceIP != null) {
                acceptPayload.setSourceIP(sourceIP);
                acceptPayload.setIpAddress(sourceIP);
            }
            acceptPayload.setUpdatedAt(OffsetDateTime.now());

            BookingEventDTO acceptEvent = new BookingEventDTO(BookingEventType.BOOKING_ACCEPTED, booking.getId(), acceptPayload);

            // Combine main event with additional events
            BookingEventDTO[] allEvents = new BookingEventDTO[1 + (additionalEvents != null ? additionalEvents.length : 0)];
            allEvents[0] = acceptEvent;
            if (additionalEvents != null) {
                System.arraycopy(additionalEvents, 0, allEvents, 1, additionalEvents.length);
            }

            // Send all events atomically
            sendEventsAtomically(allEvents);

            logger.info("✅ Booking {} ACCEPTED with {} follow-up events sent atomically",
                    booking.getId(), additionalEvents != null ? additionalEvents.length : 0);

        } catch (Exception e) {
            logger.error("Failed to send BOOKING_ACCEPTED with follow-up events for booking {}", booking.getId(), e);
            throw new RuntimeException("Atomic booking acceptance failed", e);
        }
    }

    /**
     * Enhanced method for booking initiated with device detection
     */
    public void sendBookingInitiatedWithContext(UUID tenantId, UUID landlordId, UUID propertyId,
            LocalDate startDate, LocalDate endDate,
            String sessionId, String userAgent, String sourceIP,
            String referralSource, String deviceType) {
        try {
            BookingEventPayload payload = BookingEventPayload.forBookingInitiated(
                    tenantId, landlordId, propertyId, startDate, endDate);

            // Enhanced context information
            payload.setSessionId(sessionId);
            payload.setUserAgent(userAgent);
            payload.setSourceIP(sourceIP);
            payload.setReferralSource(referralSource);
            payload.setDeviceType(deviceType);
            payload.setBookingChannel(detectBookingChannel(userAgent));

            UUID tempBookingId = UUID.randomUUID();
            BookingEventDTO event = new BookingEventDTO(BookingEventType.BOOKING_INITIATED, tempBookingId, payload);

            sendEvent(event, "BOOKING_INITIATED_WITH_CONTEXT");

        } catch (Exception e) {
            logger.error("Failed to send enhanced BOOKING_INITIATED event for tenant {} and property {}",
                    tenantId, propertyId, e);
        }
    }

    /**
     * Enhanced method for booking initiated with device detection and payment info
     */
    public void sendBookingInitiatedWithContext(UUID tenantId, UUID landlordId, UUID propertyId,
            LocalDate startDate, LocalDate endDate,
            String sessionId, String userAgent, String sourceIP,
            String referralSource, String deviceType,
            String email, Double amount, String currency) {
        try {
            BookingEventPayload payload = BookingEventPayload.forBookingInitiated(
                    tenantId, landlordId, propertyId, startDate, endDate);

            // Enhanced context information
            payload.setSessionId(sessionId);
            payload.setUserAgent(userAgent);
            payload.setSourceIP(sourceIP);
            payload.setReferralSource(referralSource);
            payload.setDeviceType(deviceType);
            payload.setBookingChannel(detectBookingChannel(userAgent));

            // NEW: Payment service fields
            payload.setEmail(email);
            payload.setTotalAmount(amount);
            payload.setCurrency(currency != null ? currency : "NGN");

            UUID tempBookingId = UUID.randomUUID();
            BookingEventDTO event = new BookingEventDTO(BookingEventType.BOOKING_INITIATED, tempBookingId, payload);

            sendEvent(event, "BOOKING_INITIATED_WITH_PAYMENT_CONTEXT");

        } catch (Exception e) {
            logger.error("Failed to send enhanced BOOKING_INITIATED event with payment info for tenant {} and property {}",
                    tenantId, propertyId, e);
        }
    }

    /**
     * Enhanced method for booking created with property context
     */
    public void sendBookingCreatedWithContext(ShortletBookingDTO booking,
            String propertyTitle, String propertyLocation, String propertyType) {
        try {
            BookingEventPayload payload = BookingEventPayload.fromBookingDTO(booking);

            // Add property context
            payload.setPropertyTitle(propertyTitle);
            payload.setPropertyLocation(propertyLocation);
            payload.setPropertyType(propertyType);
            payload.setCreatedAt(OffsetDateTime.now());

            BookingEventDTO event = new BookingEventDTO(BookingEventType.BOOKING_CREATED, booking.getId(), payload);

            sendEvent(event, "BOOKING_CREATED_WITH_CONTEXT");

        } catch (Exception e) {
            logger.error("Failed to send enhanced BOOKING_CREATED event for booking {}", booking.getId(), e);
        }
    }

    /**
     * Utility method to detect booking channel from user agent
     */
    private String detectBookingChannel(String userAgent) {
        if (userAgent == null) {
            return "unknown";
        }

        String lowerUserAgent = userAgent.toLowerCase();

        if (lowerUserAgent.contains("mobile") || lowerUserAgent.contains("android") || lowerUserAgent.contains("iphone")) {
            return "mobile";
        } else if (lowerUserAgent.contains("tablet") || lowerUserAgent.contains("ipad")) {
            return "tablet";
        } else {
            return "web";
        }
    }

    /**
     * Batch send multiple events (for bulk operations)
     */
    public void sendBatchEvents(BookingEventDTO... events) {
        for (BookingEventDTO event : events) {
            sendEvent(event, event.getEventType().name());
        }
    }

    /**
     * Transactional batch send - sends multiple events atomically
     * Production-ready version with transaction guarantee
     */
    public void sendBatchEventsTransactional(BookingEventDTO... events) {
        sendEventsAtomically(events);
    }

    /**
     * Production-ready booking creation with transactional guarantee
     * Sends BOOKING_CREATED event with transaction safety
     */
    public void sendBookingCreatedTransactional(ShortletBookingDTO booking) {
        try {
            BookingEventPayload payload = BookingEventPayload.fromBookingDTO(booking);
            payload.setCreatedAt(OffsetDateTime.now());

            BookingEventDTO event = new BookingEventDTO(BookingEventType.BOOKING_CREATED, booking.getId(), payload);

            sendEventTransactional(event, "BOOKING_CREATED_TRANSACTIONAL");

        } catch (Exception e) {
            logger.error("Failed to send transactional BOOKING_CREATED event for booking {}", booking.getId(), e);
            throw new RuntimeException("Transactional booking creation failed", e);
        }
    }

    /**
     * Production-ready booking acceptance with transactional guarantee
     * Sends BOOKING_ACCEPTED event with transaction safety
     */
    public void sendBookingAcceptedTransactional(ShortletBookingDTO booking, String previousStatus,
            String propertyTitle, String propertyLocation, String propertyType,
            String adminUserId, String sessionId, String userAgent,
            String sourceIP, String acceptanceNotes) {
        try {
            BookingEventPayload payload = BookingEventPayload.fromBookingDTO(booking);

            // Add rich context with null safety
            payload.setPreviousStatus(previousStatus);
            if (propertyTitle != null) payload.setPropertyTitle(propertyTitle);
            if (propertyLocation != null) payload.setPropertyLocation(propertyLocation);
            if (propertyType != null) payload.setPropertyType(propertyType);
            if (adminUserId != null) payload.setAdminUserId(adminUserId);
            if (acceptanceNotes != null && !acceptanceNotes.trim().isEmpty()) {
                payload.setChangeReason(acceptanceNotes);
            }
            if (sessionId != null) payload.setSessionId(sessionId);
            if (userAgent != null) {
                payload.setUserAgent(userAgent);
                payload.setBookingChannel(detectBookingChannel(userAgent));
            }
            if (sourceIP != null) {
                payload.setSourceIP(sourceIP);
                payload.setIpAddress(sourceIP);
            }
            payload.setUpdatedAt(OffsetDateTime.now());

            BookingEventDTO event = new BookingEventDTO(BookingEventType.BOOKING_ACCEPTED, booking.getId(), payload);

            sendEventTransactional(event, "BOOKING_ACCEPTED_TRANSACTIONAL");

            logger.info("✅ Booking {} ACCEPTED transactionally by admin {}", booking.getId(),
                    adminUserId != null ? adminUserId : "unknown");

        } catch (Exception e) {
            logger.error("Failed to send transactional BOOKING_ACCEPTED event for booking {}", booking.getId(), e);
            throw new RuntimeException("Transactional booking acceptance failed", e);
        }
    }

    /**
     * Critical booking workflow - atomically send booking creation + immediate follow-up events
     * Example: BOOKING_CREATED + PAYMENT_INITIATED + NOTIFICATION_SENT all in one transaction
     */
    public void sendBookingWorkflowAtomically(ShortletBookingDTO booking, BookingEventDTO... workflowEvents) {
        try {
            // Create the main booking created event
            BookingEventPayload createdPayload = BookingEventPayload.fromBookingDTO(booking);
            createdPayload.setCreatedAt(OffsetDateTime.now());
            BookingEventDTO createdEvent = new BookingEventDTO(BookingEventType.BOOKING_CREATED, booking.getId(), createdPayload);

            // Combine with workflow events
            BookingEventDTO[] allEvents = new BookingEventDTO[1 + (workflowEvents != null ? workflowEvents.length : 0)];
            allEvents[0] = createdEvent;
            if (workflowEvents != null) {
                System.arraycopy(workflowEvents, 0, allEvents, 1, workflowEvents.length);
            }

            // Send all events atomically
            sendEventsAtomically(allEvents);

            logger.info("🔄 Booking {} workflow completed atomically with {} total events",
                    booking.getId(), allEvents.length);

        } catch (Exception e) {
            logger.error("❌ Atomic booking workflow failed for booking {}", booking.getId(), e);
            throw new RuntimeException("Booking workflow transaction failed", e);
        }
    }
}
