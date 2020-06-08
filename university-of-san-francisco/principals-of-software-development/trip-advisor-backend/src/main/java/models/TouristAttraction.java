package models;

/**
 * Tourist attraction
 */
public class TouristAttraction {
    private String name, address, id;
    private double rating;
    private static final String toStringFormat = "Tourist Attraction:" + System.lineSeparator()
            + "Id: %s" + System.lineSeparator()
            + "Name: %s" + System.lineSeparator()
            + "Rating: %f" + System.lineSeparator()
            + "Address: %s";

    /** Constructor for TouristAttraction
     *
     * @param id id of the attraction
     * @param name name of the attraction
     * @param rating overall rating of the attraction
     * @param address address of the attraction
     */
    public TouristAttraction(String id, String name, double rating, String address) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.address = address;
    }

    /**
     * Name getter
     * @return name
     */
    public String getName() {
        return name;
    }

    /** toString() method
     * @return a String representing this TouristAttraction
     */
    @Override
    public String toString() {
        return String.format(toStringFormat, this.id, this.name, this.rating, this.address);
    }
}

