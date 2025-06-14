package dev.visitingservice.service.impl;

import dev.visitingservice.model.AvailabilitySlot;
import dev.visitingservice.repository.AvailabilitySlotRepository;
import dev.visitingservice.service.AvailabilitySlotService;
import dev.visitingservice.service.VisitValidationService;
import dev.visitingservice.exception.SlotUnavailableException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
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
}