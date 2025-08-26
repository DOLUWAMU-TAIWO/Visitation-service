package dev.visitingservice.service;

import dev.visitingservice.model.ShortletAvailability;
import dev.visitingservice.model.ShortletBooking;
import dev.visitingservice.model.ShortletBooking.BookingStatus;
import dev.visitingservice.repository.ShortletAvailabilityRepository;
import dev.visitingservice.repository.ShortletBookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=password",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "jwt.secret=mySecretKey123456789012345678901234567890123456789012345678901234567890",
    "jwt.expirationMs=86400000",
    "jwt.refreshExpirationMs=604800000"
})
public class SameDayBookingTest {

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
    void testSameDayCheckoutCheckinScenario() {
        System.out.println("=== TESTING SAME DAY CHECKOUT/CHECKIN SCENARIO ===");

        // Set up availability for September 1-30
        ShortletAvailability avail = new ShortletAvailability();
        avail.setLandlordId(landlordId);
        avail.setPropertyId(propertyId);
        avail.setStartDate(LocalDate.of(2024, 9, 1));
        avail.setEndDate(LocalDate.of(2024, 9, 30));
        availabilityRepository.save(avail);
        System.out.println("✓ Created availability: Sept 1-30, 2024");

        // Create existing booking: Sept 3-5 (checkout on Sept 5)
        ShortletBooking existingBooking = new ShortletBooking();
        existingBooking.setLandlordId(landlordId);
        existingBooking.setPropertyId(propertyId);
        existingBooking.setTenantId(UUID.randomUUID());
        existingBooking.setStartDate(LocalDate.of(2024, 9, 3));
        existingBooking.setEndDate(LocalDate.of(2024, 9, 5));
        existingBooking.setStatus(BookingStatus.ACCEPTED);
        existingBooking.setFirstName("John");
        existingBooking.setLastName("Doe");
        existingBooking.setPhoneNumber("1234567890");
        bookingRepository.save(existingBooking);
        System.out.println("✓ Created existing booking: Sept 3-5, 2024 (ACCEPTED)");

        // Test 1: Try to search for availability Sept 5-7 (new guest checkin on checkout day)
        System.out.println("\n--- TEST 1: Search availability Sept 5-7 ---");
        List<UUID> availableProperties = availabilityService.getAvailablePropertyIdsInRange(
            LocalDate.of(2024, 9, 5),
            LocalDate.of(2024, 9, 7)
        );

        if (availableProperties.contains(propertyId)) {
            System.out.println("❌ SYSTEM ALLOWS same-day booking (Sept 5-7) when existing booking ends Sept 5");
            System.out.println("   This means two guests could both have the property on Sept 5!");
        } else {
            System.out.println("✅ SYSTEM BLOCKS same-day booking (Sept 5-7) when existing booking ends Sept 5");
            System.out.println("   This prevents checkout/checkin conflicts on the same day");
        }

        // Test 2: Test the exact overlap logic manually
        System.out.println("\n--- TEST 2: Manual overlap logic verification ---");

        // Existing booking: Sept 3-5
        // New request: Sept 5-7
        // Query: b.startDate < 7 AND b.endDate > 5
        // Existing: 3 < 7 (true) AND 5 > 5 (false)
        boolean wouldOverlap = (3 < 7) && (5 > 5); // This is the actual logic
        System.out.println("Manual calculation: startDate(3) < endDate(7) = " + (3 < 7));
        System.out.println("Manual calculation: endDate(5) > startDate(5) = " + (5 > 5));
        System.out.println("Manual calculation: Overall overlap = " + wouldOverlap);

        // Test 3: Try to search for availability Sept 6-8 (should work)
        System.out.println("\n--- TEST 3: Search availability Sept 6-8 (should be allowed) ---");
        List<UUID> availablePropertiesNextDay = availabilityService.getAvailablePropertyIdsInRange(
            LocalDate.of(2024, 9, 6),
            LocalDate.of(2024, 9, 8)
        );

        if (availablePropertiesNextDay.contains(propertyId)) {
            System.out.println("✓ SYSTEM ALLOWS booking Sept 6-8 (day after checkout)");
        } else {
            System.out.println("❌ SYSTEM BLOCKS booking Sept 6-8 (unexpected!)");
        }

        System.out.println("\n=== TEST RESULTS ===");
        System.out.println("Same-day booking (Sept 5-7) allowed: " + availableProperties.contains(propertyId));
        System.out.println("Next-day booking (Sept 6-8) allowed: " + availablePropertiesNextDay.contains(propertyId));

        // The assertion - document what the system actually does
        boolean sameDayAllowed = availableProperties.contains(propertyId);
        System.out.println("\n=== CONCLUSION ===");
        if (sameDayAllowed) {
            System.out.println("⚠️  POTENTIAL ISSUE: The system allows same-day checkout/checkin!");
            System.out.println("    Guest 1 checks out Sept 5, Guest 2 can check in Sept 5");
            System.out.println("    This could cause conflicts if checkout is late or checkin is early");
        } else {
            System.out.println("✅ SAFE: The system prevents same-day checkout/checkin conflicts");
            System.out.println("    Guests must wait until the day after checkout to check in");
        }
    }
}
