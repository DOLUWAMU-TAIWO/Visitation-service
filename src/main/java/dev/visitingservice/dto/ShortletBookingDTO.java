package dev.visitingservice.dto;

import java.time.LocalDate;
import java.util.UUID;

public class ShortletBookingDTO {
    private UUID id;
    private UUID tenantId;
    private UUID landlordId;
    private UUID propertyId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Integer guestNumber;
    private PaymentStatus paymentStatus;
    private String paymentReference;
    private Double paymentAmount;

    // NEW: fields the payment service needs
    private String tenantEmail;       // source of truth for payer email
    private Double totalAmount;       // amount to charge (naira) — if you use kobo downstream, convert there
    private String currency = "NGN";  // default

    public enum PaymentStatus { PENDING, PAID, FAILED }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public UUID getLandlordId() { return landlordId; }
    public void setLandlordId(UUID landlordId) { this.landlordId = landlordId; }
    public UUID getPropertyId() { return propertyId; }
    public void setPropertyId(UUID propertyId) { this.propertyId = propertyId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public Integer getGuestNumber() { return guestNumber; }
    public void setGuestNumber(Integer guestNumber) { this.guestNumber = guestNumber; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    public Double getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(Double paymentAmount) { this.paymentAmount = paymentAmount; }

    // NEW getters/setters
    public String getTenantEmail() { return tenantEmail; }
    public void setTenantEmail(String tenantEmail) { this.tenantEmail = tenantEmail; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}