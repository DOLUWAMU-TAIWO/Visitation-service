package dev.visitingservice.client;

import dev.visitingservice.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.UUID;

@Service
public class UserGraphQLClient {

    private static final Logger logger = LoggerFactory.getLogger(UserGraphQLClient.class);
    private final HttpGraphQlClient graphQlClient;

    public UserGraphQLClient(WebClient.Builder webClientBuilder,
                             @Value("${userservice.api.key}") String apiKey,
                             @Value("${userservice.api.url}") String apiUrl) {
        WebClient client = webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader("X-API-KEY", apiKey)
                .build();
        this.graphQlClient = HttpGraphQlClient.builder(client).build();
    }

    public boolean doesUserExistById(UUID userId) {
        String query = """
        query checkUserExists($id: ID!) {
            getUserById(id: $id) {
                id
            }
        }
        """;
        try {
            Map<String, Object> userMap = (Map<String, Object>) graphQlClient.document(query)
                    .variable("id", userId.toString())
                    .retrieve("getUserById")
                    .toEntity(Map.class)
                    .block();
            logger.info("GraphQL response for user ID {}: {}", userId, userMap);
            return userMap != null && userMap.get("id") != null;
        } catch (Exception e) {
            logger.error("Error checking user ID: {}", userId, e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public UUID getUserUUIDByEmail(String email) {
        String query = """
        query getUserByEmail($email: String!) {
            getUserByEmail(email: $email) {
                id
            }
        }
        """;
        try {
            Map<String, Object> userMap = (Map<String, Object>) graphQlClient.document(query)
                    .variable("email", email)
                    .retrieve("getUserByEmail")
                    .toEntity(Map.class)
                    .block();
            logger.info("GraphQL response for email {}: {}", email, userMap);
            if (userMap == null || userMap.get("id") == null) {
                return null;
            }
            String idStr = (String) userMap.get("id");
            return UUID.fromString(idStr);
        } catch (Exception e) {
            logger.error("Error retrieving user by email: {}", email, e);
            throw new ExternalServiceException("Failed to retrieve user by email " + email, e);
        }
    }

    @SuppressWarnings("unchecked")
    public String getUserEmail(UUID userId) {
        String query = """
        query getUserEmail($id: ID!) {
            getUserById(id: $id) {
                email
            }
        }
        """;
        try {
            Map<String, Object> userMap = (Map<String, Object>) graphQlClient.document(query)
                    .variable("id", userId.toString())
                    .retrieve("getUserById")
                    .toEntity(Map.class)
                    .block();
            if (userMap == null || userMap.get("email") == null) {
                return null;
            }
            return (String) userMap.get("email");
        } catch (Exception e) {
            logger.error("Error fetching email for user {}: {}", userId, e.getMessage());
            throw new ExternalServiceException("Failed to fetch email for user " + userId, e);
        }
    }
    @SuppressWarnings("unchecked")
    public UserDTO getUser(UUID userId) {
        String query = """
    query getUser($id: ID!) {
        getUserById(id: $id) {
            firstName
            lastName
        }
    }
    """;
        try {
            Map<String, Object> userMap = (Map<String, Object>) graphQlClient.document(query)
                    .variable("id", userId.toString())
                    .retrieve("getUserById")
                    .toEntity(Map.class)
                    .block();

            if (userMap == null) {
                return null;
            }

            String firstName = (String) userMap.get("firstName");
            String lastName = (String) userMap.get("lastName");

            return new UserDTO(firstName, lastName);
        } catch (Exception e) {
            logger.error("Error retrieving user details for ID {}: {}", userId, e.getMessage());
            throw new ExternalServiceException("Failed to fetch user details for " + userId, e);
        }
    }
}