package dev.visitingservice.service;

import dev.visitingservice.dto.ShortletBookingDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ShortletBookingService {
    ShortletBookingDTO createBooking(UUID tenantId, UUID landlordId, UUID propertyId, LocalDate startDate, LocalDate endDate);
    List<ShortletBookingDTO> getBookings(UUID landlordId, int page, int size);
    ShortletBookingDTO acceptBooking(UUID bookingId);
    ShortletBookingDTO rejectBooking(UUID bookingId);
    ShortletBookingDTO cancelBooking(UUID bookingId);
    ShortletBookingDTO rescheduleBooking(UUID bookingId, LocalDate newStartDate, LocalDate newEndDate);
}
