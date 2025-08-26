package dev.visitingservice.repository;

import dev.visitingservice.model.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, UUID> {

    boolean existsByPropertyIdAndStartTimeLessThanAndEndTimeGreaterThan(UUID propertyId, OffsetDateTime end, OffsetDateTime start);

    boolean existsByPropertyIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqualAndBookedFalse(UUID propertyId, OffsetDateTime start, OffsetDateTime end);

    List<AvailabilitySlot> findByPropertyIdAndStartTimeAfterAndBookedFalse(UUID propertyId, OffsetDateTime after);

    // ✅ Add this line below
    List<AvailabilitySlot> findByPropertyIdAndLandlordIdAndStartTimeAfterAndBookedFalse(UUID propertyId, UUID landlordId, OffsetDateTime after);

    List<AvailabilitySlot> findByPropertyIdAndLandlordIdAndStartTimeAfter(UUID propertyId, UUID landlordId, OffsetDateTime after);

    // Method for calendar view
    List<AvailabilitySlot> findByPropertyIdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(UUID propertyId, OffsetDateTime start, OffsetDateTime end);

    // Cleanup method
    int deleteByEndTimeBefore(OffsetDateTime cutoffTime);
    List<AvailabilitySlot> findByEndTimeBefore(OffsetDateTime cutoffTime);
}