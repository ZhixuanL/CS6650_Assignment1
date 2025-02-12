import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleSkiClient {
  private static final String SERVER_URL = "http://35.92.43.147:8080/CS6650_Assignment1_war/skiers";
  private static final int NUM_THREADS = 32;
  private static final int REQUESTS_PER_THREAD = 1000;

  private static final AtomicInteger successCount = new AtomicInteger(0);
  private static final AtomicInteger failureCount = new AtomicInteger(0);

  public static void main(String[] args) {
    ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
    HttpClient client = HttpClient.newHttpClient();

    long startTime = System.currentTimeMillis(); // record the starting time

    for (int i = 0; i < NUM_THREADS; i++) {
      executor.execute(() -> {
        for (int j = 0; j < REQUESTS_PER_THREAD; j++) {
          sendPostRequest(client);
        }
      });
    }

    executor.shutdown();
    while (!executor.isTerminated()) {
      // wait for all threads to finish
    }

    long endTime = System.currentTimeMillis(); // record the finishing time
    double totalTimeSeconds = (endTime - startTime) / 1000.0;

    // 统计结果
    System.out.println("====== Results ======");
    System.out.println("Total Requests Sent: " + (successCount.get() + failureCount.get()));
    System.out.println("Successful Requests: " + successCount.get());
    System.out.println("Failed Requests: " + failureCount.get());
    System.out.println("Total Time Taken: " + totalTimeSeconds + " seconds");
    System.out.println("Throughput: " + (successCount.get() / totalTimeSeconds) + " requests/sec");
  }

  private static void sendPostRequest(HttpClient client) {
    String jsonBody = LiftRideDataGenerator.generateRandomLiftRide();

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(SERVER_URL))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
        .build();

    try {
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 201) {
        successCount.incrementAndGet();
      } else {
        failureCount.incrementAndGet();
      }
    } catch (Exception e) {
      failureCount.incrementAndGet();
      e.printStackTrace();
    }
  }
}
