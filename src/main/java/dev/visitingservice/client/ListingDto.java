package dev.visitingservice.client;

import java.util.UUID;
import java.math.BigDecimal;

public class ListingDto {
    private UUID id;
    private UUID ownerId;
    private String title;
    private String description;
    private BigDecimal price;
    private ListingStatus status;

    // Enhanced pricing-related fields from GraphQL schema
    private BigDecimal pricePerNight;
    private BigDecimal cleaningFee;
    private BigDecimal securityAmount;
    private Integer guestCapacity;
    private Integer minNights;
    private Integer maxNights;
    private String cancellationPolicy;
    private Boolean instantBooking;
    private String purpose;
    private Boolean isShortlet;
    private String propertyType;
    private Integer bedrooms;
    private Integer bathrooms;
    private String furnishingStatus;
    private java.util.List<String> amenities;
    private java.util.List<String> imageKeys;
    private dev.visitingservice.dto.AddressDto address;

    // Existing getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public ListingStatus getStatus() { return status; }
    public void setStatus(ListingStatus status) { this.status = status; }

    // New pricing-related getters and setters
    public BigDecimal getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }

    public BigDecimal getCleaningFee() { return cleaningFee; }
    public void setCleaningFee(BigDecimal cleaningFee) { this.cleaningFee = cleaningFee; }

    public BigDecimal getSecurityAmount() { return securityAmount; }
    public void setSecurityAmount(BigDecimal securityAmount) { this.securityAmount = securityAmount; }

    public Integer getGuestCapacity() { return guestCapacity; }
    public void setGuestCapacity(Integer guestCapacity) { this.guestCapacity = guestCapacity; }

    public Integer getMinNights() { return minNights; }
    public void setMinNights(Integer minNights) { this.minNights = minNights; }

    public Integer getMaxNights() { return maxNights; }
    public void setMaxNights(Integer maxNights) { this.maxNights = maxNights; }

    public String getCancellationPolicy() { return cancellationPolicy; }
    public void setCancellationPolicy(String cancellationPolicy) { this.cancellationPolicy = cancellationPolicy; }

    public Boolean getInstantBooking() { return instantBooking; }
    public void setInstantBooking(Boolean instantBooking) { this.instantBooking = instantBooking; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public Boolean getIsShortlet() { return isShortlet; }
    public void setIsShortlet(Boolean isShortlet) { this.isShortlet = isShortlet; }

    public String getPropertyType() { return propertyType; }
    public void setPropertyType(String propertyType) { this.propertyType = propertyType; }

    public Integer getBedrooms() { return bedrooms; }
    public void setBedrooms(Integer bedrooms) { this.bedrooms = bedrooms; }

    public Integer getBathrooms() { return bathrooms; }
    public void setBathrooms(Integer bathrooms) { this.bathrooms = bathrooms; }

    public String getFurnishingStatus() { return furnishingStatus; }
    public void setFurnishingStatus(String furnishingStatus) { this.furnishingStatus = furnishingStatus; }

    public java.util.List<String> getAmenities() { return amenities; }
    public void setAmenities(java.util.List<String> amenities) { this.amenities = amenities; }

    public java.util.List<String> getImageKeys() { return imageKeys; }
    public void setImageKeys(java.util.List<String> imageKeys) { this.imageKeys = imageKeys; }

    public dev.visitingservice.dto.AddressDto getAddress() { return address; }
    public void setAddress(dev.visitingservice.dto.AddressDto address) { this.address = address; }
}
