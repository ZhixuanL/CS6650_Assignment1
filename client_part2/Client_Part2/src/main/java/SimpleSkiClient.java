import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleSkiClient {
  private static final String SERVER_URL = "http://35.90.241.210:8080/CS6650_Assignment1_war/skiers";
  private static final int NUM_THREADS = 32;
  private static final int REQUESTS_PER_THREAD = 1000;
  private static final int TOTAL_REQUESTS = 200000;
  private static final BlockingQueue<String> requestQueue = new LinkedBlockingQueue<>();
  private static final AtomicInteger successCount = new AtomicInteger(0);
  private static final AtomicInteger failureCount = new AtomicInteger(0);

  // Using `ConcurrentLinkedQueue` instead of `CopyOnWriteArrayList` for better performance
  private static final Queue<String[]> requestLogs = new ConcurrentLinkedQueue<>();

  public static void main(String[] args) {
    System.out.println("Threads used: " + NUM_THREADS);

    generateLiftRideData();

    ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
    HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(60))
        .build();

    long startTime = System.currentTimeMillis(); // Record test start time

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

    long endTime = System.currentTimeMillis(); // Record test end time
    double totalTimeSeconds = (endTime - startTime) / 1000.0;

    // Print basic results
    System.out.println("====== Results ======");
    System.out.println("Total Requests Sent: " + (successCount.get() + failureCount.get()));
    System.out.println("Successful Requests: " + successCount.get());
    System.out.println("Failed Requests: " + failureCount.get());
    System.out.println("Total Time Taken: " + totalTimeSeconds + " seconds");
    System.out.println("Throughput: " + (successCount.get() / totalTimeSeconds) + " requests/sec");

    // Write logs asynchronously
    writeLogsToCSV("request_logs.csv");

    // Compute performance metrics
    calculateStatistics(totalTimeSeconds);
  }

  // Generate lift ride data and store in BlockingQueue
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

  // Send HTTP POST request with retry mechanism
  private static void sendPostRequest(HttpClient client, String jsonBody) {
    int retryCount = 0;
    while (retryCount < 5) {
      long startTime = System.currentTimeMillis();
      try {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SERVER_URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        long endTime = System.currentTimeMillis();
        long latency = endTime - startTime;

        if (response.statusCode() == 201) {
          successCount.incrementAndGet();
        } else {
          failureCount.incrementAndGet();
        }

        // Store logs but defer CSV writing
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

  // Asynchronously write logs to CSV
  private static void writeLogsToCSV(String filename) {
    new Thread(() -> {
      try (FileWriter writer = new FileWriter(filename)) {
        writer.write("Start Time,Request Type,Latency (ms),Response Code\n");
        for (String[] log : requestLogs) {
          writer.write(String.join(",", log) + "\n");
        }
        System.out.println("Request logs saved to " + filename);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }).start();
  }

  // Compute performance metrics based on logged data
  private static void calculateStatistics(double totalTimeSeconds) {
    List<Long> latencies = new ArrayList<>();
    long minLatency = Long.MAX_VALUE;
    long maxLatency = Long.MIN_VALUE;
    long totalLatency = 0;

    for (String[] log : requestLogs) {
      long latency = Long.parseLong(log[2]);

      latencies.add(latency);
      totalLatency += latency;
      minLatency = Math.min(minLatency, latency);
      maxLatency = Math.max(maxLatency, latency);
    }

    if (latencies.isEmpty()) {
      System.out.println("No latency data found.");
      return;
    }

    // Sort for percentile calculations
    Collections.sort(latencies);
    double mean = (double) totalLatency / latencies.size();
    long median = latencies.get(latencies.size() / 2);
    long p99 = latencies.get((int) (latencies.size() * 0.99));

    // Print performance metrics
    System.out.println("====== Performance Metrics ======");
    System.out.println("Mean Response Time: " + String.format("%.2f", mean) + " ms");
    System.out.println("Median Response Time: " + median + " ms");
    System.out.println("99th Percentile Response Time: " + p99 + " ms");
    System.out.println("Min Response Time: " + minLatency + " ms");
    System.out.println("Max Response Time: " + maxLatency + " ms");
    System.out.println("Throughput: " + String.format("%.2f", requestLogs.size() / totalTimeSeconds) + " requests/sec");
  }
}
