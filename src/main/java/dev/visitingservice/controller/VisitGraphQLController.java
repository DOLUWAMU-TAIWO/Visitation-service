package dev.visitingservice.controller;

import dev.visitingservice.model.Status;
import dev.visitingservice.model.Visit;
import dev.visitingservice.repository.VisitRepository;
import dev.visitingservice.service.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class VisitGraphQLController {

    private final VisitService visitService;
    private final VisitRepository visitRepository;

    @Autowired
    public VisitGraphQLController(VisitService visitService, VisitRepository visitRepository) {
        this.visitService = visitService;
        this.visitRepository = visitRepository;
    }

    @SchemaMapping(typeName = "Visit", field = "scheduledDate")
    public String scheduledDate(Visit visit) {
        if (visit.getScheduledAt() != null) {
            return visit.getScheduledAt().toString();
        }
        return "Not Scheduled";
    }

    @QueryMapping
    public Visit visit(@Argument String id) {
        return visitService.getVisit(UUID.fromString(id));
    }

    @QueryMapping
    public List<Visit> visits(@Argument Map<String, Object> filter) {
        if (filter == null || filter.isEmpty()) {
            return visitRepository.findAll();
        }

        if (filter.containsKey("status") && filter.containsKey("fromDate") && filter.containsKey("toDate")) {
            Status status = Status.valueOf((String) filter.get("status"));
            OffsetDateTime start = OffsetDateTime.parse((String) filter.get("fromDate"));
            OffsetDateTime end = OffsetDateTime.parse((String) filter.get("toDate"));
            return visitService.getVisitsByStatusWithin(status, start, end);
        }

        Integer page = filter.containsKey("page") ? (Integer) filter.get("page") : 0;
        Integer size = filter.containsKey("size") ? (Integer) filter.get("size") : 20;

        List<Visit> visits = visitRepository.findAll();
        return visits.stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
    }

    @QueryMapping
    public List<Visit> visitsByProperty(@Argument String propertyId) {
        return visitService.getVisitsByProperty(UUID.fromString(propertyId));
    }

    @QueryMapping
    public List<Visit> visitsByVisitor(@Argument String visitorId) {
        return visitService.getVisitsByVisitor(UUID.fromString(visitorId));
    }

    @QueryMapping
    public List<Visit> visitsByLandlord(@Argument String landlordId) {
        return visitService.getVisitsByLandlord(UUID.fromString(landlordId));
    }

    @MutationMapping
    public Visit requestVisit(@Argument Map<String, Object> input) {
        Visit visit = new Visit();
        visit.setVisitorId(UUID.fromString((String) input.get("visitorId")));
        visit.setLandlordId(UUID.fromString((String) input.get("landlordId")));
        visit.setPropertyId(UUID.fromString((String) input.get("propertyId")));

        if (input.containsKey("slotId")) {
            visit.setSlotId(UUID.fromString((String) input.get("slotId")));
        }

        if (input.containsKey("scheduledDate")) {
            visit.setScheduledAt(OffsetDateTime.parse((String) input.get("scheduledDate")));
        }

        if (input.containsKey("durationMinutes")) {
            visit.setDurationMinutes((Integer) input.get("durationMinutes"));
        }

        return visitService.requestVisit(visit);
    }

    @MutationMapping
    public Visit approveVisit(@Argument String id) {
        return visitService.approveVisit(UUID.fromString(id));
    }

    @MutationMapping
    public Visit rejectVisit(@Argument String id) {
        return visitService.rejectVisit(UUID.fromString(id));
    }

    @MutationMapping
    public Visit cancelVisit(@Argument String id) {
        return visitService.cancelVisit(UUID.fromString(id));
    }

    @MutationMapping
    public Visit completeVisit(@Argument String id) {
        return visitService.completeVisit(UUID.fromString(id));
    }
}
