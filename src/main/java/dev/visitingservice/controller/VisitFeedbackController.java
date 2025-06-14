package dev.visitingservice.controller;

import dev.visitingservice.model.VisitFeedback;
import dev.visitingservice.service.VisitFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/feedback")
public class VisitFeedbackController {

    @Autowired
    private VisitFeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<VisitFeedback> submitFeedback(@RequestBody VisitFeedback feedback) {
        return ResponseEntity.ok(feedbackService.submitFeedback(feedback));
    }

    @GetMapping("/{visitId}")
    public ResponseEntity<List<VisitFeedback>> getFeedbackForVisit(@PathVariable UUID visitId) {
        return ResponseEntity.ok(feedbackService.getFeedbackForVisit(visitId));
    }
}
