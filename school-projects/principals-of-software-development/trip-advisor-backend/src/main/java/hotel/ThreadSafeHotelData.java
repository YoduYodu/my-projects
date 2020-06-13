package hotel;

import models.Hotel;
import models.Review;
import models.TouristAttraction;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Class ThreadSafeHotelData - extends class HotelData (rename your class from project 1 as needed).
 * Thread-safe, uses ReentrantReadWriteLock to synchronize access to all data structures.
 */
public class ThreadSafeHotelData extends HotelData {

    private ReentrantReadWriteLock lock;

    /**
     * Default constructor.
     */
    public ThreadSafeHotelData() {
        super();
        lock = new ReentrantReadWriteLock();
    }

    /* Add an attraction. */
    public void addAttraction(String hotelId, String id, String name, double rating, String address) {
        lock.lockWrite();
        try {
            super.addAttraction(hotelId, id, name, rating, address);
        } finally {
            lock.unlockWrite();
        }
    }

    /**
     * Add attractions
     * @param hotelId
     * @param attractions
     */
    public void addAllAttractions(String hotelId, List<TouristAttraction> attractions) {
        lock.lockWrite();
        try {
            super.addAllAttractions(hotelId, attractions);
        } finally {
            lock.unlockWrite();
        }
    }

    /**
     * Get attractions
     * @param hotelId
     * @return a list of attractions
     */
    public List<TouristAttraction> getAttractions(String hotelId) {
        lock.lockRead();
        try {
            return super.getAttractions(hotelId);
        } finally {
            lock.unlockRead();
        }
    }

    /**
     * Overrides addHotel method from HotelData class to make it thread-safe; uses the lock.
     * Create a Hotel given the parameters, and add it to the appropriate data
     * structure(s).
     *
     * @param hotelId       - the id of the hotel
     * @param hotelName     - the name of the hotel
     * @param city          - the city where the hotel is located
     * @param state         - the state where the hotel is located.
     * @param streetAddress - the building number and the street
     * @param lat           latitude
     * @param lon           longitude
     */
    public void addHotel(String hotelId, String hotelName, String city, String state, String streetAddress, double lat,
                         double lon) {
        lock.lockWrite();
        try {
            super.addHotel(hotelId, hotelName, city, state, streetAddress, lat, lon);
        } finally {
            lock.unlockWrite();
        }
    }

    /**
     * Get a hotel
     * @param hotelId hotel id
     * @return hotel
     */
    public Hotel getHotel(String hotelId) {
        lock.lockRead();
        try {
            return super.getHotel(hotelId);
        } finally {
            lock.unlockRead();
        }
    }

    /**
     * Add a list of reviews
     * @param reviews a list of reviews
     */
    public void addReviews(List<Review> reviews) {
        lock.lockWrite();
        try {
            super.addReviews(reviews);
        } finally {
            lock.unlockWrite();
        }
    }

    /**
     * Overrides addReview method from HotelData class to make it thread-safe; uses the lock.
     *
     * @param hotelId     - the id of the hotel reviewed
     * @param reviewId    - the id of the review
     * @param rating      - integer rating 1-5.
     * @param reviewTitle - the title of the review
     * @param reviewText  - text of the review
     * @param isRecom     - whether the user recommends it or not
     * @param date        - date of the review
     * @param username    - the nickname of the user writing the review.
     * @return true if successful, false if unsuccessful because of invalid date
     * or rating. Needs to catch and handle the following exceptions:
     * ParseException if the date is invalid InvalidRatingException if
     * the rating is out of range
     */
    public boolean addReview(String hotelId, String reviewId, int rating, String reviewTitle, String reviewText,
                             boolean isRecom, String date, String username) {
        lock.lockWrite();
        try {
            return super.addReview(hotelId, reviewId, rating, reviewTitle, reviewText, isRecom, date, username);
        } finally {
            lock.unlockWrite();
        }
    }

    /**
     * Reviews getter
     * @param hotelId hotel id
     * @return Reviews
     */
    public Set<Review> getReviews(String hotelId) {
        lock.lockRead();
        try {
            return super.getReviews(hotelId);
        } finally {
            lock.unlockRead();
        }
    }

    /**
     * Overrides toString method of class HotelData to make it thread-safe.
     * Returns a string representing information about the hotel with the given
     * id, including all the reviews for this hotel separated by
     * --------------------
     * Format of the string: HotelName: hotelId
     * streetAddress city, state
     * --------------------
     * Review by username: rating
     * ReviewTitle ReviewText
     * --------------------
     * Review by username: rating
     * ReviewTitle ReviewText ...
     *
     * @param hotelId
     * @return - output string.
     */
    public String toString(String hotelId) {
        lock.lockRead();
        try {
            return super.toString(hotelId);
        } finally {
            lock.unlockRead();
        }
    }

    /**
     * Overrides the method printToFile of the parent class to make it thread-safe.
     * Save the string representation of the hotel data to the file specified by
     * filename in the following format: an empty line A line of 20 asterisks
     * ******************** on the next line information for each hotel, printed
     * in the format described in the toString method of this class.
     * <p>
     * The hotels should be sorted by hotel ids
     *
     * @param filename - Path specifying where to save the output.
     */
    public void printToFile(Path filename) {
        lock.lockRead();
        try {
            super.printToFile(filename);
        } finally {
            lock.unlockRead();
        }
    }

    /**
     * Overrides a method of the parent class to make it thread-safe.
     * Return an alphabetized list of the ids of all hotels
     *
     * @return an alphabetized list of the ids of all hotels
     */
    public List<String> getHotels() {
        lock.lockRead();
        try {
            return super.getHotels();
        } finally {
            lock.unlockRead();
        }
    }

    /**
     * Put area description
     * @param areaDescription
     */
    public boolean putAreaDescription(String hotelId, String areaDescription) {
        lock.lockWrite();
        try {
            return super.putAreaDescription(hotelId, areaDescription);
        } finally {
            lock.unlockWrite();
        }
    }

    /**
     * Put property description
     * @param propertyDescription
     */
    public boolean putPropertyDescription(String hotelId, String propertyDescription) {
        lock.lockWrite();
        try {
            return super.putPropertyDescription(hotelId, propertyDescription);
        } finally {
            lock.unlockWrite();
        }
    }

    /**
     * Get area description
     * @return area description
     */
    public String getAreaDescription(String hotelId) {
        lock.lockWrite();
        try {
            return super.getAreaDescription(hotelId);
        } finally {
            lock.unlockWrite();
        }
    }

    /**
     * Get property description
     * @return property description
     */
    public String getPropertyDescription(String hotelId) {
        lock.lockWrite();
        try {
            return super.getPropertyDescription(hotelId);
        } finally {
            lock.unlockWrite();
        }
    }

}