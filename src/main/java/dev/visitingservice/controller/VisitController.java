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

    @GetMapping
    public ResponseEntity<List<AvailabilitySlot>> getSlots(@RequestParam("propertyId") UUID propertyId,
                                                           @RequestParam("landlordId") UUID landlordId) {
        List<AvailabilitySlot> slots = slotService.getSlots(propertyId, landlordId);
        return ResponseEntity.ok(slots);
    }

    // TENANT-FACING VISIT ENDPOINTS
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<VisitResponseDto>> getTenantVisits(
            @PathVariable UUID tenantId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<Visit> visits = visitService.getVisitsByVisitor(tenantId);

        // Filter by status if provided
        if (status != null && !status.equalsIgnoreCase("all")) {
            try {
                Status visitStatus = Status.valueOf(status.toUpperCase());
                visits = visits.stream()
                    .filter(v -> v.getStatus() == visitStatus)
                    .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // Invalid status, return all visits
            }
        }

        // Apply pagination
        List<VisitResponseDto> dtos = visits.stream()
                .skip((long) page * size)
                .limit(size)
                .map(VisitResponseDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/tenant/{tenantId}/upcoming")
    public ResponseEntity<List<VisitResponseDto>> getTenantUpcomingVisits(
            @PathVariable UUID tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        OffsetDateTime now = OffsetDateTime.now();
        List<Visit> visits = visitService.getVisitsByVisitor(tenantId).stream()
                .filter(v -> v.getScheduledAt() != null && v.getScheduledAt().isAfter(now))
                .filter(v -> v.getStatus() == Status.APPROVED || v.getStatus() == Status.PENDING)
                .collect(Collectors.toList());

        List<VisitResponseDto> dtos = visits.stream()
                .skip((long) page * size)
                .limit(size)
                .map(VisitResponseDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/tenant/{tenantId}/past")
    public ResponseEntity<List<VisitResponseDto>> getTenantPastVisits(
            @PathVariable UUID tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        OffsetDateTime now = OffsetDateTime.now();
        List<Visit> visits = visitService.getVisitsByVisitor(tenantId).stream()
                .filter(v -> v.getStatus() == Status.COMPLETED ||
                           v.getStatus() == Status.CANCELLED ||
                           (v.getScheduledAt() != null && v.getScheduledAt().isBefore(now)))
                .collect(Collectors.toList());

        List<VisitResponseDto> dtos = visits.stream()
                .skip((long) page * size)
                .limit(size)
                .map(VisitResponseDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/tenant/{tenantId}/pending")
    public ResponseEntity<List<VisitResponseDto>> getTenantPendingVisits(
            @PathVariable UUID tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<Visit> visits = visitService.getVisitsByVisitor(tenantId).stream()
                .filter(v -> v.getStatus() == Status.PENDING)
                .collect(Collectors.toList());

        List<VisitResponseDto> dtos = visits.stream()
                .skip((long) page * size)
                .limit(size)
                .map(VisitResponseDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/tenant/{tenantId}/details/{visitId}")
    public ResponseEntity<VisitResponseDto> getTenantVisitDetails(
            @PathVariable UUID tenantId,
            @PathVariable UUID visitId) {

        Visit visit = visitService.getVisit(visitId);

        // Verify the visit belongs to the tenant
        if (visit == null || !visit.getVisitorId().equals(tenantId)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(VisitResponseDto.fromEntity(visit));
    }
}
