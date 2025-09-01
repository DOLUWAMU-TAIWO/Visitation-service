package dev.visitingservice.service;

import dev.visitingservice.dto.ShortletBookingDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ShortletBookingService {
    ShortletBookingDTO createBooking(UUID tenantId, UUID landlordId, UUID propertyId, LocalDate startDate, LocalDate endDate, String firstName, String lastName, String phoneNumber, Integer guestNumber, String email, Double amount, String currency, String sessionId, String userAgent, String sourceIP);
    List<ShortletBookingDTO> getBookings(UUID landlordId, int page, int size);
    ShortletBookingDTO acceptBooking(UUID bookingId);
    ShortletBookingDTO rejectBooking(UUID bookingId);
    ShortletBookingDTO cancelBooking(UUID bookingId);
    ShortletBookingDTO rescheduleBooking(UUID bookingId, LocalDate newStartDate, LocalDate newEndDate);
    ShortletBookingDTO updateBookingPayment(UUID bookingId, String paymentStatus, String paymentReference, Double paymentAmount);

    // NEW MISSING METHODS FOR TENANT FUNCTIONALITY
    List<ShortletBookingDTO> getTenantBookings(UUID tenantId, int page, int limit, String status);
    ShortletBookingDTO getBookingById(UUID bookingId);
    List<ShortletBookingDTO> getAllBookings(int page, int limit, String status, LocalDate dateFrom, LocalDate dateTo);
    List<ShortletBookingDTO> getBookingsByProperty(UUID propertyId);
}
