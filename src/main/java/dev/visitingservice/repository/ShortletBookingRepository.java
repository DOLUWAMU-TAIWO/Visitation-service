package dev.visitingservice.repository;

import dev.visitingservice.model.ShortletBooking;
import dev.visitingservice.model.ShortletBooking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ShortletBookingRepository extends JpaRepository<ShortletBooking, UUID> {
    List<ShortletBooking> findByLandlordId(UUID landlordId);

    // Methods for calendar view and filtering
    List<ShortletBooking> findByPropertyIdAndStartDateGreaterThanEqualAndEndDateLessThanEqual(UUID propertyId, LocalDate startDate, LocalDate endDate);

    // Methods for booking history and filtering
    List<ShortletBooking> findByTenantId(UUID tenantId);
    List<ShortletBooking> findByStatus(BookingStatus status);
    List<ShortletBooking> findByTenantIdAndStatus(UUID tenantId, BookingStatus status);
    List<ShortletBooking> findByLandlordIdAndStatus(UUID landlordId, BookingStatus status);

    // Methods for date filtering
    List<ShortletBooking> findByStartDateGreaterThanEqual(LocalDate startDate);
    List<ShortletBooking> findByEndDateLessThanEqual(LocalDate endDate);
    List<ShortletBooking> findByStartDateGreaterThanEqualAndEndDateLessThanEqual(LocalDate startDate, LocalDate endDate);

    // Combined filters
    List<ShortletBooking> findByTenantIdAndStatusAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
            UUID tenantId, BookingStatus status, LocalDate startDate, LocalDate endDate);
    List<ShortletBooking> findByLandlordIdAndStatusAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
            UUID landlordId, BookingStatus status, LocalDate startDate, LocalDate endDate);

    @Query("SELECT DISTINCT b.propertyId FROM ShortletBooking b WHERE b.status = dev.visitingservice.model.ShortletBooking.BookingStatus.ACCEPTED AND b.startDate < :endDate AND b.endDate > :startDate")
    List<UUID> findAcceptedBookedPropertyIdsInRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Cleanup methods
    List<ShortletBooking> findByStatusAndStartDateBefore(BookingStatus status, LocalDate cutoffDate);
    List<ShortletBooking> findByStatusAndCreatedAtBefore(BookingStatus status, OffsetDateTime cutoffTime);
    long countByStatus(BookingStatus status);
    long countByStatusAndUpdatedAtAfter(BookingStatus status, OffsetDateTime after);

    @Query("SELECT b FROM ShortletBooking b WHERE (b.status = 'NO_SHOW' OR b.status = 'COMPLETED') AND b.endDate < :cutoffDate AND (b.reminder24hSent = true OR b.reminder5hSent = true OR b.reminder1hSent = true)")
    List<ShortletBooking> findCompletedBookingsWithReminderFlags(@Param("cutoffDate") LocalDate cutoffDate);

    // Additional method for optimized BookingScheduler
    List<ShortletBooking> findByStatusAndStartDate(BookingStatus status, LocalDate startDate);

    // Method for manual property-specific reminders
    List<ShortletBooking> findByPropertyIdAndLandlordIdAndStatusAndStartDateGreaterThanEqualAndStartDateLessThanEqual(
        UUID propertyId, UUID landlordId, BookingStatus status, LocalDate fromDate, LocalDate toDate);
}
