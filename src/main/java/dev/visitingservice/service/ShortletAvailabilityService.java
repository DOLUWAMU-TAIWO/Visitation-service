package dev.visitingservice.service;

import dev.visitingservice.dto.ShortletAvailabilityDTO;
import dev.visitingservice.model.ShortletAvailability;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ShortletAvailabilityService {
    ShortletAvailabilityDTO setAvailability(UUID landlordId, UUID propertyId, LocalDate startDate, LocalDate endDate);
    List<ShortletAvailabilityDTO> getAvailability(UUID landlordId, UUID propertyId);
    void deleteAvailability(UUID availabilityId);
    boolean isAvailable(UUID landlordId, UUID propertyId, LocalDate startDate, LocalDate endDate);
}
