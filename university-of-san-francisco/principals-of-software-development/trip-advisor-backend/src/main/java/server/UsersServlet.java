package server;

import com.google.gson.JsonObject;
import database.DataBaseHandler;
import models.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* API for endpoint /users */
public class UsersServlet extends HttpServlet {
    private Random random = new Random();
    private final DataBaseHandler dataBaseHandler = DataBaseHandler.getInstance();

    /**
     * Sign-up new user
     * @param req request
     * @param resp response
     * @throws ServletException ServletException
     * @throws IOException IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Server.setJsonResponseHeader(resp);
        JsonObject properties = Server.parseRequest(req);

        String username = properties.get("username").getAsString();
        String password = properties.get("password").getAsString();

        if (!getPasswordValidity(password)) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("sign_up_success", false);
            PrintWriter out = resp.getWriter();
            out.println(jsonObject);
            return;
        }


        try {
            User user;
            user = dataBaseHandler.selectFromUsers(username);

            JsonObject jsonObject = new JsonObject();
            if (user != null) {
                jsonObject.addProperty("sign_up_success", false);
            } else {
                String salt = generateSalt();
                dataBaseHandler.insertIntoUsers(username, salt, encrypt(password, salt));
                jsonObject.addProperty("sign_up_success", true);
            }

            PrintWriter out = resp.getWriter();
            out.println(jsonObject);
        } catch (SQLException | NoSuchAlgorithmException e) {
            System.out.println(e.toString());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Server.setJsonResponseHeader(resp);

        String username = req.getParameter("username");
        String password = req.getParameter("password");
        try {
            User user = dataBaseHandler.selectFromUsers(username);
            JsonObject jsonObject = new JsonObject();
            if (user == null) {
                jsonObject.addProperty("sign_up_success", false);
            } else if (checkPassword(user, password)) {
                jsonObject.addProperty("sign_up_success", true);
            } else {
                jsonObject.addProperty("sign_up_success", false);
            }
            PrintWriter out = resp.getWriter();
            out.println(jsonObject);
        } catch (SQLException | NoSuchAlgorithmException e) {
            System.out.println(e.toString());
        }

    }

    /**
     * Returns the hex encoding of a byte array.
     *
     * @param bytes - byte array to encode
     * @param length - desired length of encoding
     * @return hex encoded byte array
     */
    private String encodeHex(byte[] bytes, int length) {
        BigInteger bigint = new BigInteger(1, bytes);
        String hex = String.format("%0" + length + "X", bigint);
        assert hex.length() == length;
        return hex;
    }

    /**
     * Calculates the hash of a password and salt using SHA-256.
     *
     * @param unprocessedPassword - password to hash
     * @return processed password
     */
    private String encrypt(String unprocessedPassword, String salt) throws NoSuchAlgorithmException{
        String combination = salt + unprocessedPassword;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(combination.getBytes());
            return encodeHex(messageDigest.digest(), 64);
        }
        catch (NoSuchAlgorithmException ex) {
            System.out.println(ex.toString());
            throw ex;
        }
    }

    /**
     * Generate a random salt
     * @return salt
     */
    private String generateSalt() {
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        return encodeHex(saltBytes, 32);
    }

    /**
     * Check if username/password from request are valid
     * @param user user from DB
     * @param password password from request
     * @return true if valid
     */
    private boolean checkPassword(User user, String password) throws NoSuchAlgorithmException{
        String salt = user.getUsersalt();
        return encrypt(password, salt).equals(user.getPassword());
    }

    /**
     * Password length should be between 5 to 10 characters, contains at least one number, one letter and one special character
     * @param password password
     * @return true if valid
     */
    private boolean getPasswordValidity(String password) {
        if (password.length() >= 5 && password.length() <= 10) {
            Pattern letterRegex = Pattern.compile("[a-zA-Z]");
            Pattern digitRegex = Pattern.compile("\\d");
            Pattern specialRegex = Pattern.compile("\\W");

            Matcher letterMatcher = letterRegex.matcher(password);
            Matcher digitMatcher = digitRegex.matcher(password);
            Matcher specialMatcher = specialRegex.matcher(password);

            return letterMatcher.find() && digitMatcher.find() && specialMatcher.find();
        }
        return false;
    }
}
