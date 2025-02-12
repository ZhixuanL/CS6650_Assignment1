import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LatencyAnalyzer {
  private static final String CSV_FILE = "request_logs.csv"; // CSV file path

  public static void main(String[] args) {
    calculateStatistics();
  }

  private static void calculateStatistics() {
    List<Long> latencies = new ArrayList<>();
    long minLatency = Long.MAX_VALUE;
    long maxLatency = Long.MIN_VALUE;
    long totalLatency = 0;
    long startTime = Long.MAX_VALUE;
    long endTime = Long.MIN_VALUE;

    try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
      String line;
      br.readLine(); // Skip header row

      while ((line = br.readLine()) != null) {
        String[] values = line.split(",");
        long requestStartTime = Long.parseLong(values[0]); // Extract start time
        long latency = Long.parseLong(values[2]); // Extract latency

        latencies.add(latency);
        totalLatency += latency;
        minLatency = Math.min(minLatency, latency);
        maxLatency = Math.max(maxLatency, latency);

        // Update start and end times
        startTime = Math.min(startTime, requestStartTime);
        endTime = Math.max(endTime, requestStartTime + latency);
      }

      if (latencies.isEmpty()) {
        System.out.println("No data found in CSV file.");
        return;
      }

      // Sort latencies for percentile and median calculation
      Collections.sort(latencies);
      double mean = (double) totalLatency / latencies.size();
      long median = latencies.get(latencies.size() / 2);
      long p99 = latencies.get((int) (latencies.size() * 0.99));

      // Calculate throughput
      double totalTimeSeconds = (endTime - startTime) / 1000.0;
      double throughput = latencies.size() / totalTimeSeconds;

      // Print results
      System.out.println("====== Performance Metrics ======");
      System.out.println("Total Requests Analyzed: " + latencies.size());
      System.out.println("Mean Response Time: " + String.format("%.2f", mean) + " ms");
      System.out.println("Median Response Time: " + median + " ms");
      System.out.println("99th Percentile Response Time: " + p99 + " ms");
      System.out.println("Min Response Time: " + minLatency + " ms");
      System.out.println("Max Response Time: " + maxLatency + " ms");
      System.out.println("Total Wall Time: " + String.format("%.2f", totalTimeSeconds) + " seconds");
      System.out.println("Throughput: " + String.format("%.2f", throughput) + " requests/sec");

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
