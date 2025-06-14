package dev.visitingservice.service;

public interface EmailService {
    /**
     * Sends an email to the specified recipient email address with subject and content.
     */
    void sendEmail(String email, String subject, String content);
}
