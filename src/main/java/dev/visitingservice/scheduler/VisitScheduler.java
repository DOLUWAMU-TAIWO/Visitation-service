package dev.visitingservice.scheduler;

import dev.visitingservice.model.Visit;
import dev.visitingservice.model.Status;
import dev.visitingservice.repository.VisitRepository;
import dev.visitingservice.service.NotificationPublisher;
import dev.visitingservice.service.VisitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Component
public class VisitScheduler {

    private static final Logger logger = LoggerFactory.getLogger(VisitScheduler.class);
    private static final ZoneId NIGERIA_ZONE = ZoneId.of("Africa/Lagos");

    private final VisitRepository visitRepository;
    private final VisitService visitService;
    private final NotificationPublisher notificationPublisher;

    public VisitScheduler(VisitRepository visitRepository,
                         VisitService visitService,
                         NotificationPublisher notificationPublisher) {
        this.visitRepository = visitRepository;
        this.visitService = visitService;
        this.notificationPublisher = notificationPublisher;
    }

    // Runs every hour for precise visit reminder timing
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void sendUpcomingVisitReminders() {
        ZonedDateTime nowNigeria = ZonedDateTime.now(NIGERIA_ZONE);

        // Send 24-hour reminders
        send24HourReminders(nowNigeria);

        // Send 5-hour reminders
        send5HourReminders(nowNigeria);

        // Send 1-hour reminders
        send1HourReminders(nowNigeria);

        logger.debug("Processed visit reminders at {} Nigeria time", nowNigeria);
    }

    private void send24HourReminders(ZonedDateTime now) {
        // Find visits exactly 24 hours from now
        OffsetDateTime target24h = now.plusHours(24).toOffsetDateTime();
        OffsetDateTime startWindow = target24h.minusMinutes(30);
        OffsetDateTime endWindow = target24h.plusMinutes(30);

        List<Visit> visits = visitRepository.findByStatusAndScheduledAtBetween(
            Status.APPROVED, startWindow, endWindow);

        int sentCount = 0;
        for (Visit visit : visits) {
            // Check if we already sent 24h reminder by checking if visit was created more than 24h ago
            OffsetDateTime createdAt = visit.getCreatedAt();
            if (createdAt != null && createdAt.isBefore(now.minusHours(24).toOffsetDateTime())) {
                try {
                    notificationPublisher.sendReminder(visit, 24);
                    sentCount++;
                    logger.info("Sent 24-hour reminder for visit {}", visit.getId());
                } catch (Exception e) {
                    logger.error("Failed to send 24h reminder for visit {}: {}", visit.getId(), e.getMessage());
                }
            }
        }

        if (sentCount > 0) {
            logger.info("Sent {} 24-hour visit reminders", sentCount);
        }
    }

    private void send5HourReminders(ZonedDateTime now) {
        // Find visits in about 5 hours
        OffsetDateTime target5h = now.plusHours(5).toOffsetDateTime();
        OffsetDateTime startWindow = target5h.minusMinutes(30);
        OffsetDateTime endWindow = target5h.plusMinutes(30);

        List<Visit> visits = visitRepository.findByStatusAndScheduledAtBetween(
            Status.APPROVED, startWindow, endWindow);

        int sentCount = 0;
        for (Visit visit : visits) {
            // Only send if visit is today and we're 5-6 hours before
            OffsetDateTime visitTime = visit.getScheduledAt();
            long hoursUntilVisit = java.time.Duration.between(now.toOffsetDateTime(), visitTime).toHours();

            if (hoursUntilVisit >= 4 && hoursUntilVisit <= 6) { // 4-6 hour window
                try {
                    notificationPublisher.sendReminder(visit, 5);
                    sentCount++;
                    logger.info("Sent 5-hour reminder for visit {}", visit.getId());
                } catch (Exception e) {
                    logger.error("Failed to send 5h reminder for visit {}: {}", visit.getId(), e.getMessage());
                }
            }
        }

        if (sentCount > 0) {
            logger.info("Sent {} 5-hour visit reminders", sentCount);
        }
    }

    private void send1HourReminders(ZonedDateTime now) {
        // Find visits in about 1 hour
        OffsetDateTime target1h = now.plusHours(1).toOffsetDateTime();
        OffsetDateTime startWindow = target1h.minusMinutes(15);
        OffsetDateTime endWindow = target1h.plusMinutes(15);

        List<Visit> visits = visitRepository.findByStatusAndScheduledAtBetween(
            Status.APPROVED, startWindow, endWindow);

        int sentCount = 0;
        for (Visit visit : visits) {
            // Only send if visit is within 45-75 minutes
            OffsetDateTime visitTime = visit.getScheduledAt();
            long minutesUntilVisit = java.time.Duration.between(now.toOffsetDateTime(), visitTime).toMinutes();

            if (minutesUntilVisit >= 45 && minutesUntilVisit <= 75) { // 45-75 minute window
                try {
                    notificationPublisher.sendReminder(visit, 1);
                    sentCount++;
                    logger.info("Sent 1-hour reminder for visit {}", visit.getId());
                } catch (Exception e) {
                    logger.error("Failed to send 1h reminder for visit {}: {}", visit.getId(), e.getMessage());
                }
            }
        }

        if (sentCount > 0) {
            logger.info("Sent {} 1-hour visit reminders", sentCount);
        }
    }

    // Runs every 30 minutes to mark completed visits (more frequent for better UX)
    @Scheduled(cron = "0 */30 * * * *")
    @Transactional
    public void autoCompletePastVisits() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        // Add a grace period of 30 minutes after scheduled time before marking as completed
        OffsetDateTime cutoffTime = now.minusMinutes(30);

        List<Visit> pastVisits = visitRepository.findByStatusAndScheduledAtBefore(
            Status.APPROVED, cutoffTime);

        int completedCount = 0;
        for (Visit visit : pastVisits) {
            try {
                visitService.updateVisitStatus(visit.getId(), Status.COMPLETED);
                completedCount++;
                logger.info("Auto-completed past visit {}", visit.getId());
            } catch (Exception e) {
                logger.error("Failed to complete visit {}: {}", visit.getId(), e.getMessage());
            }
        }

        if (completedCount > 0) {
            logger.info("Auto-completed {} past visits", completedCount);
        }
    }
}

