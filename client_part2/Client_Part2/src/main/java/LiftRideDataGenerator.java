import java.util.Random;

public class LiftRideDataGenerator {
  private static final Random random = new Random();

  // Randomly generate data
  public static String generateRandomLiftRide() {
    int skierID = random.nextInt(100000) + 1;  // 1 ~ 100000
    int resortID = random.nextInt(10) + 1;     // 1 ~ 10
    int liftID = random.nextInt(40) + 1;       // 1 ~ 40
    int time = random.nextInt(360) + 1;        // 1 ~ 360

    return "{"
        + "\"skierID\": " + skierID + ","
        + "\"resortID\": " + resortID + ","
        + "\"liftID\": " + liftID + ","
        + "\"seasonID\": \"2025\","
        + "\"dayID\": \"1\","
        + "\"time\": " + time
        + "}";
  }

  public static void main(String[] args) {
    // Generate 5 random data for the test
    for (int i = 0; i < 5; i++) {
      System.out.println(generateRandomLiftRide());
    }
  }
}
