package dev.visitingservice.dto;

import java.math.BigDecimal;

public class PriceBreakdownDTO {

    private BigDecimal basePrice;
    private BigDecimal cleaningFee;
    private BigDecimal serviceFee;
    private BigDecimal taxes;
    private BigDecimal securityDeposit; // Add security deposit
    private BigDecimal total;

    public PriceBreakdownDTO() {}

    public PriceBreakdownDTO(BigDecimal basePrice, BigDecimal cleaningFee,
                             BigDecimal serviceFee, BigDecimal taxes,
                             BigDecimal securityDeposit, BigDecimal total) {
        this.basePrice = basePrice;
        this.cleaningFee = cleaningFee;
        this.serviceFee = serviceFee;
        this.taxes = taxes;
        this.securityDeposit = securityDeposit;
        this.total = total;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getCleaningFee() {
        return cleaningFee;
    }

    public void setCleaningFee(BigDecimal cleaningFee) {
        this.cleaningFee = cleaningFee;
    }

    public BigDecimal getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(BigDecimal serviceFee) {
        this.serviceFee = serviceFee;
    }

    public BigDecimal getTaxes() {
        return taxes;
    }

    public void setTaxes(BigDecimal taxes) {
        this.taxes = taxes;
    }

    public BigDecimal getSecurityDeposit() {
        return securityDeposit;
    }

    public void setSecurityDeposit(BigDecimal securityDeposit) {
        this.securityDeposit = securityDeposit;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}