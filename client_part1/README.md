# Client Part 1

## Overview
This part implements a **multithreaded Java client** that sends concurrent HTTP POST requests to a remote server, simulating skier lift ride events.


## How to Run  

Before running the client, update the server URL in the code. The base URL should match the server deployment:  
private static final String SERVER_URL = "http://<server-url>:8080/CS6650_Assignment1_war/skiers";  
Replace <server-url> with the actual server IP or hostname.  


cd client_part1    
mvn compile    
mvn exec:java -Dexec.mainClass="SimpleSkiClient"  
java -jar target/client_part1.jar  

## Example Output
====== Results ======  
Total Requests Sent: 200000  
Successful Requests: 200000  
Failed Requests: 0  
Total Time Taken: 201.084 seconds  
Throughput: 994.6092180382327 requests/sec  
