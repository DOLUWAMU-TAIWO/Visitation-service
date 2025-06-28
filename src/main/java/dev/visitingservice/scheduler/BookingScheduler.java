package dev.visitingservice.scheduler;

import dev.visitingservice.model.ShortletBooking;
import dev.visitingservice.model.ShortletBooking.BookingStatus;
import dev.visitingservice.repository.ShortletBookingRepository;
import dev.visitingservice.service.NotificationPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class BookingScheduler {
    private final ShortletBookingRepository bookingRepository;
    private final NotificationPublisher notificationPublisher;

    public BookingScheduler(ShortletBookingRepository bookingRepository, NotificationPublisher notificationPublisher) {
        this.bookingRepository = bookingRepository;
        this.notificationPublisher = notificationPublisher;
    }

    // Runs every 2 hours
    @Scheduled(cron = "0 0 */2 * * *")
    public void sendUpcomingBookingReminders() {
        ZoneId nigeriaZone = ZoneId.of("Africa/Lagos");
        ZonedDateTime now = ZonedDateTime.now(nigeriaZone);
        LocalDate today = now.toLocalDate();
        LocalDate in24h = today.plusDays(1);
        LocalDate in1h = today; // For demo, treat today as 'soon'

        sendReminderForWindow(in24h, 24);
        sendReminderForWindow(in1h, 1);
    }

    private void sendReminderForWindow(LocalDate date, int hoursBefore) {
        List<ShortletBooking> bookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.ACCEPTED && b.getStartDate().equals(date))
                .toList();
        for (ShortletBooking booking : bookings) {
            if (hoursBefore == 24 && !booking.isReminder24hSent()) {
                notificationPublisher.sendBookingReminder(booking, 24);
                booking.setReminder24hSent(true);
                bookingRepository.save(booking);
            } else if (hoursBefore == 1 && !booking.isReminder1hSent()) {
                notificationPublisher.sendBookingReminder(booking, 1);
                booking.setReminder1hSent(true);
                bookingRepository.save(booking);
            }
        }
    }

    // New method to send reminders for a custom time range
    public void sendRemindersForRange(UUID propertyId, UUID landlordId, OffsetDateTime startTime, OffsetDateTime endTime) {
        List<ShortletBooking> bookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.ACCEPTED)
                .filter(b -> b.getPropertyId().equals(propertyId))
                .filter(b -> b.getLandlordId().equals(landlordId))
                .filter(b -> !b.getStartDate().atStartOfDay().atOffset(startTime.getOffset()).isBefore(startTime)
                        && !b.getStartDate().atStartOfDay().atOffset(endTime.getOffset()).isAfter(endTime))
                .toList();
        for (ShortletBooking booking : bookings) {
            // You can calculate hoursBefore or set a default (e.g., 1)
            notificationPublisher.sendBookingReminder(booking, 1);
        }
    }

    // Test method: send a reminder at 13:00 (1pm) Lagos time
    @Scheduled(cron = "0 0 13 * * *")
    public void sendTestReminderAt1pmLagos() {
        ZoneId nigeriaZone = ZoneId.of("Africa/Lagos");
        ZonedDateTime now = ZonedDateTime.now(nigeriaZone);
        System.out.println("[TEST] sendTestReminderAt1pmLagos triggered at: " + now);
        // Optionally, send a test notification or log to verify time alignment
    }
}
