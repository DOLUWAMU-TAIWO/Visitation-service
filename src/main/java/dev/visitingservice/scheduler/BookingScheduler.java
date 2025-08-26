package dev.visitingservice.scheduler;

import dev.visitingservice.model.ShortletBooking;
import dev.visitingservice.model.ShortletBooking.BookingStatus;
import dev.visitingservice.repository.ShortletBookingRepository;
import dev.visitingservice.service.NotificationPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Component
public class BookingScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BookingScheduler.class);
    private static final ZoneId NIGERIA_ZONE = ZoneId.of("Africa/Lagos");

    private final ShortletBookingRepository bookingRepository;
    private final NotificationPublisher notificationPublisher;

    public BookingScheduler(ShortletBookingRepository bookingRepository,
                           NotificationPublisher notificationPublisher) {
        this.bookingRepository = bookingRepository;
        this.notificationPublisher = notificationPublisher;
    }

    // Runs every hour for precise booking reminder timing
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void sendUpcomingBookingReminders() {
        ZonedDateTime nowNigeria = ZonedDateTime.now(NIGERIA_ZONE);

        // Send 24-hour reminders (exactly 24 hours before check-in)
        send24HourReminders(nowNigeria);

        // Send 5-hour reminders (exactly 5 hours before check-in)
        send5HourReminders(nowNigeria);

        // Send 1-hour reminders (exactly 1 hour before check-in)
        send1HourReminders(nowNigeria);

        logger.debug("Processed booking reminders at {} Nigeria time", nowNigeria);
    }

    private void send24HourReminders(ZonedDateTime now) {
        // Find bookings starting exactly 24 hours from now (tomorrow same time)
        LocalDate tomorrow = now.plusDays(1).toLocalDate();

        List<ShortletBooking> bookings = bookingRepository
            .findByStatusAndStartDate(BookingStatus.ACCEPTED, tomorrow);

        int sentCount = 0;
        for (ShortletBooking booking : bookings) {
            if (!booking.isReminder24hSent()) {
                try {
                    notificationPublisher.sendBookingReminder(booking, 24);
                    booking.setReminder24hSent(true);
                    bookingRepository.save(booking);
                    sentCount++;
                    logger.info("Sent 24-hour reminder for booking {}", booking.getId());
                } catch (Exception e) {
                    logger.error("Failed to send 24h reminder for booking {}: {}", booking.getId(), e.getMessage());
                }
            }
        }

        if (sentCount > 0) {
            logger.info("Sent {} 24-hour booking reminders for {}", sentCount, tomorrow);
        }
    }

    private void send5HourReminders(ZonedDateTime now) {
        // Find bookings starting in about 5 hours (today if after 7pm, tomorrow if before 7am)
        LocalDate targetDate;
        int currentHour = now.getHour();

        if (currentHour >= 19) { // After 7 PM, remind for tomorrow morning
            targetDate = now.plusDays(1).toLocalDate();
        } else { // Before 7 PM, remind for today
            targetDate = now.toLocalDate();
        }

        List<ShortletBooking> bookings = bookingRepository
            .findByStatusAndStartDate(BookingStatus.ACCEPTED, targetDate);

        int sentCount = 0;
        for (ShortletBooking booking : bookings) {
            // Now use the dedicated reminder5hSent field instead of hijacking reminder1hSent
            if (!booking.isReminder5hSent()) {
                try {
                    notificationPublisher.sendBookingReminder(booking, 5);
                    booking.setReminder5hSent(true); // Mark as 5h reminder sent
                    bookingRepository.save(booking);
                    sentCount++;
                    logger.info("Sent 5-hour reminder for booking {}", booking.getId());
                } catch (Exception e) {
                    logger.error("Failed to send 5h reminder for booking {}: {}", booking.getId(), e.getMessage());
                }
            }
        }

        if (sentCount > 0) {
            logger.info("Sent {} 5-hour booking reminders for {}", sentCount, targetDate);
        }
    }

    private void send1HourReminders(ZonedDateTime now) {
        // Find bookings starting today (1-hour reminders are for same-day check-ins)
        LocalDate today = now.toLocalDate();

        List<ShortletBooking> bookings = bookingRepository
            .findByStatusAndStartDate(BookingStatus.ACCEPTED, today);

        int sentCount = 0;
        for (ShortletBooking booking : bookings) {
            // Now properly check for 1h reminder - only send if 5h was already sent and 1h hasn't been sent
            if (booking.isReminder5hSent() && !booking.isReminder1hSent()) {
                // Check if current time is within 1-2 hours of check-in (assuming 12 PM check-in)
                int currentHour = now.getHour();
                if (currentHour >= 10 && currentHour <= 11) { // Between 10-11 AM for 12 PM check-in
                    try {
                        notificationPublisher.sendBookingReminder(booking, 1);
                        booking.setReminder1hSent(true); // Mark as 1h reminder sent
                        bookingRepository.save(booking);
                        sentCount++;
                        logger.info("Sent 1-hour reminder for booking {}", booking.getId());
                    } catch (Exception e) {
                        logger.error("Failed to send 1h reminder for booking {}: {}", booking.getId(), e.getMessage());
                    }
                }
            }
        }

        if (sentCount > 0) {
            logger.info("Sent {} 1-hour booking reminders for {}", sentCount, today);
        }
    }
}
