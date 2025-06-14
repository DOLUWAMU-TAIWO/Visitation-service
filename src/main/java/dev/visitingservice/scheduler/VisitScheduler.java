package dev.visitingservice.scheduler;

import dev.visitingservice.model.Visit;
import dev.visitingservice.model.Status;
import dev.visitingservice.repository.VisitRepository;
import dev.visitingservice.service.NotificationPublisher;
import dev.visitingservice.service.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
public class VisitScheduler {

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private VisitService visitService;

    @Autowired
    private NotificationPublisher notificationPublisher;

    // Runs every 15 minutes to send visit reminders
    @Scheduled(cron = "0 */15 * * * *")
    public void sendUpcomingVisitReminders() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        sendReminderForWindow(now.plusHours(24), 24);
        sendReminderForWindow(now.plusHours(1), 1);
    }

    private void sendReminderForWindow(OffsetDateTime start, int hoursBefore) {
        OffsetDateTime end = start.plusMinutes(15);
        List<Visit> visits = visitRepository.findByStatusAndScheduledAtBetween(Status.APPROVED, start, end);

        for (Visit visit : visits) {
            notificationPublisher.sendReminder(visit, hoursBefore);
        }
    }

    // Runs every hour to mark approved past visits as completed
    @Scheduled(cron = "0 0 * * * *")
    public void autoCompletePastVisits() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        List<Visit> pastVisits = visitRepository.findByStatusAndScheduledAtBefore(Status.APPROVED, now);

        for (Visit visit : pastVisits) {
            visitService.updateVisitStatus(visit.getId(), Status.COMPLETED);
        }
    }
}