# Use Eclipse Temurin JDK 21 on Alpine as the base image
FROM eclipse-temurin:21-jdk-alpine

# Create a non-root user and group for better container security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy built JAR into the image
COPY target/VisitingService-0.0.1-SNAPSHOT.jar app.jar

# Set proper ownership for the app files
RUN chown -R appuser:appgroup /app

# Use non-root user
USER appuser

# Expose application port
EXPOSE 2525

# Define a health check on the correct exposed port
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --spider -q http://localhost:2525/actuator/health || exit 1

# Launch the app with production profile
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]