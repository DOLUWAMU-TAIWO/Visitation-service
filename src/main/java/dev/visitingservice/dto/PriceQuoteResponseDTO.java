package dev.visitingservice.dto;

import java.time.OffsetDateTime;

public class PriceQuoteResponseDTO {

    private String quoteId;
    private Integer nights;
    private String currency;
    private PriceBreakdownDTO breakdown;
    private OffsetDateTime priceValidUntil;

    public PriceQuoteResponseDTO() {}

    public PriceQuoteResponseDTO(String quoteId, Integer nights, String currency,
                                PriceBreakdownDTO breakdown, OffsetDateTime priceValidUntil) {
        this.quoteId = quoteId;
        this.nights = nights;
        this.currency = currency;
        this.breakdown = breakdown;
        this.priceValidUntil = priceValidUntil;
    }

    public String getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
    }

    public Integer getNights() {
        return nights;
    }

    public void setNights(Integer nights) {
        this.nights = nights;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PriceBreakdownDTO getBreakdown() {
        return breakdown;
    }

    public void setBreakdown(PriceBreakdownDTO breakdown) {
        this.breakdown = breakdown;
    }

    public OffsetDateTime getPriceValidUntil() {
        return priceValidUntil;
    }

    public void setPriceValidUntil(OffsetDateTime priceValidUntil) {
        this.priceValidUntil = priceValidUntil;
    }
}

