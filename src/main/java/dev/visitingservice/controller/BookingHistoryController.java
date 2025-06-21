package dev.visitingservice.controller;

import dev.visitingservice.dto.BookingFilterDTO;
import dev.visitingservice.dto.ShortletBookingDTO;
import dev.visitingservice.model.ShortletBooking;
import dev.visitingservice.model.ShortletBooking.BookingStatus;
import dev.visitingservice.repository.ShortletBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings/history")
public class BookingHistoryController {

    private final ShortletBookingRepository bookingRepository;

    @Autowired
    public BookingHistoryController(ShortletBookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @PostMapping("/search")
    public ResponseEntity<List<ShortletBookingDTO>> searchBookings(@RequestBody BookingFilterDTO filter) {
        List<ShortletBooking> bookings;

        // Complex query with multiple filters
        if (filter.getTenantId() != null && filter.getStatus() != null && filter.getFromDate() != null && filter.getToDate() != null) {
            BookingStatus status = BookingStatus.valueOf(filter.getStatus());
            bookings = bookingRepository.findByTenantIdAndStatusAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                    filter.getTenantId(), status, filter.getFromDate(), filter.getToDate());
        }
        // Landlord and status and date range
        else if (filter.getLandlordId() != null && filter.getStatus() != null && filter.getFromDate() != null && filter.getToDate() != null) {
            BookingStatus status = BookingStatus.valueOf(filter.getStatus());
            bookings = bookingRepository.findByLandlordIdAndStatusAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                    filter.getLandlordId(), status, filter.getFromDate(), filter.getToDate());
        }
        // Filter by status only
        else if (filter.getStatus() != null) {
            BookingStatus status = BookingStatus.valueOf(filter.getStatus());
            bookings = bookingRepository.findByStatus(status);
        }
        // Filter by tenant only
        else if (filter.getTenantId() != null) {
            bookings = bookingRepository.findByTenantId(filter.getTenantId());
        }
        // Filter by landlord only
        else if (filter.getLandlordId() != null) {
            bookings = bookingRepository.findByLandlordId(filter.getLandlordId());
        }
        // Filter by date range only
        else if (filter.getFromDate() != null && filter.getToDate() != null) {
            bookings = bookingRepository.findByStartDateGreaterThanEqualAndEndDateLessThanEqual(
                    filter.getFromDate(), filter.getToDate());
        }
        // No filters - return all (paginated)
        else {
            bookings = bookingRepository.findAll();
        }

        // Apply pagination manually
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? filter.getSize() : 20;
        bookings = bookings.stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());

        // Convert to DTOs
        List<ShortletBookingDTO> result = bookings.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<ShortletBookingDTO>> getBookingsByTenant(
            @PathVariable UUID tenantId,
            @RequestParam(required = false) String status) {
        List<ShortletBooking> bookings;
        if (status != null) {
            bookings = bookingRepository.findByTenantIdAndStatus(tenantId, BookingStatus.valueOf(status));
        } else {
            bookings = bookingRepository.findByTenantId(tenantId);
        }
        return ResponseEntity.ok(bookings.stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @GetMapping("/landlord/{landlordId}")
    public ResponseEntity<List<ShortletBookingDTO>> getBookingsByLandlord(
            @PathVariable UUID landlordId,
            @RequestParam(required = false) String status) {
        List<ShortletBooking> bookings;
        if (status != null) {
            bookings = bookingRepository.findByLandlordIdAndStatus(landlordId, BookingStatus.valueOf(status));
        } else {
            bookings = bookingRepository.findByLandlordId(landlordId);
        }
        return ResponseEntity.ok(bookings.stream().map(this::toDTO).collect(Collectors.toList()));
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
        return dto;
    }
}
