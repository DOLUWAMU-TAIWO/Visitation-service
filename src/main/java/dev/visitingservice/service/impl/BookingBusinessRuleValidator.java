package dev.visitingservice.service.impl;

import dev.visitingservice.model.ShortletAvailability;
import dev.visitingservice.model.ShortletBooking;
import dev.visitingservice.model.ShortletBooking.BookingStatus;
import dev.visitingservice.repository.ShortletAvailabilityRepository;
import dev.visitingservice.repository.ShortletBookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Separate service for business rule validation with pessimistic locking.
 * This ensures proper Spring AOP proxy creation for @Transactional methods.
 */
@Service
public class BookingBusinessRuleValidator {

    private static final Logger logger = LoggerFactory.getLogger(BookingBusinessRuleValidator.class);

    private final ShortletAvailabilityRepository availabilityRepository;
    private final ShortletBookingRepository bookingRepository;

    @Autowired
    public BookingBusinessRuleValidator(ShortletAvailabilityRepository availabilityRepository,
                                       ShortletBookingRepository bookingRepository) {
        this.availabilityRepository = availabilityRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Validates business rules with pessimistic locking in a separate transaction.
     * Uses REQUIRES_NEW to ensure proper isolation and immediate lock release.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW,
                   rollbackFor = Exception.class,
                   timeout = 30) // 30 second timeout for locking operations
    public void validateWithLocking(UUID landlordId, UUID propertyId, LocalDate startDate,
                                   LocalDate endDate, UUID tenantId) {

        logger.debug("🔒 Acquiring locks for business rule validation: landlord={}, property={}",
                    landlordId, propertyId);

        // Use pessimistic locking to prevent concurrent booking conflicts
        List<ShortletAvailability> availabilities = availabilityRepository
            .findByLandlordIdAndPropertyIdWithLock(landlordId, propertyId);

        if (availabilities.isEmpty()) {
            throw new IllegalArgumentException("No availability information found for this property");
        }

        // Check availability
        boolean isAvailable = availabilities.stream()
            .anyMatch(a -> !a.getStartDate().isAfter(startDate) && !a.getEndDate().isBefore(endDate));

        if (!isAvailable) {
            throw new IllegalArgumentException("Requested dates are not available for booking");
        }

        // Check for overlapping accepted bookings (with pessimistic locking)
        List<ShortletBooking> existingBookings = bookingRepository
            .findByLandlordIdAndPropertyIdWithLock(landlordId, propertyId);

        boolean hasOverlap = existingBookings.stream()
            .filter(booking -> booking.getStatus() == BookingStatus.ACCEPTED)
            .anyMatch(booking ->
                booking.getStartDate().isBefore(endDate) && booking.getEndDate().isAfter(startDate)
            );

        if (hasOverlap) {
            throw new IllegalArgumentException("Requested dates conflict with existing accepted booking");
        }

        logger.debug("✅ Business rules validation passed with locks: landlord={}, property={}, dates={} to {}",
                    landlordId, propertyId, startDate, endDate);
    }
}
