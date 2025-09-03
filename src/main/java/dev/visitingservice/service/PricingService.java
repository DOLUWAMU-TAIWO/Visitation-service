package dev.visitingservice.service;

import dev.visitingservice.dto.ListingDto;
import dev.visitingservice.client.ListingGraphQLClient;
import dev.visitingservice.dto.PriceBreakdownDTO;
import dev.visitingservice.dto.PriceQuoteRequestDTO;
import dev.visitingservice.dto.PriceQuoteResponseDTO;
import dev.visitingservice.exception.InvalidRequestException;
import dev.visitingservice.exception.ExternalServiceException;
import dev.visitingservice.model.PricingConfig;
import dev.visitingservice.repository.PricingConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class PricingService {

    private static final int DECIMAL_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Autowired
    private PricingConfigRepository pricingConfigRepository;

    @Autowired
    private ListingGraphQLClient listingGraphQLClient;

    public PriceQuoteResponseDTO generatePriceQuote(PriceQuoteRequestDTO request) {
        // Validate request
        validatePriceQuoteRequest(request);

        // Fetch listing details
        ListingDto listing = fetchListingDetails(request.getListingId());

        // Get pricing configuration
        PricingConfig config = getPricingConfig();

        // Calculate nights
        int nights = calculateNights(request.getCheckInDate(), request.getCheckOutDate());

        // Calculate pricing breakdown
        PriceBreakdownDTO breakdown = calculatePriceBreakdown(
            listing,
            request,
            nights,
            config
        );

        // Generate quote ID
        String quoteId = generateQuoteId();

        // Calculate quote validity
        OffsetDateTime priceValidUntil = OffsetDateTime.now()
            .plusHours(config.getQuoteValidityHours());

        return new PriceQuoteResponseDTO(
            quoteId,
            nights,
            config.getCurrency(),
            breakdown,
            priceValidUntil
        );
    }

    private void validatePriceQuoteRequest(PriceQuoteRequestDTO request) {
        if (request.getCheckInDate().isAfter(request.getCheckOutDate()) ||
            request.getCheckInDate().isEqual(request.getCheckOutDate())) {
            throw new InvalidRequestException("Check-out date must be after check-in date");
        }

        if (request.getCheckInDate().isBefore(LocalDate.now())) {
            throw new InvalidRequestException("Check-in date cannot be in the past");
        }

        if (request.getGuests() <= 0) {
            throw new InvalidRequestException("Number of guests must be greater than 0");
        }
    }

    private ListingDto fetchListingDetails(UUID listingId) {
        try {
            ListingDto listing = listingGraphQLClient.getListingById(listingId.toString());
            if (listing == null) {
                throw new InvalidRequestException("Listing not found: " + listingId);
            }

            // Validate listing is available for short-term rental using purpose field
            if (listing.getPurpose() == null || !"SHORT_STAY".equals(listing.getPurpose())) {
                throw new InvalidRequestException("This listing is not available for short-term rental. Current purpose: " + listing.getPurpose());
            }

            return listing;
        } catch (Exception e) {
            throw new ExternalServiceException("Failed to fetch listing details: " + e.getMessage());
        }
    }

    private PricingConfig getPricingConfig() {
        return pricingConfigRepository.findActiveDefaultConfig()
            .orElseThrow(() -> new InvalidRequestException("No active pricing configuration found"));
    }

    private int calculateNights(LocalDate checkIn, LocalDate checkOut) {
        return (int) ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    private void validateBookingConstraints(ListingDto listing, PriceQuoteRequestDTO request, int nights) {
        // Validate minimum nights requirement
        if (listing.getMinNights() != null && nights < listing.getMinNights()) {
            throw new InvalidRequestException(
                String.format("Minimum stay is %d nights, requested: %d nights",
                    listing.getMinNights(), nights));
        }

        // Validate maximum nights requirement
        if (listing.getMaxNights() != null && nights > listing.getMaxNights()) {
            throw new InvalidRequestException(
                String.format("Maximum stay is %d nights, requested: %d nights",
                    listing.getMaxNights(), nights));
        }

        // Validate guest capacity
        if (listing.getGuestCapacity() != null && request.getGuests() > listing.getGuestCapacity()) {
            throw new InvalidRequestException(
                String.format("Maximum guest capacity is %d, requested: %d guests",
                    listing.getGuestCapacity(), request.getGuests()));
        }
    }

    private PriceBreakdownDTO calculatePriceBreakdown(
            ListingDto listing,
            PriceQuoteRequestDTO request,
            int nights,
            PricingConfig config) {

        // Validate booking constraints first
        validateBookingConstraints(listing, request, nights);

        // Determine the base price per night - prioritize listing's pricePerNight, fallback to price
        BigDecimal pricePerNight = listing.getPricePerNight() != null ?
            listing.getPricePerNight() :
            (listing.getPrice() != null ? listing.getPrice() : BigDecimal.ZERO);

        if (pricePerNight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("No valid pricing found for this listing");
        }

        // Base price calculation with seasonal and weekend adjustments
        BigDecimal basePrice = calculateBasePrice(
            pricePerNight,
            nights,
            request.getCheckInDate(),
            request.getCheckOutDate(),
            config,
            listing
        );

        // Extra guest fees (use listing capacity if available)
        BigDecimal extraGuestFees = calculateExtraGuestFees(
            request.getGuests(),
            nights,
            config,
            listing.getGuestCapacity()
        );

        // Add extra guest fees to base price
        BigDecimal adjustedBasePrice = basePrice.add(extraGuestFees);

        // Cleaning fee calculation - prioritize listing's cleaning fee
        BigDecimal cleaningFee = calculateCleaningFee(request.getGuests(), config, listing);

        // Security deposit from listing
        BigDecimal securityDeposit = listing.getSecurityAmount() != null ?
            listing.getSecurityAmount() : BigDecimal.ZERO;

        // Service fee calculation (percentage of base + cleaning, NOT including security deposit)
        BigDecimal serviceFee = calculateServiceFee(adjustedBasePrice, cleaningFee, config);

        // Tax calculation (on base + service + cleaning, NOT including security deposit)
        BigDecimal subtotal = adjustedBasePrice.add(cleaningFee).add(serviceFee);
        BigDecimal taxes = calculateTaxes(subtotal, config);

        // Total calculation (including security deposit)
        BigDecimal total = subtotal.add(taxes).add(securityDeposit);

        return new PriceBreakdownDTO(
            adjustedBasePrice.setScale(DECIMAL_SCALE, ROUNDING_MODE),
            cleaningFee.setScale(DECIMAL_SCALE, ROUNDING_MODE),
            serviceFee.setScale(DECIMAL_SCALE, ROUNDING_MODE),
            taxes.setScale(DECIMAL_SCALE, ROUNDING_MODE),
            securityDeposit.setScale(DECIMAL_SCALE, ROUNDING_MODE),
            total.setScale(DECIMAL_SCALE, ROUNDING_MODE)
        );
    }

    private BigDecimal calculateBasePrice(
            BigDecimal listingPricePerNight,
            int nights,
            LocalDate checkIn,
            LocalDate checkOut,
            PricingConfig config,
            ListingDto listing) {

        BigDecimal totalBasePrice = BigDecimal.ZERO;

        // Calculate price for each night with potential multipliers
        LocalDate currentDate = checkIn;
        while (currentDate.isBefore(checkOut)) {
            BigDecimal nightPrice = listingPricePerNight;

            // Apply weekend multiplier if applicable
            if (isWeekend(currentDate) && config.getWeekendMultiplier() != null) {
                nightPrice = nightPrice.multiply(config.getWeekendMultiplier());
            }

            // Apply peak season multiplier if applicable
            if (isPeakSeason(currentDate) && config.getPeakSeasonMultiplier() != null) {
                nightPrice = nightPrice.multiply(config.getPeakSeasonMultiplier());
            }

            // Apply property type based pricing adjustments
            nightPrice = applyPropertyTypeMultiplier(nightPrice, listing.getPropertyType());

            totalBasePrice = totalBasePrice.add(nightPrice);
            currentDate = currentDate.plusDays(1);
        }

        return totalBasePrice.setScale(DECIMAL_SCALE, ROUNDING_MODE);
    }

    private BigDecimal calculateExtraGuestFees(int guests, int nights, PricingConfig config, Integer listingCapacity) {
        // Use listing's guest capacity if available, otherwise use config default
        int maxGuestsBaseRate = listingCapacity != null && listingCapacity > 0 ?
            Math.min(listingCapacity, config.getMaxGuestsBaseRate()) :
            config.getMaxGuestsBaseRate();

        if (guests <= maxGuestsBaseRate) {
            return BigDecimal.ZERO;
        }

        int extraGuests = guests - maxGuestsBaseRate;
        BigDecimal extraGuestFeeTotal = config.getExtraGuestFee()
            .multiply(BigDecimal.valueOf(extraGuests))
            .multiply(BigDecimal.valueOf(nights));

        return extraGuestFeeTotal.setScale(DECIMAL_SCALE, ROUNDING_MODE);
    }

    private BigDecimal calculateCleaningFee(int guests, PricingConfig config, ListingDto listing) {
        // Prioritize listing's cleaning fee if available
        if (listing.getCleaningFee() != null && listing.getCleaningFee().compareTo(BigDecimal.ZERO) > 0) {
            return listing.getCleaningFee().setScale(DECIMAL_SCALE, ROUNDING_MODE);
        }

        // Fallback to config-based calculation
        BigDecimal cleaningFee = config.getCleaningFeeFixed();

        // Add per-guest cleaning fee if applicable
        if (config.getCleaningFeePerGuest() != null && guests > config.getMaxGuestsBaseRate()) {
            int extraGuests = guests - config.getMaxGuestsBaseRate();
            BigDecimal extraCleaningFee = config.getCleaningFeePerGuest()
                .multiply(BigDecimal.valueOf(extraGuests));
            cleaningFee = cleaningFee.add(extraCleaningFee);
        }

        return cleaningFee.setScale(DECIMAL_SCALE, ROUNDING_MODE);
    }

    private BigDecimal applyPropertyTypeMultiplier(BigDecimal basePrice, String propertyType) {
        // Apply property type specific pricing adjustments
        if (propertyType == null) return basePrice;

        return switch (propertyType.toUpperCase()) {
            case "VILLA", "PENTHOUSE", "MANSION" -> basePrice.multiply(new BigDecimal("1.3")); // 30% premium
            case "APARTMENT", "FLAT" -> basePrice.multiply(new BigDecimal("1.0")); // No change
            case "STUDIO", "ROOM" -> basePrice.multiply(new BigDecimal("0.8")); // 20% discount
            case "HOUSE", "DUPLEX" -> basePrice.multiply(new BigDecimal("1.1")); // 10% premium
            default -> basePrice; // No adjustment for other types
        };
    }

    private BigDecimal calculateServiceFee(BigDecimal basePrice, BigDecimal cleaningFee, PricingConfig config) {
        BigDecimal serviceFeeBase = basePrice.add(cleaningFee);
        return serviceFeeBase.multiply(config.getServiceFeePercentage())
            .setScale(DECIMAL_SCALE, ROUNDING_MODE);
    }

    private BigDecimal calculateTaxes(BigDecimal subtotal, PricingConfig config) {
        return subtotal.multiply(config.getTaxPercentage())
            .setScale(DECIMAL_SCALE, ROUNDING_MODE);
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    private boolean isPeakSeason(LocalDate date) {
        // Define peak seasons for Nigeria (Christmas/New Year, Easter)
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        // December (Christmas/New Year period)
        if (month == 12 && day >= 20) return true;

        // January (New Year period)
        return month == 1 && day <= 7;
    }

    private String generateQuoteId() {
        return "q_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
