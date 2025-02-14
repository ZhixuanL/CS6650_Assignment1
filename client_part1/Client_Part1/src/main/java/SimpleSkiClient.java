import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleSkiClient {
  private static final String SERVER_URL = "http://35.90.241.210:8080/CS6650_Assignment1_war/skiers";
  private static final int NUM_THREADS = 32;  // Set to 32 threads as required
  private static final int REQUESTS_PER_THREAD = 1000;
  private static final int TOTAL_REQUESTS = 200000;  // Target 200,000 requests
  private static final int ADDITIONAL_THREADS = 0;  // Additional threads if needed
  private static final BlockingQueue<String> requestQueue = new LinkedBlockingQueue<>(); // Thread-safe queue

  private static final AtomicInteger successCount = new AtomicInteger(0);
  private static final AtomicInteger failureCount = new AtomicInteger(0);

  public static void main(String[] args) {
    System.out.println("Threads used: " + NUM_THREADS);

    // Start data generation thread
    generateLiftRideData();

    ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS + ADDITIONAL_THREADS);
    HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(60))  // Set connection timeout to 60 seconds
        .build();

    long startTime = System.currentTimeMillis(); // Record start time

    // Start all request threads (ensure a total of 200,000 requests are sent)
    for (int i = 0; i < NUM_THREADS + ADDITIONAL_THREADS; i++) {
      executor.execute(() -> {
        while (true) {
          try {
            String jsonBody = requestQueue.poll(10, TimeUnit.SECONDS); // 10-second timeout
            if (jsonBody == null) break; // End thread if the queue is empty

            sendPostRequest(client, jsonBody);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      });
    }

    executor.shutdown();
    try {
      executor.awaitTermination(10, TimeUnit.MINUTES); // Wait up to 10 minutes for all threads to finish
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    long endTime = System.currentTimeMillis(); // Record end time
    double totalTimeSeconds = (endTime - startTime) / 1000.0;

    // Result statistics
    System.out.println("====== Results ======");
    System.out.println("Total Requests Sent: " + (successCount.get() + failureCount.get()));
    System.out.println("Successful Requests: " + successCount.get());
    System.out.println("Failed Requests: " + failureCount.get());
    System.out.println("Total Time Taken: " + totalTimeSeconds + " seconds");
    System.out.println("Throughput: " + (successCount.get() / totalTimeSeconds) + " requests/sec");
  }

  // Generate 200,000 lift ride data and store them in the BlockingQueue
  private static void generateLiftRideData() {
    new Thread(() -> {
      for (int i = 0; i < TOTAL_REQUESTS; i++) {
        try {
          String jsonBody = LiftRideDataGenerator.generateRandomLiftRide();
          requestQueue.put(jsonBody); // Put data into queue (blocking)
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      System.out.println("Data Generation Completed: 200,000 Lift Rides Generated!");
    }).start();
  }

  // Send HTTP POST request with error handling and retry mechanism
  private static void sendPostRequest(HttpClient client, String jsonBody) {
    int retryCount = 0;
    while (retryCount < 5) {  // Retry up to 5 times
      try {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SERVER_URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 201) {
          successCount.incrementAndGet();  // Increment success count on successful POST
          return;  // Exit after success
        } else {
          failureCount.incrementAndGet();  // Increment failure count if status code is not 201
        }
      } catch (Exception e) {
        failureCount.incrementAndGet();
        e.printStackTrace();
      }

      retryCount++;  // Increment retry count
      if (retryCount < 5) {
        try {
          Thread.sleep(500);  // Sleep for 500ms before retrying
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    System.out.println("Request failed after 5 retries.");
  }
}
