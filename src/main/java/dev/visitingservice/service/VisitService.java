package dev.visitingservice.service;

import dev.visitingservice.model.Visit;
import dev.visitingservice.model.Status;

import java.util.List;
import java.util.UUID;
import java.time.OffsetDateTime;

public interface VisitService {
    Visit requestVisit(Visit visit);
    Visit approveVisit(UUID visitId);
    Visit rejectVisit(UUID visitId);
    Visit cancelVisit(UUID visitId);
    Visit completeVisit(UUID visitId);
    List<Visit> getVisitsByProperty(UUID propertyId);
    List<Visit> getVisitsByVisitor(UUID visitorId);
    List<Visit> getVisitsByLandlord(UUID landlordId);
    Visit getVisit(UUID visitId);
    Visit updateVisitStatus(UUID visitId, Status newStatus);
    List<Visit> getVisitsByStatusWithin(Status status, OffsetDateTime start, OffsetDateTime end);
}
