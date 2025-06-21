package dev.visitingservice.service;

import dev.visitingservice.dto.ShortletAvailabilityDTO;
import dev.visitingservice.model.ShortletAvailability;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ShortletAvailabilityService {
    ShortletAvailabilityDTO setAvailability(UUID landlordId, LocalDate startDate, LocalDate endDate);
    List<ShortletAvailabilityDTO> getAvailability(UUID landlordId);
    void deleteAvailability(UUID availabilityId);
    boolean isAvailable(UUID landlordId, LocalDate startDate, LocalDate endDate);
}

