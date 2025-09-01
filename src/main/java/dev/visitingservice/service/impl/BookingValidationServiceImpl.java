package dev.visitingservice.service.impl;

import dev.visitingservice.client.ListingDto;
import dev.visitingservice.client.ListingRestClient;
import dev.visitingservice.client.UserGraphQLClient;
import dev.visitingservice.exception.ExternalServiceException;
import dev.visitingservice.exception.InvalidRequestException;
import dev.visitingservice.service.BookingValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class BookingValidationServiceImpl implements BookingValidationService {

    private final UserGraphQLClient userClient;
    private final ListingRestClient listingClient;
    private final ExecutorService executor;

    @Autowired
    public BookingValidationServiceImpl(UserGraphQLClient userClient,
                                        ListingRestClient listingClient) {
        this.userClient = userClient;
        this.listingClient = listingClient;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public void validateUserExists(UUID userId, String role) {
        if (userId == null) {
            throw new InvalidRequestException(role + " ID cannot be null");
        }

        boolean exists;
        try {
            exists = CompletableFuture.supplyAsync(() -> userClient.doesUserExistById(userId), executor)
                                  .join();
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ExternalServiceException) throw (ExternalServiceException) cause;
            throw new ExternalServiceException("Failed to verify user existence for " + userId, cause);
        }

        if (!exists) {
            throw new InvalidRequestException(
                role.substring(0,1).toUpperCase() + role.substring(1) +
                " with ID " + userId + " does not exist"
            );
        }
    }

    @Override
    public void validatePropertyOwnership(UUID propertyId, UUID landlordId) {
        if (propertyId == null) {
            throw new InvalidRequestException("Property ID cannot be null");
        }
        if (landlordId == null) {
            throw new InvalidRequestException("Landlord ID cannot be null");
        }

        ListingDto listing;
        try {
            listing = CompletableFuture.supplyAsync(() -> listingClient.getListing(propertyId), executor)
                               .join();
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ExternalServiceException) throw (ExternalServiceException) cause;
            throw new ExternalServiceException("Failed to fetch property " + propertyId, cause);
        }

        if (listing == null) {
            throw new InvalidRequestException("Property with ID " + propertyId + " not found");
        }

        if (!listing.getOwnerId().equals(landlordId)) {
            throw new InvalidRequestException(
                "Landlord " + landlordId + " does not own property " + propertyId +
                ". Property is owned by " + listing.getOwnerId()
            );
        }
    }

    @Override
    public void validateTenantAndLandlord(UUID tenantId, UUID landlordId) {
        if (tenantId == null) {
            throw new InvalidRequestException("Tenant ID cannot be null");
        }
        if (landlordId == null) {
            throw new InvalidRequestException("Landlord ID cannot be null");
        }

        if (tenantId.equals(landlordId)) {
            throw new InvalidRequestException(
                "Tenant and landlord must be different users. Cannot book your own property."
            );
        }
    }

    @Override
    public void validatePropertyExists(UUID propertyId) {
        if (propertyId == null) {
            throw new InvalidRequestException("Property ID cannot be null");
        }

        ListingDto listing;
        try {
            listing = CompletableFuture.supplyAsync(() -> listingClient.getListing(propertyId), executor)
                               .join();
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ExternalServiceException) throw (ExternalServiceException) cause;
            throw new ExternalServiceException("Failed to verify property existence for " + propertyId, cause);
        }

        if (listing == null) {
            throw new InvalidRequestException("Property with ID " + propertyId + " not found or not available");
        }

        // Additional property validation can be added here
        // e.g., check if property is active, published, etc.
    }
}
