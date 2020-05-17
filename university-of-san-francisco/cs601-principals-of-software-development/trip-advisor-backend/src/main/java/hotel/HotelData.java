package hotel;

import models.Hotel;
import models.Review;
import models.TouristAttraction;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** The main class for project 1.
 * The main function should take the following 4 command line arguments:
 * -hotelsForIdSearch hotelFile -reviews reviewsDirectory
 *
 * and read general information about the hotelsForIdSearch from the hotelFile (a JSON file)
 * and read hotel reviews from the json files in reviewsDirectory.
 * The data should be loaded into data structures that allow efficient search.
 * See Readme for details.
 *
 * You are expected to add other classes and methods to this project.
 */
public class HotelData {
    private Map<String, Hotel> hotelsForIdSearch; // hotelId : Hotel
    private Map<String, Set<Review>> reviews; // hotelId : Review
    private Map<String, List<TouristAttraction>> attractionsMap; // hotelId : List of attractions

    /* Constructor */
    public HotelData() {
        hotelsForIdSearch = new TreeMap<>();
        reviews = new HashMap<>();
        attractionsMap = new HashMap<>();
    }

    /* Add an attraction. */
    public void addAttraction(String hotelId, String id, String name, double rating, String address) {
        attractionsMap.putIfAbsent(hotelId, new ArrayList<>());
        attractionsMap.get(hotelId).add(new TouristAttraction(id, name, rating, address));
    }

    /* Add attractions */
    public void addAllAttractions(String hotelId, List<TouristAttraction> attractions) {
        attractionsMap.putIfAbsent(hotelId, new ArrayList<>());
        attractionsMap.get(hotelId).addAll(attractions);
    }

    /* Get attractions */
    public List<TouristAttraction> getAttractions(String hotelId) {
        return attractionsMap.get(hotelId);
    }

    /**
     * To display general information about the hotel with the given id
     *
     * @param hotelId hotel id
     */
    public Hotel getHotel(String hotelId) {
        return hotelsForIdSearch.get(hotelId);
    }

    /**
     * To add hotel into hotelsForIdSearch
     */
    public void addHotel(String hotelId, String hotelName, String city, String state, String streetAddress, double lat,
                         double lon) {
        hotelsForIdSearch.put(hotelId, new Hotel(hotelId, hotelName, city, state, streetAddress, lat, lon));
    }

    /**
     * Return an alphabetized list of the ids of all hotels
     *
     * @return an alphabetized list of the ids of all hotels
     */
    public List<String> getHotels() {
        return new ArrayList<>(hotelsForIdSearch.keySet());
    }

    /**
     * To display the reviews given hotel id
     *
     * @param hotelId reviews
     */
    public Set<Review> findReviews(String hotelId) {
        if (!reviews.containsKey(hotelId)) {
            return new TreeSet<>();
        } else {
            return reviews.get(hotelId);
        }
    }

    /**
     * Reviews getter
     * @param hotelId hotel id
     * @return Reviews
     */
    public Set<Review> getReviews(String hotelId) {
        return reviews.getOrDefault(hotelId, null);
    }

    /**
     * Add a list of reviews to memory
     * @param reviews a list of reviews
     * @return true if succeeded false otherwise
     */
    public void addReviews(List<Review> reviews) {
        for (Review review : reviews) {
            addReview(review.getHotelId(), review);
        }
    }

    /**
     * Add review
     * @param hotelId
     * @param reviewId
     * @param averageRating
     * @param reviewTitle
     * @param reviewText
     * @param isRecom
     * @param reviewSubmissionTime
     * @param username
     * @return true if succeed, false otherwise
     */
    public boolean addReview(String hotelId, String reviewId, int averageRating, String reviewTitle, String reviewText,
                             boolean isRecom, String reviewSubmissionTime, String username) {
        try {
            Review review = new Review(hotelId, reviewId, averageRating, reviewTitle, reviewText, username, reviewSubmissionTime, isRecom);
            addReview(hotelId, review);
            return true;
        } catch (InvalidRatingException | ParseException e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * To add review into reviews
     *
     * @param hotelId hotel id
     * @param review  review
     */
    public void addReview(String hotelId, Review review) {
        reviews.putIfAbsent(hotelId, new TreeSet<>());
        reviews.get(hotelId).add(review);
    }

    /**
     * Save the string representation of the hotel data to the file specified by
     * * filename in the following format: an empty line A line of 20 asterisks
     * * ******************** on the next line information for each hotel, printed
     * in the format described in the toString method of this class.
     * <p>
     * The hotels should be sorted by hotel ids
     *
     * @param filename - Path specifying where to save the output.
     */
    public void printToFile(Path filename) {
        try (PrintWriter writer = new PrintWriter(filename.toFile())) {
            for (String hotelId : getHotels()) {
                writer.print(System.lineSeparator() + "********************" + System.lineSeparator());
                writer.print(toString(hotelId));
            }
            writer.flush();
        } catch (IOException e) {
            System.out.println("Cannot write to file");
        }
    }

    /**
     * Returns a string representing information about the hotel with the given
     * id, including all the reviews for this hotel separated by
     * --------------------
     * Format of the string:
     * HoteName: hotelId
     * streetAddress
     * city, state
     * --------------------
     * Review by username on reviewDate
     * Rating: 4
     * ReviewTitle
     * ReviewText
     * --------------------
     * Review by username on reviewDate
     * Rating: 4
     * ReviewTitle
     * ReviewText
     *
     * @param hotelId
     * @return - output string.
     */
    public String toString(String hotelId) {
        StringBuilder stringBuilder = new StringBuilder();
        Hotel hotel = hotelsForIdSearch.get(hotelId);
        if (hotel == null) return stringBuilder.toString();

        stringBuilder.append(String.format("%s: %s" + System.lineSeparator() + "%s" + System.lineSeparator() + "%s, %s" + System.lineSeparator(),
                hotel.getHotelName(),
                hotel.getHotelId(),
                hotel.getStreetAddress(),
                hotel.getCity(),
                hotel.getState()));

        Set<Review> reviews = findReviews(hotelId);
        if (!reviews.isEmpty()) {
            String reviewFormat = "--------------------" + System.lineSeparator() + "Review by %s on %s" + System.lineSeparator() + "Rating: %d" + System.lineSeparator() + "%s" + System.lineSeparator() + "%s" + System.lineSeparator();
            for (Review review : reviews) {
                String userNickname = review.getUserNickname();
                if (userNickname.equals("")) userNickname = "Anonymous";
                stringBuilder.append(
                        String.format(reviewFormat, userNickname, review.getReviewSubmissionTime(), review.getAverageRating(), review.getReviewTitle(), review.getReviewText()));
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Put area description
     * @param areaDescription
     */
    public boolean putAreaDescription(String hotelId, String areaDescription) {
        Hotel hotel = hotelsForIdSearch.get(hotelId);
        if (hotel != null) {
            hotel.putAreaDescription(areaDescription);
            return true;
        }
        return false;
    }

    /**
     * Put property description
     * @param propertyDescription
     */
    public boolean putPropertyDescription(String hotelId, String propertyDescription) {
        Hotel hotel = hotelsForIdSearch.get(hotelId);
        if (hotel != null) {
            hotel.putPropertyDescription(propertyDescription);
            return true;
        }
        return false;
    }

    /**
     * Get area description
     * @return area description
     */
    public String getAreaDescription(String hotelId) {
        Hotel hotel = hotelsForIdSearch.get(hotelId);
        if (hotel != null) {
            return hotel.getAreaDescription();
        }
        return "No description";
    }

    /**
     * Get property description
     * @return property description
     */
    public String getPropertyDescription(String hotelId) {
        Hotel hotel = hotelsForIdSearch.get(hotelId);
        if (hotel != null) {
            return hotel.getPropertyDescription();
        }
        return "No description";
    }
}