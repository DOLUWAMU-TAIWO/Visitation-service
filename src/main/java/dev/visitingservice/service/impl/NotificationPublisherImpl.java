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
        AvailabilitySlot slot = slotRepository.findById(visit.getSlotId()).orElse(null);
        if (slot != null) {
            ZonedDateTime start = TimeConverter.convertUtcToNigeria(slot.getStartTime());
            ZonedDateTime end = TimeConverter.convertUtcToNigeria(slot.getEndTime());
            timeWindow = start.format(formatter) + " – " + end.format(formatter);
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
}