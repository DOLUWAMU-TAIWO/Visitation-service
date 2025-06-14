package dev.visitingservice.service.impl;

import dev.visitingservice.exception.ExternalServiceException;
import dev.visitingservice.service.EmailService;
import dev.visitingservice.util.EmailTemplateBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private final RestTemplate restTemplate;
    private final String emailServiceUrl;
    private final String messagingApiKey;

    public EmailServiceImpl(RestTemplate restTemplate,
                            @Value("${notification.email.service.url}") String emailServiceUrl,
                            @Value("${notification.email.service.api-key}") String messagingApiKey) {
        this.restTemplate = restTemplate;
        this.emailServiceUrl = emailServiceUrl;
        this.messagingApiKey = messagingApiKey;
    }

    @Override
    public void sendEmail(String email, String subject, String content) {
        String htmlWrappedContent = EmailTemplateBuilder.wrap(content, subject);

        Map<String, Object> payload = new HashMap<>();
        payload.put("to", email);
        payload.put("subject", subject);
        payload.put("content", htmlWrappedContent);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(messagingApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            restTemplate.postForEntity(emailServiceUrl, request, Void.class);
        } catch (RestClientException e) {
            throw new ExternalServiceException("Failed to send email to " + email, e);
        }
    }
}