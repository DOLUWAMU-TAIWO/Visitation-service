package dev.visitingservice.service.impl;

import dev.visitingservice.client.ListingRestClient;
import dev.visitingservice.client.UserGraphQLClient;
import dev.visitingservice.client.ListingDto;
import dev.visitingservice.model.Visit;
import dev.visitingservice.service.NotificationPublisher;
import dev.visitingservice.service.EmailService;
import dev.visitingservice.util.TimeConverter;
import org.springframework.stereotype.Component;
import dev.visitingservice.repository.AvailabilitySlotRepository;
import dev.visitingservice.model.AvailabilitySlot;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class NotificationPublisherImpl implements NotificationPublisher {

    private final EmailService emailService;
    private final UserGraphQLClient userClient;
    private final ListingRestClient listingClient;
    private final AvailabilitySlotRepository slotRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy 'at' h:mm a");
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NotificationPublisher.class);
    public NotificationPublisherImpl(EmailService emailService,
                                     UserGraphQLClient userClient,
                                     ListingRestClient listingClient,
                                     AvailabilitySlotRepository slotRepository) {
        this.emailService = emailService;
        this.userClient = userClient;
        this.listingClient = listingClient;
        this.slotRepository = slotRepository;
    }

    private void sendEvent(String type, Visit visit, Map<String, Object> extra) {
        String subject = getSubject(type);

        UUID[] recipients = switch (type) {
            case "VISIT_REQUESTED" -> new UUID[]{visit.getVisitorId(), visit.getLandlordId()};
            case "VISIT_APPROVED", "VISIT_REJECTED", "VISIT_CANCELLED", "VISIT_REMINDER", "FEEDBACK_PROMPT" ->
                    new UUID[]{visit.getVisitorId(), visit.getLandlordId()};
            default -> new UUID[]{visit.getVisitorId()};
        };

        for (UUID userId : recipients) {
            System.out.println("📧 Resolving email for userId: " + userId);
            String recipientEmail = userClient.getUserEmail(userId);
            if (recipientEmail != null) {
                String content = getContent(type, visit, extra, userId); // ✨ Pass userId here
                emailService.sendEmail(recipientEmail, subject, content);
                System.out.println("✅ Email sent to " + recipientEmail + " for event type: " + type);
            } else {
                System.err.println("⚠️ Unable to resolve email for user " + userId + " — skipping notification.");
            }
        }
    }

    private String getSubject(String type) {
        return switch (type) {
            case "VISIT_REQUESTED" -> "🔔 ZenNest: Visit Request Received";
            case "VISIT_APPROVED" -> "✅ ZenNest: Your Visit Has Been Approved!";
            case "VISIT_REJECTED" -> "❌ ZenNest: Visit Request Not Approved";
            case "VISIT_CANCELLED" -> "⚠️ ZenNest: Your Visit Has Been Cancelled";
            case "VISIT_REMINDER" -> "⏰ ZenNest: Upcoming Visit Reminder";
            case "FEEDBACK_PROMPT" -> "📝 ZenNest: How Was Your Visit?";
            default -> "📬 ZenNest Notification";
        };
    }

    private String getContent(String type, Visit visit, Map<String, Object> extra, UUID recipientId) {
        boolean isVisitor = recipientId.equals(visit.getVisitorId());
        ListingDto listing = listingClient.getListing(visit.getPropertyId());

        String propertyTitle = (listing != null && listing.getTitle() != null)
                ? listing.getTitle()
                : "your selected property";

        String visitorName = getVisitorName(visit.getVisitorId()); // for landlord view

        // Determine time window
        String timeWindow;
        if (visit.getSlotId() != null) {
            AvailabilitySlot slot = slotRepository.findById(visit.getSlotId()).orElse(null);
            if (slot != null) {
                ZonedDateTime start = TimeConverter.convertUtcToNigeria(slot.getStartTime());
                ZonedDateTime end = TimeConverter.convertUtcToNigeria(slot.getEndTime());
                timeWindow = start.format(formatter) + " – " + end.format(formatter);
            } else {
                ZonedDateTime single = TimeConverter.convertUtcToNigeria(visit.getScheduledAt());
                timeWindow = single.format(formatter);
            }
        } else {
            ZonedDateTime single = TimeConverter.convertUtcToNigeria(visit.getScheduledAt());
            timeWindow = single.format(formatter);
        }

        return switch (type) {

            case "VISIT_REQUESTED" -> isVisitor
                    ? String.format("""
                <h2 style="color:#2b5adc;">ZenNest Booking Request Received ✅</h2>
                <p>Hi there,</p>
                <p>We've received your request to visit <strong>%s</strong>.</p>
                <p><strong>Date & Time:</strong> %s</p>
                <p>We'll notify you once the landlord responds. Thanks for using ZenNest!</p>
                """, propertyTitle, timeWindow)
                    : String.format("""
                <h2 style="color:#2b5adc;">New Visit Request 📩</h2>
                <p>Hello,</p>
                <p><strong>%s</strong> has requested a visit to your property <strong>%s</strong>.</p>
                <p><strong>Requested time:</strong> %s</p>
                <p>Log in to your dashboard to approve or reject this request.</p>
                """, visitorName, propertyTitle, timeWindow);

            case "VISIT_APPROVED" -> isVisitor
                    ? String.format("""
                <h2 style="color:#2b5adc;">Your Visit is Confirmed 🎉</h2>
                <p>Hi there,</p>
                <p>Your visit to <strong>%s</strong> is confirmed for:</p>
                <p><strong>%s</strong></p>
                <p>We hope you enjoy your visit. Safe travels!</p>
                """, propertyTitle, timeWindow)
                    : String.format("""
                <h2 style="color:#2b5adc;">You Approved a Visit ✅</h2>
                <p>Hello,</p>
                <p>You successfully approved a visit by <strong>%s</strong> to your property <strong>%s</strong>.</p>
                <p><strong>Date:</strong> %s</p>
                <p>The visitor has been notified automatically.</p>
                """, visitorName, propertyTitle, timeWindow);

            case "VISIT_REJECTED" -> isVisitor
                    ? String.format("""
                <h2 style="color:#2b5adc;">Visit Request Declined ❌</h2>
                <p>Hello,</p>
                <p>Your request to visit <strong>%s</strong> on <strong>%s</strong> was declined by the landlord.</p>
                <p>You're free to book another available slot or browse more listings on ZenNest.</p>
                """, propertyTitle, timeWindow)
                    : String.format("""
                <h2 style="color:#2b5adc;">Visit Rejected</h2>
                <p>You’ve declined the visit request from <strong>%s</strong>.</p>
                <p><strong>Scheduled time:</strong> %s</p>
                <p>The visitor has been informed accordingly.</p>
                """, visitorName, timeWindow);

            case "VISIT_CANCELLED" -> String.format("""
    <h2 style="color:#2b5adc;">Visit Cancelled 🔔</h2>
    <p>Hello,</p>
    <p>The scheduled visit to <strong>%s</strong> on <strong>%s</strong> has been cancelled.</p>
    <p>If this was a mistake, you can reschedule via your dashboard.</p>
    """, propertyTitle, timeWindow);

            case "VISIT_REMINDER" -> {
                Object hrs = extra != null ? extra.getOrDefault("hoursBefore", "a few") : "a few";
                yield String.format("""
                <h2 style="color:#2b5adc;">Upcoming Visit Reminder ⏰</h2>
                <p>Hello,</p>
                <p>This is a friendly reminder that you have a visit scheduled for <strong>%s</strong> in <strong>%s</strong> hours.</p>
                <p><strong>Time:</strong> %s</p>
                <p>Be prepared and enjoy your ZenNest experience!</p>
                """, propertyTitle, hrs, timeWindow);
            }

            case "FEEDBACK_PROMPT" -> String.format("""
            <h2 style="color:#2b5adc;">How Was Your Visit? 💬</h2>
            <p>Hi,</p>
            <p>We’d love to hear how your visit to <strong>%s</strong> went.</p>
            <p>Your feedback helps us improve and support more renters like you.</p>
            <p><a href="https://zennest.africa/feedback" style="color:#2b5adc;">Leave Feedback</a></p>
            """, propertyTitle);

            default -> "";
        };
    }

    private String getVisitorName(UUID visitorId) {
        try {
            var user = userClient.getUser(visitorId);
            if (user != null) {
                String first = user.getFirstName();
                String last = user.getLastName();
                if (first != null && last != null) {
                    return first + " " + last;
                } else if (first != null) {
                    return first;
                }
            }
        } catch (Exception e) {
            logger.warn("Could not resolve visitor name for ID {}: {}", visitorId, e.getMessage());
        }
        return "Unknown Visitor";
    }
    @Override
    public void sendVisitRequested(Visit visit) {
        sendEvent("VISIT_REQUESTED", visit, null);
    }

    @Override
    public void sendVisitApproved(Visit visit) {
        sendEvent("VISIT_APPROVED", visit, null);
    }

    @Override
    public void sendVisitRejected(Visit visit) {
        sendEvent("VISIT_REJECTED", visit, null);
    }

    @Override
    public void sendVisitCancelled(Visit visit) {
        sendEvent("VISIT_CANCELLED", visit, null);
    }

    @Override
    public void sendReminder(Visit visit, int hoursBefore) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("hoursBefore", hoursBefore);
        sendEvent("VISIT_REMINDER", visit, extra);
    }

    @Override
    public void sendFeedbackPrompt(Visit visit) {
        sendEvent("FEEDBACK_PROMPT", visit, null);
    }

    // --- Shortlet Booking Notification Methods ---
    @Override
    public void sendBookingCreated(dev.visitingservice.model.ShortletBooking booking) {
        sendBookingEvent("BOOKING_CREATED", booking);
    }

    @Override
    public void sendBookingAccepted(dev.visitingservice.model.ShortletBooking booking) {
        sendBookingEvent("BOOKING_ACCEPTED", booking);
    }

    @Override
    public void sendBookingRejected(dev.visitingservice.model.ShortletBooking booking) {
        sendBookingEvent("BOOKING_REJECTED", booking);
    }

    @Override
    public void sendBookingCancelled(dev.visitingservice.model.ShortletBooking booking) {
        sendBookingEvent("BOOKING_CANCELLED", booking);
    }

    @Override
    public void sendBookingRescheduled(dev.visitingservice.model.ShortletBooking booking) {
        sendBookingEvent("BOOKING_RESCHEDULED", booking);
    }

    private void sendBookingEvent(String type, dev.visitingservice.model.ShortletBooking booking) {
        String subject = getBookingSubject(type);
        UUID[] recipients = getBookingRecipients(booking);
        for (UUID userId : recipients) {
            String recipientEmail = userClient.getUserEmail(userId);
            if (recipientEmail != null) {
                String content = getBookingContent(type, booking, userId);
                emailService.sendEmail(recipientEmail, subject, content);
                logger.info("Booking email sent to {} for event type: {}", recipientEmail, type);
            } else {
                logger.warn("Unable to resolve email for user {} — skipping booking notification.", userId);
            }
        }
    }

    private String getBookingSubject(String type) {
        return switch (type) {
            case "BOOKING_CREATED" -> "ZenNest: New Booking Request";
            case "BOOKING_ACCEPTED" -> "ZenNest: Your Booking is Confirmed!";
            case "BOOKING_REJECTED" -> "ZenNest: Booking Request Rejected";
            case "BOOKING_CANCELLED" -> "ZenNest: Booking Cancelled";
            case "BOOKING_RESCHEDULED" -> "ZenNest: Booking Dates Changed";
            default -> "ZenNest Booking Notification";
        };
    }

    private UUID[] getBookingRecipients(dev.visitingservice.model.ShortletBooking booking) {
        return new UUID[]{booking.getTenantId(), booking.getLandlordId()};
    }

    private String getBookingContent(String type, dev.visitingservice.model.ShortletBooking booking, UUID recipientId) {
        boolean isTenant = recipientId.equals(booking.getTenantId());
        String propertyTitle = "your property";
        try {
            dev.visitingservice.client.ListingDto listing = listingClient.getListing(booking.getPropertyId());
            if (listing != null && listing.getTitle() != null) {
                propertyTitle = listing.getTitle();
            }
        } catch (Exception ignored) {}
        String start = booking.getStartDate().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"));
        String end = booking.getEndDate().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"));
        int nights = (int) (java.time.temporal.ChronoUnit.DAYS.between(booking.getStartDate(), booking.getEndDate()));
        String tenantName = "your guest";
        String landlordName = "the landlord";
        try {
            var tenant = userClient.getUser(booking.getTenantId());
            if (tenant != null && tenant.getFirstName() != null) {
                tenantName = tenant.getFirstName();
                if (tenant.getLastName() != null) tenantName += " " + tenant.getLastName();
            }
            var landlord = userClient.getUser(booking.getLandlordId());
            if (landlord != null && landlord.getFirstName() != null) {
                landlordName = landlord.getFirstName();
                if (landlord.getLastName() != null) landlordName += " " + landlord.getLastName();
            }
        } catch (Exception ignored) {}
        return switch (type) {
            case "BOOKING_CREATED" -> isTenant
                ? String.format("""
                    <h2>Booking Request Submitted</h2>
                    <p>Hi %s,</p>
                    <p>Your booking request for <b>%s</b> from <b>%s</b> to <b>%s</b> (%d night%s) has been sent to %s. We will notify you once the landlord responds.</p>
                    <p>Thank you for choosing ZenNest!</p>
                    """, tenantName, propertyTitle, start, end, nights, nights == 1 ? "" : "s", landlordName)
                : String.format("""
                    <h2>New Booking Request</h2>
                    <p>Hello %s,</p>
                    <p>You have received a new booking request for <b>%s</b> from <b>%s</b> to <b>%s</b> (%d night%s) from %s.</p>
                    <p>Please review and respond at your earliest convenience.</p>
                    """, landlordName, propertyTitle, start, end, nights, nights == 1 ? "" : "s", tenantName);
            case "BOOKING_ACCEPTED" -> isTenant
                ? String.format("""
                    <h2>Your Booking is Confirmed!</h2>
                    <p>Hi %s,</p>
                    <p>Great news! Your booking for <b>%s</b> from <b>%s</b> to <b>%s</b> (%d night%s) has been accepted by %s.</p>
                    <p>Please set a reminder for your upcoming stay. We look forward to hosting you!</p>
                    """, tenantName, propertyTitle, start, end, nights, nights == 1 ? "" : "s", landlordName)
                : String.format("""
                    <h2>Booking Accepted</h2>
                    <p>Hello %s,</p>
                    <p>You have accepted a booking for <b>%s</b> from <b>%s</b> to <b>%s</b> (%d night%s) for %s.</p>
                    <p>The guest has been notified and is looking forward to their stay.</p>
                    """, landlordName, propertyTitle, start, end, nights, nights == 1 ? "" : "s", tenantName);
            case "BOOKING_REJECTED" -> isTenant
                ? String.format("""
                    <h2>Booking Request Not Accepted</h2>
                    <p>Hi %s,</p>
                    <p>Unfortunately, your booking request for <b>%s</b> from <b>%s</b> to <b>%s</b> was not accepted by %s.</p>
                    <p>You can explore other available dates or properties on ZenNest.</p>
                    """, tenantName, propertyTitle, start, end, landlordName)
                : String.format("""
                    <h2>Booking Rejected</h2>
                    <p>Hello %s,</p>
                    <p>You have rejected a booking request for <b>%s</b> from <b>%s</b> to <b>%s</b> from %s.</p>
                    <p>The guest has been notified.</p>
                    """, landlordName, propertyTitle, start, end, tenantName);
            case "BOOKING_CANCELLED" -> isTenant
                ? String.format("""
                    <h2>Booking Cancelled</h2>
                    <p>Hi %s,</p>
                    <p>Your booking for <b>%s</b> from <b>%s</b> to <b>%s</b> has been cancelled. If you have any questions, please contact us or the landlord directly.</p>
                    <p>We hope to host you in the future!</p>
                    """, tenantName, propertyTitle, start, end)
                : String.format("""
                    <h2>Booking Cancelled</h2>
                    <p>Hello %s,</p>
                    <p>You have cancelled a booking for <b>%s</b> from <b>%s</b> to <b>%s</b> for %s.</p>
                    <p>The guest has been notified.</p>
                    """, landlordName, propertyTitle, start, end, tenantName);
            case "BOOKING_RESCHEDULED" -> isTenant
                ? String.format("""
                    <h2>Your Booking Has Been Rescheduled</h2>
                    <p>Hi %s,</p>
                    <p>Your booking for <b>%s</b> has been rescheduled. Please find the new dates below:</p>
                    <p><b>New Dates:</b> %s to %s</p>
                    <p>If you have any questions, feel free to contact us.</p>
                    """, tenantName, propertyTitle, start, end)
                : String.format("""
                    <h2>Booking Rescheduled</h2>
                    <p>Hello %s,</p>
                    <p>The booking for <b>%s</b> has been rescheduled. Please note the new dates:</p>
                    <p><b>New Dates:</b> %s to %s</p>
                    <p>If you have any questions, feel free to contact us.</p>
                    """, landlordName, propertyTitle, start, end);
            default -> "ZenNest Booking Notification";
        };
    }

    @Override
    public void sendBookingReminder(dev.visitingservice.model.ShortletBooking booking, int hoursBefore) {
        String subject = "ZenNest: Upcoming Stay Reminder";
        UUID[] recipients = getBookingRecipients(booking);
        for (UUID userId : recipients) {
            String recipientEmail = userClient.getUserEmail(userId);
            if (recipientEmail != null) {
                String content = getBookingReminderContent(booking, userId, hoursBefore);
                emailService.sendEmail(recipientEmail, subject, content);
                logger.info("Booking reminder email sent to {} for booking {} ({} hours before)", recipientEmail, booking.getId(), hoursBefore);
            } else {
                logger.warn("Unable to resolve email for user {} — skipping booking reminder.", userId);
            }
        }
    }

    private String getBookingReminderContent(dev.visitingservice.model.ShortletBooking booking, UUID recipientId, int hoursBefore) {
        boolean isTenant = recipientId.equals(booking.getTenantId());
        String propertyTitle = "your property";
        try {
            dev.visitingservice.client.ListingDto listing = listingClient.getListing(booking.getPropertyId());
            if (listing != null && listing.getTitle() != null) {
                propertyTitle = listing.getTitle();
            }
        } catch (Exception ignored) {}
        String start = booking.getStartDate().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"));
        String tenantName = "your guest";
        String landlordName = "the landlord";
        try {
            var tenant = userClient.getUser(booking.getTenantId());
            if (tenant != null && tenant.getFirstName() != null) {
                tenantName = tenant.getFirstName();
                if (tenant.getLastName() != null) tenantName += " " + tenant.getLastName();
            }
            var landlord = userClient.getUser(booking.getLandlordId());
            if (landlord != null && landlord.getFirstName() != null) {
                landlordName = landlord.getFirstName();
                if (landlord.getLastName() != null) landlordName += " " + landlord.getLastName();
            }
        } catch (Exception ignored) {}
        return isTenant
            ? String.format("""
                <h2>Upcoming Stay Reminder</h2>
                <p>Hi %s,</p>
                <p>This is a friendly reminder that your stay at <b>%s</b> begins on <b>%s</b> (in about %d hour%s).</p>
                <p>We look forward to welcoming you!</p>
                """, tenantName, propertyTitle, start, hoursBefore, hoursBefore == 1 ? "" : "s")
            : String.format("""
                <h2>Upcoming Guest Arrival</h2>
                <p>Hello %s,</p>
                <p>Your guest %s is scheduled to arrive at <b>%s</b> on <b>%s</b> (in about %d hour%s).</p>
                <p>Please ensure the property is ready for their stay.</p>
                """, landlordName, tenantName, propertyTitle, start, hoursBefore, hoursBefore == 1 ? "" : "s");
    }
}
