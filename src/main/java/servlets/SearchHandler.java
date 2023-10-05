package servlets;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import com.google.gson.Gson;
import java.util.HashMap;

/**
 *
 * @author csd3829
 */
public class SearchHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        Gson gson = new Gson();

        // Get the query parameter from the request
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> queryParams = getQueryParameters(query);

        // Extract the 'query' parameter
        String queryParam = queryParams.get("query");

        try {
            ArrayList<String> titles = main.utilities.getFileFromSolr(queryParam);

            String jsonResponse = gson.toJson(titles);

            // Set response headers
            exchange.getResponseHeaders().set("Content-Type", "application/json");

            exchange.sendResponseHeaders(200, jsonResponse.length());

            // Send the JSON response
            try ( OutputStream os = exchange.getResponseBody()) {
                os.write(jsonResponse.getBytes());
                os.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, 0);
        } finally {
            exchange.close();
        }
    }

    public static Map<String, String> getQueryParameters(String query) {
        Map<String, String> params = new HashMap<>();

        if (query != null) {
            String[] paramPairs = query.split("&");
            for (String paramPair : paramPairs) {
                String[] keyValue = paramPair.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = keyValue[1];
                    params.put(key, value);
                }
            }
        }

        return params;
    }
}
