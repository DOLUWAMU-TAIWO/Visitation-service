package dev.visitingservice.service;

import java.util.UUID;

public interface VisitValidationService {
    void validateUserExists(UUID userId, String role);
    void validateListingOwnership(UUID propertyId, UUID landlordId);
    void validateTenantAndLandlord(UUID tenantId, UUID landlordId);
}
