package dev.visitingservice.dto;

import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import dev.visitingservice.model.Visit;

public class VisitCreateRequest {
    private UUID propertyId;
    private UUID landlordId;
    private UUID visitorId;
    private ZonedDateTime scheduledAt;
    private String notes;
    private UUID slotId;

    public UUID getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(UUID propertyId) {
        this.propertyId = propertyId;
    }

    public UUID getLandlordId() {
        return landlordId;
    }

    public void setLandlordId(UUID landlordId) {
        this.landlordId = landlordId;
    }

    public UUID getVisitorId() {
        return visitorId;
    }

    public void setVisitorId(UUID visitorId) {
        this.visitorId = visitorId;
    }

    public ZonedDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(ZonedDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public UUID getSlotId() {
        return slotId;
    }

    public void setSlotId(UUID slotId) {
        this.slotId = slotId;
    }

    public Visit toEntity() {
        Visit v = new Visit();
        v.setPropertyId(this.propertyId);
        v.setLandlordId(this.landlordId);
        v.setVisitorId(this.visitorId);
        v.setSlotId(this.slotId);
        // scheduledAt and duration set based on slot in service layer
        v.setDurationMinutes(60); // default 60-minute
        v.setNotes(this.notes);
        return v;
    }
}