package dev.visitingservice.service;

import dev.visitingservice.model.VisitFeedback;
import java.util.List;
import java.util.UUID;

public interface VisitFeedbackService {
    VisitFeedback submitFeedback(VisitFeedback feedback);
    List<VisitFeedback> getFeedbackForVisit(UUID visitId);
}
