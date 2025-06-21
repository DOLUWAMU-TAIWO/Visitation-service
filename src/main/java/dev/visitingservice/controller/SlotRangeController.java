package dev.visitingservice.controller;

import dev.visitingservice.dto.SlotRangeRequestDTO;
import dev.visitingservice.model.AvailabilitySlot;
import dev.visitingservice.service.AvailabilitySlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/slots")
public class SlotRangeController {
    private final AvailabilitySlotService slotService;

    @Autowired
    public SlotRangeController(AvailabilitySlotService slotService) {
        this.slotService = slotService;
    }

    @PostMapping("/range")
    public ResponseEntity<List<AvailabilitySlot>> createSlotsForRange(@RequestBody SlotRangeRequestDTO dto) {
        List<AvailabilitySlot> slots = slotService.createSlotsForRange(
                dto.getPropertyId(),
                dto.getLandlordId(),
                dto.getDate(),
                dto.getStartTime(),
                dto.getEndTime(),
                dto.getIntervalMinutes()
        );
        return ResponseEntity.ok(slots);
    }
}

