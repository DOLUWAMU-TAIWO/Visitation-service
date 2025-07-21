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
        return dto;
    }
}
