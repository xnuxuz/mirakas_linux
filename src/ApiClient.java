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
    public static boolean sendImage(String base64Image) {
        try {
            // Use URI to construct URL to avoid deprecated constructor
            URI uri = new URI("http", null, "localhost", 8000, "/api/fingerprint", null, null);
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            // Create JSON payload
            String jsonInputString = "{\"image\":\"" + base64Image + "\"}";
            
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