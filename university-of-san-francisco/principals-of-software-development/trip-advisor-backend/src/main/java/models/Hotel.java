package models;

/**
 * Hotel model
 */
public class Hotel {
    private String hotelId, hotelName, city, state, streetAddress, areaDescription, propertyDescription;
    private double averageRating, latitude, longitude;

    /**
     * Constructor
     * @param hotelId
     * @param hotelName
     * @param city
     * @param state
     * @param streetAddress
     * @param latitude
     * @param longitude
     */
    public Hotel(String hotelId, String hotelName, double averageRating, String city, String state, String streetAddress, double latitude, double longitude) {
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.averageRating = averageRating;
        this.city = city;
        this.state = state;
        this.streetAddress = streetAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /* Alternative builder */
    public Hotel(String hotelId, String hotelName, String city, String state, String streetAddress, double latitude, double longitude) {
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.city = city;
        this.state = state;
        this.streetAddress = streetAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /* Get averageRating */
    public double getAverageRating() {
        return this.averageRating;
    }

    /**
     * Name getter
     * @return name
     */
    public String getHotelName() {
        return this.hotelName;
    }

    /**
     * Street address getter
     * @return address getter
     */
    public String getStreetAddress() {
        return streetAddress;
    }

    /**
     * City getter
     * @return city
     */
    public String getCity() {
        return city;
    }

    /**
     * State getter
     * @return state
     */
    public String getState() {
        return state;
    }

    /**
     * Id getter
     * @return id
     */
    public String getHotelId() {
        return this.hotelId;
    }

    /**
     * Latitude getter
     * @return latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Longitude getter
     * @return Longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Readable string
     * @return Readable string
     */
    @Override
    public String toString() {
        return "{" +
                "name='" + hotelName + '\'' +
                ", id=" + hotelId +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", address=" + streetAddress +
                '}';
    }

    /**
     * Only get name and id for displaying
     * @return name and id
     */
    public String getNameAndId() {
        return "{hotelId: " + this.getHotelId() + " name: " + this.getHotelName() + "} ";
    }

    /**
     * Put area description
     * @param areaDescription
     */
    public void putAreaDescription(String areaDescription) {
        this.areaDescription = areaDescription;
    }

    /**
     * Put property description
     * @param propertyDescription
     */
    public void putPropertyDescription(String propertyDescription) {
        this.propertyDescription = propertyDescription;
    }

    /**
     * Get area description
     * @return area description
     */
    public String getAreaDescription() {
        return areaDescription == null ? "No description" : areaDescription;
    }

    /**
     * Get property description
     * @return property description
     */
    public String getPropertyDescription() {
        return propertyDescription == null ? "No description" : propertyDescription;
    }
}