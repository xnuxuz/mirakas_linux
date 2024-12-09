// src/Main.java

import com.digitalpersona.uareu.UareUGlobal;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Main class for the Fingerprint Capture Application.
 */
public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static Reader selectedReader;

    // List to store captured fingerprint data
    public static final List<FingerprintData> fingerprintDataList = new ArrayList<>();

    public static void main(String[] args) {
        try {
            // Initialize the UareU SDK
            ReaderCollection readers = UareUGlobal.GetReaderCollection();
            readers.GetReaders();

            if (readers.size() == 0) {
                logger.warning("No fingerprint readers found.");
                System.out.println("No fingerprint readers detected. Exiting application.");
                return;
            }

            selectedReader = readers.get(0);
            selectedReader.Open(Reader.Priority.EXCLUSIVE);
            System.out.println("Fingerprint reader opened successfully.");

            // Loop for repeated capture
            while (true) {
                System.out.println("\nPlease scan your finger.");

                // Start the capture process
                FingerprintData data = Capture.Run(selectedReader);

                if (data != null && data.getImage() != null) {
                    fingerprintDataList.add(data);
                    // System.out.println("Fingerprint captured successfully.");

                    // Save the captured fingerprint image
                    String fingerName = "Captured_Finger_" + (fingerprintDataList.size());
                    String fileName = fingerName + ".png"; // Example: Captured_Finger_1.png
                    File outputFile = new File("fingerprints/" + fileName);
                    try {
                        // Ensure the directory exists
                        outputFile.getParentFile().mkdirs();
                        ImageIO.write(data.getImage(), "png", outputFile);
                        System.out.println(fingerName + " saved to " + outputFile.getAbsolutePath());
                        boolean ApiResponse = ApiClient.sendImage(data.base64());
                        if(ApiResponse){
                            logger.info("Image successfully sent to the API.");
                        }else{
                            logger.warning("Failed to send image to the API.");
                        }
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Error saving image for " + fingerName, e);
                    }

                    // You can add additional logic here if needed
                } else {
                    System.out.println("No fingerprint scanned within 1 minute. Closing the reader.");
                    logger.warning("Capture failed or timed out.");
                    break; // Exit the loop
                }
            }

            // Close the reader after the capture process is complete
            selectedReader.Close();
            System.out.println("Fingerprint reader closed.");

            // Summary of captures
            System.out.println("\nFingerprint capture process completed.");
            System.out.println("Total fingerprints captured: " + fingerprintDataList.size());

            // You can add additional logic here if needed
        } catch (UareUException e) {
            logger.log(Level.SEVERE, "UareUException occurred", e);
            System.out.println("An error occurred with the fingerprint reader. Check logs for details.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An unexpected error occurred", e);
            System.out.println("An unexpected error occurred. Check logs for details.");
        }
    }

    /**
     * Inner class to hold fingerprint data.
     */
    public static class FingerprintData {
        private BufferedImage image;
        private byte[] fmd; // Fingerprint Minutes Data or other relevant data
        private String base64;
        
        public FingerprintData(BufferedImage image, byte[] fmd, String base64) {
            this.image = image;
            this.fmd = fmd;
            this.base64 = base64;
        }

        public String base64(){
            return base64;
        }

        public BufferedImage getImage() {
            return image;
        }

        public byte[] getFmd() {
            return fmd;
        }
    }
}
