package dev.visitingservice.repository;

import dev.visitingservice.model.ShortletAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShortletAvailabilityRepository extends JpaRepository<ShortletAvailability, UUID> {
    List<ShortletAvailability> findByLandlordId(UUID landlordId);
    boolean existsByLandlordIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(UUID landlordId, LocalDate endDate, LocalDate startDate);
    List<ShortletAvailability> findByLandlordIdAndPropertyId(UUID landlordId, UUID propertyId);
    boolean existsByLandlordIdAndPropertyIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(UUID landlordId, UUID propertyId, LocalDate endDate, LocalDate startDate);
}
