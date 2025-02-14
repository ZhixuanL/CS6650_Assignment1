# Server

## The `.war` file can be found in the GitHub repository at:
CS6650_Assignment1/server/CS6650_Assignment1/out/artifacts/CS6650_Assignment1_war/CS6650_Assignment1_war.war

## API endpoints:
- **POST** `/CS6650_Assignment1_war/skiers` -- submit skier data

curl -X POST "http://<server-url>:8080/CS6650_Assignment1_war/skiers" \
     -H "Content-Type: application/json" \
     -d '{
          "skierID": 12345,
          "resortID": 1,
          "liftID": 10,
          "seasonID": "2025",
          "dayID": 1,
          "time": 300
         }'
         
{"status": "success"}
