package dev.visitingservice.controller;

import dev.visitingservice.dto.ApiResponse;
import dev.visitingservice.dto.VisitCreateRequest;
import dev.visitingservice.dto.VisitResponseDto;
import dev.visitingservice.model.AvailabilitySlot;
import dev.visitingservice.model.Status;
import dev.visitingservice.model.Visit;
import dev.visitingservice.service.AvailabilitySlotService;
import dev.visitingservice.service.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/visits")
public class VisitController {

    @Autowired
    private VisitService visitService;
    @Autowired
    private AvailabilitySlotService slotService;

    @PostMapping
    public ResponseEntity<VisitResponseDto> requestVisit(@RequestBody VisitCreateRequest createRequest) {
        Visit created = visitService.requestVisit(createRequest.toEntity());
        return ResponseEntity.ok(VisitResponseDto.fromEntity(created));
    }


    @PutMapping("/{id}/approve")
    public ResponseEntity<VisitResponseDto> approveVisit(@PathVariable UUID id) {
        Visit updated = visitService.approveVisit(id);
        return ResponseEntity.ok(VisitResponseDto.fromEntity(updated));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<VisitResponseDto> rejectVisit(@PathVariable UUID id) {
        Visit updated = visitService.rejectVisit(id);
        return ResponseEntity.ok(VisitResponseDto.fromEntity(updated));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<VisitResponseDto> updateStatus(@PathVariable UUID id, @RequestParam Status status) {
        Visit updated = visitService.updateVisitStatus(id, status);
        return ResponseEntity.ok(VisitResponseDto.fromEntity(updated));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<VisitResponseDto> cancelVisit(@PathVariable UUID id) {
        Visit updated = visitService.cancelVisit(id);
        return ResponseEntity.ok(VisitResponseDto.fromEntity(updated));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<VisitResponseDto> completeVisit(@PathVariable UUID id) {
        Visit updated = visitService.completeVisit(id);
        return ResponseEntity.ok(VisitResponseDto.fromEntity(updated));
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<VisitResponseDto>> getByProperty(@PathVariable UUID propertyId) {
        List<VisitResponseDto> dtos = visitService.getVisitsByProperty(propertyId).stream()
            .map(VisitResponseDto::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Availability endpoints merged here
    @GetMapping("/availability/{propertyId}")
    public ResponseEntity<ApiResponse<List<AvailabilitySlot>>> getSlots(@PathVariable UUID propertyId) {
        List<AvailabilitySlot> slots = slotService.getAvailableSlots(propertyId);
        if (slots.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, "No available slots for property " + propertyId, null));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched available slots", slots));
    }

    @PostMapping("/availability")
    public ResponseEntity<ApiResponse<AvailabilitySlot>> createSlot(@RequestBody AvailabilitySlot slot) {
        AvailabilitySlot created = slotService.createSlot(slot);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(true, "Availability slot created successfully", created));
    }

    @GetMapping("/availability/{propertyId}/check")
    public ResponseEntity<ApiResponse<Boolean>> isSlotAvailable(@PathVariable UUID propertyId,
                                                                 @RequestParam OffsetDateTime start,
                                                                 @RequestParam OffsetDateTime end) {
        boolean available = slotService.isSlotAvailable(propertyId, start, end);
        String msg = available ? "Slot is available" : "Slot is not available";
        return ResponseEntity.ok(new ApiResponse<>(true, msg, available));
    }

    @GetMapping("/availability")
    public ResponseEntity<ApiResponse<List<AvailabilitySlot>>> getSlotsByQuery(
            @RequestParam UUID propertyId,
            @RequestParam(required = false) UUID landlordId) {
        List<AvailabilitySlot> slots = (landlordId != null)
            ? slotService.getAvailableSlots(propertyId, landlordId)
            : slotService.getAvailableSlots(propertyId);
        if (slots.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, "No available slots found for property " + propertyId
                    + (landlordId != null ? " and landlord " + landlordId : ""), null));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "Available slots fetched successfully", slots));
    }

}
