package dev.visitingservice.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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
    private Boolean reminder24hSent = false;

    @Column(name = "reminder_5h_sent")
    private Boolean reminder5hSent = false;

    @Column(name = "reminder_1h_sent")
    private Boolean reminder1hSent = false;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "guest_number")
    private Integer guestNumber;

    public enum BookingStatus {
        PENDING, ACCEPTED, REJECTED, CANCELLED, RESCHEDULED, NO_SHOW
    }

    public enum PaymentStatus {
        PENDING, PAID, FAILED
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "payment_amount")
    private java.math.BigDecimal paymentAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "tenant_email")
    private String tenantEmail;

    @Column(name = "total_amount")
    private java.math.BigDecimal totalAmount;

    @Column(name = "currency")
    private String currency;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now(java.time.ZoneOffset.UTC);
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now(java.time.ZoneOffset.UTC);
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
    public Boolean getReminder24hSent() {
        return reminder24hSent;
    }
    public void setReminder24hSent(Boolean reminder24hSent) {
        this.reminder24hSent = reminder24hSent;
    }
    public Boolean getReminder5hSent() {
        return reminder5hSent;
    }
    public void setReminder5hSent(Boolean reminder5hSent) {
        this.reminder5hSent = reminder5hSent;
    }
    public Boolean getReminder1hSent() {
        return reminder1hSent;
    }
    public void setReminder1hSent(Boolean reminder1hSent) {
        this.reminder1hSent = reminder1hSent;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public Integer getGuestNumber() {
        return guestNumber;
    }

    public void setGuestNumber(Integer guestNumber) {
        this.guestNumber = guestNumber;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    public String getPaymentReference() {
        return paymentReference;
    }
    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }
    public java.math.BigDecimal getPaymentAmount() {
        return paymentAmount;
    }
    public void setPaymentAmount(java.math.BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // NEW: getters and setters for payment service fields
    public String getTenantEmail() {
        return tenantEmail;
    }

    public void setTenantEmail(String tenantEmail) {
        this.tenantEmail = tenantEmail;
    }

    public java.math.BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(java.math.BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
