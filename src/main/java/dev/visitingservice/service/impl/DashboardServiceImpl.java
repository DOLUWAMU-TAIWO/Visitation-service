package dev.visitingservice.service.impl;

import dev.visitingservice.dto.DashboardStatsDTO;
import dev.visitingservice.model.ShortletBooking;
import dev.visitingservice.model.ShortletBooking.BookingStatus;
import dev.visitingservice.model.Status;
import dev.visitingservice.model.Visit;
import dev.visitingservice.repository.ShortletBookingRepository;
import dev.visitingservice.repository.VisitRepository;
import dev.visitingservice.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final VisitRepository visitRepository;
    private final ShortletBookingRepository bookingRepository;

    @Autowired
    public DashboardServiceImpl(VisitRepository visitRepository, ShortletBookingRepository bookingRepository) {
        this.visitRepository = visitRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public DashboardStatsDTO getTenantStats(UUID tenantId) {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // Get all tenant visits
        List<Visit> allVisits = visitRepository.findByVisitorId(tenantId);

        // Get all tenant bookings
        List<ShortletBooking> allBookings = bookingRepository.findByTenantId(tenantId);

        // Visit Statistics
        stats.setTotalVisits(allVisits.size());
        stats.setPendingVisits(countVisitsByStatus(allVisits, Status.PENDING));
        stats.setUpcomingVisits(countUpcomingVisits(allVisits));

        // Visit breakdown stats
        Map<String, Integer> visitStats = new HashMap<>();
        visitStats.put("approved", countVisitsByStatus(allVisits, Status.APPROVED));
        visitStats.put("rejected", countVisitsByStatus(allVisits, Status.REJECTED));
        visitStats.put("completed", countVisitsByStatus(allVisits, Status.COMPLETED));
        visitStats.put("cancelled", countVisitsByStatus(allVisits, Status.CANCELLED));
        stats.setVisitStats(visitStats);

        // Booking Statistics
        stats.setTotalBookings(allBookings.size());
        stats.setActiveBookings(countActiveBookings(allBookings));

        // Booking breakdown stats
        Map<String, Integer> bookingStats = new HashMap<>();
        bookingStats.put("confirmed", countBookingsByStatus(allBookings, BookingStatus.ACCEPTED));
        bookingStats.put("pending", countBookingsByStatus(allBookings, BookingStatus.PENDING));
        bookingStats.put("completed", countCompletedBookings(allBookings));
        bookingStats.put("cancelled", countBookingsByStatus(allBookings, BookingStatus.CANCELLED));
        stats.setBookingStats(bookingStats);

        // Financial Stats
        stats.setTotalSpent(calculateTotalSpent(allBookings));

        // Upcoming dates
        stats.setNextVisitDate(getNextVisitDate(allVisits));
        stats.setNextPaymentDue(getNextPaymentDue(allBookings));

        // Favorites (placeholder - would need favorites repository)
        stats.setFavoriteProperties(0); // TODO: implement when favorites system is added

        return stats;
    }

    @Override
    public DashboardStatsDTO getLandlordStats(UUID landlordId) {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // Get all landlord visits
        List<Visit> allVisits = visitRepository.findByLandlordId(landlordId);

        // Get all landlord bookings
        List<ShortletBooking> allBookings = bookingRepository.findByLandlordId(landlordId);

        // Visit Statistics
        stats.setTotalVisits(allVisits.size());
        stats.setPendingVisits(countVisitsByStatus(allVisits, Status.PENDING));
        stats.setUpcomingVisits(countUpcomingVisits(allVisits));

        // Visit breakdown stats
        Map<String, Integer> visitStats = new HashMap<>();
        visitStats.put("approved", countVisitsByStatus(allVisits, Status.APPROVED));
        visitStats.put("rejected", countVisitsByStatus(allVisits, Status.REJECTED));
        visitStats.put("completed", countVisitsByStatus(allVisits, Status.COMPLETED));
        stats.setVisitStats(visitStats);

        // Booking Statistics
        stats.setTotalBookings(allBookings.size());
        stats.setActiveBookings(countActiveBookings(allBookings));

        // Booking breakdown stats
        Map<String, Integer> bookingStats = new HashMap<>();
        bookingStats.put("confirmed", countBookingsByStatus(allBookings, BookingStatus.ACCEPTED));
        bookingStats.put("pending", countBookingsByStatus(allBookings, BookingStatus.PENDING));
        bookingStats.put("completed", countCompletedBookings(allBookings));
        stats.setBookingStats(bookingStats);

        // Financial Stats
        stats.setTotalSpent(calculateTotalEarned(allBookings));

        return stats;
    }

    // Helper methods
    private int countVisitsByStatus(List<Visit> visits, Status status) {
        return (int) visits.stream()
                .filter(v -> v.getStatus() == status)
                .count();
    }

    private int countBookingsByStatus(List<ShortletBooking> bookings, BookingStatus status) {
        return (int) bookings.stream()
                .filter(b -> b.getStatus() == status)
                .count();
    }

    private int countUpcomingVisits(List<Visit> visits) {
        OffsetDateTime now = OffsetDateTime.now();
        return (int) visits.stream()
                .filter(v -> v.getScheduledAt() != null && v.getScheduledAt().isAfter(now))
                .filter(v -> v.getStatus() == Status.APPROVED)
                .count();
    }

    private int countActiveBookings(List<ShortletBooking> bookings) {
        LocalDate today = LocalDate.now();
        return (int) bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.ACCEPTED)
                .filter(b -> b.getEndDate().isAfter(today))
                .count();
    }

    private int countCompletedBookings(List<ShortletBooking> bookings) {
        LocalDate today = LocalDate.now();
        return (int) bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.ACCEPTED)
                .filter(b -> b.getEndDate().isBefore(today))
                .count();
    }

    private Double calculateTotalSpent(List<ShortletBooking> bookings) {
        return bookings.stream()
                .filter(b -> b.getPaymentAmount() != null)
                .filter(b -> b.getStatus() == BookingStatus.ACCEPTED)
                .mapToDouble(b -> b.getPaymentAmount().doubleValue())
                .sum();
    }

    private Double calculateTotalEarned(List<ShortletBooking> bookings) {
        return calculateTotalSpent(bookings); // Same calculation, different perspective
    }

    private LocalDate getNextVisitDate(List<Visit> visits) {
        OffsetDateTime now = OffsetDateTime.now();
        return visits.stream()
                .filter(v -> v.getScheduledAt() != null && v.getScheduledAt().isAfter(now))
                .filter(v -> v.getStatus() == Status.APPROVED)
                .map(v -> v.getScheduledAt().toLocalDate())
                .min(LocalDate::compareTo)
                .orElse(null);
    }

    private LocalDate getNextPaymentDue(List<ShortletBooking> bookings) {
        // For shortlet bookings, this could be the start date of upcoming bookings
        // or payment due dates if we had that field
        LocalDate today = LocalDate.now();
        return bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.ACCEPTED)
                .filter(b -> b.getStartDate().isAfter(today))
                .map(ShortletBooking::getStartDate)
                .min(LocalDate::compareTo)
                .orElse(null);
    }
}
