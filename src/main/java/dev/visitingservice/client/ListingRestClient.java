package dev.visitingservice.client;

import dev.visitingservice.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class ListingRestClient {
    private static final Logger logger = LoggerFactory.getLogger(ListingRestClient.class);

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final HttpHeaders headers;

    public ListingRestClient(RestTemplate restTemplate,
                             @Value("${listingservice.api.url}") String apiUrl,
                             @Value("${listingservice.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.headers = new HttpHeaders();
        this.headers.set("X-API-KEY", apiKey);
    }

    public ListingDto getListing(UUID listingId) {
        String url = apiUrl + "/api/listings/" + listingId;
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<ListingDto> response = restTemplate.exchange(url, HttpMethod.GET, entity, ListingDto.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            logger.info("Listing not found: {}", listingId);
            return null;
        } catch (Exception e) {
            logger.error("Error fetching listing {}: {}", listingId, e.getMessage());
            throw new ExternalServiceException("Failed to fetch listing " + listingId, e);
        }
    }
}
