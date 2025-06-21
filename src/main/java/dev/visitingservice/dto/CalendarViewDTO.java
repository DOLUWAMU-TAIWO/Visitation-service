package dev.visitingservice.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class CalendarViewDTO {
    public static class SlotInfo {
        public UUID id;
        public OffsetDateTime startTime;
        public OffsetDateTime endTime;
        public boolean booked;
    }
    public static class BookingInfo {
        public UUID id;
        public OffsetDateTime startDate;
        public OffsetDateTime endDate;
        public String status;
    }
    private List<SlotInfo> slots;
    private List<BookingInfo> bookings;

    public List<SlotInfo> getSlots() { return slots; }
    public void setSlots(List<SlotInfo> slots) { this.slots = slots; }
    public List<BookingInfo> getBookings() { return bookings; }
    public void setBookings(List<BookingInfo> bookings) { this.bookings = bookings; }
}

