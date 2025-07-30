package dev.visitingservice.dto;

public class AddressDto {
    private String streetNumber;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String neighborhood;
    private String sublocality;
    private String formattedAddress;
    private String placeId;

    public String getStreetNumber() { return streetNumber; }
    public void setStreetNumber(String streetNumber) { this.streetNumber = streetNumber; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getNeighborhood() { return neighborhood; }
    public void setNeighborhood(String neighborhood) { this.neighborhood = neighborhood; }
    public String getSublocality() { return sublocality; }
    public void setSublocality(String sublocality) { this.sublocality = sublocality; }
    public String getFormattedAddress() { return formattedAddress; }
    public void setFormattedAddress(String formattedAddress) { this.formattedAddress = formattedAddress; }
    public String getPlaceId() { return placeId; }
    public void setPlaceId(String placeId) { this.placeId = placeId; }

    @Override
    public String toString() {
        return "AddressDto{" +
                "streetNumber='" + streetNumber + '\'' +
                ", street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", country='" + country + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", neighborhood='" + neighborhood + '\'' +
                ", sublocality='" + sublocality + '\'' +
                ", formattedAddress='" + formattedAddress + '\'' +
                ", placeId='" + placeId + '\'' +
                '}';
    }
}

