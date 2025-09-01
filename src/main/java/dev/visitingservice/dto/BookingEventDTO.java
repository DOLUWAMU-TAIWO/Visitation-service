package dev.visitingservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;
import java.util.UUID;

public class BookingEventDTO {
    private UUID eventId;
    private BookingEventType eventType;
    private UUID bookingId; // idempotency key

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime timestamp;

    private BookingEventPayload payload;

    // Source service information
    private String sourceService = "VisitingService";
    private String version = "1.0";

    // Constructors
    public BookingEventDTO() {
        this.eventId = UUID.randomUUID();
        this.timestamp = OffsetDateTime.now();
    }

    public BookingEventDTO(BookingEventType eventType, UUID bookingId, BookingEventPayload payload) {
        this();
        this.eventType = eventType;
        this.bookingId = bookingId;
        this.payload = payload;
    }

    // Getters and setters
    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public BookingEventType getEventType() {
        return eventType;
    }

    public void setEventType(BookingEventType eventType) {
        this.eventType = eventType;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public BookingEventPayload getPayload() {
        return payload;
    }

    public void setPayload(BookingEventPayload payload) {
        this.payload = payload;
    }

    public String getSourceService() {
        return sourceService;
    }

    public void setSourceService(String sourceService) {
        this.sourceService = sourceService;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "BookingEventDTO{" +
                "eventId=" + eventId +
                ", eventType=" + eventType +
                ", bookingId=" + bookingId +
                ", timestamp=" + timestamp +
                ", sourceService='" + sourceService + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
