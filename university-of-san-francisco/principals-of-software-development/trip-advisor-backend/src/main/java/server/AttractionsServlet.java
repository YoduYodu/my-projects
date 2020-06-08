package server;

import database.DataBaseHandler;
import hotel.TouristAttractionFinder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class AttractionsServlet extends HttpServlet {
    private final DataBaseHandler dataBaseHandler = DataBaseHandler.getInstance();
    private final TouristAttractionFinder finder = TouristAttractionFinder.getInstance();

    @Override
    /* GET attractions */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Server.setJsonResponseHeader(resp);
        String hotelId = req.getParameter("hotelId");
        int radius = Integer.parseInt(req.getParameter("radius"));
        PrintWriter out = resp.getWriter();
        out.println(finder.fetchOneHotelAttractions(radius, hotelId));
    }
}
