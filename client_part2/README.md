# Client Part 2

## Overview:
This is an enhanced version of the **multithreaded Java client** from **Client Part 1**, with additional performance instrumentation. The client sends **200,000 POST requests** to the server while tracking request latency and response codes.


## How to Run:  
Before running the client, update the server URL in the code. The base URL should match the server deployment:  
private static final String SERVER_URL = "http://<server-url>:8080/CS6650_Assignment1_war/skiers";  
Replace <server-url> with the actual server IP or hostname.  

The client records each request's timestamp, latency, response code, and request type in a CSV file:  
CS6650_Assignment1/client_part2/Client_Part2/request/logs.csv  


## Example output:
====== Results ======  
Total Requests Sent: 200000  
Successful Requests: 200000  
Failed Requests: 0  
Total Time Taken: 199.729seconds  
Throughput: 1001.3568385161893 requests/sec  

==== Performance Metrics =====  
Mean Response Time: 30.35 ms  
Median Response Time: 29 ms  
99th Percentile Response Time: 56 ms  
Min Response Time: 15 ms  
Max Response Time: 321 ms  
Throughput: 1001.36 requests/sec  
Request logs saved to request_logs.csv  
