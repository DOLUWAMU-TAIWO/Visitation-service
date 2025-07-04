package dev.visitingservice.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "shortlet_booking")
public class ShortletBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "landlord_id", nullable = false)
    private UUID landlordId;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status;

    @Column(name = "reminder_24h_sent")
    private boolean reminder24hSent = false;

    @Column(name = "reminder_1h_sent")
    private boolean reminder1hSent = false;

    public enum BookingStatus {
        PENDING, ACCEPTED, REJECTED, CANCELLED, RESCHEDULED
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public UUID getTenantId() {
        return tenantId;
    }
    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }
    public UUID getLandlordId() {
        return landlordId;
    }
    public void setLandlordId(UUID landlordId) {
        this.landlordId = landlordId;
    }
    public UUID getPropertyId() {
        return propertyId;
    }
    public void setPropertyId(UUID propertyId) {
        this.propertyId = propertyId;
    }
    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    public LocalDate getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    public BookingStatus getStatus() {
        return status;
    }
    public void setStatus(BookingStatus status) {
        this.status = status;
    }
    public boolean isReminder24hSent() {
        return reminder24hSent;
    }
    public void setReminder24hSent(boolean reminder24hSent) {
        this.reminder24hSent = reminder24hSent;
    }
    public boolean isReminder1hSent() {
        return reminder1hSent;
    }
    public void setReminder1hSent(boolean reminder1hSent) {
        this.reminder1hSent = reminder1hSent;
    }
}
