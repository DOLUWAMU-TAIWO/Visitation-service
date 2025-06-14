package dev.visitingservice.service;

import dev.visitingservice.model.AvailabilitySlot;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AvailabilitySlotService {
    AvailabilitySlot createSlot(AvailabilitySlot slot);

    List<AvailabilitySlot> getAvailableSlots(UUID propertyId);

    List<AvailabilitySlot> getAvailableSlots(UUID propertyId, UUID landlordId); // ✅ New method

    boolean isSlotAvailable(UUID propertyId, OffsetDateTime start, OffsetDateTime end);
}