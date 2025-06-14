package dev.visitingservice.controller;

import dev.visitingservice.model.AvailabilitySlot;
import dev.visitingservice.service.AvailabilitySlotService;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    @Autowired
    private AvailabilitySlotService slotService;

    @GetMapping("/{propertyId}")
    public ResponseEntity<List<AvailabilitySlot>> getSlots(@PathVariable UUID propertyId) {
        return ResponseEntity.ok(slotService.getAvailableSlots(propertyId));
    }

    @PostMapping
    public ResponseEntity<AvailabilitySlot> createSlot(@RequestBody AvailabilitySlot slot) {
        return ResponseEntity.ok(slotService.createSlot(slot));
    }

    @GetMapping("/{propertyId}/check")
    public ResponseEntity<Boolean> isSlotAvailable(@PathVariable UUID propertyId,
                                                 @RequestParam OffsetDateTime start,
                                                 @RequestParam OffsetDateTime end) {
        boolean available = slotService.isSlotAvailable(propertyId, start, end);
        return ResponseEntity.ok(available);
    }

    @GetMapping
    public ResponseEntity<List<AvailabilitySlot>> getSlotsByQuery(
            @RequestParam UUID propertyId,
            @RequestParam(required = false) UUID landlordId) {

        if (landlordId != null) {
            return ResponseEntity.ok(slotService.getAvailableSlots(propertyId, landlordId));
        }
        return ResponseEntity.ok(slotService.getAvailableSlots(propertyId));
    }

}
