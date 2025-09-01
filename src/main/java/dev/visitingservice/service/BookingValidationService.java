package dev.visitingservice.service;

import java.util.UUID;

public interface BookingValidationService {
    /**
     * Validate that a user exists and has the correct role
     */
    void validateUserExists(UUID userId, String role);

    /**
     * Validate that the landlord owns the specified property
     */
    void validatePropertyOwnership(UUID propertyId, UUID landlordId);

    /**
     * Validate that tenant and landlord are different users
     */
    void validateTenantAndLandlord(UUID tenantId, UUID landlordId);

    /**
     * Validate that the property exists and is available for booking
     */
    void validatePropertyExists(UUID propertyId);
}
