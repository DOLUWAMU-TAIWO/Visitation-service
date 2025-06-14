package dev.visitingservice.model;

public enum Status {
    PENDING, APPROVED, REJECTED, CANCELLED, RESCHEDULED, COMPLETED;

    public boolean isTerminal() {
        return this == CANCELLED || this == REJECTED || this == COMPLETED;
    }
}
