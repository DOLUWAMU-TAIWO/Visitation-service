package dev.visitingservice.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
public class Visit {
    @Id
    @GeneratedValue
    private UUID id;

    private UUID propertyId;
    private UUID visitorId;
    private UUID landlordId;
    private OffsetDateTime scheduledAt;
    private int durationMinutes;

    @Enumerated(EnumType.STRING)
    private Status status;

    private UUID rescheduledFromId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String notes;
    private UUID slotId;
    private boolean feedbackEmailSent = false;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPropertyId() { return propertyId; }
    public void setPropertyId(UUID propertyId) { this.propertyId = propertyId; }
    public UUID getVisitorId() { return visitorId; }
    public void setVisitorId(UUID visitorId) { this.visitorId = visitorId; }
    public UUID getLandlordId() { return landlordId; }
    public void setLandlordId(UUID landlordId) { this.landlordId = landlordId; }
    public OffsetDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(OffsetDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public UUID getRescheduledFromId() { return rescheduledFromId; }
    public void setRescheduledFromId(UUID rescheduledFromId) { this.rescheduledFromId = rescheduledFromId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public UUID getSlotId() { return slotId; }
    public void setSlotId(UUID slotId) { this.slotId = slotId; }
    public boolean isFeedbackEmailSent() { return feedbackEmailSent; }
    public void setFeedbackEmailSent(boolean feedbackEmailSent) { this.feedbackEmailSent = feedbackEmailSent; }
}
