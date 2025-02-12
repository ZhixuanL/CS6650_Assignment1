package neu.cs6650;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.BufferedReader;
import org.json.JSONObject;

public class SkierServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json");
    res.setStatus(HttpServletResponse.SC_OK);
    res.getWriter().write("{\"message\": \"This is the skiers API. Use POST to send data.\"}");
  }


  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json");

    // read JSON request body
    StringBuilder sb = new StringBuilder();
    String line;
    try (BufferedReader reader = req.getReader()) {
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
    }

    // parse JSON
    try {
      JSONObject json = new JSONObject(sb.toString());

      int skierID = json.getInt("skierID");
      int resortID = json.getInt("resortID");
      int liftID = json.getInt("liftID");
      int seasonID = json.getInt("seasonID");
      int dayID = json.getInt("dayID");
      int time = json.getInt("time");

      if (!isValid(skierID, resortID, liftID, seasonID, dayID, time)) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.getWriter().write("{\"message\": \"Invalid parameters\"}");
        return;
      }

      res.setStatus(HttpServletResponse.SC_CREATED);
      res.getWriter().write("{\"message\": \"Lift ride recorded successfully\"}");

    } catch (Exception e) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("{\"message\": \"Invalid JSON format\"}");
    }
  }

  private boolean isValid(int skierID, int resortID, int liftID, int seasonID, int dayID, int time) {
    return skierID > 0 && skierID <= 100000 &&
        resortID > 0 && resortID <= 10 &&
        liftID > 0 && liftID <= 40 &&
        seasonID == 2025 &&
        dayID == 1 &&
        time > 0 && time <= 360;
  }
}
