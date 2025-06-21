package dev.visitingservice.scheduler;

import dev.visitingservice.model.ShortletBooking;
import dev.visitingservice.model.ShortletBooking.BookingStatus;
import dev.visitingservice.repository.ShortletBookingRepository;
import dev.visitingservice.service.NotificationPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
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

    // Runs every minute for testing
    @Scheduled(cron = "0 * * * * *")
    public void sendUpcomingBookingReminders() {
        LocalDate now = LocalDate.now();
        LocalDate in24h = now.plusDays(1);
        LocalDate in1h = now.plusDays(0); // For demo, treat today as 'soon'

        sendReminderForWindow(in24h, 24);
        sendReminderForWindow(in1h, 1);
    }

    private void sendReminderForWindow(LocalDate date, int hoursBefore) {
        List<ShortletBooking> bookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.ACCEPTED && b.getStartDate().equals(date))
                .toList();
        for (ShortletBooking booking : bookings) {
            notificationPublisher.sendBookingReminder(booking, hoursBefore);
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
}
