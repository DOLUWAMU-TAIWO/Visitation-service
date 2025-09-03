package dev.visitingservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "pricing_config")
public class PricingConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "config_name", unique = true, nullable = false)
    private String configName;

    @Column(name = "service_fee_percentage", precision = 5, scale = 4, nullable = false)
    private BigDecimal serviceFeePercentage; // e.g., 0.0500 for 5%

    @Column(name = "tax_percentage", precision = 5, scale = 4, nullable = false)
    private BigDecimal taxPercentage; // e.g., 0.0750 for 7.5%

    @Column(name = "cleaning_fee_fixed", precision = 15, scale = 2)
    private BigDecimal cleaningFeeFixed; // Fixed cleaning fee

    @Column(name = "cleaning_fee_per_guest", precision = 15, scale = 2)
    private BigDecimal cleaningFeePerGuest; // Additional fee per guest

    @Column(name = "max_guests_base_rate", nullable = false)
    private Integer maxGuestsBaseRate; // Max guests included in base price

    @Column(name = "extra_guest_fee", precision = 15, scale = 2)
    private BigDecimal extraGuestFee; // Fee per additional guest

    @Column(name = "weekend_multiplier", precision = 5, scale = 4)
    private BigDecimal weekendMultiplier; // e.g., 1.2000 for 20% weekend markup

    @Column(name = "peak_season_multiplier", precision = 5, scale = 4)
    private BigDecimal peakSeasonMultiplier;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "NGN";

    @Column(name = "quote_validity_hours", nullable = false)
    private Integer quoteValidityHours = 24; // How long quote remains valid

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // Constructors
    public PricingConfig() {}

    public PricingConfig(String configName) {
        this.configName = configName;
        // Set default values for Nigerian market
        this.serviceFeePercentage = new BigDecimal("0.0500"); // 5%
        this.taxPercentage = new BigDecimal("0.0750"); // 7.5% VAT
        this.cleaningFeeFixed = new BigDecimal("50000.00"); // ₦50,000
        this.cleaningFeePerGuest = new BigDecimal("10000.00"); // ₦10,000 per extra guest
        this.maxGuestsBaseRate = 2;
        this.extraGuestFee = new BigDecimal("25000.00"); // ₦25,000 per extra guest
        this.weekendMultiplier = new BigDecimal("1.2000"); // 20% weekend markup
        this.peakSeasonMultiplier = new BigDecimal("1.5000"); // 50% peak season markup
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getConfigName() { return configName; }
    public void setConfigName(String configName) { this.configName = configName; }

    public BigDecimal getServiceFeePercentage() { return serviceFeePercentage; }
    public void setServiceFeePercentage(BigDecimal serviceFeePercentage) { this.serviceFeePercentage = serviceFeePercentage; }

    public BigDecimal getTaxPercentage() { return taxPercentage; }
    public void setTaxPercentage(BigDecimal taxPercentage) { this.taxPercentage = taxPercentage; }

    public BigDecimal getCleaningFeeFixed() { return cleaningFeeFixed; }
    public void setCleaningFeeFixed(BigDecimal cleaningFeeFixed) { this.cleaningFeeFixed = cleaningFeeFixed; }

    public BigDecimal getCleaningFeePerGuest() { return cleaningFeePerGuest; }
    public void setCleaningFeePerGuest(BigDecimal cleaningFeePerGuest) { this.cleaningFeePerGuest = cleaningFeePerGuest; }

    public Integer getMaxGuestsBaseRate() { return maxGuestsBaseRate; }
    public void setMaxGuestsBaseRate(Integer maxGuestsBaseRate) { this.maxGuestsBaseRate = maxGuestsBaseRate; }

    public BigDecimal getExtraGuestFee() { return extraGuestFee; }
    public void setExtraGuestFee(BigDecimal extraGuestFee) { this.extraGuestFee = extraGuestFee; }

    public BigDecimal getWeekendMultiplier() { return weekendMultiplier; }
    public void setWeekendMultiplier(BigDecimal weekendMultiplier) { this.weekendMultiplier = weekendMultiplier; }

    public BigDecimal getPeakSeasonMultiplier() { return peakSeasonMultiplier; }
    public void setPeakSeasonMultiplier(BigDecimal peakSeasonMultiplier) { this.peakSeasonMultiplier = peakSeasonMultiplier; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Integer getQuoteValidityHours() { return quoteValidityHours; }
    public void setQuoteValidityHours(Integer quoteValidityHours) { this.quoteValidityHours = quoteValidityHours; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
