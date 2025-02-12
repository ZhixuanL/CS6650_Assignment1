# Client Part 1

## Overview
This part implements a **multithreaded Java client** that sends concurrent HTTP POST requests to a remote server, simulating skier lift ride events.
- Uses **Java 11 `HttpClient`** to send HTTP requests.
- Implements **multithreading with `ExecutorService`**, creating **32 threads**, each sending **1,000 requests** (total: **32,000 requests**).
- Measures **successful requests, failed requests, total execution time, and throughput**.

## How to Run  
cd client_part1  
mvn compile  
mvn exec:java -Dexec.mainClass="SimpleSkiClient"
