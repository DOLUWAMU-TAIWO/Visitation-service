package dev.visitingservice.controller;

import dev.visitingservice.dto.ShortletAvailabilityDTO;
import dev.visitingservice.dto.ShortletBookingDTO;
import dev.visitingservice.service.ShortletAvailabilityService;
import dev.visitingservice.service.ShortletBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/shortlets")
public class ShortletUnifiedController {

    private final ShortletAvailabilityService availabilityService;
    private final ShortletBookingService bookingService;

    @Autowired
    public ShortletUnifiedController(ShortletAvailabilityService availabilityService, ShortletBookingService bookingService) {
        this.availabilityService = availabilityService;
        this.bookingService = bookingService;
    }

    // --- Availability Endpoints ---
    @PostMapping("/availability/{landlordId}")
    public ResponseEntity<?> setAvailability(@PathVariable UUID landlordId, @RequestBody Map<String, String> body) {
        try {
            UUID propertyId = UUID.fromString(body.get("propertyId"));
            LocalDate startDate = LocalDate.parse(body.get("startDate"));
            LocalDate endDate = LocalDate.parse(body.get("endDate"));
            ShortletAvailabilityDTO dto = availabilityService.setAvailability(landlordId, propertyId, startDate, endDate);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/availability/{landlordId}/{propertyId}")
    public ResponseEntity<List<ShortletAvailabilityDTO>> getAvailability(@PathVariable UUID landlordId, @PathVariable UUID propertyId) {
        List<ShortletAvailabilityDTO> list = availabilityService.getAvailability(landlordId, propertyId);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/availability/{availabilityId}")
    public ResponseEntity<?> deleteAvailability(@PathVariable UUID availabilityId) {
        try {
            availabilityService.deleteAvailability(availabilityId);
            return ResponseEntity.ok(Map.of("message", "Availability deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- Booking Endpoints ---
    @PostMapping("/bookings")
    public ResponseEntity<?> createBooking(@RequestBody Map<String, String> body) {
        try {
            UUID tenantId = UUID.fromString(body.get("tenantId"));
            UUID landlordId = UUID.fromString(body.get("landlordId"));
            UUID propertyId = UUID.fromString(body.get("propertyId"));
            LocalDate startDate = LocalDate.parse(body.get("startDate"));
            LocalDate endDate = LocalDate.parse(body.get("endDate"));
            String firstName = body.get("firstName");
            String lastName = body.get("lastName");
            String phoneNumber = body.get("phoneNumber");
            ShortletBookingDTO dto = bookingService.createBooking(tenantId, landlordId, propertyId, startDate, endDate, firstName, lastName, phoneNumber);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/bookings/{landlordId}")
    public ResponseEntity<List<ShortletBookingDTO>> getBookings(
            @PathVariable UUID landlordId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<ShortletBookingDTO> bookings = bookingService.getBookings(landlordId, page, size);
        return ResponseEntity.ok(bookings);
    }

    @PostMapping("/bookings/{bookingId}/accept")
    public ResponseEntity<?> acceptBooking(@PathVariable UUID bookingId) {
        try {
            bookingService.acceptBooking(bookingId);
            // Publish notification to tenant and landlord
            // Send custom email to tenant and landlord
            // Schedule reminder email for tenant (e.g., 1 day before startDate)
            // These can be implemented via NotificationService, EmailService, and a scheduler
            return ResponseEntity.ok(Map.of("message", "Booking accepted successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/bookings/{bookingId}/reject")
    public ResponseEntity<?> rejectBooking(@PathVariable UUID bookingId) {
        try {
            bookingService.rejectBooking(bookingId);
            return ResponseEntity.ok(Map.of("message", "Booking rejected successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable UUID bookingId) {
        try {
            bookingService.cancelBooking(bookingId);
            return ResponseEntity.ok(Map.of("message", "Booking cancelled successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/bookings/{bookingId}/reschedule")
    public ResponseEntity<?> rescheduleBooking(
            @PathVariable UUID bookingId,
            @RequestBody Map<String, String> body) {
        try {
            LocalDate newStartDate = LocalDate.parse(body.get("startDate"));
            LocalDate newEndDate = LocalDate.parse(body.get("endDate"));
            ShortletBookingDTO dto = bookingService.rescheduleBooking(bookingId, newStartDate, newEndDate);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
