package dev.visitingservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Thrown when an availability slot cannot be taken due to overlap.
 */
public class SlotUnavailableException extends ResponseStatusException {
    public SlotUnavailableException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
