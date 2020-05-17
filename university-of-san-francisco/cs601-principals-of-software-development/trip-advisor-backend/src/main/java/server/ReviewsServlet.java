package server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import database.DataBaseHandler;
import models.Review;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/* Reviews servlet */
public class ReviewsServlet extends HttpServlet {
    private final DataBaseHandler dataBaseHandler = DataBaseHandler.getInstance();

    @Override
    /* Get list of reviews */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Server.setJsonResponseHeader(resp);

        String hotelId = req.getParameter("hotelId");
        try {
            List<Review> reviews = dataBaseHandler.selectFromReviews(hotelId);
            JsonArray jsonArray = new JsonArray();
            JsonObject result = new JsonObject();
            result.add("result", jsonArray);

            for (Review review : reviews) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("hotelId", review.getHotelId());
                jsonObject.addProperty("reviewId", review.getReviewId());
                jsonObject.addProperty("rating", review.getAverageRating());
                jsonObject.addProperty("title", review.getReviewTitle());
                jsonObject.addProperty("text", review.getReviewText());
                jsonObject.addProperty("userNickName", review.getUserNickname());
                jsonObject.addProperty("submissionTime", review.getReviewSubmissionTime().toString());
                jsonObject.addProperty("isRecommend", review.getIsRecommend());
                jsonArray.add(jsonObject);
            }
            PrintWriter out = resp.getWriter();
            out.println(result);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    @Override
    /* Add like/unlike */
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
