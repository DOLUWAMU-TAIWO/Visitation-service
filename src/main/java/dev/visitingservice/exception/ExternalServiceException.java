package dev.visitingservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an external service call fails.
 */
public class ExternalServiceException extends RuntimeException {
    private final HttpStatus status;

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.BAD_GATEWAY;
    }

    public ExternalServiceException(String message) {
        super(message);
        this.status = HttpStatus.BAD_GATEWAY;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
