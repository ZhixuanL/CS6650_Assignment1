import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class SimpleSkiClient {
  public static void main(String[] args) {
    // URL of servlet
    String serverUrl = "http://35.92.43.147:8080/CS6650_Assignment1_war/skiers";

    // JSON data
    String jsonBody = "{"
        + "\"skierID\": 12345,"
        + "\"resortID\": 5,"
        + "\"liftID\": 12,"
        + "\"seasonID\": \"2025\","
        + "\"dayID\": \"1\","
        + "\"time\": 135"
        + "}";

    try {
      // create HTTP client
      HttpClient client = HttpClient.newHttpClient();

      // create HTTP request
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(serverUrl))
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
          .build();

      // send request
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      // print the response code and body
      System.out.println("Response Code: " + response.statusCode());
      System.out.println("Response Body: " + response.body());

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
