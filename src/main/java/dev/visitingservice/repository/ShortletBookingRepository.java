package dev.visitingservice.repository;

import dev.visitingservice.model.ShortletBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShortletBookingRepository extends JpaRepository<ShortletBooking, UUID> {
    List<ShortletBooking> findByLandlordId(UUID landlordId);

    // Methods for calendar view and filtering
    List<ShortletBooking> findByPropertyIdAndStartDateGreaterThanEqualAndEndDateLessThanEqual(UUID propertyId, LocalDate startDate, LocalDate endDate);

    // Methods for booking history and filtering
    List<ShortletBooking> findByTenantId(UUID tenantId);
    List<ShortletBooking> findByStatus(ShortletBooking.BookingStatus status);
    List<ShortletBooking> findByTenantIdAndStatus(UUID tenantId, ShortletBooking.BookingStatus status);
    List<ShortletBooking> findByLandlordIdAndStatus(UUID landlordId, ShortletBooking.BookingStatus status);

    // Methods for date filtering
    List<ShortletBooking> findByStartDateGreaterThanEqual(LocalDate startDate);
    List<ShortletBooking> findByEndDateLessThanEqual(LocalDate endDate);
    List<ShortletBooking> findByStartDateGreaterThanEqualAndEndDateLessThanEqual(LocalDate startDate, LocalDate endDate);

    // Combined filters
    List<ShortletBooking> findByTenantIdAndStatusAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
            UUID tenantId, ShortletBooking.BookingStatus status, LocalDate startDate, LocalDate endDate);
    List<ShortletBooking> findByLandlordIdAndStatusAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
            UUID landlordId, ShortletBooking.BookingStatus status, LocalDate startDate, LocalDate endDate);
}
