package database;

import hotel.HotelDataBuilder;
import hotel.ThreadSafeHotelData;
import models.Hotel;
import models.Review;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Set;

public class Driver {
    private ThreadSafeHotelData threadSafeHotelData;

    /* Constructor */
    public Driver() {
        this.threadSafeHotelData = new ThreadSafeHotelData();
        HotelDataBuilder builder = new HotelDataBuilder(threadSafeHotelData);
        builder.loadHotelInfo("input/hotels.json");
        builder.loadReviews(Path.of("input/reviews"));
    }

    /* ---------- users ---------- */

    /* DROP and CREATE a new TABLE users */
    public void initialUsers() {
        try {
            DataBaseHandler dataBaseHandler = DataBaseHandler.getInstance();
            try {
                dataBaseHandler.dropTableUsers();
            } catch (SQLException e) {
                System.out.println("TABLE users might not exist");
            }
            dataBaseHandler.createTableUsers();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }

    /* ---------- hotels ---------- */

    /* Get hotel */
    public boolean getHotel() {
        Hotel result = threadSafeHotelData.getHotel("25622");
        return result != null;
    }

    /* Get reviews */
    public boolean getReviews() {
        Set<Review> result = threadSafeHotelData.getReviews("25622");
        return result != null;
    }

    /* Migrate hotels to database */
    public void migrateHotels() {
        try {
            DataBaseHandler dataBaseHandler = DataBaseHandler.getInstance();
            try {
                dataBaseHandler.dropTableHotels();
            } catch (SQLException e) {
                System.out.println("TABLE hotels may not exist");
            }
            dataBaseHandler.createTableHotels();
            for (String hotelId : threadSafeHotelData.getHotels()) {
                double averageRating = getAverageRating(hotelId);
                Hotel hotel = threadSafeHotelData.getHotel(hotelId);
                dataBaseHandler.insertIntoHotels(
                    hotelId,
                    hotel.getHotelName(),
                    averageRating,
                    hotel.getStreetAddress(),
                    hotel.getCity(),
                    hotel.getState(),
                    hotel.getLatitude(),
                    hotel.getLongitude()
                );
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }

    /**
     * Get average of a hotel
     * @param hotelId hotel id
     * @return average rating
     */
    private double getAverageRating(String hotelId) {
        double sum = 0.0;
        int count = 0;
        Set<Review> reviews = threadSafeHotelData.getReviews(hotelId);
        if (reviews == null) return 0;

        for (Review review : reviews) {
            sum += review.getAverageRating();
            count++;
        }
        return sum / count;
    }

    /* ---------- reviews ---------- */

    /* Migrate reviews */
    public void migrateReviews() {
        try {
            DataBaseHandler dataBaseHandler = DataBaseHandler.getInstance();
            try {
                dataBaseHandler.dropTableReviews();
            } catch (SQLException e) {
                System.out.println("TABLE reviews may not exist");
            }
            dataBaseHandler.createTableReviews();
            for (String hotelId : threadSafeHotelData.getHotels()) {
                Set<Review> reviews = threadSafeHotelData.getReviews(hotelId);
                if (reviews == null) continue;
                for (Review review : reviews) {
                    dataBaseHandler.insertIntoReviews(
                        review.getHotelId(),
                        review.getReviewId(),
                        review.getAverageRating(),
                        review.getReviewTitle(),
                        review.getReviewText(),
                        review.getUserNickname(),
                        review.getReviewSubmissionTime(),
                        review.getIsRecommend()
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }


    public static void main(String[] args) {
        Driver driver = new Driver();
        driver.migrateReviews();
    }
}
