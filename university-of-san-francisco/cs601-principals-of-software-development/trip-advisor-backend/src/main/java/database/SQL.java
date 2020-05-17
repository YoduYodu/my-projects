package database;

/**
 * SQL
 * Note:
 * Be careful with , and spaces when adding/removing lines
 */
public class SQL {

    /* ---------- Table: users ---------- */

    public static String CREATE_TABLE_USERS =
        "CREATE TABLE users (" +
                "username NVARCHAR(32) NOT NULL PRIMARY KEY, " +
                "usersalt CHAR(32) NOT NULL, " +
                "password CHAR(64) NOT NULL" +
                ");";

    public static String INSERT_INTO_USERS =
        "INSERT INTO users (username, usersalt, password)" +
            "VALUES (?, ?, ?)";

    public static String SELECT_FROM_USERS =
        "SELECT username, usersalt, password " +
                "FROM users " +
                "WHERE username=?;";

    public static String DROP_TABLE_USERS = "DROP TABLE users";

    /* ---------- Table: hotels ---------- */

    public static String CREATE_TABLE_HOTELS =
        "CREATE TABLE hotels (" +
            "hotelId NVARCHAR(16) NOT NULL PRIMARY KEY, " +
            "hotelName NVARCHAR(128) NOT NULL, " +
            "averageRating DOUBLE(3,2) NOT NULL DEFAULT 0, " +
            "address NVARCHAR(128) NOT NULL, " +
            "city NVARCHAR(128) NOT NULL, " +
            "state NVARCHAR(128) NOT NULL, " +
            "latitude DOUBLE NOT NULL, " +
            "longitude DOUBLE NOT NULL" +
            ");";

    public static String INSERT_INTO_HOTELS =
        "INSERT INTO hotels (hotelId, hotelName, averageRating, address, city, state, latitude, longitude)" +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    public static String SELECT_FROM_HOTELS =
        "SELECT * FROM hotels " +
            "WHERE city=? " +
            "AND hotelName LIKE ?;";

    public static String SELECT_ONE_HOTEL =
        "SELECT * FROM hotels " +
            "WHERE hotelId = ?;";

    public static String DROP_TABLE_HOTELS = "DROP TABLE hotels";

    /* ---------- Table: reviews ---------- */

    public static String CREATE_TABLE_REVIEWS =
        "CREATE TABLE reviews (" +
            "hotelId NVARCHAR(16) NOT NULL, " +
            "reviewId NVARCHAR(32) NOT NULL PRIMARY KEY, " +
            "rating INTEGER, " +
            "title NVARCHAR(256), " +
            "text NVARCHAR(4000), " +
            "userNickName NVARCHAR(64), " +
            "submissionTime TIMESTAMP, " +
            "isRecommend INTEGER, " +
            "likes INTEGER DEFAULT 0" +
            ");";

    public static String INSERT_INTO_REVIEWS =
        "INSERT INTO reviews (hotelId, reviewId, rating, title, text, userNickName, submissionTime, isRecommend)" +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    public static String SELECT_FROM_REVIEWS =
        "SELECT * FROM reviews " +
            "WHERE hotelId=?" +
            ";";

    public static String DROP_TABLE_REVIEWS = "DROP TABLE reviews";

    /* ---------- Table: likes ---------- */

    public static String CREATE_TABLE_LIKES =
        "CREATE TABLE likes (" +
            "username NVARCHAR(32), " +
            "reviewId INTEGER" +
            ");";

    /* ---------- Table: custom review ---------- */

    public static String CREATE_TABLE_CUSTOM_REVIEW =
        "CREATE TABLE customReview (" +
            "username NVARCHAR(32) NOT NULL PRIMARY KEY, " +
            "hotelId NVARCHAR(16) NOT NULL, " +
            "title NVARCHAR(256) NOT NULL, " +
            "text NVARCHAR(4000) NOT NULL" +
            ");";

    public static String INSERT_INTO_CUSTOM_REVIEW =
        "INSERT INTO customReview (username, hotelId, title, text) " +
            "VALUES (?, ?, ?, ?)";

    public static String SELECT_FROM_CUSTOM_REVIEW =
        "SELECT username, title, text " +
            "FROM customReview " +
            "WHERE username=? " +
            "AND hotelId=?" +
            ";";

    public static String DROP_TABLE_CUSTOM_REVIEW = "DROP TABLE customReview;";

    public static String DELETE_FROM_CUSTOM_REVIEW =
        "DELETE FROM customReview " +
            "WHERE username=? " +
            "AND hotelId=?" +
            ";";

    public static String UPDATE_CUSTOM_REVIEW =
        "UPDATE customReview " +
            "SET title = ?, " +
            "text = ? " +
            "WHERE username=? " +
            "AND hotelId=?" +
            ";";

    /* ---------- Table: custom review ---------- */
    public static String CREATE_TABLE_LOGIN =
        "CREATE TABLE login (" +
            "username NVARCHAR(32) NOT NULL PRIMARY KEY, " +
            "this TIMESTAMP, " +
            "last TIMESTAMP" +
            ");";

    public static String INSERT_INTO_LOGIN =
        "INSERT INTO login (username) " +
            "VALUES (?)";

    public static String UPDATE_CUSTOM_LOGIN_SIGN_IN =
        "UPDATE login " +
            "SET this = ? " +
            "WHERE username=?" +
            ";";

    public static String UPDATE_CUSTOM_LOGIN_SIGN_OUT =
        "UPDATE login " +
            "SET last = ? " +
            "WHERE username=?" +
            ";";

    public static String SELECT_FROM_LOGIN =
        "SELECT username, this, last " +
            "FROM login " +
            "WHERE username=?" +
            ";";
}

