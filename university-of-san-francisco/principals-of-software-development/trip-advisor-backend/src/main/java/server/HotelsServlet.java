package server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import database.DataBaseHandler;
import models.Hotel;
import org.apache.commons.text.StringEscapeUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

public class HotelsServlet extends HttpServlet {
    private final DataBaseHandler dataBaseHandler = DataBaseHandler.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getParameter("hotelId") != null) {
            doGet_SearchOneHotel(req, resp);
        } else {
            doGet_SearchHotels(req, resp);
        }
    }

    /* Handle one hotel search */
    private void doGet_SearchOneHotel(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Server.setJsonResponseHeader(resp);
        String hotelId = StringEscapeUtils.unescapeHtml4(req.getParameter("hotelId"));
        try {
            Hotel hotel = dataBaseHandler.selectOneHotel(hotelId);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("hotelId", hotel.getHotelId());
            jsonObject.addProperty("hotelName", hotel.getHotelName());
            jsonObject.addProperty("averageRating", hotel.getAverageRating());
            jsonObject.addProperty("address", hotel.getStreetAddress());
            jsonObject.addProperty("city", hotel.getCity());
            jsonObject.addProperty("state", hotel.getState());
            jsonObject.addProperty("latitude", hotel.getLatitude());
            jsonObject.addProperty("longitude", hotel.getLongitude());

            PrintWriter out = resp.getWriter();
            out.println(jsonObject);
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }

    /* Handle search on city and keyword */
    private void doGet_SearchHotels(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Server.setJsonResponseHeader(resp);

        String city = StringEscapeUtils.unescapeHtml4(req.getParameter("city"));
        String keyword = StringEscapeUtils.unescapeHtml4(req.getParameter("keyword"));

        try {
            List<Hotel> hotels = dataBaseHandler.selectFromHotels(city, keyword);
            JsonArray jsonArray = new JsonArray();
            JsonObject result = new JsonObject();
            result.add("result", jsonArray);

            for (Hotel hotel : hotels) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("hotelId", hotel.getHotelId());
                jsonObject.addProperty("hotelName", hotel.getHotelName());
                jsonObject.addProperty("averageRating", hotel.getAverageRating());
                jsonObject.addProperty("address", hotel.getStreetAddress());
                jsonObject.addProperty("city", hotel.getCity());
                jsonObject.addProperty("state", hotel.getState());
                jsonObject.addProperty("latitude", hotel.getLatitude());
                jsonObject.addProperty("longitude", hotel.getLongitude());
                jsonArray.add(jsonObject);
            }
            PrintWriter out = resp.getWriter();
            out.println(result);
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }
}
