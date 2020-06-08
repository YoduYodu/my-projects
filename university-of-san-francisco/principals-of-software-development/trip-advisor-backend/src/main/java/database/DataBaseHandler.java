package database;

import hotel.InvalidRatingException;
import models.Hotel;
import models.Review;
import models.User;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/* Data base handler */
public class DataBaseHandler {
    private static DataBaseHandler singletonDataBaseHandler = null;
    private static String dbUsername, dbPassword, database, hostname, databaseURL;

    /* For singleton purpose */
    private  DataBaseHandler() {
        Properties properties = loadProperties();
        dbUsername = properties.getProperty("username");
        dbPassword = properties.getProperty("password");
        database = properties.getProperty("database");
        hostname = properties.getProperty("hostname");
        databaseURL = String.format("jdbc:mysql://%s/%s", hostname, database);
    }

    /* Load properties */
    private Properties loadProperties() {
        Properties properties = new Properties();
        try (FileReader fileReader = new FileReader("input/database.properties")) {
            properties.load(fileReader);
        } catch (IOException e) {
            System.out.println("Failed loading database.properties ");
            System.exit(1);
        }
        return properties;
    }

    /* Get singleton instance */
    public static DataBaseHandler getInstance() {
        if (singletonDataBaseHandler == null) {
            synchronized (DataBaseHandler.class) {
                if (singletonDataBaseHandler == null) {
                    singletonDataBaseHandler = new DataBaseHandler();
                }
            }
        }
        return singletonDataBaseHandler;
    }

    /* ---------- Table: users ---------- */

    /* Create table UsersServlet */
    synchronized public void createTableUsers() throws SQLException{
        try (Connection connection = DriverManager.getConnection(databaseURL, dbUsername, dbPassword)) {
            Statement statement = connection.createStatement();
            synchronized (this) {
                statement.executeUpdate(SQL.CREATE_TABLE_USERS);
            }
        } catch (SQLException e) {
            System.out.println("Failed to CREATE users TABLE");
            throw e;
        }
    }

    /* Insert user into users table */
    public void insertIntoUsers(String username, String salt, String password) throws SQLException{
        try (Connection connection = DriverManager.getConnection(databaseURL, dbUsername, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL.INSERT_INTO_USERS);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, salt);
            preparedStatement.setString(3, password);
            synchronized (this) {
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (SQLException e) {
            System.out.println("Failed to INSERT INTO users");
            throw e;
        }
    }

    /**
     * Select user by username
     * @param username username
     */
    public User selectFromUsers(String username) throws SQLException{
        try (Connection connection = DriverManager.getConnection(databaseURL, dbUsername, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL.SELECT_FROM_USERS);
            preparedStatement.setString(1, username);
            ResultSet resultSet;
            synchronized (this) {
                resultSet = preparedStatement.executeQuery();
            }
            if (resultSet.next()) {
                return new User(resultSet.getString("username"),
                        resultSet.getString("usersalt"),
                        resultSet.getString("password"));
            } else {
                return null;
            }

        } catch (SQLException e) {
            System.out.println("Failed to SELECT * FROM users");
            throw e;
        }
    }

    /* Drop the table users */
    public void dropTableUsers() throws SQLException{
        try (Connection connection = DriverManager.getConnection(databaseURL, dbUsername, dbPassword)) {
            Statement statement = connection.createStatement();
            synchronized (this) {
                statement.executeUpdate(SQL.DROP_TABLE_USERS);
            }
        } catch (SQLException e) {
            System.out.println("Failed to DROP TABLE users");
            throw e;
        }
    }

    /* ---------- Table: hotels ---------- */

    /* CREATE TABLE hotels */
    public void createTableHotels() throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseURL, dbUsername, dbPassword)) {
            Statement statement = connection.createStatement();
            synchronized (this) {
                statement.executeUpdate(SQL.CREATE_TABLE_HOTELS);
            }
        } catch (SQLException e) {
            System.out.println("Failed to CREATE TABLE hotels");
            throw e;
        }
    }

    /* DROP TABLE hotels */
    public void dropTableHotels() throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseURL, dbUsername, dbPassword)) {
            Statement statement = connection.createStatement();
            synchronized (this) {
                statement.executeUpdate(SQL.DROP_TABLE_HOTELS);
            }
        } catch (SQLException e) {
            System.out.println("Failed to DROP TABLE hotels");
            throw e;
        }
    }

    /* INSERT INTO hotels */
    public void insertIntoHotels(String hotelId, String hotelName, double averageRating,
                                 String address, String city, String state,
                                 double latitude, double longitude) throws SQLException{
        try (Connection connection = DriverManager.getConnection(databaseURL, dbUsername, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL.INSERT_INTO_HOTELS);
            preparedStatement.setString(1, hotelId);
            preparedStatement.setString(2, hotelName);
            preparedStatement.setDouble(3, averageRating);
            preparedStatement.setString(4, address);
            preparedStatement.setString(5, city);
            preparedStatement.setString(6, state);
            preparedStatement.setDouble(7, latitude);
            preparedStatement.setDouble(8, longitude);
            synchronized (this) {
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (SQLException e) {
            System.out.println("Failed to INSERT INTO hotels");
            throw e;
        }
    }

    /* Select FROM hotels */
    public List<Hotel> selectFromHotels(String city, String keyword) throws SQLException{
        try (Connection connection = DriverManager.getConnection(databaseURL, dbUsername, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL.SELECT_FROM_HOTELS);
            preparedStatement.setString(1, city);
            preparedStatement.setString(2, processKeyword(keyword));

            ResultSet resultSet;
            synchronized (this) {
                resultSet = preparedStatement.executeQuery();
            }
            List<Hotel> hotels = new ArrayList<>();
            while (resultSet.next()) {
                hotels.add(new Hotel(resultSet.getString("hotelId"),
                    resultSet.getString("hotelName"),
                    resultSet.getDouble("averageRating"),
                    resultSet.getString("city"),
                    resultSet.getString("state"),
                    resultSet.getString("address"),
                    resultSet.getDouble("latitude"),
                    resultSet.getDouble("longitude")));
            }
            return hotels;
        } catch (SQLException e) {
            System.out.println("Failed to select hotels");
            throw e;
        }
    }

    /* Select one hotel given hotelId */
    public Hotel selectOneHotel(String hotelId) throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseURL, dbUsername, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL.SELECT_ONE_HOTEL);
            preparedStatement.setString(1, hotelId);

            ResultSet resultSet;
            synchronized (this) {
                resultSet = preparedStatement.executeQuery();
            }

            resultSet.next();
            return new Hotel(resultSet.getString("hotelId"),
                resultSet.getString("hotelName"),
                resultSet.getDouble("averageRating"),
                resultSet.getString("city"),
                resultSet.getString("state"),
                resultSet.getString("address"),
                resultSet.getDouble("latitude"),
                resultSet.getDouble("longitude"));
        } catch (SQLException e) {
            System.out.println("Failed to select on hotel");
            throw e;
        }
    }

    /* Pre-process keyword */
    private String processKeyword(String keyword) {
        return "%" + keyword
            .replace("!", "!!")
            .replace("%", "!%")
            .replace("_", "!_")
            .replace("[", "![") + "%";
    }

    /* ---------- Table: likes ---------- */

    /* CREATE TABLE likes */
    public void createTableLikes() throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseURL, dbUsername, dbPassword)) {
            Statement statement = connection.createStatement();
            synchronized (this) {
                statement.executeUpdate(SQL.CREATE_TABLE_LIKES);
            }
        } catch (SQLException e) {
            System.out.println("Failed to CREATE TABLE likes");
            throw e;
        }
    }

    /* ---------- Table: reviews ---------- */

    /* CREATE TABLE reviews */
    public void createTableReviews() throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseURL, dbUsername, dbPassword)) {
            Statement statement = connection.createStatement();
            synchronized (this) {
                statement.executeUpdate(SQL.CREATE_TABLE_REVIEWS);
            }
        } catch (SQLException e) {
            System.out.println("Failed to CREATE TABLE reviews");
            throw e;
        }
    }

    /* INSERT INTO reviews */
    public void insertIntoReviews(String hotelId, String reviewId, int rating, String title, String text,
                                  String userNickName, Date submissionTime, boolean isRecommend) throws SQLException{
        try (Connection connection = DriverManager.getConnection(databaseURL, dbUsername, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL.INSERT_INTO_REVIEWS);
            preparedStatement.setString(1, hotelId);
            preparedStatement.setString(2, reviewId);
            preparedStatement.setInt(3, rating);
            preparedStatement.setString(4, title);
            preparedStatement.setString(5, text);
            preparedStatement.setString(6, userNickName);
            preparedStatement.setTimestamp(7, new Timestamp(submissionTime.getTime()));
            preparedStatement.setInt(8, isRecommend ? 1 : 0);
            synchronized (this) {
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (SQLException e) {
            System.out.println("Failed to INSERT INTO reviews");
            throw e;
        }
    }

    /* SELECT reviews */
    public List<Review> selectFromReviews(String hotelId) throws SQLException, InvalidRatingException, ParseException {
        try (Connection connection = DriverManager.getConnection(databaseURL, dbUsername, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL.SELECT_FROM_REVIEWS);
            preparedStatement.setString(1, hotelId);

            ResultSet resultSet;
            synchronized (this) {
                resultSet = preparedStatement.executeQuery();
            }
            List<Review> reviews = new ArrayList<>();
            while (resultSet.next()) {
                reviews.add(new Review(
                    resultSet.getString("hotelId"),
                    resultSet.getString("reviewId"),
                    resultSet.getInt("rating"),
                    resultSet.getString("title"),
                    resultSet.getString("text"),
                    resultSet.getString("userNickName"),
                    resultSet.getTimestamp("submissionTime").toString(),
                    resultSet.getInt("isRecommend") == 1
                ));
            }
            return reviews;
        } catch (SQLException | InvalidRatingException | ParseException e) {
            System.out.println("Failed to select hotels");
            throw e;
        }
    }

    /* DROP TABLE reviews */
    public void dropTableReviews() throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseURL, dbUsername, dbPassword)) {
            Statement statement = connection.createStatement();
            synchronized (this) {
                statement.executeUpdate(SQL.DROP_TABLE_REVIEWS);
            }
        } catch (SQLException e) {
            System.out.println("Failed to DROP TABLE hotels");
            throw e;
        }
    }

    /* ---------- Table: customReview ---------- */

    public void createTableCustomReview() throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseURL, dbUsername, dbPassword)) {
            Statement statement = connection.createStatement();
            synchronized (this) {
                statement.executeUpdate(SQL.CREATE_TABLE_CUSTOM_REVIEW);
            }
        } catch (SQLException e) {
            System.out.println("Failed to CREATE TABLE customReview");
            throw e;
        }
    }

    /* INSERT INTO reviews */
    public void insertIntoCustomReview(String username, String hotelId, String title, String text) throws SQLException{
        try (Connection connection = DriverManager.getConnection(databaseURL, dbUsername, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL.INSERT_INTO_CUSTOM_REVIEW);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, hotelId);
            preparedStatement.setString(3, title);
            preparedStatement.setString(4, text);
            synchronized (this) {
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (SQLException e) {
            System.out.println("Failed to INSERT INTO customReview");
            throw e;
        }
    }

    /* drop table */
    public void dropTableCustomReview() throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseURL, dbUsername, dbPassword)) {
            Statement statement = connection.createStatement();
            synchronized (this) {
                statement.executeUpdate(SQL.DROP_TABLE_CUSTOM_REVIEW);
            }
        } catch (SQLException e) {
            System.out.println("Failed to DROP TABLE customReview");
            throw e;
        }
    }

    /* SELECT reviews */
    public List<String> selectFromCustomReview(String username, String hotelId) throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseURL, dbUsername, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL.SELECT_FROM_CUSTOM_REVIEW);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, hotelId);

            ResultSet resultSet;
            synchronized (this) {
                resultSet = preparedStatement.executeQuery();
            }

            if (resultSet.next()) {
                List<String> result = new ArrayList<>();
                result.add(username);
                result.add(resultSet.getString("title"));
                result.add(resultSet.getString("text"));
                return result;
            } else {
                return null;
            }

        } catch (SQLException e) {
            System.out.println("Failed to select custom review");
            throw e;
        }
    }

    /* update table */
    public void updateTableCustomReview(String username, String hotelId, String title, String text) throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseURL, dbUsername, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL.UPDATE_CUSTOM_REVIEW);
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, text);
            preparedStatement.setString(3, username);
            preparedStatement.setString(4, hotelId);
            synchronized (this) {
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (SQLException e) {
            System.out.println("Failed to UPDATE TABLE customReview");
            throw e;
        }
    }

    /* delete custom review */
    public void deleteFromCustomReview(String username, String hotelId) throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseURL, dbUsername, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL.DELETE_FROM_CUSTOM_REVIEW);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, hotelId);
            synchronized (this) {
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (SQLException e) {
            System.out.println("Failed to DELETE FROM customReview");
            throw e;
        }
    }

    /* ---------- Table: login ---------- */

    /* INSERT INTO reviews */
    public void insertIntoLogin(String username) throws SQLException{
        try (Connection connection = DriverManager.getConnection(databaseURL, dbUsername, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL.INSERT_INTO_LOGIN);
            preparedStatement.setString(1, username);
            synchronized (this) {
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (SQLException e) {
            System.out.println("Failed to INSERT INTO login");
            throw e;
        }
    }

    public List<String> selectFromLogin(String username) throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseURL, dbUsername, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL.SELECT_FROM_LOGIN);
            preparedStatement.setString(1, username);

            ResultSet resultSet;
            synchronized (this) {
                resultSet = preparedStatement.executeQuery();
            }

            if (resultSet.next()) {
                List<String> result = new ArrayList<>();
                result.add(username);
                result.add(resultSet.getString("title"));
                result.add(resultSet.getString("text"));
                return result;
            } else {
                return null;
            }

        } catch (SQLException e) {
            System.out.println("Failed to select custom review");
            throw e;
        }
    }


    public static void main(String[] args) {
        try {
            DataBaseHandler dataBaseHandler = DataBaseHandler.getInstance();
//            dataBaseHandler.dropTableUsers();
//            dataBaseHandler.createTableUsers();
//            dataBaseHandler.insertIntoUsers(new User("tong21", "a good salt", "mima"));
//            System.out.println(dataBaseHandler.selectFromUsers("tong221"));

            /* hotels */
//            dataBaseHandler.createTableHotels();
//            dataBaseHandler.dropTableHotels();
//            dataBaseHandler.insertIntoHotels("1231", "Big Hotel", 3.21334123, "744 44th Ave", "San Francisco", "CA");
//            System.out.println(dataBaseHandler.selectFromHotels("San Francisco", "beach"));
//            System.out.println(dataBaseHandler.selectOneHotel("5875"));

            /* likes */
//            dataBaseHandler.createTableLikes();

            /* Reviews */
//            dataBaseHandler.dropTableReviews();
//            dataBaseHandler.createTableReviews();
//            dataBaseHandler.insertIntoReviews("hotelId", "reviewId1", 3, "title", "text", "OK", new Date(), false);
//            System.out.println(dataBaseHandler.selectFromReviews("6271901"));

            /* CustomReview */
            dataBaseHandler.dropTableCustomReview();
            dataBaseHandler.createTableCustomReview();
//            dataBaseHandler.insertIntoCustomReview("tong", "its good", "not really");
//            dataBaseHandler.updateTableCustomReview("tong", "its bad", "really");
//            dataBaseHandler.deleteFromCustomReview("tong");
        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }
}
