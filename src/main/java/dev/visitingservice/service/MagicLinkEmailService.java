package dev.visitingservice.service;

import dev.visitingservice.model.Visit;
import java.util.UUID;

public interface MagicLinkEmailService {

    /**
     * Send visit request notification to landlord with magic link to dashboard
     */
    void sendVisitRequestToLandlord(Visit visit, UUID landlordId, String landlordEmail);

    /**
     * Send visit confirmation to visitor with magic link to their dashboard
     */
    void sendVisitConfirmationToVisitor(Visit visit, UUID visitorId, String visitorEmail);

    /**
     * Send visit reminder with magic link for quick access
     */
    void sendVisitReminderWithMagicLink(Visit visit, UUID userId, String email, String userRole);

    /**
     * Send visit completion notification with magic link to feedback/dashboard
     */
    void sendVisitCompletionNotification(Visit visit, UUID userId, String email, String userRole);

    /**
     * Generic method to send any email with magic link
     */
    void sendEmailWithMagicLink(String email, String subject, String content, UUID userId, String userRole, String dashboardType);
}
