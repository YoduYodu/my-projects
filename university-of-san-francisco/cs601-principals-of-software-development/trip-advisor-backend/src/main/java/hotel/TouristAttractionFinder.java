package hotel;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import models.Hotel;
import models.TouristAttraction;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Class responsible for getting tourist attractions near each hotel from the Google Places API.
 *  Also scrapes some data about hotels from Expedia html web page.
 */
public class TouristAttractionFinder {
    private static TouristAttractionFinder singleton = new TouristAttractionFinder();;
    private static final String CONFIG_PATH = "input/config.json";

    SSLSocketFactory singleFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

    private String host, path, key;
    private ThreadSafeHotelData hdata;

    /* Constructor for TouristAttractionFinder */
    private TouristAttractionFinder() {
        this.hdata = new ThreadSafeHotelData();
        HotelDataBuilder builder = new HotelDataBuilder(hdata);
        builder.loadHotelInfo("input/hotels.json");
        readConfig();
    }

    /* Singleton */
    public static TouristAttractionFinder getInstance() {
        if (singleton == null) {
            synchronized (TouristAttractionFinder.class) {
                if (singleton == null) {
                    singleton = new TouristAttractionFinder();
                }
            }
        }
        return singleton;
    }

    /**
     * Read config from config file.
     */
    private void readConfig() {
        try {
            JsonObject root = new JsonParser().parse(new FileReader(CONFIG_PATH)).getAsJsonObject();
            host = root.get("host").getAsString();
            path = root.get("path").getAsString();
            key = root.get("apikey").getAsString();
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find config file in TouristAttractionFinder");
        }
    }

    /**
     * Creates a secure socket to communicate with Google Places API server,
     * sends a GET request (to find attractions close to
     * the hotel within a given radius), and gets a response as a string.
     * Removes headers from the response string and parses the remaining json to
     * get Attractions info. Adds attractions to the corresponding data structure that supports
     * efficient search for tourist attractions given the hotel id.
     *
     */
    public void fetchAttractions(int radiusInMiles) {
        try {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            for (String hotelId : hdata.getHotels()) {
                JsonObject root = fetchHotelAttractions(radiusInMiles, factory, hotelId);
                if (root == null) continue;

                JsonArray results = root.get("results").getAsJsonArray();
                List<TouristAttraction> attractions = new ArrayList<>();
                for (JsonElement element : results) {
                    JsonObject result = element.getAsJsonObject();
                    System.out.println(result);
                    TouristAttraction attraction = new TouristAttraction(result.get("id").getAsString(),
                            result.get("name").getAsString(),
                            result.get("rating").getAsDouble(),
                            result.get("formatted_address").getAsString());
                    attractions.add(attraction);
                }
                hdata.addAllAttractions(hotelId, attractions);
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Creates a secure socket to communicate with Google Places API server,
     * sends a GET request (to find attractions close to
     * the hotel within a given radius), and gets a response as a string.
     * Removes headers from the response string and parses the remaining json to
     * get Attractions info. Adds attractions to the corresponding data structure that supports
     * efficient search for tourist attractions given the hotel id.
     *
     */
    public JsonObject fetchOneHotelAttractions(int radiusInMiles, String hotelId) {
        try {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            return fetchHotelAttractions(radiusInMiles, singleFactory, hotelId);
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }
    }

    /**
     * Fetch attractions for a single hotel
     * @param radiusInMiles radius
     * @return Response from Google Map API
     */
    public JsonObject fetchHotelAttractions(int radiusInMiles, SSLSocketFactory factory, String hotelId) throws IOException {
        SSLSocket socket = (SSLSocket) factory.createSocket(host, 443);

        // Generate request from hotel
        Hotel hotel = hdata.getHotel(hotelId);
        String query = "tourist+attractions+in+" + String.join("", hotel.getCity().split(" "));
        String location = String.format("%f,%f", hotel.getLatitude(), hotel.getLongitude());
        String request = getRequest(this.host, this.path, query, location, milesToMeters(radiusInMiles), key);

        // Send the request via socket
        PrintWriter requestWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        requestWriter.println(request);
        requestWriter.flush();

        // Get response and add attractions
        StringBuilder sb = new StringBuilder();
        BufferedReader response = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line;
        while ((line = response.readLine()) != null) {
            sb.append(line);
        }

        // Parse the Json response
        Pattern pattern = Pattern.compile("Connection:\\sclose(.*)");
        Matcher matcher = pattern.matcher(sb.toString());
        if (!matcher.find()) {
            return null;
        }
        String jsonResponse = matcher.group(1);
        JsonReader jsonReader = new JsonReader(new StringReader(jsonResponse));
        jsonReader.setLenient(true);

        return new JsonParser().parse(jsonReader).getAsJsonObject();
    }

    /**
     * Convert miles to meters
     * @param radiusInMiles
     * @return
     */
    private double milesToMeters(int radiusInMiles) {
        return 1609.34 * radiusInMiles;
    }

    /**
     * Generate request string
     * @param host
     * @param query
     * @param key
     * @return request string
     */
    private static String getRequest(String host, String path, String query, String location, double radius, String key) {
        return "GET " + path + "?" + "query=" + query + "&location=" + location + "&radius=" + radius + "&key=" + key + " HTTP/1.1" + System.lineSeparator()
                + "Host: " + host + System.lineSeparator()
                + "Connection: close" + System.lineSeparator();
    }

    /** Print attractions near the hotels to a file.
     * The format is described in the project description.
     *
     * @param filename
     */
    public void printAttractions(Path filename) {
        try (PrintWriter writer = new PrintWriter(filename.toFile())) {
            for (String hotelId : hdata.getHotels()) {
                String hotelName = hdata.getHotel(hotelId).getHotelName();
                writer.println(String.format("Attractions near %s, %s", hotelName, hotelId));

                List<TouristAttraction> attractions = hdata.getAttractions(hotelId);
                if (attractions != null) {
                    for (TouristAttraction attraction : attractions) {
                        writer.println(attraction.getName());
                    }
                }
                writer.println("++++++++++++++++++++");
            }
            writer.flush();
        } catch (IOException e) {
            System.out.println("Cannot write to " + filename.toString());
        }
    }
    /**
     * Takes an html file from expedia for a particular hotelId, and scrapes it for some data about this hotel:
     * About this area and About this property descriptions. Stores this information in ThreadSafeHotelData so that
     * we are able to efficiently access it given the hotel Id.
     * @param filename
     */
    public void parseHTML(String hotelId, Path filename) throws FileNotFoundException {
        Pattern pattern = Pattern.compile(".*?<h3 class=\"uitk-type-heading-600 uitk-type-bold\">About this area</h3>.*?<h4 class=\"location-title uitk-type-heading-500\" data-stid=\"whats-around-neighborhood\">(.*?)</h4>.*?<p class=\"uitk-type-paragraph-300\">(.*?)</p>.*?<h3 class=\"property-description__title policies__header--primary uitk-type-heading-600 uitk-type-bold all-b-padding-two\">About this property</h3>.*?<h4 class=\"uitk-type-heading-500\">(.*?)</h4>.*?<p class=\"uitk-type-paragraph-300\">(.*?)</p>.*");
        StringBuilder sb = new StringBuilder();

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filename.toFile()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            Matcher matcher = pattern.matcher(sb.toString());
            matcher.find();
            hdata.putPropertyDescription(hotelId, matcher.group(1) + System.lineSeparator() + matcher.group(2));
            hdata.putAreaDescription(hotelId, matcher.group(3) + System.lineSeparator() + matcher.group(4));
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            System.out.println("Reader cannot readLine");
        }

    }

    /** Prints property descriptions and area descriptions for each hotel from
     * the ThreadSafeHotelData to the given file. Format specified in the project description.
     * @param filename output file
     */
    public void printDescriptions(Path filename) {
        try (PrintWriter writer = new PrintWriter(filename.toFile())) {
            for (String hotelId : hdata.getHotels()) {
                writer.println(hotelId);
                writer.println(hdata.getPropertyDescription(hotelId));
                writer.println();
                writer.println(hdata.getAreaDescription(hotelId));
                writer.println("++++++++++++++++++++");
            }
            writer.flush();
        } catch (IOException e) {
            System.out
                    .println("Cannot write to " + filename.toString());
        }
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        TouristAttractionFinder finder = TouristAttractionFinder.getInstance();
        System.out.println(finder.fetchOneHotelAttractions(1, "25622"));

    }
}
