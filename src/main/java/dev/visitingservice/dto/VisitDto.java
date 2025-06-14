package dev.visitingservice.dto;

import dev.visitingservice.model.Status;
import java.time.ZonedDateTime;
import java.util.UUID;

public class VisitDto {
    private UUID id;
    private UUID propertyId;
    private UUID visitorId;
    private UUID landlordId;
    private ZonedDateTime scheduledAt;
    private int durationMinutes;
    private Status status;
    private UUID rescheduledFromId;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private String notes;

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPropertyId() { return propertyId; }
    public void setPropertyId(UUID propertyId) { this.propertyId = propertyId; }
    public UUID getVisitorId() { return visitorId; }
    public void setVisitorId(UUID visitorId) { this.visitorId = visitorId; }
    public UUID getLandlordId() { return landlordId; }
    public void setLandlordId(UUID landlordId) { this.landlordId = landlordId; }
    public ZonedDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(ZonedDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public UUID getRescheduledFromId() { return rescheduledFromId; }
    public void setRescheduledFromId(UUID rescheduledFromId) { this.rescheduledFromId = rescheduledFromId; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public static VisitDto fromEntity(dev.visitingservice.model.Visit v) {
        VisitDto dto = new VisitDto();
        dto.setId(v.getId());
        dto.setPropertyId(v.getPropertyId());
        dto.setVisitorId(v.getVisitorId());
        dto.setLandlordId(v.getLandlordId());
        dto.setDurationMinutes(v.getDurationMinutes());
        dto.setStatus(v.getStatus());
        dto.setRescheduledFromId(v.getRescheduledFromId());
        dto.setNotes(v.getNotes());
        // convert times
        dto.setScheduledAt(
            dev.visitingservice.util.TimeConverter.convertUtcToNigeria(v.getScheduledAt())
        );
        dto.setCreatedAt(
            dev.visitingservice.util.TimeConverter.convertUtcToNigeria(v.getCreatedAt())
        );
        dto.setUpdatedAt(
            dev.visitingservice.util.TimeConverter.convertUtcToNigeria(v.getUpdatedAt())
        );
        return dto;
    }

    public dev.visitingservice.model.Visit toEntity() {
        dev.visitingservice.model.Visit v = new dev.visitingservice.model.Visit();
        v.setPropertyId(this.propertyId);
        v.setVisitorId(this.visitorId);
        v.setLandlordId(this.landlordId);
        v.setDurationMinutes(this.durationMinutes);
        v.setRescheduledFromId(this.rescheduledFromId);
        v.setNotes(this.notes);
        // convert scheduledAt back to UTC OffsetDateTime
        v.setScheduledAt(this.scheduledAt.withZoneSameInstant(java.time.ZoneOffset.UTC).toOffsetDateTime());
        return v;
    }
}
