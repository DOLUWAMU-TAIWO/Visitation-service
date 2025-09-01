package dev.visitingservice.repository;

import dev.visitingservice.model.Visit;
import dev.visitingservice.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface VisitRepository extends JpaRepository<Visit, UUID> {
    
    // ========== STANDARD QUERIES (Working and Tested) ==========
    List<Visit> findByPropertyId(UUID propertyId);
    List<Visit> findByVisitorId(UUID visitorId);
    List<Visit> findByLandlordId(UUID landlordId);
    List<Visit> findByStatus(Status status);
    List<Visit> findByStatusAndScheduledAtBetween(Status status, OffsetDateTime start, OffsetDateTime end);
    List<Visit> findByStatusAndScheduledAtBefore(Status status, OffsetDateTime time);

    // Cleanup methods
    List<Visit> findByStatusAndCreatedAtBefore(Status status, OffsetDateTime cutoffTime);
    long countByStatus(Status status);
    long countByStatusAndUpdatedAtAfter(Status status, OffsetDateTime after);
}
