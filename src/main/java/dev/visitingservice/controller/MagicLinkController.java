package dev.visitingservice.controller;

import dev.visitingservice.client.UserGraphQLClient;
import dev.visitingservice.client.UserDTO;
import dev.visitingservice.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/magic-link")
public class MagicLinkController {

    private static final Logger logger = Logger.getLogger(MagicLinkController.class.getName());

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserGraphQLClient userGraphQLClient;

    /**
     * Generate a magic link token for testing purposes
     * POST /api/magic-link/generate
     * Body: { "email": "user@example.com", "userId": "uuid" }
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateMagicLink(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String userIdStr = request.get("userId");

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email is required"));
            }

            UUID userId;
            String role = "USER"; // Default role

            // If userId provided, use it directly
            if (userIdStr != null && !userIdStr.trim().isEmpty()) {
                try {
                    userId = UUID.fromString(userIdStr);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid userId format"));
                }
            } else {
                // Try to fetch user from UserService
                try {
                    UserDTO user = userGraphQLClient.getUserByEmail(email);
                    if (user != null) {
                        userId = user.getId();
                        role = user.getRole() != null ? user.getRole() : "USER";
                    } else {
                        // User not found, generate a random UUID for testing
                        userId = UUID.randomUUID();
                        logger.warning("User not found for email: " + email + ". Using random UUID for testing.");
                    }
                } catch (Exception e) {
                    // Fallback to random UUID if user service fails
                    userId = UUID.randomUUID();
                    logger.warning("Failed to fetch user from UserService. Using random UUID for testing.");
                }
            }

            // Generate magic link token
            String magicToken = jwtUtils.generateMagicLinkToken(userId, email, role);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("magicToken", magicToken);
            response.put("userId", userId.toString());
            response.put("email", email);
            response.put("role", role);
            response.put("expiresIn", "30 minutes");
            response.put("testUrl", "https://qorelabs.online/auth/magic-signin?token=" + magicToken);

            logger.info("Generated magic link token for user: " + email);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.severe("Error generating magic link: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to generate magic link"));
        }
    }

    /**
     * Generate magic link for visit completion
     * POST /api/magic-link/visit-completion
     * Body: { "visitId": "uuid", "userId": "uuid" }
     */
    @PostMapping("/visit-completion")
    public ResponseEntity<?> generateVisitCompletionMagicLink(@RequestBody Map<String, String> request) {
        try {
            String visitId = request.get("visitId");
            String userIdStr = request.get("userId");

            if (visitId == null || userIdStr == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "visitId and userId are required"));
            }

            UUID userId;
            try {
                userId = UUID.fromString(userIdStr);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid userId format"));
            }

            // Try to get user details from UserService
            String email = "unknown@example.com";
            String role = "USER";

            logger.info("=== DEBUGGING USER LOOKUP ===");
            logger.info("Attempting to fetch user details for userId: " + userId);

            try {
                UserDTO user = userGraphQLClient.getUserById(userId);
                logger.info("UserGraphQLClient.getUserById() completed");

                if (user != null) {
                    logger.info("UserService returned user object:");
                    logger.info("- ID: " + (user.getId() != null ? user.getId().toString() : "null"));
                    logger.info("- Email: " + (user.getEmail() != null ? user.getEmail() : "null"));
                    logger.info("- Role: " + (user.getRole() != null ? user.getRole() : "null"));
                    logger.info("- FirstName: " + (user.getFirstName() != null ? user.getFirstName() : "null"));
                    logger.info("- LastName: " + (user.getLastName() != null ? user.getLastName() : "null"));

                    email = user.getEmail();
                    role = user.getRole() != null ? user.getRole() : "USER";
                } else {
                    logger.warning("UserService returned null for userId: " + userId);
                }
            } catch (Exception e) {
                logger.severe("Exception occurred while fetching user details:");
                logger.severe("- Exception type: " + e.getClass().getSimpleName());
                logger.severe("- Exception message: " + e.getMessage());
                e.printStackTrace();
            }

            logger.info("Final values before generating magic token:");
            logger.info("- email: " + email);
            logger.info("- role: " + role);
            logger.info("=== END DEBUGGING ===");

            // Generate magic link token with visit context
            String magicToken = jwtUtils.generateMagicLinkToken(userId, email, role);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("magicToken", magicToken);
            response.put("visitId", visitId);
            response.put("userId", userId.toString());
            response.put("email", email);
            response.put("role", role);
            response.put("expiresIn", "30 minutes");
            response.put("testUrl", "https://qorelabs.online/auth/magic-signin?token=" + magicToken);
            response.put("dashboardUrl", "https://qorelabs.online/dashboard?token=" + magicToken);

            logger.info("Generated visit completion magic link for user: " + email + ", visit: " + visitId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.severe("Error generating visit completion magic link: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to generate visit completion magic link"));
        }
    }

    /**
     * Validate a magic link token (for testing)
     * POST /api/magic-link/validate
     * Body: { "token": "jwt-token-here" }
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateMagicLink(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");

            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Token is required"));
            }

            // Validate token
            if (!jwtUtils.validateJwtToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired token"));
            }

            // Check if it's a magic link token
            if (!jwtUtils.isMagicLinkToken(token)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Not a magic link token"));
            }

            // Extract user details
            Map<String, Object> userDetails = jwtUtils.getUserDetailsFromJwtToken(token);
            long expirationMs = jwtUtils.getExpirationFromToken(token);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("userDetails", userDetails);
            response.put("expiresInMs", expirationMs);
            response.put("expiresInMinutes", expirationMs / (1000 * 60));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.severe("Error validating magic link: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to validate magic link"));
        }
    }
}
