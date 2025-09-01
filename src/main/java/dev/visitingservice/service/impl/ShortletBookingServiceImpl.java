package dev.visitingservice.service.impl;

import dev.visitingservice.dto.ShortletBookingDTO;
import dev.visitingservice.exception.ExternalServiceException;
import dev.visitingservice.model.ShortletBooking;
import dev.visitingservice.model.ShortletBooking.BookingStatus;
import dev.visitingservice.model.ShortletAvailability;
import dev.visitingservice.repository.ShortletBookingRepository;
import dev.visitingservice.repository.ShortletAvailabilityRepository;
import dev.visitingservice.service.BookingEventProducerService;
import dev.visitingservice.service.BookingValidationService;
import dev.visitingservice.service.NotificationPublisher;
import dev.visitingservice.service.ShortletBookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Service
public class ShortletBookingServiceImpl implements ShortletBookingService {

    private static final Logger logger = LoggerFactory.getLogger(ShortletBookingServiceImpl.class);

    // Add a small result type for tracking new vs existing bookings
    public record BookingResult(ShortletBookingDTO dto, boolean isNew) {}

    private final ShortletBookingRepository bookingRepository;
    private final ShortletAvailabilityRepository availabilityRepository;
    private final NotificationPublisher notificationPublisher;
    private final BookingEventProducerService eventProducer;
    private final BookingValidationService bookingValidationService;
    private final BookingBusinessRuleValidator businessRuleValidator;

    @Autowired
    public ShortletBookingServiceImpl(ShortletBookingRepository bookingRepository,
                                      ShortletAvailabilityRepository availabilityRepository,
                                      NotificationPublisher notificationPublisher,
                                      BookingEventProducerService eventProducer,
                                      BookingValidationService bookingValidationService,
                                      BookingBusinessRuleValidator businessRuleValidator) {
        this.bookingRepository = bookingRepository;
        this.availabilityRepository = availabilityRepository;
        this.notificationPublisher = notificationPublisher;
        this.eventProducer = eventProducer;
        this.bookingValidationService = bookingValidationService;
        this.businessRuleValidator = businessRuleValidator;

        // Validate critical microservice dependencies at startup
        if (eventProducer == null) {
            logger.error("❌ CRITICAL: BookingEventProducerService is null - microservice coordination will fail!");
            throw new IllegalStateException("BookingEventProducerService is required for microservice coordination");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShortletBookingDTO createBooking(UUID tenantId, UUID landlordId, UUID propertyId,
                                          LocalDate startDate, LocalDate endDate, String firstName,
                                          String lastName, String phoneNumber, Integer guestNumber,
                                          String email, Double amount, String currency,
                                          String sessionId, String userAgent, String sourceIP) {

        // ===== PHASE 1: COMPREHENSIVE INPUT VALIDATION (FAST FAIL) =====
        validateBasicInputs(tenantId, landlordId, propertyId, startDate, endDate, email, amount);

        // ===== PHASE 2: EXTERNAL SYSTEM VALIDATION (WITH RETRY) =====
        // No transaction needed - external calls only
        validateExternalDependencies(tenantId, landlordId, propertyId);

        // ===== PHASE 3: BUSINESS RULE VALIDATION (WITH SEPARATE TRANSACTION FOR LOCKING) =====
        businessRuleValidator.validateWithLocking(landlordId, propertyId, startDate, endDate, tenantId);

        // ===== PHASE 4: ATOMIC BOOKING CREATION (IN MAIN TRANSACTION) =====
        BookingResult result = createBookingAtomically(
            tenantId, landlordId, propertyId, startDate, endDate,
            firstName, lastName, phoneNumber, guestNumber,
            email, amount, currency
        );

        // ===== PHASE 5: POST-CREATION EVENTS (OUTSIDE TRANSACTION) - ONLY FOR NEW BOOKINGS =====
        if (result.isNew()) {
            publishBookingEventsAsync(result.dto(), sessionId, userAgent, sourceIP);
        } else {
            logger.info("🔄 Skipping event publishing for duplicate booking: {}", result.dto().getId());
        }

        return result.dto();
    }

    private void validateBasicInputs(UUID tenantId, UUID landlordId, UUID propertyId,
                                   LocalDate startDate, LocalDate endDate, String email, Double amount) {
        // Null validations
        if (tenantId == null) throw new IllegalArgumentException("Tenant ID is required");
        if (landlordId == null) throw new IllegalArgumentException("Landlord ID is required");
        if (propertyId == null) throw new IllegalArgumentException("Property ID is required");
        if (startDate == null) throw new IllegalArgumentException("Start date is required");
        if (endDate == null) throw new IllegalArgumentException("End date is required");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required");
        if (amount == null || amount <= 0) throw new IllegalArgumentException("Amount must be greater than 0");

        // Date validations
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot create booking in the past");
        }

        // Email validation (basic)
        if (!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Invalid email format");
        }

        logger.debug("✅ Basic input validation passed for booking: tenant={}, landlord={}, property={}",
                    tenantId, landlordId, propertyId);
    }

    // No @Transactional - external service calls only
    private void validateExternalDependencies(UUID tenantId, UUID landlordId, UUID propertyId) {
        try {
            // Use CompletableFuture for parallel validation of external dependencies
            CompletableFuture<Void> tenantValidation = CompletableFuture.runAsync(() ->
                retryValidation(() -> bookingValidationService.validateUserExists(tenantId, "tenant"))
            );

            CompletableFuture<Void> landlordValidation = CompletableFuture.runAsync(() ->
                retryValidation(() -> bookingValidationService.validateUserExists(landlordId, "landlord"))
            );

            CompletableFuture<Void> propertyValidation = CompletableFuture.runAsync(() ->
                retryValidation(() -> bookingValidationService.validatePropertyExists(propertyId))
            );

            // Wait for all validations to complete
            CompletableFuture.allOf(tenantValidation, landlordValidation, propertyValidation).join();

            // Sequential validations that depend on the above
            retryValidation(() -> bookingValidationService.validateTenantAndLandlord(tenantId, landlordId));
            retryValidation(() -> bookingValidationService.validatePropertyOwnership(propertyId, landlordId));

            logger.debug("✅ External dependency validation passed for booking: tenant={}, landlord={}, property={}",
                        tenantId, landlordId, propertyId);

        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new ExternalServiceException("Failed to validate external dependencies", cause);
        }
    }

    private void retryValidation(Runnable validation) {
        int maxRetries = 3;
        int attempts = 0;

        while (attempts < maxRetries) {
            try {
                validation.run();
                return; // Success
            } catch (ExternalServiceException e) {
                attempts++;
                if (attempts >= maxRetries) {
                    logger.error("❌ Validation failed after {} attempts: {}", maxRetries, e.getMessage());
                    throw e;
                }

                try {
                    Thread.sleep(100 * attempts); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Validation interrupted", ie);
                }

                logger.warn("⚠️ Validation attempt {} failed, retrying: {}", attempts, e.getMessage());
            }
        }
    }


    /**
     * Creates booking atomically without separate transaction - joins parent transaction.
     * This ensures booking creation is part of the main transaction boundary.
     *
     * @return BookingResult indicating the booking DTO and whether it's a new booking
     */
    private BookingResult createBookingAtomically(UUID tenantId, UUID landlordId, UUID propertyId,
                                                 LocalDate startDate, LocalDate endDate,
                                                 String firstName, String lastName, String phoneNumber,
                                                 Integer guestNumber, String email, Double amount, String currency) {

        // Check for idempotency - exact same booking already exists
        Optional<ShortletBooking> existingBooking = bookingRepository
            .findExistingPendingBooking(tenantId, landlordId, propertyId, startDate, endDate);

        if (existingBooking.isPresent()) {
            logger.info("📋 Idempotent booking request - returning existing booking: {}", existingBooking.get().getId());
            return new BookingResult(toDTO(existingBooking.get()), false);  // <-- not new
        }

        // Create new booking
        ShortletBooking booking = new ShortletBooking();
        booking.setTenantId(tenantId);
        booking.setLandlordId(landlordId);
        booking.setPropertyId(propertyId);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setStatus(BookingStatus.PENDING);
        booking.setFirstName(firstName);
        booking.setLastName(lastName);
        booking.setPhoneNumber(phoneNumber);
        booking.setGuestNumber(guestNumber);
        booking.setPaymentStatus(ShortletBooking.PaymentStatus.PENDING);
        booking.setTenantEmail(email);
        booking.setTotalAmount(java.math.BigDecimal.valueOf(amount));
        booking.setCurrency(currency != null ? currency : "NGN");

        ShortletBooking savedBooking = bookingRepository.save(booking);

        logger.info("✅ Booking created successfully: ID={}, tenant={}, landlord={}, property={}",
                   savedBooking.getId(), tenantId, landlordId, propertyId);

        return new BookingResult(toDTO(savedBooking), true); // <-- new
    }

    /**
     * Publishes events asynchronously to avoid impacting the main transaction.
     * Events are sent after successful booking creation but won't cause rollback if they fail.
     */
    private void publishBookingEventsAsync(ShortletBookingDTO booking, String sessionId, String userAgent, String sourceIP) {
        // Use CompletableFuture to publish events asynchronously
        CompletableFuture.runAsync(() -> publishBookingEvents(booking, sessionId, userAgent, sourceIP))
            .exceptionally(throwable -> {
                logger.error("❌ Async event publishing failed for booking: {} - {}",
                           booking.getId(), throwable.getMessage());
                return null;
            });
    }

    private void publishBookingEvents(ShortletBookingDTO booking, String sessionId, String userAgent, String sourceIP) {
        try {
            // Send BOOKING_INITIATED event for tracking
            eventProducer.sendBookingInitiatedWithContext(
                booking.getTenantId(), booking.getLandlordId(), booking.getPropertyId(),
                booking.getStartDate(), booking.getEndDate(), sessionId, userAgent, sourceIP,
                null, null, booking.getTenantEmail(), booking.getTotalAmount(), booking.getCurrency()
            );

            // Send BOOKING_CREATED event for downstream processing
            eventProducer.sendBookingCreated(booking);

            logger.debug("✅ Booking events published successfully for booking: {}", booking.getId());

        } catch (Exception e) {
            logger.error("❌ Failed to publish booking events for booking: {} - {}", booking.getId(), e.getMessage());
            // Don't fail the entire transaction for event publishing failures
            // The booking has already been successfully created
        }

        // Send notification email (non-critical)
        try {
            ShortletBooking bookingEntity = bookingRepository.findById(booking.getId())
                .orElseThrow(() -> new IllegalStateException("Booking not found after creation"));
            notificationPublisher.sendBookingCreated(bookingEntity);
        } catch (Exception e) {
            logger.warn("⚠️ Failed to send booking notification email: {}", e.getMessage());
        }
    }

    @Override
    public List<ShortletBookingDTO> getBookings(UUID landlordId, int page, int size) {
        if (landlordId == null) {
            throw new IllegalArgumentException("landlordId cannot be null");
        }
        return bookingRepository.findByLandlordId(landlordId).stream()
                .skip((long) page * size)
                .limit(size)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // NEW MISSING METHODS FOR TENANT FUNCTIONALITY
    @Override
    public List<ShortletBookingDTO> getTenantBookings(UUID tenantId, int page, int limit, String status) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId cannot be null");
        }

        List<ShortletBooking> bookings = bookingRepository.findByTenantId(tenantId);

        // Filter by status if provided
        if (status != null && !status.equalsIgnoreCase("all")) {
            try {
                BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
                bookings = bookings.stream()
                    .filter(b -> b.getStatus() == bookingStatus)
                    .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // Invalid status, keep all bookings
            }
        }

        // Apply pagination
        return bookings.stream()
                .skip((long) page * limit)
                .limit(limit)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ShortletBookingDTO getBookingById(UUID bookingId) {
        if (bookingId == null) {
            throw new IllegalArgumentException("bookingId cannot be null");
        }

        Optional<ShortletBooking> booking = bookingRepository.findById(bookingId);
        return booking.map(this::toDTO).orElse(null);
    }

    @Override
    public List<ShortletBookingDTO> getAllBookings(int page, int limit, String status, LocalDate dateFrom, LocalDate dateTo) {
        List<ShortletBooking> bookings = bookingRepository.findAll();

        // Filter by status if provided
        if (status != null && !status.equalsIgnoreCase("all")) {
            try {
                BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
                bookings = bookings.stream()
                    .filter(b -> b.getStatus() == bookingStatus)
                    .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // Invalid status, keep all bookings
            }
        }

        // Filter by date range if provided
        if (dateFrom != null && dateTo != null) {
            bookings = bookings.stream()
                .filter(b -> !b.getStartDate().isBefore(dateFrom) && !b.getEndDate().isAfter(dateTo))
                .collect(Collectors.toList());
        }

        // Apply pagination
        return bookings.stream()
                .skip((long) page * limit)
                .limit(limit)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private boolean isAllowedTransition(BookingStatus from, BookingStatus to) {
        return switch (from) {
            case PENDING -> to == BookingStatus.ACCEPTED || to == BookingStatus.REJECTED || to == BookingStatus.CANCELLED || to == BookingStatus.RESCHEDULED;
            case ACCEPTED -> to == BookingStatus.CANCELLED || to == BookingStatus.RESCHEDULED;
            case RESCHEDULED -> to == BookingStatus.ACCEPTED || to == BookingStatus.REJECTED || to == BookingStatus.CANCELLED;
            default -> false;
        };
    }

    @Override
    @Transactional
    public ShortletBookingDTO acceptBooking(UUID bookingId) {
        ShortletBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        String previousStatus = booking.getStatus().name();

        if (!isAllowedTransition(booking.getStatus(), BookingStatus.ACCEPTED)) {
            throw new IllegalStateException("Cannot accept booking from status " + booking.getStatus());
        }
        if (booking.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Cannot accept a booking that starts in the past");
        }
        // Block the dates by removing the availability slot that covers this booking
        List<ShortletAvailability> availabilities = availabilityRepository.findByLandlordIdAndPropertyId(booking.getLandlordId(), booking.getPropertyId());
        Optional<ShortletAvailability> covering = availabilities.stream()
                .filter(a -> !a.getStartDate().isAfter(booking.getStartDate()) && !a.getEndDate().isBefore(booking.getEndDate()))
                .findFirst();
        if (covering.isEmpty()) {
            throw new IllegalStateException("No availability found for these dates");
        }
        ShortletAvailability availability = covering.get();
        // Split or remove the availability slot
        if (availability.getStartDate().isBefore(booking.getStartDate())) {
            ShortletAvailability before = new ShortletAvailability();
            before.setLandlordId(availability.getLandlordId());
            before.setPropertyId(availability.getPropertyId());
            before.setStartDate(availability.getStartDate());
            before.setEndDate(booking.getStartDate().minusDays(1));
            availabilityRepository.save(before);
        }
        if (availability.getEndDate().isAfter(booking.getEndDate())) {
            ShortletAvailability after = new ShortletAvailability();
            after.setLandlordId(availability.getLandlordId());
            after.setPropertyId(availability.getPropertyId());
            after.setStartDate(booking.getEndDate().plusDays(1));
            after.setEndDate(availability.getEndDate());
            availabilityRepository.save(after);
        }
        availabilityRepository.deleteById(availability.getId());
        booking.setStatus(BookingStatus.ACCEPTED);
        bookingRepository.save(booking);

        // Reject overlapping bookings
        List<ShortletBooking> overlappingPending = bookingRepository.findByLandlordId(booking.getLandlordId()).stream()
                .filter(b -> b.getPropertyId().equals(booking.getPropertyId()))
                .filter(b -> b.getStatus() == BookingStatus.PENDING)
                .filter(b -> !b.getId().equals(booking.getId())) // FIX: Use .equals() for UUID comparison
                .filter(b -> b.getStartDate().isBefore(booking.getEndDate()) && b.getEndDate().isAfter(booking.getStartDate()))
                .collect(Collectors.toList());
        for (ShortletBooking pending : overlappingPending) {
            String pendingPreviousStatus = pending.getStatus().name(); // FIX: Use different variable name
            pending.setStatus(BookingStatus.REJECTED);
            bookingRepository.save(pending);

            // CRITICAL: Notify other microservices of rejection with correct previous status
            eventProducer.sendBookingRejected(toDTO(pending), pendingPreviousStatus); // pendingPreviousStatus is "PENDING"

            // Supplementary: Send email to users
            try {
                notificationPublisher.sendBookingRejected(pending);
            } catch (Exception e) {
                logger.warn("Failed to send rejection email: {}", e.getMessage());
            }
        }

        // CRITICAL: Send BOOKING_ACCEPTED event - triggers payment processing, calendar updates, etc.
        eventProducer.sendBookingAccepted(toDTO(booking), previousStatus);

        // Supplementary: Send email notification to users
        try {
            notificationPublisher.sendBookingAccepted(booking);
        } catch (Exception e) {
            logger.warn("Failed to send acceptance email: {}", e.getMessage());
        }

        return toDTO(booking);
    }

    @Override
    @Transactional
    public ShortletBookingDTO rejectBooking(UUID bookingId) {
        ShortletBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        String previousStatus = booking.getStatus().name();

        if (!isAllowedTransition(booking.getStatus(), BookingStatus.REJECTED)) {
            throw new IllegalStateException("Cannot reject booking from status " + booking.getStatus());
        }
        if (booking.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Cannot reject a booking that has already started");
        }
        booking.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(booking);

        // CRITICAL: Send BOOKING_REJECTED event - other microservices must know about this
        eventProducer.sendBookingRejected(toDTO(booking), previousStatus);

        // Supplementary: Send email notification to users
        try {
            notificationPublisher.sendBookingRejected(booking);
        } catch (Exception e) {
            logger.warn("Failed to send rejection email: {}", e.getMessage());
        }

        return toDTO(booking);
    }

    @Override
    @Transactional
    public ShortletBookingDTO cancelBooking(UUID bookingId) {
        ShortletBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        String previousStatus = booking.getStatus().name();

        if (!isAllowedTransition(booking.getStatus(), BookingStatus.CANCELLED)) {
            throw new IllegalStateException("Cannot cancel booking from status " + booking.getStatus());
        }
        if (booking.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Cannot cancel a booking that has already started");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // CRITICAL: Send BOOKING_CANCELLED event - triggers refund processing, availability restoration, etc.
        eventProducer.sendBookingCancelled(toDTO(booking), previousStatus);

        // Supplementary: Send email notification to users
        try {
            notificationPublisher.sendBookingCancelled(booking);
        } catch (Exception e) {
            logger.warn("Failed to send cancellation email: {}", e.getMessage());
        }

        return toDTO(booking);
    }

    @Override
    @Transactional
    public ShortletBookingDTO rescheduleBooking(UUID bookingId, LocalDate newStartDate, LocalDate newEndDate) {
        // Validate inputs
        if (newStartDate == null || newEndDate == null) {
            throw new IllegalArgumentException("New dates cannot be null");
        }
        if (newStartDate.isAfter(newEndDate)) {
            throw new IllegalArgumentException("newStartDate must be before newEndDate");
        }
        if (newStartDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot reschedule booking to a date in the past");
        }

        // Find booking
        ShortletBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // Check if reschedule is allowed for this booking status
        if (!isAllowedTransition(booking.getStatus(), BookingStatus.RESCHEDULED)) {
            throw new IllegalStateException("Cannot reschedule booking from status " + booking.getStatus());
        }

        // Check if the requested new dates are available
        List<ShortletAvailability> availabilities = availabilityRepository.findByLandlordId(booking.getLandlordId());
        boolean available = availabilities.stream()
                .anyMatch(a -> !a.getStartDate().isAfter(newStartDate) && !a.getEndDate().isBefore(newEndDate));

        if (!available) {
            throw new IllegalArgumentException("Requested new dates are not available for booking");
        }

        // Store old dates for event tracking
        LocalDate previousStartDate = booking.getStartDate();
        LocalDate previousEndDate = booking.getEndDate();

        // Update booking with new dates
        booking.setStartDate(newStartDate);
        booking.setEndDate(newEndDate);
        booking.setStatus(BookingStatus.RESCHEDULED);

        ShortletBooking updated = bookingRepository.save(booking);

        // CRITICAL: Send BOOKING_RESCHEDULED event - triggers availability updates, calendar sync, etc.
        eventProducer.sendBookingRescheduled(toDTO(updated), previousStartDate, previousEndDate);

        // Supplementary: Send email notification to users
        try {
            notificationPublisher.sendBookingRescheduled(updated);
        } catch (Exception e) {
            logger.warn("Failed to send reschedule email: {}", e.getMessage());
        }

        return toDTO(updated);
    }

    @Override
    @Transactional
    public ShortletBookingDTO updateBookingPayment(UUID bookingId, String paymentStatus, String paymentReference, Double paymentAmount) {
        ShortletBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        String previousPaymentStatus = booking.getPaymentStatus() != null ? booking.getPaymentStatus().name() : null;

        if (paymentStatus != null) {
            try {
                booking.setPaymentStatus(ShortletBooking.PaymentStatus.valueOf(paymentStatus));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid payment status: " + paymentStatus);
            }
        }
        if (paymentReference != null) {
            booking.setPaymentReference(paymentReference);
        }
        if (paymentAmount != null) {
            booking.setPaymentAmount(java.math.BigDecimal.valueOf(paymentAmount));
        }
        bookingRepository.save(booking);

        // CRITICAL: Send BOOKING_PAYMENT_UPDATED event - triggers financial reconciliation, reporting, etc.
        eventProducer.sendBookingPaymentUpdated(toDTO(booking), previousPaymentStatus);

        return toDTO(booking);
    }

    @Override
    public List<ShortletBookingDTO> getBookingsByProperty(UUID propertyId) {
        if (propertyId == null) {
            throw new IllegalArgumentException("propertyId cannot be null");
        }
        return bookingRepository.findByPropertyId(propertyId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private ShortletBookingDTO toDTO(ShortletBooking booking) {
        ShortletBookingDTO dto = new ShortletBookingDTO();
        dto.setId(booking.getId());
        dto.setTenantId(booking.getTenantId());
        dto.setLandlordId(booking.getLandlordId());
        dto.setPropertyId(booking.getPropertyId());
        dto.setStartDate(booking.getStartDate());
        dto.setEndDate(booking.getEndDate());
        dto.setStatus(booking.getStatus().name());
        dto.setFirstName(booking.getFirstName());
        dto.setLastName(booking.getLastName());
        dto.setPhoneNumber(booking.getPhoneNumber());
        dto.setGuestNumber(booking.getGuestNumber());
        dto.setPaymentStatus(
            booking.getPaymentStatus() != null
                ? ShortletBookingDTO.PaymentStatus.valueOf(booking.getPaymentStatus().name())
                : null
        );
        dto.setPaymentReference(booking.getPaymentReference());

        // NEW: ensure these 4 are mapped
        dto.setTenantEmail(booking.getTenantEmail());
        if (booking.getTotalAmount() != null) {
            dto.setTotalAmount(booking.getTotalAmount().doubleValue());
        }
        if (booking.getPaymentAmount() != null) {
            dto.setPaymentAmount(booking.getPaymentAmount().doubleValue());
        }
        dto.setCurrency(booking.getCurrency());

        return dto;
    }
}
