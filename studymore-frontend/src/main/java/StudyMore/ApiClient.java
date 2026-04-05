package StudyMore;

import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;

public class ApiClient {

    private static final String BASE = "https://studymore-production.up.railway.app/api";
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    public static String get(String path) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + path))
                    .header("Content-Type", "application/json")
                    .GET().build();
            HttpResponse<String> response = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            System.out.println("GET " + path + " → " + response.statusCode() + " " + body);
            return body;
        } catch (Exception e) {
            System.err.println("GET " + path + " failed: " + e.getMessage());
            return "[]";
        }
    }

    public static String post(String path, String body) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + path))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(body)).build();
            HttpResponse<String> response = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("POST " + path + " → " + response.statusCode() + " " + response.body());
            return response.body();
        } catch (Exception e) {
            System.err.println("POST " + path + " failed: " + e.getMessage());
            return "{}";
        }
    }

    public static String put(String path) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + path))
                    .header("Content-Type", "application/json")
                    .PUT(BodyPublishers.noBody()).build();
            HttpResponse<String> response = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("PUT " + path + " → " + response.statusCode() + " " + response.body());
            return response.body();
        } catch (Exception e) {
            System.err.println("PUT " + path + " failed: " + e.getMessage());
            return "{}";
        }
    }
}