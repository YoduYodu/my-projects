package server;

import com.google.gson.JsonObject;
import database.DataBaseHandler;
import org.apache.commons.text.StringEscapeUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

public class CustomReview extends HttpServlet {
    private final DataBaseHandler dataBaseHandler = DataBaseHandler.getInstance();

    @Override
    /* Get custom review */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Server.setJsonResponseHeader(resp);
        String username = StringEscapeUtils.unescapeHtml4(req.getParameter("username"));
        String hotelId = StringEscapeUtils.unescapeHtml4(req.getParameter("hotelId"));
        try {
            List<String> customReview = dataBaseHandler.selectFromCustomReview(username, hotelId);

            JsonObject jsonObject = new JsonObject();
            if (customReview != null) {
                jsonObject.addProperty("username", customReview.get(0));
                jsonObject.addProperty("title", customReview.get(1));
                jsonObject.addProperty("text", customReview.get(2));
            } else {
                jsonObject.addProperty("failed", true);
            }

            PrintWriter out = resp.getWriter();
            out.println(jsonObject);
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonObject properties = Server.parseRequest(req);
        String request = properties.get("request").getAsString();

        if (request.equals("add")) {
            doPost_AddCustomReview(req, resp, properties);
        } else if (request.equals("edit")) {
            doPost_EditCustomReview(req, resp, properties);
        } else {
            doPost_Delete(req,resp, properties);
        }
    }

    /* Add review */
    private void doPost_AddCustomReview(HttpServletRequest req, HttpServletResponse resp, JsonObject properties) throws IOException {
        Server.setJsonResponseHeader(resp);
        String username = properties.get("username").getAsString();
        String hotelId = properties.get("hotelId").getAsString();
        String title = properties.get("title").getAsString();
        String text = properties.get("text").getAsString();

        try {
            dataBaseHandler.insertIntoCustomReview(username, hotelId, title, text);
            PrintWriter out = resp.getWriter();
            out.println(properties);
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }

    /* edit review */
    private void doPost_EditCustomReview(HttpServletRequest req, HttpServletResponse resp, JsonObject properties) throws IOException {
        Server.setJsonResponseHeader(resp);
        String username = properties.get("username").getAsString();
        String hotelId = properties.get("hotelId").getAsString();
        String title = properties.get("title").getAsString();
        String text = properties.get("text").getAsString();

        try {
            dataBaseHandler.updateTableCustomReview(username, hotelId, title, text);
            PrintWriter out = resp.getWriter();
            out.println(properties);
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }

    /* Delete review */
    private void doPost_Delete(HttpServletRequest req, HttpServletResponse resp, JsonObject properties) throws IOException {
        Server.setJsonResponseHeader(resp);
        String username = properties.get("username").getAsString();
        String hotelId = properties.get("hotelId").getAsString();

        try {
            dataBaseHandler.deleteFromCustomReview(username, hotelId);
            PrintWriter out = resp.getWriter();
            out.println(properties);
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }
}
