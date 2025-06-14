package dev.visitingservice.controller;

import dev.visitingservice.dto.VisitCreateRequest;
import dev.visitingservice.dto.VisitResponseDto;
import dev.visitingservice.model.Status;
import dev.visitingservice.model.Visit;
import dev.visitingservice.service.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/visits")
public class VisitController {

    @Autowired
    private VisitService visitService;

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


}
