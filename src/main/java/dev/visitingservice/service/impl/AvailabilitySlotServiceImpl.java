package dev.visitingservice.service.impl;

import dev.visitingservice.model.AvailabilitySlot;
import dev.visitingservice.repository.AvailabilitySlotRepository;
import dev.visitingservice.service.AvailabilitySlotService;
import dev.visitingservice.service.VisitValidationService;
import dev.visitingservice.exception.SlotUnavailableException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AvailabilitySlotServiceImpl implements AvailabilitySlotService {

    @Autowired
    private AvailabilitySlotRepository repository;

    @Autowired
    private VisitValidationService validationService;

    @Override
    public AvailabilitySlot createSlot(AvailabilitySlot slot) {
        // validate landlord owns listing
        validationService.validateListingOwnership(slot.getPropertyId(), slot.getLandlordId());

        if (slot.getStartTime().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Cannot create slot in the past");
        }

        boolean overlap = repository.existsByPropertyIdAndStartTimeLessThanAndEndTimeGreaterThan(
                slot.getPropertyId(), slot.getEndTime(), slot.getStartTime());

        if (overlap) {
            throw new SlotUnavailableException("Slot cannot be taken: overlaps existing slot");
        }

        return repository.save(slot);
    }

    @Override
    public List<AvailabilitySlot> getAvailableSlots(UUID propertyId) {
        return repository.findByPropertyIdAndStartTimeAfterAndBookedFalse(propertyId, OffsetDateTime.now());
    }

    @Override
    public List<AvailabilitySlot> getAvailableSlots(UUID propertyId, UUID landlordId) {
        return repository.findByPropertyIdAndLandlordIdAndStartTimeAfterAndBookedFalse(
                propertyId, landlordId, OffsetDateTime.now());
    }

    @Override
    public boolean isSlotAvailable(UUID propertyId, OffsetDateTime start, OffsetDateTime end) {
        return repository.existsByPropertyIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqualAndBookedFalse(
                propertyId, start, end);
    }

    @Override
    public List<AvailabilitySlot> createSlotsForRange(UUID propertyId, UUID landlordId, LocalDate date, LocalTime startTime, LocalTime endTime, Integer intervalMinutes) {
        if (intervalMinutes == null || intervalMinutes <= 0) intervalMinutes = 60;
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        ZoneId nigeriaZone = ZoneId.of("Africa/Lagos");
        ZonedDateTime startZdt = ZonedDateTime.of(date, startTime, nigeriaZone);
        ZonedDateTime endZdt = ZonedDateTime.of(date, endTime, nigeriaZone);
        List<AvailabilitySlot> createdSlots = new ArrayList<>();
        ZonedDateTime slotStart = startZdt;
        while (slotStart.plusMinutes(intervalMinutes).compareTo(endZdt) <= 0) {
            ZonedDateTime slotEnd = slotStart.plusMinutes(intervalMinutes);
            OffsetDateTime slotStartUtc = slotStart.withZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime();
            OffsetDateTime slotEndUtc = slotEnd.withZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime();
            // Check for overlap
            boolean overlap = repository.existsByPropertyIdAndStartTimeLessThanAndEndTimeGreaterThan(
                propertyId, slotEndUtc, slotStartUtc);
            if (!overlap) {
                AvailabilitySlot slot = new AvailabilitySlot();
                slot.setPropertyId(propertyId);
                slot.setLandlordId(landlordId);
                slot.setStartTime(slotStartUtc);
                slot.setEndTime(slotEndUtc);
                slot.setBooked(false);
                createdSlots.add(repository.save(slot));
            }
            slotStart = slotEnd;
        }
        return createdSlots;
    }

    @Override
    public List<AvailabilitySlot> getSlots(UUID propertyId, UUID landlordId) {
        return repository.findByPropertyIdAndLandlordIdAndStartTimeAfter(
            propertyId, landlordId, OffsetDateTime.now());
    }
}