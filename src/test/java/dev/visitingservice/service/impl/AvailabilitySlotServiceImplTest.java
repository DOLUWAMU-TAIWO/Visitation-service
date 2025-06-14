package dev.visitingservice.service.impl;

import dev.visitingservice.exception.SlotUnavailableException;
import dev.visitingservice.model.AvailabilitySlot;
import dev.visitingservice.repository.AvailabilitySlotRepository;
import dev.visitingservice.service.VisitValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AvailabilitySlotServiceImplTest {

    @Mock
    private AvailabilitySlotRepository repository;

    @Mock
    private VisitValidationService validationService;

    @InjectMocks
    private AvailabilitySlotServiceImpl service;

    private AvailabilitySlot slot;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        slot = new AvailabilitySlot();
        slot.setPropertyId(UUID.randomUUID());
        slot.setLandlordId(UUID.randomUUID());
        slot.setStartTime(OffsetDateTime.now().plusDays(1));
        slot.setEndTime(OffsetDateTime.now().plusDays(1).plusHours(1));
    }

    @Test
    void createSlot_ShouldSave_WhenNoOverlap() {
        when(repository.existsByPropertyIdAndStartTimeLessThanAndEndTimeGreaterThan(
                slot.getPropertyId(), slot.getEndTime(), slot.getStartTime()))
                .thenReturn(false);
        when(repository.save(slot)).thenReturn(slot); // <-- Add this line
        AvailabilitySlot result = service.createSlot(slot);
        assertNotNull(result);
        verify(repository).save(slot);
    }

    @Test
    void createSlot_ShouldThrow_WhenOverlap() {
        when(repository.existsByPropertyIdAndStartTimeLessThanAndEndTimeGreaterThan(
                slot.getPropertyId(), slot.getEndTime(), slot.getStartTime()))
            .thenReturn(true);
        assertThrows(SlotUnavailableException.class, () -> service.createSlot(slot));
    }

    @Test
    void isSlotAvailable_ShouldReturnTrue_WhenNotBookedAndNoOverlap() {
        OffsetDateTime start = OffsetDateTime.now().plusDays(2);
        OffsetDateTime end = start.plusHours(2);
        when(repository.existsByPropertyIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqualAndBookedFalse(
                slot.getPropertyId(), start, end))
            .thenReturn(true);
        assertTrue(service.isSlotAvailable(slot.getPropertyId(), start, end));
    }
}

