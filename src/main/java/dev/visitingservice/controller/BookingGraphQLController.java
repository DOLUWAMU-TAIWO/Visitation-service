package dev.visitingservice.controller;

import dev.visitingservice.dto.BookingFilterDTO;
import dev.visitingservice.dto.CalendarViewDTO;
import dev.visitingservice.dto.ShortletBookingDTO;
import dev.visitingservice.model.ShortletBooking;
import dev.visitingservice.model.ShortletBooking.BookingStatus;
import dev.visitingservice.repository.AvailabilitySlotRepository;
import dev.visitingservice.repository.ShortletBookingRepository;
import dev.visitingservice.service.ShortletBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class BookingGraphQLController {

    private final ShortletBookingService bookingService;
    private final ShortletBookingRepository bookingRepository;
    private final AvailabilitySlotRepository slotRepository;

    @Autowired
    public BookingGraphQLController(
            ShortletBookingService bookingService,
            ShortletBookingRepository bookingRepository,
            AvailabilitySlotRepository slotRepository) {
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
    }

    @QueryMapping
    public ShortletBookingDTO booking(@Argument String id) {
        UUID bookingId = UUID.fromString(id);
        ShortletBooking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking != null) {
            return toDTO(booking);
        }
        return null;
    }

    private ShortletBookingDTO toDTO(ShortletBooking booking) {
        ShortletBookingDTO dto = new ShortletBookingDTO();
        dto.setId(booking.getId());
        dto.setTenantId(booking.getTenantId());
        dto.setLandlordId(booking.getLandlordId());
        dto.setPropertyId(booking.getPropertyId());
        dto.setStartDate(booking.getStartDate());
        dto.setEndDate(booking.getEndDate());
        dto.setStatus(booking.getStatus().name());
        return dto;
    }

    @QueryMapping
    public List<ShortletBookingDTO> bookings(@Argument BookingFilterDTO filter) {
        // Implement filtering logic similar to your BookingHistoryController
        if (filter.getLandlordId() != null) {
            return bookingService.getBookings(filter.getLandlordId(),
                    filter.getPage() != null ? filter.getPage() : 0,
                    filter.getSize() != null ? filter.getSize() : 20);
        }
        // Add more filtering options here based on your existing repository methods
        return List.of();
    }

    @QueryMapping
    public CalendarViewDTO calendar(
            @Argument String propertyId,
            @Argument String from,
            @Argument String to) {

        UUID propertyUuid = UUID.fromString(propertyId);
        OffsetDateTime fromDate = OffsetDateTime.parse(from);
        OffsetDateTime toDate = OffsetDateTime.parse(to);

        // This replicates the logic in your CalendarController
        var slots = slotRepository.findByPropertyIdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(
                propertyUuid, fromDate, toDate);
        var bookings = bookingRepository.findByPropertyIdAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                propertyUuid, fromDate.toLocalDate(), toDate.toLocalDate());

        CalendarViewDTO dto = new CalendarViewDTO();
        dto.setSlots(slots.stream().map(slot -> {
            CalendarViewDTO.SlotInfo slotInfo = new CalendarViewDTO.SlotInfo();
            slotInfo.id = slot.getId();
            slotInfo.startTime = slot.getStartTime();
            slotInfo.endTime = slot.getEndTime();
            slotInfo.booked = slot.isBooked();
            return slotInfo;
        }).toList());

        dto.setBookings(bookings.stream().map(booking -> {
            CalendarViewDTO.BookingInfo bookingInfo = new CalendarViewDTO.BookingInfo();
            bookingInfo.id = booking.getId();
            bookingInfo.startDate = booking.getStartDate().atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
            bookingInfo.endDate = booking.getEndDate().atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
            bookingInfo.status = booking.getStatus().name();
            return bookingInfo;
        }).toList());

        return dto;
    }

    @MutationMapping
    public ShortletBookingDTO createBooking(@Argument Map<String, String> input) {
        UUID tenantId = UUID.fromString(input.get("tenantId"));
        UUID landlordId = UUID.fromString(input.get("landlordId"));
        UUID propertyId = UUID.fromString(input.get("propertyId"));
        LocalDate startDate = LocalDate.parse(input.get("startDate"));
        LocalDate endDate = LocalDate.parse(input.get("endDate"));

        return bookingService.createBooking(tenantId, landlordId, propertyId, startDate, endDate);
    }

    @MutationMapping
    public ShortletBookingDTO acceptBooking(@Argument String id) {
        return bookingService.acceptBooking(UUID.fromString(id));
    }

    @MutationMapping
    public ShortletBookingDTO rejectBooking(@Argument String id) {
        return bookingService.rejectBooking(UUID.fromString(id));
    }

    @MutationMapping
    public ShortletBookingDTO cancelBooking(@Argument String id) {
        return bookingService.cancelBooking(UUID.fromString(id));
    }

    @MutationMapping
    public ShortletBookingDTO rescheduleBooking(
            @Argument String id,
            @Argument String startDate,
            @Argument String endDate) {
        return bookingService.rescheduleBooking(
                UUID.fromString(id),
                LocalDate.parse(startDate),
                LocalDate.parse(endDate));
    }
}
