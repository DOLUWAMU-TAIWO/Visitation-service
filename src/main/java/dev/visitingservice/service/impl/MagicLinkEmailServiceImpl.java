package dev.visitingservice.service.impl;

import dev.visitingservice.client.UserGraphQLClient;
import dev.visitingservice.client.UserDTO;
import dev.visitingservice.model.Visit;
import dev.visitingservice.service.EmailService;
import dev.visitingservice.service.MagicLinkEmailService;
import dev.visitingservice.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class MagicLinkEmailServiceImpl implements MagicLinkEmailService {

    private static final Logger logger = Logger.getLogger(MagicLinkEmailServiceImpl.class.getName());

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserGraphQLClient userGraphQLClient;

    @Value("${magic.link.landlord.dashboard.url}")
    private String landlordDashboardUrl;

    @Value("${magic.link.tenant.dashboard.url}")
    private String tenantDashboardUrl;

    @Value("${magic.link.general.dashboard.url}")
    private String generalDashboardUrl;

    @Override
    public void sendVisitRequestToLandlord(Visit visit, UUID landlordId, String landlordEmail) {
        try {
            // Generate magic link token for landlord
            String magicToken = jwtUtils.generateMagicLinkToken(landlordId, landlordEmail, "LANDLORD");
            String magicLink = landlordDashboardUrl + "?token=" + magicToken;

            String subject = "New Visit Request - Immediate Action Required";
            String content = buildVisitRequestContent(visit, magicLink);

            emailService.sendEmail(landlordEmail, subject, content);
            logger.info("Sent visit request email with magic link to landlord: " + landlordEmail);

        } catch (Exception e) {
            logger.severe("Failed to send visit request email to landlord: " + landlordEmail + ". Error: " + e.getMessage());
        }
    }

    @Override
    public void sendVisitConfirmationToVisitor(Visit visit, UUID visitorId, String visitorEmail) {
        try {
            // Generate magic link token for visitor
            String magicToken = jwtUtils.generateMagicLinkToken(visitorId, visitorEmail, "TENANT");
            String magicLink = tenantDashboardUrl + "?token=" + magicToken;

            String subject = "Visit Confirmed - Access Your Dashboard";
            String content = buildVisitConfirmationContent(visit, magicLink);

            emailService.sendEmail(visitorEmail, subject, content);
            logger.info("Sent visit confirmation email with magic link to visitor: " + visitorEmail);

        } catch (Exception e) {
            logger.severe("Failed to send visit confirmation email to visitor: " + visitorEmail + ". Error: " + e.getMessage());
        }
    }

    @Override
    public void sendVisitReminderWithMagicLink(Visit visit, UUID userId, String email, String userRole) {
        try {
            String dashboardUrl = getDashboardUrlByRole(userRole);
            String magicToken = jwtUtils.generateMagicLinkToken(userId, email, userRole);
            String magicLink = dashboardUrl + "?token=" + magicToken;

            String subject = "Visit Reminder - Quick Access to Details";
            String content = buildVisitReminderContent(visit, magicLink, userRole);

            emailService.sendEmail(email, subject, content);
            logger.info("Sent visit reminder email with magic link to: " + email);

        } catch (Exception e) {
            logger.severe("Failed to send visit reminder email to: " + email + ". Error: " + e.getMessage());
        }
    }

    @Override
    public void sendVisitCompletionNotification(Visit visit, UUID userId, String email, String userRole) {
        try {
            String dashboardUrl = getDashboardUrlByRole(userRole);
            String magicToken = jwtUtils.generateMagicLinkToken(userId, email, userRole);
            String magicLink = dashboardUrl + "?token=" + magicToken;

            String subject = "Visit Completed - Share Your Feedback";
            String content = buildVisitCompletionContent(visit, magicLink, userRole);

            emailService.sendEmail(email, subject, content);
            logger.info("Sent visit completion email with magic link to: " + email);

        } catch (Exception e) {
            logger.severe("Failed to send visit completion email to: " + email + ". Error: " + e.getMessage());
        }
    }

    @Override
    public void sendEmailWithMagicLink(String email, String subject, String content, UUID userId, String userRole, String dashboardType) {
        try {
            String dashboardUrl = getDashboardUrlByType(dashboardType);
            String magicToken = jwtUtils.generateMagicLinkToken(userId, email, userRole);
            String magicLink = dashboardUrl + "?token=" + magicToken;

            // Replace placeholder in content with actual magic link
            String finalContent = content.replace("{MAGIC_LINK}", magicLink);

            emailService.sendEmail(email, subject, finalContent);
            logger.info("Sent custom email with magic link to: " + email);

        } catch (Exception e) {
            logger.severe("Failed to send custom email with magic link to: " + email + ". Error: " + e.getMessage());
        }
    }

    private String getDashboardUrlByRole(String userRole) {
        return switch (userRole.toLowerCase()) {
            case "landlord" -> landlordDashboardUrl;
            case "tenant", "visitor" -> tenantDashboardUrl;
            default -> generalDashboardUrl;
        };
    }

    private String getDashboardUrlByType(String dashboardType) {
        return switch (dashboardType.toLowerCase()) {
            case "landlord" -> landlordDashboardUrl;
            case "tenant", "visitor" -> tenantDashboardUrl;
            case "general" -> generalDashboardUrl;
            default -> generalDashboardUrl;
        };
    }

    private String buildVisitRequestContent(Visit visit, String magicLink) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");
        String scheduledTime = visit.getScheduledAt().format(formatter);

        return String.format("""
            <div style="margin-bottom: 20px;">
                <h2 style="color: #2b5adc; margin-bottom: 15px;">New Visit Request</h2>
                <p>You have a new property visit request that requires your immediate attention.</p>
            </div>
            
            <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;">
                <h3 style="color: #333; margin-top: 0;">Visit Details:</h3>
                <p><strong>Scheduled Time:</strong> %s</p>
                <p><strong>Duration:</strong> %d minutes</p>
                <p><strong>Property ID:</strong> %s</p>
                <p><strong>Status:</strong> Pending Your Approval</p>
                %s
            </div>
            
            <div style="text-align: center; margin: 30px 0;">
                <a href="%s" 
                   style="background-color: #2b5adc; color: white; padding: 12px 30px; text-decoration: none; 
                          border-radius: 5px; font-weight: bold; display: inline-block;">
                    🏠 Review Visit Request
                </a>
            </div>
            
            <p style="font-size: 14px; color: #666;">
                <strong>Quick Access:</strong> Click the button above to instantly access your landlord dashboard 
                and approve or reject this visit request. No login required!
            </p>
            """,
            scheduledTime,
            visit.getDurationMinutes(),
            visit.getPropertyId(),
            visit.getNotes() != null ? "<p><strong>Notes:</strong> " + visit.getNotes() + "</p>" : "",
            magicLink
        );
    }

    private String buildVisitConfirmationContent(Visit visit, String magicLink) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");
        String scheduledTime = visit.getScheduledAt().format(formatter);

        return String.format("""
            <div style="margin-bottom: 20px;">
                <h2 style="color: #28a745; margin-bottom: 15px;">✅ Visit Confirmed!</h2>
                <p>Great news! Your property visit has been approved and confirmed.</p>
            </div>
            
            <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;">
                <h3 style="color: #333; margin-top: 0;">Visit Details:</h3>
                <p><strong>Scheduled Time:</strong> %s</p>
                <p><strong>Duration:</strong> %d minutes</p>
                <p><strong>Property ID:</strong> %s</p>
                <p><strong>Status:</strong> Confirmed</p>
            </div>
            
            <div style="text-align: center; margin: 30px 0;">
                <a href="%s" 
                   style="background-color: #28a745; color: white; padding: 12px 30px; text-decoration: none; 
                          border-radius: 5px; font-weight: bold; display: inline-block;">
                    📱 View Visit Details
                </a>
            </div>
            
            <p style="font-size: 14px; color: #666;">
                Click above to access your dashboard and view all visit details, directions, and contact information.
            </p>
            """,
            scheduledTime,
            visit.getDurationMinutes(),
            visit.getPropertyId(),
            magicLink
        );
    }

    private String buildVisitReminderContent(Visit visit, String magicLink, String userRole) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");
        String scheduledTime = visit.getScheduledAt().format(formatter);
        boolean isLandlord = "LANDLORD".equalsIgnoreCase(userRole);

        return String.format("""
            <div style="margin-bottom: 20px;">
                <h2 style="color: #ffc107; margin-bottom: 15px;">⏰ Visit Reminder</h2>
                <p>%s</p>
            </div>
            
            <div style="background-color: #fff3cd; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #ffc107;">
                <h3 style="color: #333; margin-top: 0;">Upcoming Visit:</h3>
                <p><strong>Scheduled Time:</strong> %s</p>
                <p><strong>Duration:</strong> %d minutes</p>
                <p><strong>Property ID:</strong> %s</p>
            </div>
            
            <div style="text-align: center; margin: 30px 0;">
                <a href="%s" 
                   style="background-color: #ffc107; color: #333; padding: 12px 30px; text-decoration: none; 
                          border-radius: 5px; font-weight: bold; display: inline-block;">
                    🚀 Quick Access Dashboard
                </a>
            </div>
            
            <p style="font-size: 14px; color: #666;">
                %s
            </p>
            """,
            isLandlord ? "Your property has a scheduled visit coming up." : "Your visit is coming up soon!",
            scheduledTime,
            visit.getDurationMinutes(),
            visit.getPropertyId(),
            magicLink,
            isLandlord ? "Access your landlord dashboard instantly to view visit details and contact information." :
                        "Access your dashboard to view visit details, get directions, and contact the landlord if needed."
        );
    }

    private String buildVisitCompletionContent(Visit visit, String magicLink, String userRole) {
        boolean isLandlord = "LANDLORD".equalsIgnoreCase(userRole);

        return String.format("""
            <div style="margin-bottom: 20px;">
                <h2 style="color: #28a745; margin-bottom: 15px;">✅ Visit Completed</h2>
                <p>%s</p>
            </div>
            
            <div style="background-color: #d4edda; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #28a745;">
                <h3 style="color: #333; margin-top: 0;">Visit Summary:</h3>
                <p><strong>Property ID:</strong> %s</p>
                <p><strong>Status:</strong> Completed Successfully</p>
                <p><strong>Duration:</strong> %d minutes</p>
            </div>
            
            <div style="text-align: center; margin: 30px 0;">
                <a href="%s" 
                   style="background-color: #28a745; color: white; padding: 12px 30px; text-decoration: none; 
                          border-radius: 5px; font-weight: bold; display: inline-block;">
                    💬 %s
                </a>
            </div>
            
            <p style="font-size: 14px; color: #666;">
                %s
            </p>
            """,
            isLandlord ? "The property visit has been completed successfully." : "Thank you for visiting! We hope you found what you were looking for.",
            visit.getPropertyId(),
            visit.getDurationMinutes(),
            magicLink,
            isLandlord ? "View Visit Summary" : "Share Your Feedback",
            isLandlord ? "Access your dashboard to view the visit summary and any feedback from the visitor." :
                        "We'd love to hear about your experience! Click above to share your feedback and view more properties."
        );
    }
}
