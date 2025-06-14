package dev.visitingservice.dto;

import dev.visitingservice.model.Visit;
import dev.visitingservice.model.Status;
import dev.visitingservice.util.TimeConverter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

public class VisitResponseDto {
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
    private UUID slotId;

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
    public UUID getSlotId() { return slotId; }
    public void setSlotId(UUID slotId) { this.slotId = slotId; }

    public static VisitResponseDto fromEntity(Visit v) {
        VisitResponseDto dto = new VisitResponseDto();
        dto.setId(v.getId());
        dto.setPropertyId(v.getPropertyId());
        dto.setVisitorId(v.getVisitorId());
        dto.setLandlordId(v.getLandlordId());
        dto.setDurationMinutes(v.getDurationMinutes());
        dto.setStatus(v.getStatus());
        dto.setRescheduledFromId(v.getRescheduledFromId());
        dto.setNotes(v.getNotes());
        dto.setScheduledAt(TimeConverter.convertUtcToNigeria(
            v.getScheduledAt().withOffsetSameInstant(ZoneOffset.UTC)
        ));
        dto.setCreatedAt(TimeConverter.convertUtcToNigeria(
            v.getCreatedAt().withOffsetSameInstant(ZoneOffset.UTC)
        ));
        dto.setUpdatedAt(TimeConverter.convertUtcToNigeria(
            v.getUpdatedAt().withOffsetSameInstant(ZoneOffset.UTC)
        ));
        dto.setSlotId(v.getSlotId());
        return dto;
    }
}
