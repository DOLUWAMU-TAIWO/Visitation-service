package dev.visitingservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class BookingEventPayload {
    private UUID tenantId;
    private UUID landlordId;
    private UUID propertyId;
    @JsonFormat(pattern = "yyyy-MM-dd") private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd") private LocalDate endDate;
    private String status;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Integer guestNumber;
    private String email;           // <- tenant email carried in the event
    private String paymentStatus;
    private String paymentReference;
    private Double paymentAmount;
    private String userAgent;
    private String sourceIP;
    private String deviceType;
    private String referralSource;
    private String sessionId;
    private String propertyTitle;
    private String propertyLocation;
    private String propertyType;
    private Integer totalNights;
    private Double totalAmount;     // <- amount carried in the event
    private String currency = "NGN";
    private String bookingChannel;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") private OffsetDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") private OffsetDateTime updatedAt;
    private String previousStatus;
    private LocalDate previousStartDate;
    private LocalDate previousEndDate;
    private String previousPaymentStatus;
    private Double previousAmount;
    private String changeReason;
    private String adminUserId;
    private String ipAddress;
    private String browserInfo;

    public static BookingEventPayload fromBookingDTO(ShortletBookingDTO booking) {
        BookingEventPayload payload = new BookingEventPayload();
        payload.tenantId = booking.getTenantId();
        payload.landlordId = booking.getLandlordId();
        payload.propertyId = booking.getPropertyId();
        payload.startDate = booking.getStartDate();
        payload.endDate = booking.getEndDate();
        payload.status = booking.getStatus();
        payload.firstName = booking.getFirstName();
        payload.lastName = booking.getLastName();
        payload.phoneNumber = booking.getPhoneNumber();
        payload.guestNumber = booking.getGuestNumber();

        // Existing payment fields from booking (if any)
        payload.paymentStatus = booking.getPaymentStatus() != null ? booking.getPaymentStatus().name() : null;
        payload.paymentReference = booking.getPaymentReference();
        payload.paymentAmount = booking.getPaymentAmount();

        // NEW: copy enriched fields so they go into Kafka
        payload.email = booking.getTenantEmail();                  // may be looked up before sending the event
        payload.totalAmount = booking.getTotalAmount() != null
                ? booking.getTotalAmount()
                : booking.getPaymentAmount();                      // fallback if you reuse paymentAmount
        payload.currency = booking.getCurrency() != null ? booking.getCurrency() : "NGN";

        payload.calculateTotalNights();
        return payload;
    }

    // NEW: Static factory method for booking initiated events
    public static BookingEventPayload forBookingInitiated(UUID tenantId, UUID landlordId, UUID propertyId, LocalDate startDate, LocalDate endDate) {
        BookingEventPayload payload = new BookingEventPayload();
        payload.tenantId = tenantId;
        payload.landlordId = landlordId;
        payload.propertyId = propertyId;
        payload.startDate = startDate;
        payload.endDate = endDate;
        payload.status = "INITIATED"; // Special status for pre-creation events
        payload.calculateTotalNights();
        return payload;
    }

    private void calculateTotalNights() {
        if (this.totalNights == null && startDate != null && endDate != null) {
            this.totalNights = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        }
    }

    // Getters and setters
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
        // Only auto-calculate if totalNights wasn't explicitly set
        calculateTotalNights();
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        // Only auto-calculate if totalNights wasn't explicitly set
        calculateTotalNights();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Integer getGuestNumber() {
        return guestNumber;
    }

    public void setGuestNumber(Integer guestNumber) {
        this.guestNumber = guestNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public Double getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(Double paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getSourceIP() {
        return sourceIP;
    }

    public void setSourceIP(String sourceIP) {
        this.sourceIP = sourceIP;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getReferralSource() {
        return referralSource;
    }

    public void setReferralSource(String referralSource) {
        this.referralSource = referralSource;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getPropertyTitle() {
        return propertyTitle;
    }

    public void setPropertyTitle(String propertyTitle) {
        this.propertyTitle = propertyTitle;
    }

    public String getPropertyLocation() {
        return propertyLocation;
    }

    public void setPropertyLocation(String propertyLocation) {
        this.propertyLocation = propertyLocation;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public Integer getTotalNights() {
        return totalNights;
    }

    public void setTotalNights(Integer totalNights) {
        // When explicitly setting totalNights, use the provided value
        this.totalNights = totalNights;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getBookingChannel() {
        return bookingChannel;
    }

    public void setBookingChannel(String bookingChannel) {
        this.bookingChannel = bookingChannel;
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

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public LocalDate getPreviousStartDate() {
        return previousStartDate;
    }

    public void setPreviousStartDate(LocalDate previousStartDate) {
        this.previousStartDate = previousStartDate;
    }

    public LocalDate getPreviousEndDate() {
        return previousEndDate;
    }

    public void setPreviousEndDate(LocalDate previousEndDate) {
        this.previousEndDate = previousEndDate;
    }

    public String getPreviousPaymentStatus() {
        return previousPaymentStatus;
    }

    public void setPreviousPaymentStatus(String previousPaymentStatus) {
        this.previousPaymentStatus = previousPaymentStatus;
    }

    public Double getPreviousAmount() {
        return previousAmount;
    }

    public void setPreviousAmount(Double previousAmount) {
        this.previousAmount = previousAmount;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    public String getAdminUserId() {
        return adminUserId;
    }

    public void setAdminUserId(String adminUserId) {
        this.adminUserId = adminUserId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getBrowserInfo() {
        return browserInfo;
    }

    public void setBrowserInfo(String browserInfo) {
        this.browserInfo = browserInfo;
    }

    @Override
    public String toString() {
        return "BookingEventPayload{" +
                "bookingId=" + tenantId + "-" + propertyId +
                ", status='" + status + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", totalNights=" + totalNights +
                '}';
    }
}
