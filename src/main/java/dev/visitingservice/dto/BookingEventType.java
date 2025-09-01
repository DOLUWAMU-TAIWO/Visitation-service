package dev.visitingservice.dto;

public enum BookingEventType {
    BOOKING_INITIATED("User clicks book button"),
    BOOKING_CREATED("Booking saved to database"),
    BOOKING_ACCEPTED("Landlord accepts booking"),
    BOOKING_REJECTED("Landlord rejects booking"),
    BOOKING_CANCELLED("Booking cancelled"),
    BOOKING_RESCHEDULED("Dates changed"),
    BOOKING_PAYMENT_UPDATED("Payment status changed");

    private final String description;

    BookingEventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
