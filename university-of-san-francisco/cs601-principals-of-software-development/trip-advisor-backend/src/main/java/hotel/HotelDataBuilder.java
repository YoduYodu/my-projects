package hotel;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.Review;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Class HotelDataBuilder. Loads hotel info from input files to ThreadSafeHotelData (using multithreading). */
public class HotelDataBuilder {
    private ThreadSafeHotelData hdata; // the "big" ThreadSafeHotelData that will contain all hotel and reviews info
    private ExecutorService executor;
    private static Logger log = LogManager.getRootLogger();

    /** Constructor for class HotelDataBuilder.
     *  @param data */
    public HotelDataBuilder(ThreadSafeHotelData data) {
        hdata = data;
        executor = Executors.newFixedThreadPool(1);
    }

    /** Constructor for class HotelDataBuilder that takes ThreadSafeHotelData and
     * the number of threads to create as a parameter.
     * @param data
     * @param numThreads
     */
    public HotelDataBuilder(ThreadSafeHotelData data, int numThreads) {
        hdata = data;
        executor = Executors.newFixedThreadPool(numThreads);
    }


    /**
     * Read the json file with information about the hotels and load it into the
     * appropriate data structure(s).
     * @param jsonFilename
     */
    public void loadHotelInfo(String jsonFilename) {
        log.info("Start logging hotels");
        try {
            JsonElement root = new JsonParser().parse(new FileReader(jsonFilename));
            JsonArray hotelsJsonArray = root.getAsJsonObject().get("sr").getAsJsonArray();
            for (JsonElement hotelJsonElement : hotelsJsonArray) {
                JsonObject hotelObject = hotelJsonElement.getAsJsonObject();
                JsonObject latitudeLongitudeObject = hotelObject.get("ll").getAsJsonObject();
                double latitude = latitudeLongitudeObject.get("lat").getAsDouble();
                double longitude = latitudeLongitudeObject.get("lng").getAsDouble();
                String hotelId = hotelObject.get("id").getAsString();
                String hotelName = hotelObject.get("f").getAsString();
                String city = hotelObject.get("ci").getAsString();
                String state = hotelObject.get("pr").getAsString();
                String address = hotelObject.get("ad").getAsString();

                hdata.addHotel(hotelId, hotelName, city, state, address, latitude, longitude);
            }
            log.info("Successfully logged hotels");
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
        }

    }

    /** Loads reviews from json files. Recursively processes subfolders.
     *  Each json file with reviews should be processed concurrently (you need to create a new runnable job for each
     *  json file that you encounter)
     *  @param dir
     */
    public void loadReviews(Path dir) {
        log.info("Start logging reviews");
        loadReviewsHelper(dir);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(15000, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
            log.info(String.format("Successfully loaded reviews file: %s", dir));
        } catch (InterruptedException ex) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            log.error(ex.getMessage());
        }
    }

    /**
     * loadReviews helper
     * @param dir
     */
    private void loadReviewsHelper(Path dir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    loadReviewsHelper(entry);
                } else {
                    executor.submit(new LoadReviewWorker(entry));
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Loads a review file from a Json file.
     * @param filename
     */
    private void loadReview(Path filename) {
        try {
            JsonElement root = new JsonParser().parse(new FileReader(filename.toFile()));
            JsonArray reviewsJsonArray = root.getAsJsonObject()
                    .get("reviewDetails").getAsJsonObject()
                    .get("reviewCollection").getAsJsonObject()
                    .get("review").getAsJsonArray();

            // Add reviews to temporary list, then write the list to hdata all at once
            List<Review> tempReviewList = new ArrayList<>();
            for (JsonElement reviewJsonElement : reviewsJsonArray) {
                JsonObject reviewObject = reviewJsonElement.getAsJsonObject();
                try {
                    tempReviewList.add(new Review(reviewObject.get("hotelId").getAsString(),
                            reviewObject.get("reviewId").getAsString(),
                            reviewObject.get("ratingOverall").getAsInt(),
                            reviewObject.get("title").getAsString(),
                            reviewObject.get("reviewText").getAsString(),
                            reviewObject.get("userNickname").getAsString(),
                            reviewObject.get("reviewSubmissionTime").getAsString(),
                            reviewObject.get("isRecommended").getAsString().equals("YES")));
                } catch (InvalidRatingException | ParseException e) {
                    log.error(String.format("Review with id %s failed, invalid rating or date", reviewObject.get("reviewId").getAsString()));
                }
            }
            log.info(String.format("Successfully load review file %s, now write the list to hdata", filename.toString()));
            hdata.addReviews(tempReviewList);
        } catch (FileNotFoundException e) {
            log.error("Cannot find file" + filename);
        }
    }

    /** Prints all hotel info to the file. Calls hdata's printToFile method. */
    public void printToFile(Path filename) {
        log.info("Start printing to file");
        hdata.printToFile(filename);
        log.info("Successfully printed to file");
    }

    /**
     * A Runnable task for executor.
     */
    private class LoadReviewWorker implements Runnable{
        private Path entry;

        private LoadReviewWorker(Path entry) {
            this.entry = entry;
        }

        @Override
        public void run() {
            loadReview(entry);
        }
    }

}
