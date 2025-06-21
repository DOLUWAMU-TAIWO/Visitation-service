package dev.visitingservice.service.impl;

import dev.visitingservice.dto.ShortletAvailabilityDTO;
import dev.visitingservice.model.ShortletAvailability;
import dev.visitingservice.repository.ShortletAvailabilityRepository;
import dev.visitingservice.service.ShortletAvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ShortletAvailabilityServiceImpl implements ShortletAvailabilityService {

    private final ShortletAvailabilityRepository availabilityRepository;

    @Autowired
    public ShortletAvailabilityServiceImpl(ShortletAvailabilityRepository availabilityRepository) {
        this.availabilityRepository = availabilityRepository;
    }

    @Override
    @Transactional
    public ShortletAvailabilityDTO setAvailability(UUID landlordId, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }
        boolean overlap = availabilityRepository.existsByLandlordIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                landlordId, endDate, startDate);
        if (overlap) {
            throw new IllegalArgumentException("Overlapping availability exists for these dates");
        }
        ShortletAvailability availability = new ShortletAvailability();
        availability.setLandlordId(landlordId);
        availability.setStartDate(startDate);
        availability.setEndDate(endDate);
        ShortletAvailability saved = availabilityRepository.save(availability);
        return toDTO(saved);
    }

    @Override
    public List<ShortletAvailabilityDTO> getAvailability(UUID landlordId) {
        if (landlordId == null) {
            throw new IllegalArgumentException("landlordId cannot be null");
        }
        return availabilityRepository.findByLandlordId(landlordId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAvailability(UUID availabilityId) {
        if (availabilityId == null) {
            throw new IllegalArgumentException("availabilityId cannot be null");
        }
        if (!availabilityRepository.existsById(availabilityId)) {
            throw new IllegalArgumentException("Availability not found for the provided ID");
        }
        availabilityRepository.deleteById(availabilityId);
    }

    @Override
    public boolean isAvailable(UUID landlordId, LocalDate startDate, LocalDate endDate) {
        if (landlordId == null || startDate == null || endDate == null) {
            throw new IllegalArgumentException("landlordId, startDate, and endDate cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }
        // Check if any availability slot fully covers the requested range
        return availabilityRepository.findByLandlordId(landlordId).stream()
                .anyMatch(a -> !a.getStartDate().isAfter(startDate) && !a.getEndDate().isBefore(endDate));
    }

    private ShortletAvailabilityDTO toDTO(ShortletAvailability availability) {
        ShortletAvailabilityDTO dto = new ShortletAvailabilityDTO();
        dto.setId(availability.getId());
        dto.setStartDate(availability.getStartDate());
        dto.setEndDate(availability.getEndDate());
        return dto;
    }
}
