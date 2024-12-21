import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;

public class ApiClient {
    /**
     * Sends the Base64-encoded image to the specified API endpoint.
     *
     * @param base64Image The Base64-encoded fingerprint image.
     * @return true if the image was sent successfully, false otherwise.
     */

    public static Integer validateEmail(String email){
        try {
            URI uri = new URI("http", null, "cms.mirakas.id", 80, "/api/check-email", null, null);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            String jsonInputString = "{\"email\":\"" + email + "\"}";
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return responseCode;
                }
            } else {
                return responseCode;
            }
        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
            return 500;
        }
    }

    public static boolean sendImage(String base64Image, String email) {
        try {
            // Use URI to construct URL to avoid deprecated constructor
            URI uri = new URI("http", null, "cms.mirakas.id", 80, "/api/fingerprint", null, null);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            // Create JSON payload
            String jsonInputString = "{\"image\":\"" + base64Image +"\",\"email\":\"" + email +"\"}";
            // System.out.println(jsonInputString);
            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // System.out.println(conn.getResponseMessage());
            int responseCode = conn.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED){
                return true;
            } else {
                System.out.println("API Response Code: " + responseCode);
                return false;
            }

        } catch (Exception e) {
            System.out.println("Error sending image to API: " + e.getMessage());
            return false;
        }
    }
}