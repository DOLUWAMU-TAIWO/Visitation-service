package dev.visitingservice.service;

import dev.visitingservice.dto.ShortletAvailabilityDTO;
import dev.visitingservice.model.ShortletAvailability;
import dev.visitingservice.model.ShortletBooking;
import dev.visitingservice.model.ShortletBooking.BookingStatus;
import dev.visitingservice.repository.ShortletAvailabilityRepository;
import dev.visitingservice.repository.ShortletBookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ShortletAvailabilityServiceIntegrationTest {
    @Autowired
    private ShortletAvailabilityService availabilityService;
    @Autowired
    private ShortletAvailabilityRepository availabilityRepository;
    @Autowired
    private ShortletBookingRepository bookingRepository;

    private UUID landlordId;
    private UUID propertyId;

    @BeforeEach
    void setup() {
        landlordId = UUID.randomUUID();
        propertyId = UUID.randomUUID();
        availabilityRepository.deleteAll();
        bookingRepository.deleteAll();
    }

    @Test
    void testAvailableWithNoBookings() {
        // Availability: 2025-10-01 to 2025-10-30
        ShortletAvailability avail = new ShortletAvailability();
        avail.setLandlordId(landlordId);
        avail.setPropertyId(propertyId);
        avail.setStartDate(LocalDate.of(2025, 10, 1));
        avail.setEndDate(LocalDate.of(2025, 10, 30));
        availabilityRepository.save(avail);

        List<UUID> available = availabilityService.getAvailablePropertyIdsInRange(LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 30));
        assertTrue(available.contains(propertyId));
    }

    @Test
    void testUnavailableWithOverlappingAcceptedBooking() {
        // Availability: 2025-10-01 to 2025-10-30
        ShortletAvailability avail = new ShortletAvailability();
        avail.setLandlordId(landlordId);
        avail.setPropertyId(propertyId);
        avail.setStartDate(LocalDate.of(2025, 10, 1));
        avail.setEndDate(LocalDate.of(2025, 10, 30));
        availabilityRepository.save(avail);
        // Booking: 2025-10-10 to 2025-10-20 (ACCEPTED)
        ShortletBooking booking = new ShortletBooking();
        booking.setLandlordId(landlordId);
        booking.setPropertyId(propertyId);
        booking.setTenantId(UUID.randomUUID());
        booking.setStartDate(LocalDate.of(2025, 10, 10));
        booking.setEndDate(LocalDate.of(2025, 10, 20));
        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setFirstName("Test");
        booking.setLastName("User");
        booking.setPhoneNumber("1234567890");
        bookingRepository.save(booking);

        List<UUID> available = availabilityService.getAvailablePropertyIdsInRange(LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 30));
        assertFalse(available.contains(propertyId));
    }

    @Test
    void testAvailableWithRejectedBooking() {
        // Availability: 2025-10-01 to 2025-10-30
        ShortletAvailability avail = new ShortletAvailability();
        avail.setLandlordId(landlordId);
        avail.setPropertyId(propertyId);
        avail.setStartDate(LocalDate.of(2025, 10, 1));
        avail.setEndDate(LocalDate.of(2025, 10, 30));
        availabilityRepository.save(avail);
        // Booking: 2025-10-10 to 2025-10-20 (REJECTED)
        ShortletBooking booking = new ShortletBooking();
        booking.setLandlordId(landlordId);
        booking.setPropertyId(propertyId);
        booking.setStartDate(LocalDate.of(2025, 10, 10));
        booking.setEndDate(LocalDate.of(2025, 10, 20));
        booking.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(booking);

        List<UUID> available = availabilityService.getAvailablePropertyIdsInRange(LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 30));
        assertTrue(available.contains(propertyId));
    }

    @Test
    void testAvailableWithAdjacentBooking() {
        // Availability: 2025-10-01 to 2025-10-30
        ShortletAvailability avail = new ShortletAvailability();
        avail.setLandlordId(landlordId);
        avail.setPropertyId(propertyId);
        avail.setStartDate(LocalDate.of(2025, 10, 1));
        avail.setEndDate(LocalDate.of(2025, 10, 30));
        availabilityRepository.save(avail);
        // Booking: 2025-09-25 to 2025-09-30 (ACCEPTED)
        ShortletBooking booking = new ShortletBooking();
        booking.setLandlordId(landlordId);
        booking.setPropertyId(propertyId);
        booking.setStartDate(LocalDate.of(2025, 9, 25));
        booking.setEndDate(LocalDate.of(2025, 9, 30));
        booking.setStatus(BookingStatus.ACCEPTED);
        bookingRepository.save(booking);

        List<UUID> available = availabilityService.getAvailablePropertyIdsInRange(LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 30));
        assertTrue(available.contains(propertyId));
    }

    @Test
    void testUnavailableWithPartialOverlapBooking() {
        // Availability: 2025-10-01 to 2025-10-30
        ShortletAvailability avail = new ShortletAvailability();
        avail.setLandlordId(landlordId);
        avail.setPropertyId(propertyId);
        avail.setStartDate(LocalDate.of(2025, 10, 1));
        avail.setEndDate(LocalDate.of(2025, 10, 30));
        availabilityRepository.save(avail);
        // Booking: 2025-09-25 to 2025-10-05 (ACCEPTED)
        ShortletBooking booking = new ShortletBooking();
        booking.setLandlordId(landlordId);
        booking.setPropertyId(propertyId);
        booking.setStartDate(LocalDate.of(2025, 9, 25));
        booking.setEndDate(LocalDate.of(2025, 10, 5));
        booking.setStatus(BookingStatus.ACCEPTED);
        bookingRepository.save(booking);

        List<UUID> available = availabilityService.getAvailablePropertyIdsInRange(LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 30));
        assertFalse(available.contains(propertyId));
    }
}
