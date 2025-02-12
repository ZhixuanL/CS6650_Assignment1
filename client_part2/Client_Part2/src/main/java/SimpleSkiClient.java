import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.Collections;

public class SimpleSkiClient {
  private static final String SERVER_URL = "http://18.236.127.96:8080/CS6650_Assignment1_war/skiers";
  private static final int NUM_THREADS = 32;
  private static final int REQUESTS_PER_THREAD = 1000;
  private static final int TOTAL_REQUESTS = 200000;
  private static final BlockingQueue<String> requestQueue = new LinkedBlockingQueue<>();
  private static final AtomicInteger successCount = new AtomicInteger(0);
  private static final AtomicInteger failureCount = new AtomicInteger(0);

  // Thread-safe list to store request logs
  private static final List<String[]> requestLogs = new CopyOnWriteArrayList<>();

  public static void main(String[] args) {
    generateLiftRideData();

    ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
    HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(60))
        .build();

    long startTime = System.currentTimeMillis(); // Start time for performance measurement

    for (int i = 0; i < NUM_THREADS; i++) {
      executor.execute(() -> {
        while (true) {
          try {
            String jsonBody = requestQueue.poll(10, TimeUnit.SECONDS);
            if (jsonBody == null) break;

            sendPostRequest(client, jsonBody);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      });
    }

    executor.shutdown();
    try {
      executor.awaitTermination(10, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    long endTime = System.currentTimeMillis(); // End time for performance measurement
    double totalTimeSeconds = (endTime - startTime) / 1000.0;

    System.out.println("====== Results ======");
    System.out.println("Total Requests Sent: " + (successCount.get() + failureCount.get()));
    System.out.println("Successful Requests: " + successCount.get());
    System.out.println("Failed Requests: " + failureCount.get());
    System.out.println("Total Time Taken: " + totalTimeSeconds + " seconds");
    System.out.println("Throughput: " + (successCount.get() / totalTimeSeconds) + " requests/sec");

    System.out.println("Total logged requests: " + requestLogs.size());

    writeLogsToCSV("request_logs.csv");
  }

  private static void generateLiftRideData() {
    new Thread(() -> {
      for (int i = 0; i < TOTAL_REQUESTS; i++) {
        try {
          String jsonBody = LiftRideDataGenerator.generateRandomLiftRide();
          requestQueue.put(jsonBody);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      System.out.println("Data Generation Completed: 200,000 Lift Rides Generated!");
    }).start();
  }

  private static void sendPostRequest(HttpClient client, String jsonBody) {
    int retryCount = 0;
    while (retryCount < 5) {
      long startTime = System.currentTimeMillis(); // Record request start time
      try {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SERVER_URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        long endTime = System.currentTimeMillis(); // Record request end time
        long latency = endTime - startTime; // Calculate response latency

        if (response.statusCode() == 201) {
          successCount.incrementAndGet();
        } else {
          failureCount.incrementAndGet();
        }

        requestLogs.add(new String[]{
            String.valueOf(startTime),
            "POST",
            String.valueOf(latency),
            String.valueOf(response.statusCode())
        });

        return;
      } catch (Exception e) {
        failureCount.incrementAndGet();
        e.printStackTrace();
      }

      retryCount++;
      if (retryCount < 5) {
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    System.out.println("Request failed after 5 retries.");
  }

  private static void writeLogsToCSV(String filename) {
    try (FileWriter writer = new FileWriter(filename)) {
      writer.write("Start Time,Request Type,Latency (ms),Response Code\n");
      for (String[] log : requestLogs) {
        writer.write(String.join(",", log) + "\n");
      }
      System.out.println("Request logs saved to " + filename);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
