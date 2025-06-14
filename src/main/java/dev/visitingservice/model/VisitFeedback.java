package dev.visitingservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class VisitFeedback {
    @Id
    @GeneratedValue
    private UUID id;

    private UUID visitId;
    private UUID visitorId;
    private int rating;
    private String comment;
    private boolean followUpNeeded;
    private String followUpReason;

    @Enumerated(EnumType.STRING)
    private FollowUpStatus followUpStatus;

    private UUID handlerId;
    private LocalDateTime createdAt;

    public enum FollowUpStatus { PENDING, IN_PROGRESS, RESOLVED }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and setters omitted for brevity
}
