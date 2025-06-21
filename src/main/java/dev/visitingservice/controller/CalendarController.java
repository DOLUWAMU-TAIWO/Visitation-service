package dev.visitingservice.controller;

import dev.visitingservice.dto.CalendarViewDTO;
import dev.visitingservice.model.AvailabilitySlot;
import dev.visitingservice.model.ShortletBooking;
import dev.visitingservice.repository.AvailabilitySlotRepository;
import dev.visitingservice.repository.ShortletBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.UUID;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final AvailabilitySlotRepository slotRepository;
    private final ShortletBookingRepository bookingRepository;

    @Autowired
    public CalendarController(AvailabilitySlotRepository slotRepository, ShortletBookingRepository bookingRepository) {
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/{propertyId}")
    public ResponseEntity<CalendarViewDTO> getCalendarView(
            @PathVariable UUID propertyId,
            @RequestParam OffsetDateTime from,
            @RequestParam OffsetDateTime to) {

        List<AvailabilitySlot> slots = slotRepository.findByPropertyIdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(
                propertyId, from, to);
        List<ShortletBooking> bookings = bookingRepository.findByPropertyIdAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                propertyId, from.toLocalDate(), to.toLocalDate());

        CalendarViewDTO dto = new CalendarViewDTO();
        dto.setSlots(slots.stream().map(slot -> {
            CalendarViewDTO.SlotInfo slotInfo = new CalendarViewDTO.SlotInfo();
            slotInfo.id = slot.getId();
            slotInfo.startTime = slot.getStartTime();
            slotInfo.endTime = slot.getEndTime();
            slotInfo.booked = slot.isBooked();
            return slotInfo;
        }).collect(Collectors.toList()));

        dto.setBookings(bookings.stream().map(booking -> {
            CalendarViewDTO.BookingInfo bookingInfo = new CalendarViewDTO.BookingInfo();
            bookingInfo.id = booking.getId();
            bookingInfo.startDate = booking.getStartDate().atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
            bookingInfo.endDate = booking.getEndDate().atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
            bookingInfo.status = booking.getStatus().name();
            return bookingInfo;
        }).collect(Collectors.toList()));

        return ResponseEntity.ok(dto);
    }
}
