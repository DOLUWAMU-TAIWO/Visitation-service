package dev.visitingservice.service.impl;

import dev.visitingservice.model.VisitFeedback;
import dev.visitingservice.model.VisitFeedback.FollowUpStatus;
import dev.visitingservice.repository.VisitFeedbackRepository;
import dev.visitingservice.service.VisitFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class VisitFeedbackServiceImpl implements VisitFeedbackService {

    @Autowired
    private VisitFeedbackRepository feedbackRepository;

    @Override
    @Transactional
    public VisitFeedback submitFeedback(VisitFeedback feedback) {
        // auto-flag if rating <=2
//        if (feedback.getRating() <= 2) {
//            feedback.setFollowUpNeeded(true);
//            feedback.setFollowUpReason("Low rating");
//            feedback.setFollowUpStatus(FollowUpStatus.PENDING);
//        } else {
//            feedback.setFollowUpNeeded(false);
//            feedback.setFollowUpStatus(FollowUpStatus.RESOLVED);
//        }
       return feedbackRepository.save(feedback);
    }

    @Override
    public List<VisitFeedback> getFeedbackForVisit(UUID visitId) {
        return feedbackRepository.findByVisitId(visitId);
    }
}
