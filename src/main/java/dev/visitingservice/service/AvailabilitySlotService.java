package dev.visitingservice.service;

import dev.visitingservice.model.AvailabilitySlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AvailabilitySlotService {
    AvailabilitySlot createSlot(AvailabilitySlot slot);

    List<AvailabilitySlot> getAvailableSlots(UUID propertyId);

    List<AvailabilitySlot> getAvailableSlots(UUID propertyId, UUID landlordId); // ✅ New method

    boolean isSlotAvailable(UUID propertyId, OffsetDateTime start, OffsetDateTime end);

    List<AvailabilitySlot> createSlotsForRange(
        UUID propertyId, UUID landlordId, LocalDate date, LocalTime startTime, LocalTime endTime, Integer intervalMinutes
    );
}