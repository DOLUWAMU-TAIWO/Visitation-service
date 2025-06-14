package dev.visitingservice.service.impl;

import dev.visitingservice.client.ListingDto;
import dev.visitingservice.client.ListingRestClient;
import dev.visitingservice.client.UserGraphQLClient;
import dev.visitingservice.exception.ExternalServiceException;
import dev.visitingservice.exception.InvalidRequestException;
import dev.visitingservice.service.VisitValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class VisitValidationServiceImpl implements VisitValidationService {

    private final UserGraphQLClient userClient;
    private final ListingRestClient listingClient;
    private final ExecutorService executor;

    @Autowired
    public VisitValidationServiceImpl(UserGraphQLClient userClient,
                                      ListingRestClient listingClient) {
        this.userClient = userClient;
        this.listingClient = listingClient;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public void validateUserExists(UUID userId, String role) {
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
            throw new InvalidRequestException(role.substring(0,1).toUpperCase() + role.substring(1) + " does not exist");
        }
    }

    @Override
    public void validateListingOwnership(UUID propertyId, UUID landlordId) {
        ListingDto listing;
        try {
            listing = CompletableFuture.supplyAsync(() -> listingClient.getListing(propertyId), executor)
                               .join();
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ExternalServiceException) throw (ExternalServiceException) cause;
            throw new ExternalServiceException("Failed to fetch listing " + propertyId, cause);
        }
        if (listing == null) {
            throw new InvalidRequestException("Listing not found: " + propertyId);
        }
        if (!listing.getOwnerId().equals(landlordId)) {
            throw new InvalidRequestException("Landlord " + landlordId + " does not own listing " + propertyId);
        }
    }

    @Override
    public void validateTenantAndLandlord(UUID tenantId, UUID landlordId) {
        if (tenantId.equals(landlordId)) {
            throw new InvalidRequestException("Tenant and landlord must be different users");
        }
    }
}
