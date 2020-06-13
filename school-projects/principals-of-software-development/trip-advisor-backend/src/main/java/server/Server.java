package server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

public class Server {
    public static final int PORT = 8000;

    /**
     * Set json response header
     */
    public static void setJsonResponseHeader(HttpServletResponse res) {
        res.setContentType("application/json");
        res.setStatus(HttpServletResponse.SC_OK);
        res.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        res.addHeader("Access-Control-Allow-Credentials", "true");
    }

    /**
     * Parse plain text into a map
     */
    public static JsonObject parseRequest(HttpServletRequest req) throws IOException {
        BufferedReader reader = req.getReader();
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        String json = StringEscapeUtils.unescapeHtml4(stringBuilder.toString().trim());
        return new JsonParser().parse(json).getAsJsonObject();
    }

    public static void main(String[] args) {
        org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(PORT);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.addServlet(UsersServlet.class, "/users");
        context.addServlet(HotelsServlet.class, "/hotels");
        context.addServlet(ReviewsServlet.class, "/reviews");
        context.addServlet(CustomReview.class, "/customReview");
        context.addServlet(AttractionsServlet.class, "/attractions");
        server.setHandler(context);

        try {
            server.start();
        } catch (Exception e) {
            System.out.println("Failed starting server");
        }

        String s = "{\"request\":\"add\",\"username\":\"tongyyy\",\"title\":\"a\",\"text\":\"fff\"}";
        System.out.println(s);
        System.out.println((new JsonParser()).parse(s));
        JsonObject jsonObject = new JsonParser().parse(s).getAsJsonObject();
        System.out.println(jsonObject.get("request"));
    }
}
