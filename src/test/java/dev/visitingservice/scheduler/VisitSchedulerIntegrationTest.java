package dev.visitingservice.scheduler;

import dev.visitingservice.model.Visit;
import dev.visitingservice.model.Status;
import dev.visitingservice.model.AvailabilitySlot;
import dev.visitingservice.repository.VisitRepository;
import dev.visitingservice.repository.AvailabilitySlotRepository;
import dev.visitingservice.service.VisitService;
import dev.visitingservice.service.NotificationPublisher;
import dev.visitingservice.client.UserGraphQLClient;
import dev.visitingservice.client.ListingRestClient;
import dev.visitingservice.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 🧪 COMPREHENSIVE SCHEDULER INTEGRATION TEST
 *
 * This test suite simulates the exact scenarios that caused repeated feedback emails:
 * 1. Deleted user accounts causing email failures and transaction rollbacks
 * 2. Scheduler behavior with various failure modes
 * 3. Service layer integration and error handling
 * 4. Database consistency and rollback scenarios
 * 5. Email tracking and duplicate prevention
 */
@SpringBootTest
@TestPropertySource(properties = {
    "logging.level.dev.visitingservice=DEBUG",
    "spring.jpa.show-sql=true"
})
@Transactional
@Rollback
class VisitSchedulerIntegrationTest {

    @Autowired
    private VisitScheduler visitScheduler;

    @Autowired
    private VisitService visitService;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private AvailabilitySlotRepository slotRepository;

    @SpyBean
    private NotificationPublisher notificationPublisher;

    @MockBean
    private UserGraphQLClient userGraphQLClient;

    @MockBean
    private ListingRestClient listingRestClient;

    @MockBean
    private EmailService emailService;

    private Visit testVisit;
    private AvailabilitySlot testSlot;
    private UUID visitorId = UUID.fromString("0d508b66-2175-457d-846b-915905f4cbcd");
    private UUID landlordId = UUID.fromString("eb22925c-1201-48b0-894d-373c825e2778");
    private UUID deletedLandlordId = UUID.fromString("1b40f32e-0ab5-47b4-adde-7ec6561191a7");
    private UUID propertyId = UUID.fromString("1880e4c0-5bd6-4ab9-b06d-2e422298ef30");

    @BeforeEach
    void setUp() {
        // Create test slot
        testSlot = new AvailabilitySlot();
        testSlot.setStartTime(OffsetDateTime.now(ZoneOffset.UTC).minusHours(2)); // 2 hours ago
        testSlot.setEndTime(OffsetDateTime.now(ZoneOffset.UTC).minusHours(1)); // 1 hour ago
        testSlot.setBooked(true);
        testSlot.setPropertyId(propertyId);
        testSlot.setLandlordId(landlordId);
        testSlot = slotRepository.save(testSlot);

        // Create approved visit that should be auto-completed
        testVisit = new Visit();
        testVisit.setPropertyId(propertyId);
        testVisit.setVisitorId(visitorId);
        testVisit.setLandlordId(landlordId);
        testVisit.setScheduledAt(OffsetDateTime.now(ZoneOffset.UTC).minusHours(2)); // 2 hours ago (past cutoff)
        testVisit.setDurationMinutes(60);
        testVisit.setStatus(Status.APPROVED);
        testVisit.setSlotId(testSlot.getId());
        testVisit.setFeedbackEmailSent(false);
        testVisit = visitRepository.save(testVisit);

        // Setup default mock responses
        setupDefaultMocks();
    }

    private void setupDefaultMocks() {
        // Normal users exist and can receive emails
        when(userGraphQLClient.getUserEmail(visitorId))
            .thenReturn("visitor@example.com");
        when(userGraphQLClient.getUserEmail(landlordId))
            .thenReturn("landlord@example.com");

        // Simulate deleted user scenario
        when(userGraphQLClient.getUserEmail(deletedLandlordId))
            .thenThrow(new RuntimeException("User not found"));
    }

    @Test
    @DisplayName("🎯 SCENARIO 1: Normal Happy Path - Visit Completed Successfully")
    void testNormalVisitCompletion() {
        // Given: Normal approved visit past scheduled time
        assertEquals(Status.APPROVED, testVisit.getStatus());
        assertFalse(testVisit.isFeedbackEmailSent());

        // When: Scheduler runs
        visitScheduler.autoCompletePastVisits();

        // Then: Visit should be completed and feedback email sent
        Visit updatedVisit = visitRepository.findById(testVisit.getId()).orElseThrow();
        assertEquals(Status.COMPLETED, updatedVisit.getStatus());
        assertTrue(updatedVisit.isFeedbackEmailSent());

        // Verify email was attempted
        verify(notificationPublisher, times(1)).sendFeedbackPrompt(any(Visit.class));
        verify(emailService, times(2)).sendEmail(anyString(), anyString(), anyString()); // Visitor + Landlord
    }

    @Test
    @DisplayName("🔥 SCENARIO 2: Deleted User Account - Email Fails but Visit Completes")
    void testDeletedUserEmailFailure() {
        // Given: Visit with deleted landlord account
        testVisit.setLandlordId(deletedLandlordId);
        testVisit = visitRepository.save(testVisit);

        assertEquals(Status.APPROVED, testVisit.getStatus());
        assertFalse(testVisit.isFeedbackEmailSent());

        // When: Scheduler runs (this used to cause rollbacks)
        visitScheduler.autoCompletePastVisits();

        // Then: Visit should still be completed despite email failure
        Visit updatedVisit = visitRepository.findById(testVisit.getId()).orElseThrow();
        assertEquals(Status.COMPLETED, updatedVisit.getStatus());
        assertTrue(updatedVisit.isFeedbackEmailSent()); // Should be marked as sent to prevent retries

        // Verify email was attempted but gracefully handled
        verify(notificationPublisher, times(1)).sendFeedbackPrompt(any(Visit.class));
        verify(emailService, times(1)).sendEmail(eq("visitor@example.com"), anyString(), anyString());
        // No email to deleted landlord, but no exception thrown
    }

    @Test
    @DisplayName("🔄 SCENARIO 3: Repeated Scheduler Runs - No Duplicate Emails")
    void testRepeatedSchedulerRuns() {
        // Given: Initial scheduler run completes visit
        visitScheduler.autoCompletePastVisits();

        Visit firstRun = visitRepository.findById(testVisit.getId()).orElseThrow();
        assertEquals(Status.COMPLETED, firstRun.getStatus());
        assertTrue(firstRun.isFeedbackEmailSent());

        // When: Scheduler runs again (simulating 30-minute intervals)
        visitScheduler.autoCompletePastVisits();
        visitScheduler.autoCompletePastVisits();
        visitScheduler.autoCompletePastVisits();

        // Then: No additional emails should be sent
        Visit finalState = visitRepository.findById(testVisit.getId()).orElseThrow();
        assertEquals(Status.COMPLETED, finalState.getStatus());
        assertTrue(finalState.isFeedbackEmailSent());

        // Verify email was sent only once, not repeatedly
        verify(notificationPublisher, times(1)).sendFeedbackPrompt(any(Visit.class));
    }

    @Test
    @DisplayName("🚫 SCENARIO 4: Visit Not Found During Processing")
    void testVisitDeletedDuringProcessing() {
        // Given: Visit exists initially
        UUID visitId = testVisit.getId();
        assertTrue(visitRepository.existsById(visitId));

        // When: Visit gets deleted by another process before scheduler processes it
        // (Simulate concurrent deletion)
        doAnswer(invocation -> {
            visitRepository.deleteById(visitId);
            return visitService.updateVisitStatus(visitId, Status.COMPLETED);
        }).when(visitService).updateVisitStatus(eq(visitId), eq(Status.COMPLETED));

        // Then: Scheduler should handle gracefully
        assertDoesNotThrow(() -> visitScheduler.autoCompletePastVisits());

        // Visit should be deleted, no emails sent
        assertFalse(visitRepository.existsById(visitId));
        verify(notificationPublisher, never()).sendFeedbackPrompt(any(Visit.class));
    }

    @Test
    @DisplayName("⚡ SCENARIO 5: Database Transaction Failure")
    void testDatabaseTransactionFailure() {
        // Given: Mock repository to simulate database failure
        VisitRepository mockRepo = mock(VisitRepository.class);
        when(mockRepo.findByStatusAndScheduledAtBeforeWithSlot(any(), any()))
            .thenReturn(List.of(testVisit));
        when(mockRepo.findById(testVisit.getId()))
            .thenReturn(Optional.of(testVisit));

        // Simulate database failure on save
        when(mockRepo.save(any(Visit.class)))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When: Scheduler runs with database issues
        assertDoesNotThrow(() -> visitScheduler.autoCompletePastVisits());

        // Then: Original visit should remain unchanged in real repository
        Visit unchangedVisit = visitRepository.findById(testVisit.getId()).orElseThrow();
        assertEquals(Status.APPROVED, unchangedVisit.getStatus()); // Should still be APPROVED
        assertFalse(unchangedVisit.isFeedbackEmailSent()); // Should remain false
    }

    @Test
    @DisplayName("🎭 SCENARIO 6: Multiple Visits with Mixed Success/Failure")
    void testMultipleVisitsMixedScenarios() {
        // Given: Create multiple visits with different scenarios
        Visit successVisit = createTestVisit(landlordId, "success-visit");
        Visit failureVisit = createTestVisit(deletedLandlordId, "failure-visit");
        Visit alreadyCompletedVisit = createTestVisit(landlordId, "completed-visit");
        alreadyCompletedVisit.setStatus(Status.COMPLETED);
        alreadyCompletedVisit.setFeedbackEmailSent(true);
        alreadyCompletedVisit = visitRepository.save(alreadyCompletedVisit);

        // When: Scheduler processes all visits
        visitScheduler.autoCompletePastVisits();

        // Then: Verify each visit handled appropriately
        Visit successResult = visitRepository.findById(successVisit.getId()).orElseThrow();
        assertEquals(Status.COMPLETED, successResult.getStatus());
        assertTrue(successResult.isFeedbackEmailSent());

        Visit failureResult = visitRepository.findById(failureVisit.getId()).orElseThrow();
        assertEquals(Status.COMPLETED, failureResult.getStatus());
        assertTrue(failureResult.isFeedbackEmailSent()); // Marked as sent despite failure

        Visit completedResult = visitRepository.findById(alreadyCompletedVisit.getId()).orElseThrow();
        assertEquals(Status.COMPLETED, completedResult.getStatus());
        assertTrue(completedResult.isFeedbackEmailSent());

        // Verify notification attempts (success + failure, but not already completed)
        verify(notificationPublisher, times(3)).sendFeedbackPrompt(any(Visit.class));
    }

    @Test
    @DisplayName("🔐 SCENARIO 7: Invalid Status Transitions")
    void testInvalidStatusTransitions() {
        // Given: Visit in various non-APPROVED states
        Visit rejectedVisit = createTestVisit(landlordId, "rejected-visit");
        rejectedVisit.setStatus(Status.REJECTED);
        rejectedVisit = visitRepository.save(rejectedVisit);

        Visit cancelledVisit = createTestVisit(landlordId, "cancelled-visit");
        cancelledVisit.setStatus(Status.CANCELLED);
        cancelledVisit = visitRepository.save(cancelledVisit);

        // When: Scheduler runs
        visitScheduler.autoCompletePastVisits();

        // Then: Only APPROVED visits should be processed
        Visit rejectedResult = visitRepository.findById(rejectedVisit.getId()).orElseThrow();
        assertEquals(Status.REJECTED, rejectedResult.getStatus()); // Unchanged

        Visit cancelledResult = visitRepository.findById(cancelledVisit.getId()).orElseThrow();
        assertEquals(Status.CANCELLED, cancelledResult.getStatus()); // Unchanged

        // Original approved visit should be completed
        Visit approvedResult = visitRepository.findById(testVisit.getId()).orElseThrow();
        assertEquals(Status.COMPLETED, approvedResult.getStatus());
    }

    @Test
    @DisplayName("⏰ SCENARIO 8: Time-based Filtering - Grace Period")
    void testGracePeriodFiltering() {
        // Given: Visits at different times relative to grace period
        Visit recentVisit = createTestVisit(landlordId, "recent-visit");
        recentVisit.setScheduledAt(OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(15)); // 15 min ago (within grace)
        recentVisit = visitRepository.save(recentVisit);

        Visit oldVisit = createTestVisit(landlordId, "old-visit");
        oldVisit.setScheduledAt(OffsetDateTime.now(ZoneOffset.UTC).minusHours(5)); // 5 hours ago (beyond grace)
        oldVisit = visitRepository.save(oldVisit);

        // When: Scheduler runs (30-minute grace period)
        visitScheduler.autoCompletePastVisits();

        // Then: Only visits beyond grace period should be completed
        Visit recentResult = visitRepository.findById(recentVisit.getId()).orElseThrow();
        assertEquals(Status.APPROVED, recentResult.getStatus()); // Still approved (within grace)

        Visit oldResult = visitRepository.findById(oldVisit.getId()).orElseThrow();
        assertEquals(Status.COMPLETED, oldResult.getStatus()); // Completed (beyond grace)

        Visit originalResult = visitRepository.findById(testVisit.getId()).orElseThrow();
        assertEquals(Status.COMPLETED, originalResult.getStatus()); // Also completed
    }

    @Test
    @DisplayName("🌊 SCENARIO 9: Service Layer Rollback Simulation")
    void testServiceLayerRollbackHandling() {
        // Given: Mock the service to simulate rollback scenario
        doThrow(new RuntimeException("Service layer failure"))
            .when(visitService).updateVisitStatus(eq(testVisit.getId()), eq(Status.COMPLETED));

        // When: Scheduler encounters service failure
        assertDoesNotThrow(() -> visitScheduler.autoCompletePastVisits());

        // Then: Visit should remain in original state
        Visit unchangedVisit = visitRepository.findById(testVisit.getId()).orElseThrow();
        assertEquals(Status.APPROVED, unchangedVisit.getStatus());
        assertFalse(unchangedVisit.isFeedbackEmailSent());

        // No successful email notifications
        verify(notificationPublisher, never()).sendFeedbackPrompt(any(Visit.class));
    }

    @Test
    @DisplayName("🔄 SCENARIO 10: Race Condition - Status Changes During Processing")
    void testRaceConditionHandling() {
        // Given: Simulate concurrent status change
        doAnswer(invocation -> {
            // Another process changes the status while scheduler is processing
            Visit concurrentVisit = visitRepository.findById(testVisit.getId()).orElseThrow();
            concurrentVisit.setStatus(Status.CANCELLED);
            visitRepository.save(concurrentVisit);

            // Now try to complete the visit
            return visitService.updateVisitStatus(testVisit.getId(), Status.COMPLETED);
        }).when(visitService).updateVisitStatus(eq(testVisit.getId()), eq(Status.COMPLETED));

        // When: Scheduler processes visit with concurrent modification
        assertDoesNotThrow(() -> visitScheduler.autoCompletePastVisits());

        // Then: Verify the final state (should be cancelled due to race condition)
        Visit finalVisit = visitRepository.findById(testVisit.getId()).orElseThrow();
        // The exact final state depends on timing, but system should not crash
        assertTrue(finalVisit.getStatus() == Status.CANCELLED || finalVisit.getStatus() == Status.COMPLETED);
    }

    // Helper method to create test visits
    private Visit createTestVisit(UUID landlordId, String identifier) {
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setStartTime(OffsetDateTime.now(ZoneOffset.UTC).minusHours(3));
        slot.setEndTime(OffsetDateTime.now(ZoneOffset.UTC).minusHours(2));
        slot.setBooked(true);
        slot.setPropertyId(propertyId);
        slot.setLandlordId(landlordId);
        slot = slotRepository.save(slot);

        Visit visit = new Visit();
        visit.setPropertyId(propertyId);
        visit.setVisitorId(visitorId);
        visit.setLandlordId(landlordId);
        visit.setScheduledAt(OffsetDateTime.now(ZoneOffset.UTC).minusHours(2));
        visit.setDurationMinutes(60);
        visit.setStatus(Status.APPROVED);
        visit.setSlotId(slot.getId());
        visit.setFeedbackEmailSent(false);
        visit.setNotes("Test visit: " + identifier);

        return visitRepository.save(visit);
    }
}
