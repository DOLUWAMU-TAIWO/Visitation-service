package dev.visitingservice.service.impl;

import dev.visitingservice.dto.ShortletBookingDTO;
import dev.visitingservice.model.ShortletBooking;
import dev.visitingservice.model.ShortletBooking.BookingStatus;
import dev.visitingservice.model.ShortletAvailability;
import dev.visitingservice.repository.ShortletBookingRepository;
import dev.visitingservice.repository.ShortletAvailabilityRepository;
import dev.visitingservice.service.NotificationPublisher;
import dev.visitingservice.service.ShortletBookingService;
import dev.visitingservice.service.ShortletAvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ShortletBookingServiceImpl implements ShortletBookingService {

    private final ShortletBookingRepository bookingRepository;
    private final ShortletAvailabilityRepository availabilityRepository;
    private final NotificationPublisher notificationPublisher;

    @Autowired
    public ShortletBookingServiceImpl(ShortletBookingRepository bookingRepository,
                                      ShortletAvailabilityRepository availabilityRepository,
                                      NotificationPublisher notificationPublisher) {
        this.bookingRepository = bookingRepository;
        this.availabilityRepository = availabilityRepository;
        this.notificationPublisher = notificationPublisher;
    }

    @Override
    @Transactional
    public ShortletBookingDTO createBooking(UUID tenantId, UUID landlordId, UUID propertyId, LocalDate startDate, LocalDate endDate, String firstName, String lastName, String phoneNumber) {
        if (tenantId == null || landlordId == null || propertyId == null || startDate == null || endDate == null) {
            throw new IllegalArgumentException("All booking fields are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot create a booking in the past");
        }
        // Check if the requested dates are available for this property and landlord
        List<ShortletAvailability> availabilities = availabilityRepository.findByLandlordIdAndPropertyId(landlordId, propertyId);
        boolean available = availabilities.stream().anyMatch(a -> !a.getStartDate().isAfter(startDate) && !a.getEndDate().isBefore(endDate));
        if (!available) {
            throw new IllegalArgumentException("Requested dates are not available for booking");
        }
        // Check for overlapping bookings for this property
        boolean alreadyBooked = bookingRepository.findByLandlordId(landlordId).stream()
                .filter(b -> b.getPropertyId().equals(propertyId))
                .anyMatch(b ->
                        (b.getStartDate().isBefore(endDate) && b.getEndDate().isAfter(startDate)) &&
                        (b.getStatus() == BookingStatus.ACCEPTED)
                );
        if (alreadyBooked) {
            throw new IllegalArgumentException("Requested dates are already booked for this property");
        }
        // Idempotency: check if a booking with same details already exists and is pending
        Optional<ShortletBooking> existing = bookingRepository.findByLandlordId(landlordId).stream()
                .filter(b -> b.getTenantId().equals(tenantId)
                        && b.getPropertyId().equals(propertyId)
                        && b.getStartDate().equals(startDate)
                        && b.getEndDate().equals(endDate)
                        && b.getStatus() == BookingStatus.PENDING)
                .findFirst();
        if (existing.isPresent()) {
            return toDTO(existing.get());
        }
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
        booking.setPaymentStatus(ShortletBooking.PaymentStatus.PENDING);

        ShortletBooking saved = bookingRepository.save(booking);
        notificationPublisher.sendBookingCreated(saved);
        return toDTO(saved);
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
                // Invalid status, return empty list or all bookings
                bookings = bookings; // Keep all bookings if status is invalid
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
        // Reject all other overlapping PENDING bookings for the same property and date range
        List<ShortletBooking> overlappingPending = bookingRepository.findByLandlordId(booking.getLandlordId()).stream()
                .filter(b -> b.getPropertyId().equals(booking.getPropertyId()))
                .filter(b -> b.getStatus() == BookingStatus.PENDING)
                .filter(b -> b.getId() != booking.getId())
                .filter(b -> b.getStartDate().isBefore(booking.getEndDate()) && b.getEndDate().isAfter(booking.getStartDate()))
                .collect(Collectors.toList());
        for (ShortletBooking pending : overlappingPending) {
            pending.setStatus(BookingStatus.REJECTED);
            bookingRepository.save(pending);
            notificationPublisher.sendBookingRejected(pending);
        }
        notificationPublisher.sendBookingAccepted(booking);
        return toDTO(booking);
    }

    @Override
    @Transactional
    public ShortletBookingDTO rejectBooking(UUID bookingId) {
        ShortletBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (!isAllowedTransition(booking.getStatus(), BookingStatus.REJECTED)) {
            throw new IllegalStateException("Cannot reject booking from status " + booking.getStatus());
        }
        if (booking.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Cannot reject a booking that has already started");
        }
        booking.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(booking);
        notificationPublisher.sendBookingRejected(booking);
        return toDTO(booking);
    }

    @Override
    @Transactional
    public ShortletBookingDTO cancelBooking(UUID bookingId) {
        ShortletBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (!isAllowedTransition(booking.getStatus(), BookingStatus.CANCELLED)) {
            throw new IllegalStateException("Cannot cancel booking from status " + booking.getStatus());
        }
        if (booking.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Cannot cancel a booking that has already started");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        notificationPublisher.sendBookingCancelled(booking);
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

        // Store old dates for notification (optional)
        LocalDate oldStartDate = booking.getStartDate();
        LocalDate oldEndDate = booking.getEndDate();

        // Update booking with new dates
        booking.setStartDate(newStartDate);
        booking.setEndDate(newEndDate);
        booking.setStatus(BookingStatus.RESCHEDULED);

        // Save and send notification
        ShortletBooking updated = bookingRepository.save(booking);
        notificationPublisher.sendBookingRescheduled(updated); // This would need to be added to NotificationPublisher

        return toDTO(updated);
    }

    @Override
    @Transactional
    public ShortletBookingDTO updateBookingPayment(UUID bookingId, String paymentStatus, String paymentReference, Double paymentAmount) {
        ShortletBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
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
        return toDTO(booking);
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
        dto.setPaymentStatus(booking.getPaymentStatus() != null ? ShortletBookingDTO.PaymentStatus.valueOf(booking.getPaymentStatus().name()) : null);
        dto.setPaymentReference(booking.getPaymentReference());
        dto.setPaymentAmount(booking.getPaymentAmount() != null ? booking.getPaymentAmount().doubleValue() : null);
        return dto;
    }
}
