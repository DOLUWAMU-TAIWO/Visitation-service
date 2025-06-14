package dev.visitingservice.repository;

import dev.visitingservice.model.VisitFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VisitFeedbackRepository extends JpaRepository<VisitFeedback, UUID> {
    List<VisitFeedback> findByVisitId(UUID visitId);
}
