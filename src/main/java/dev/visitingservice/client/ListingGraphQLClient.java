package dev.visitingservice.client;

import dev.visitingservice.dto.ListingDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ListingGraphQLClient {
    private static final Logger logger = LoggerFactory.getLogger(ListingGraphQLClient.class);
    private final HttpGraphQlClient graphQlClient;

    public ListingGraphQLClient(WebClient.Builder webClientBuilder,
                                @Value("${listingservice.api.key}") String apiKey,
                                @Value("${listingservicegraphql.api.url}") String apiUrl) {
        WebClient client = webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader("X-API-KEY", apiKey)
                .build();
        this.graphQlClient = HttpGraphQlClient.builder(client).build();
    }

    @SuppressWarnings("unchecked")
    public List<ListingDto> getListingsByIds(List<UUID> ids) {
        String query = """
        query getListingsByIds($ids: [ID!]!) {
            getListingsByIds(ids: $ids) {
                id
                title
                description
                price
                status
                address {
                  streetNumber
                  street
                  city
                  state
                  country
                  postalCode
                  neighborhood
                  sublocality
                  formattedAddress
                  placeId
                }
                propertyType
                bedrooms
                bathrooms
                furnishingStatus
                amenities
                imageKeys
            }
        }
        """;
        try {
            List<Map<String, Object>> rawListings = (List<Map<String, Object>>) graphQlClient.document(query)
                    .variable("ids", ids.stream().map(UUID::toString).toList())
                    .retrieve("getListingsByIds")
                    .toEntity(List.class)
                    .block();
            logger.info("GraphQL response for listing IDs {}: {}", ids, rawListings);
            if (rawListings == null) {
                return List.of();
            }
            return rawListings.stream().map(ListingGraphQLClient::mapToListingDto).toList();
        } catch (Exception e) {
            logger.error("Error fetching listings by IDs: {}", ids, e);
            return List.of();
        }
    }

    private static ListingDto mapToListingDto(Map<String, Object> map) {
        ListingDto dto = new ListingDto();
        dto.setId(map.get("id") != null ? UUID.fromString(map.get("id").toString()) : null);
        dto.setTitle((String) map.get("title"));
        dto.setDescription((String) map.get("description"));
        dto.setPrice(map.get("price") != null ? new java.math.BigDecimal(map.get("price").toString()) : null);
        dto.setStatus((String) map.get("status"));

        // Enhanced pricing fields mapping
        dto.setPricePerNight(map.get("pricePerNight") != null ? new java.math.BigDecimal(map.get("pricePerNight").toString()) : null);
        dto.setCleaningFee(map.get("cleaningFee") != null ? new java.math.BigDecimal(map.get("cleaningFee").toString()) : null);
        dto.setSecurityAmount(map.get("securityAmount") != null ? new java.math.BigDecimal(map.get("securityAmount").toString()) : null);
        dto.setGuestCapacity(map.get("guestCapacity") != null ? ((Number) map.get("guestCapacity")).intValue() : null);
        dto.setMinNights(map.get("minNights") != null ? ((Number) map.get("minNights")).intValue() : null);
        dto.setMaxNights(map.get("maxNights") != null ? ((Number) map.get("maxNights")).intValue() : null);
        dto.setCancellationPolicy((String) map.get("cancellationPolicy"));
        dto.setInstantBooking((Boolean) map.get("instantBooking"));
        dto.setPurpose((String) map.get("purpose"));
        dto.setIsShortlet((Boolean) map.get("isShortlet"));

        // Address mapping
        @SuppressWarnings("unchecked")
        Map<String, Object> addressMap = (Map<String, Object>) map.get("address");
        if (addressMap != null) {
            dev.visitingservice.dto.AddressDto addressDto = new dev.visitingservice.dto.AddressDto();
            addressDto.setStreetNumber((String) addressMap.get("streetNumber"));
            addressDto.setStreet((String) addressMap.get("street"));
            addressDto.setCity((String) addressMap.get("city"));
            addressDto.setState((String) addressMap.get("state"));
            addressDto.setCountry((String) addressMap.get("country"));
            addressDto.setPostalCode((String) addressMap.get("postalCode"));
            addressDto.setNeighborhood((String) addressMap.get("neighborhood"));
            addressDto.setSublocality((String) addressMap.get("sublocality"));
            addressDto.setFormattedAddress((String) addressMap.get("formattedAddress"));
            addressDto.setPlaceId((String) addressMap.get("placeId"));
            dto.setAddress(addressDto);
        }

        dto.setPropertyType((String) map.get("propertyType"));
        dto.setBedrooms(map.get("bedrooms") != null ? ((Number) map.get("bedrooms")).intValue() : null);
        dto.setBathrooms(map.get("bathrooms") != null ? ((Number) map.get("bathrooms")).intValue() : null);
        dto.setFurnishingStatus((String) map.get("furnishingStatus"));
        @SuppressWarnings("unchecked")
        List<String> amenities = (List<String>) map.get("amenities");
        dto.setAmenities(amenities);
        @SuppressWarnings("unchecked")
        List<String> imageKeys = (List<String>) map.get("imageKeys");
        dto.setImageKeys(imageKeys);
        return dto;
    }

    public ListingDto getListingById(String listingId) {
        String query = """
        query getListingById($id: ID!) {
            getListingById(id: $id) {
                id
                title
                description
                price
                status
                pricePerNight
                cleaningFee
                securityAmount
                guestCapacity
                minNights
                maxNights
                cancellationPolicy
                instantBooking
                purpose
                isShortlet
                address {
                  streetNumber
                  street
                  city
                  state
                  country
                  postalCode
                  neighborhood
                  sublocality
                  formattedAddress
                  placeId
                }
                propertyType
                bedrooms
                bathrooms
                furnishingStatus
                amenities
                imageKeys
            }
        }
        """;

        try {
            Map<String, Object> rawListing = graphQlClient.document(query)
                    .variable("id", listingId)
                    .retrieve("getListingById")
                    .toEntity(Map.class)
                    .block();

            logger.info("GraphQL response for listing ID {}: {}", listingId, rawListing);

            if (rawListing == null) {
                return null;
            }

            return mapToListingDto(rawListing);
        } catch (Exception e) {
            logger.error("Error fetching listing by ID: {}", listingId, e);
            throw new RuntimeException("Failed to fetch listing details", e);
        }
    }
}
