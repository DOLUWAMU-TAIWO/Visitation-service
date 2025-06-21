package dev.visitingservice.service;

import dev.visitingservice.model.ShortletBooking;
import dev.visitingservice.model.Visit;

public interface NotificationPublisher {
    void sendVisitRequested(Visit visit);
    void sendVisitApproved(Visit visit);
    void sendVisitRejected(Visit visit);
    void sendVisitCancelled(Visit visit);
    void sendReminder(Visit visit, int hoursBefore);
    void sendFeedbackPrompt(Visit visit);
    void sendBookingCreated(ShortletBooking booking );
    void sendBookingAccepted(ShortletBooking booking);
    void sendBookingRejected(ShortletBooking booking);
    void sendBookingCancelled(ShortletBooking booking);
    void sendBookingReminder(ShortletBooking booking, int hoursBefore);
    void sendBookingRescheduled(ShortletBooking booking);
}
