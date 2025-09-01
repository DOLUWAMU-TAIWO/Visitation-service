package dev.visitingservice.service.impl;

import dev.visitingservice.model.Visit;
import dev.visitingservice.model.Status;
import dev.visitingservice.repository.VisitRepository;
import dev.visitingservice.repository.AvailabilitySlotRepository;
import dev.visitingservice.model.AvailabilitySlot;
import dev.visitingservice.exception.SlotUnavailableException;
import dev.visitingservice.exception.InvalidRequestException;
import dev.visitingservice.service.VisitService;
import dev.visitingservice.service.NotificationPublisher;
import dev.visitingservice.service.VisitValidationService;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class VisitServiceImpl implements VisitService {

    private static final Logger logger = LoggerFactory.getLogger(VisitServiceImpl.class);

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private AvailabilitySlotRepository slotRepository;

    @Autowired
    private NotificationPublisher notificationPublisher;

    @Autowired
    private VisitValidationService validationService;

    @Override
    @Transactional
    public Visit requestVisit(Visit visit) {
        // validate users and ownership
        validationService.validateUserExists(visit.getVisitorId(), "tenant");
        validationService.validateUserExists(visit.getLandlordId(), "landlord");
        validationService.validateTenantAndLandlord(visit.getVisitorId(), visit.getLandlordId());
        validationService.validateListingOwnership(visit.getPropertyId(), visit.getLandlordId());

        // verify and book slot
        UUID slotId = visit.getSlotId();
        if (slotId == null) {
            throw new InvalidRequestException("slotId is required");
        }
        AvailabilitySlot slot = slotRepository.findById(slotId)
            .orElseThrow(() -> new InvalidRequestException("Slot not found: " + slotId));
        if (slot.isBooked()) {
            throw new SlotUnavailableException("Slot is already booked");
        }
        slot.setBooked(true);
        slotRepository.save(slot);

        // set visit timing from slot
        visit.setScheduledAt(slot.getStartTime());
        visit.setDurationMinutes((int) java.time.Duration.between(slot.getStartTime(), slot.getEndTime()).toMinutes());

        // must schedule in future
        if (visit.getScheduledAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
            throw new InvalidRequestException("scheduledAt must be in the future");
        }
        // no need 12h advance if slot managed externally

        visit.setStatus(Status.PENDING);
        Visit saved = visitRepository.save(visit);
        notificationPublisher.sendVisitRequested(saved);
        return saved;
    }

    @Override
    @Transactional
    public Visit approveVisit(UUID visitId) {
        return updateVisitStatus(visitId, Status.APPROVED);
    }

    private void releaseSlotIfExists(Visit visit) {
        UUID slotId = visit.getSlotId();
        if (slotId != null) {
            slotRepository.findById(slotId).ifPresent(slot -> {
                slot.setBooked(false);
                slotRepository.save(slot);
            });
        }
    }

    @Override
    @Transactional
    public Visit rejectVisit(UUID visitId) {
        return updateVisitStatus(visitId, Status.REJECTED);
    }

    @Override
    @Transactional
    public Visit cancelVisit(UUID visitId) {
        return updateVisitStatus(visitId, Status.CANCELLED);
    }

    @Override
    @Transactional
    public Visit completeVisit(UUID visitId) {
        return updateVisitStatus(visitId, Status.COMPLETED);
    }

    private boolean isAllowedTransition(Status from, Status to) {
        return switch (from) {
            case PENDING -> to == Status.APPROVED || to == Status.REJECTED || to == Status.CANCELLED || to == Status.RESCHEDULED;
            case APPROVED -> to == Status.COMPLETED || to == Status.CANCELLED || to == Status.RESCHEDULED;
            case RESCHEDULED -> to == Status.APPROVED || to == Status.REJECTED || to == Status.CANCELLED;
            default -> false;
        };
    }

    @Override
    @Transactional
    public Visit updateVisitStatus(UUID visitId, Status newStatus) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Visit not found"));

        if (!isAllowedTransition(visit.getStatus(), newStatus)) {
            throw new IllegalStateException("Cannot change status from " + visit.getStatus() + " to " + newStatus);
        }

        if (newStatus == Status.APPROVED) {
            validationService.validateListingOwnership(visit.getPropertyId(), visit.getLandlordId());
        }

        visit.setStatus(newStatus);
        Visit updated = visitRepository.save(visit);

        switch (newStatus) {
            case APPROVED -> notificationPublisher.sendVisitApproved(updated);
            case REJECTED -> {
                releaseSlotIfExists(visit);
                notificationPublisher.sendVisitRejected(updated);
            }
            case CANCELLED -> {
                releaseSlotIfExists(visit);
                notificationPublisher.sendVisitCancelled(updated);
            }
            case COMPLETED -> {
                // Only send feedback email if it hasn't been sent already
                if (!updated.isFeedbackEmailSent()) {
                    logger.info("📧 Attempting to send feedback prompt notification for visit {}", visitId);

                    try {
                        notificationPublisher.sendFeedbackPrompt(updated);
                        logger.info("✅ Feedback email sent successfully for visit {}", visitId);
                    } catch (Exception emailError) {
                        logger.error("⚠️ Failed to send feedback email for visit {}: {}",
                            visitId, emailError.getMessage());
                        // Don't rethrow - we still want to mark it as attempted
                    }

                    // ALWAYS mark feedback email as sent/attempted, regardless of success/failure
                    updated.setFeedbackEmailSent(true);
                    visitRepository.save(updated);

                    logger.info("🏷️ Feedback email marked as sent for visit {} (prevents future attempts)", visitId);
                } else {
                    logger.info("⏭️ Skipping feedback email for visit {} - already sent", visitId);
                }
            }
            default -> {}
        }

        return updated;
    }


    @Override
    public List<Visit> getVisitsByProperty(UUID propertyId) {
        return visitRepository.findByPropertyId(propertyId);
    }

    @Override
    public List<Visit> getVisitsByVisitor(UUID visitorId) {
        return visitRepository.findByVisitorId(visitorId);
    }

    @Override
    public List<Visit> getVisitsByLandlord(UUID landlordId) {
        return visitRepository.findByLandlordId(landlordId);
    }

    @Override
    public Visit getVisit(UUID visitId) {
        return visitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Visit not found"));
    }

    @Override
    public List<Visit> getVisitsByStatusWithin(Status status, OffsetDateTime start, OffsetDateTime end) {
        return visitRepository.findByStatusAndScheduledAtBetween(status, start, end);
    }
}