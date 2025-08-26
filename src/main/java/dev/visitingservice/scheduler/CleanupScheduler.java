package dev.visitingservice.scheduler;

import dev.visitingservice.model.AvailabilitySlot;
import dev.visitingservice.model.ShortletBooking;
import dev.visitingservice.model.Visit;
import dev.visitingservice.model.Status;
import dev.visitingservice.model.ShortletBooking.BookingStatus;
import dev.visitingservice.repository.ShortletBookingRepository;
import dev.visitingservice.repository.VisitRepository;
import dev.visitingservice.repository.AvailabilitySlotRepository;
import dev.visitingservice.service.NotificationPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(CleanupScheduler.class);
    private static final String ADMIN_EMAIL = "admin@zennest.africa";

    private final ShortletBookingRepository bookingRepository;
    private final VisitRepository visitRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final NotificationPublisher notificationPublisher;

    public CleanupScheduler(ShortletBookingRepository bookingRepository,
                          VisitRepository visitRepository,
                          AvailabilitySlotRepository availabilitySlotRepository,
                          NotificationPublisher notificationPublisher) {
        this.bookingRepository = bookingRepository;
        this.visitRepository = visitRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.notificationPublisher = notificationPublisher;
    }

    // Run daily at 2 AM to delete expired pending visits and their slots
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void deleteExpiredPendingVisits() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        // Find expired pending visits (scheduled time has passed)
        List<Visit> expiredVisits = visitRepository
            .findByStatusAndScheduledAtBefore(Status.PENDING, now);

        int deletedVisits = 0;
        int releasedSlots = 0;

        for (Visit visit : expiredVisits) {
            // Release the associated slot if it exists
            if (visit.getSlotId() != null) {
                availabilitySlotRepository.findById(visit.getSlotId())
                    .ifPresent(slot -> {
                        slot.setBooked(false);
                        availabilitySlotRepository.save(slot);
                    });
                releasedSlots++;
            }

            // Delete the expired visit
            visitRepository.delete(visit);
            deletedVisits++;
        }

        logger.info("Deleted {} expired pending visits and released {} slots", deletedVisits, releasedSlots);

        // Send admin notification if any cleanup happened
        if (deletedVisits > 0) {
            sendAdminCleanupNotification("Expired Visits Cleanup",
                String.format("Deleted %d expired pending visits and released %d availability slots",
                deletedVisits, releasedSlots));
        }
    }

    // Run daily at 3 AM to delete expired pending bookings
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteExpiredPendingBookings() {
        LocalDate today = LocalDate.now();

        // Find expired pending bookings (start date has passed)
        List<ShortletBooking> expiredBookings = bookingRepository
            .findByStatusAndStartDateBefore(BookingStatus.PENDING, today);

        int deletedBookings = 0;
        for (ShortletBooking booking : expiredBookings) {
            bookingRepository.delete(booking);
            deletedBookings++;
        }

        logger.info("Deleted {} expired pending bookings", deletedBookings);

        // Send admin notification if any cleanup happened
        if (deletedBookings > 0) {
            sendAdminCleanupNotification("Expired Bookings Cleanup",
                String.format("Deleted %d expired pending bookings", deletedBookings));
        }
    }

    // Run daily at 4 AM to clean up old availability slots
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void cleanupOldAvailabilitySlots() {
        OffsetDateTime cutoffTime = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1); // Yesterday

        // Use efficient database query instead of findAll() + filter
        List<AvailabilitySlot> oldSlots = availabilitySlotRepository
            .findByEndTimeBefore(cutoffTime);

        int deletedSlots = 0;
        for (AvailabilitySlot slot : oldSlots) {
            availabilitySlotRepository.delete(slot);
            deletedSlots++;
        }

        logger.info("Deleted {} old availability slots older than {}", deletedSlots, cutoffTime);

        // Always send admin notification for daily slot cleanup
        sendAdminCleanupNotification("Daily Availability Slots Cleanup",
            String.format("Cleaned up %d availability slots older than %s",
            deletedSlots, cutoffTime.toLocalDate()));
    }

    // Run monthly on the 1st at 5 AM to reset reminder flags for completed bookings
    @Scheduled(cron = "0 0 5 1 * *")
    @Transactional
    public void resetReminderFlags() {
        LocalDate cutoffDate = LocalDate.now().minusMonths(2);

        List<ShortletBooking> completedBookings = bookingRepository
            .findCompletedBookingsWithReminderFlags(cutoffDate);

        int resetCount = 0;
        for (ShortletBooking booking : completedBookings) {
            if (booking.getReminder24hSent() || booking.getReminder5hSent() || booking.getReminder1hSent()) {
                booking.setReminder24hSent(false);
                booking.setReminder5hSent(false);
                booking.setReminder1hSent(false);
                bookingRepository.save(booking);
                resetCount++;
            }
        }

        logger.info("Reset reminder flags for {} old completed bookings", resetCount);

        if (resetCount > 0) {
            sendAdminCleanupNotification("Monthly Reminder Flags Reset",
                String.format("Reset reminder flags for %d old completed bookings", resetCount));
        }
    }

    // Run daily at 1 AM to generate and send system health report
    @Scheduled(cron = "0 0 1 * * *")
    public void generateAndSendHealthReport() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        LocalDate today = now.toLocalDate();

        // Collect system statistics
        long pendingVisits = visitRepository.countByStatus(Status.PENDING);
        long approvedVisits = visitRepository.countByStatus(Status.APPROVED);
        long completedVisits = visitRepository.countByStatus(Status.COMPLETED);

        long pendingBookings = bookingRepository.countByStatus(BookingStatus.PENDING);
        long acceptedBookings = bookingRepository.countByStatus(BookingStatus.ACCEPTED);
        long completedBookingsToday = bookingRepository.countByStatusAndUpdatedAtAfter(
            BookingStatus.ACCEPTED, now.minusDays(1));

        long totalSlots = availabilitySlotRepository.count();
        long bookedSlots = availabilitySlotRepository.findAll().stream()
            .mapToLong(slot -> slot.isBooked() ? 1 : 0)
            .sum();

        // Build health report
        StringBuilder report = new StringBuilder();
        report.append("=== ZENNEST SYSTEM HEALTH REPORT ===\n");
        report.append(String.format("Date: %s\n\n", today));

        report.append("VISITS STATUS:\n");
        report.append(String.format("- Pending: %d\n", pendingVisits));
        report.append(String.format("- Approved: %d\n", approvedVisits));
        report.append(String.format("- Completed: %d\n\n", completedVisits));

        report.append("BOOKINGS STATUS:\n");
        report.append(String.format("- Pending: %d\n", pendingBookings));
        report.append(String.format("- Accepted: %d\n", acceptedBookings));
        report.append(String.format("- Activity (last 24h): %d\n\n", completedBookingsToday));

        report.append("AVAILABILITY SLOTS:\n");
        report.append(String.format("- Total Slots: %d\n", totalSlots));
        report.append(String.format("- Booked Slots: %d\n", bookedSlots));
        report.append(String.format("- Available Slots: %d\n\n", totalSlots - bookedSlots));

        report.append("System is operating normally.");

        String reportContent = report.toString();
        logger.info("Daily Health Report:\n{}", reportContent);

        // Send health report to admin
        sendAdminHealthReport(reportContent);
    }

    private void sendAdminCleanupNotification(String subject, String message) {
        try {
            notificationPublisher.sendAdminNotification(ADMIN_EMAIL, subject, message);
            logger.info("Sent admin notification: {} - {}", subject, message);
        } catch (Exception e) {
            logger.error("Failed to send admin cleanup notification: {}", e.getMessage());
        }
    }

    private void sendAdminHealthReport(String reportContent) {
        try {
            notificationPublisher.sendAdminHealthReport(ADMIN_EMAIL, reportContent);
            logger.info("Sent daily health report to admin: {}", ADMIN_EMAIL);
        } catch (Exception e) {
            logger.error("Failed to send admin health report: {}", e.getMessage());
        }
    }
}
