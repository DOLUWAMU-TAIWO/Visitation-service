package dev.visitingservice.dto;

import java.time.LocalDate;
import java.util.Map;

public class DashboardStatsDTO {
    private Integer totalVisits;
    private Integer pendingVisits;
    private Integer upcomingVisits;
    private Integer totalBookings;
    private Integer activeBookings;
    private Integer favoriteProperties;
    private Double totalSpent;
    private LocalDate nextPaymentDue;
    private LocalDate nextVisitDate;
    private Map<String, Integer> visitStats;
    private Map<String, Integer> bookingStats;

    // Constructors
    public DashboardStatsDTO() {}

    public DashboardStatsDTO(Integer totalVisits, Integer pendingVisits, Integer upcomingVisits,
                           Integer totalBookings, Integer activeBookings, Integer favoriteProperties,
                           Double totalSpent, LocalDate nextPaymentDue, LocalDate nextVisitDate,
                           Map<String, Integer> visitStats, Map<String, Integer> bookingStats) {
        this.totalVisits = totalVisits;
        this.pendingVisits = pendingVisits;
        this.upcomingVisits = upcomingVisits;
        this.totalBookings = totalBookings;
        this.activeBookings = activeBookings;
        this.favoriteProperties = favoriteProperties;
        this.totalSpent = totalSpent;
        this.nextPaymentDue = nextPaymentDue;
        this.nextVisitDate = nextVisitDate;
        this.visitStats = visitStats;
        this.bookingStats = bookingStats;
    }

    // Getters and Setters
    public Integer getTotalVisits() { return totalVisits; }
    public void setTotalVisits(Integer totalVisits) { this.totalVisits = totalVisits; }

    public Integer getPendingVisits() { return pendingVisits; }
    public void setPendingVisits(Integer pendingVisits) { this.pendingVisits = pendingVisits; }

    public Integer getUpcomingVisits() { return upcomingVisits; }
    public void setUpcomingVisits(Integer upcomingVisits) { this.upcomingVisits = upcomingVisits; }

    public Integer getTotalBookings() { return totalBookings; }
    public void setTotalBookings(Integer totalBookings) { this.totalBookings = totalBookings; }

    public Integer getActiveBookings() { return activeBookings; }
    public void setActiveBookings(Integer activeBookings) { this.activeBookings = activeBookings; }

    public Integer getFavoriteProperties() { return favoriteProperties; }
    public void setFavoriteProperties(Integer favoriteProperties) { this.favoriteProperties = favoriteProperties; }

    public Double getTotalSpent() { return totalSpent; }
    public void setTotalSpent(Double totalSpent) { this.totalSpent = totalSpent; }

    public LocalDate getNextPaymentDue() { return nextPaymentDue; }
    public void setNextPaymentDue(LocalDate nextPaymentDue) { this.nextPaymentDue = nextPaymentDue; }

    public LocalDate getNextVisitDate() { return nextVisitDate; }
    public void setNextVisitDate(LocalDate nextVisitDate) { this.nextVisitDate = nextVisitDate; }

    public Map<String, Integer> getVisitStats() { return visitStats; }
    public void setVisitStats(Map<String, Integer> visitStats) { this.visitStats = visitStats; }

    public Map<String, Integer> getBookingStats() { return bookingStats; }
    public void setBookingStats(Map<String, Integer> bookingStats) { this.bookingStats = bookingStats; }

    @Override
    public String toString() {
        return "DashboardStatsDTO{" +
                "totalVisits=" + totalVisits +
                ", pendingVisits=" + pendingVisits +
                ", upcomingVisits=" + upcomingVisits +
                ", totalBookings=" + totalBookings +
                ", activeBookings=" + activeBookings +
                ", favoriteProperties=" + favoriteProperties +
                ", totalSpent=" + totalSpent +
                ", nextPaymentDue=" + nextPaymentDue +
                ", nextVisitDate=" + nextVisitDate +
                ", visitStats=" + visitStats +
                ", bookingStats=" + bookingStats +
                '}';
    }
}
