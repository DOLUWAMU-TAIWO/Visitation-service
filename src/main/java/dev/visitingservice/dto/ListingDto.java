package dev.visitingservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class ListingDto {
    private UUID id;
    private String title;
    private String description;
    private BigDecimal price;
    private String status;
    private AddressDto address;
    private String propertyType;
    private Integer bedrooms;
    private Integer bathrooms;
    private String furnishingStatus;
    private java.util.List<String> amenities;
    private java.util.List<String> imageKeys;
    // Add more fields as needed for your use case

    public ListingDto() {}

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public AddressDto getAddress() {
        return address;
    }
    public void setAddress(AddressDto address) {
        this.address = address;
    }

    public String getPropertyType() {
        return propertyType;
    }
    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }
    public void setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }

    public Integer getBathrooms() {
        return bathrooms;
    }
    public void setBathrooms(Integer bathrooms) {
        this.bathrooms = bathrooms;
    }

    public String getFurnishingStatus() {
        return furnishingStatus;
    }
    public void setFurnishingStatus(String furnishingStatus) {
        this.furnishingStatus = furnishingStatus;
    }

    public java.util.List<String> getAmenities() {
        return amenities;
    }
    public void setAmenities(java.util.List<String> amenities) {
        this.amenities = amenities;
    }

    public java.util.List<String> getImageKeys() {
        return imageKeys;
    }
    public void setImageKeys(java.util.List<String> imageKeys) {
        this.imageKeys = imageKeys;
    }

    @Override
    public String toString() {
        return "ListingDto{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", status='" + status + '\'' +
                ", address=" + address +
                ", propertyType='" + propertyType + '\'' +
                ", bedrooms=" + bedrooms +
                ", bathrooms=" + bathrooms +
                ", furnishingStatus='" + furnishingStatus + '\'' +
                ", amenities=" + amenities +
                ", imageKeys=" + imageKeys +
                '}';
    }
}
