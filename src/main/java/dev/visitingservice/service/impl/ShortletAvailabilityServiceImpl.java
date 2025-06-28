package dev.visitingservice.service.impl;

import dev.visitingservice.dto.ShortletAvailabilityDTO;
import dev.visitingservice.model.ShortletAvailability;
import dev.visitingservice.repository.ShortletAvailabilityRepository;
import dev.visitingservice.service.ShortletAvailabilityService;
import dev.visitingservice.service.VisitValidationService;
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
    private final VisitValidationService validationService;

    @Autowired
    public ShortletAvailabilityServiceImpl(ShortletAvailabilityRepository availabilityRepository, VisitValidationService validationService) {
        this.availabilityRepository = availabilityRepository;
        this.validationService = validationService;
    }

    @Override
    @Transactional
    public ShortletAvailabilityDTO setAvailability(UUID landlordId, UUID propertyId, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }
        // Validate property ownership
        validationService.validateListingOwnership(propertyId, landlordId);
        boolean overlap = availabilityRepository.existsByLandlordIdAndPropertyIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                landlordId, propertyId, endDate, startDate);
        if (overlap) {
            throw new IllegalArgumentException("Overlapping availability exists for these dates");
        }
        ShortletAvailability availability = new ShortletAvailability();
        availability.setLandlordId(landlordId);
        availability.setPropertyId(propertyId);
        availability.setStartDate(startDate);
        availability.setEndDate(endDate);
        ShortletAvailability saved = availabilityRepository.save(availability);
        return toDTO(saved);
    }

    @Override
    public List<ShortletAvailabilityDTO> getAvailability(UUID landlordId, UUID propertyId) {
        if (landlordId == null || propertyId == null) {
            throw new IllegalArgumentException("landlordId and propertyId cannot be null");
        }
        return availabilityRepository.findByLandlordIdAndPropertyId(landlordId, propertyId)
                .stream().map(this::toDTO).collect(Collectors.toList());
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
    public boolean isAvailable(UUID landlordId, UUID propertyId, LocalDate startDate, LocalDate endDate) {
        return !availabilityRepository.existsByLandlordIdAndPropertyIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                landlordId, propertyId, endDate, startDate);
    }

    private ShortletAvailabilityDTO toDTO(ShortletAvailability availability) {
        ShortletAvailabilityDTO dto = new ShortletAvailabilityDTO();
        dto.setId(availability.getId());
        dto.setStartDate(availability.getStartDate());
        dto.setEndDate(availability.getEndDate());
        dto.setPropertyId(availability.getPropertyId());
        dto.setLandlordId(availability.getLandlordId());
        return dto;
    }
}
