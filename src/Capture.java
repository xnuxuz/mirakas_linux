// src/Capture.java

import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.Reader.CaptureQuality;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.Fid;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Capture {
    public static void Run(Reader reader, boolean streaming) {  
        try {
            // Define the image processing constant

            // Create ImagePanel and MessageBox instances
            ImagePanel imagePanel = new ImagePanel(); // Custom class to display images
            MessageBox messageBox = new MessageBox(); // Custom class for message prompts
            
            // Initialize CaptureThread with ActionListener using a lambda expression
            CaptureThread captureThread = new CaptureThread(reader, streaming, Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT, e -> {
                CaptureThread.CaptureEvent captureEvent = (CaptureThread.CaptureEvent) e;
                if (captureEvent.getActionCommand().equals(CaptureThread.ACT_CAPTURE)) {
                    // Check if a capture result is available
                    if (captureEvent.capture_result != null && captureEvent.capture_result.image != null) {
                        Image capturedImage = convertFidToImage(captureEvent.capture_result.image);
                        imagePanel.showImage(capturedImage);
                        System.out.println("Fingerprint captured successfully.");

                        // Check the quality of the capture
                        if (captureEvent.capture_result.quality == CaptureQuality.GOOD) {
                            System.out.println("Capture quality is good.");
                        } else {
                            System.out.println("Capture quality is not optimal: " + captureEvent.capture_result.quality);
                        }

                        // Convert image to Base64 and send to API
                        System.out.println(captureEvent.toString());
                        String base64Image = imageToBase64(capturedImage);
                        if (base64Image != null) {
                            boolean apiResult = ApiClient.sendImage(base64Image); // Ensure ApiClient is properly implemented
                            if (apiResult) {
                                System.out.println("Image successfully sent to the API.");
                            } else {
                                System.out.println("Failed to send image to the API.");
                            }
                        }
                    } else if (captureEvent.exception != null) {
                        // Handle exceptions that occurred during capture
                        System.out.println("Error capturing fingerprint: " + captureEvent.exception.getMessage());
                        // Optionally, display reader status
                        if (captureEvent.reader_status != null) {
                            System.out.println("Reader Status: " + captureEvent.reader_status.status);
                        }
                    }
                }
            });

            // Start the capture thread
            captureThread.start();
            
            // Keep the main thread alive while capture is running
            while (captureThread.isAlive()) {
                Thread.sleep(100); // Reduced sleep time for better responsiveness
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Capture thread was interrupted.");
        }
    }

    // Converts an Image object to a Base64 encoded string
    private static String imageToBase64(Image image) {
        try {
            if(image != null){
                BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
                bufferedImage.getGraphics().drawImage(image, 0, 0, null);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "jpg", baos);
                byte[] bytes = baos.toByteArray();
                return java.util.Base64.getEncoder().encodeToString(bytes);
            }else{
                System.out.println("imaga error : "+image);
                return "";
            }
            
        } catch (IOException e) {
            System.out.println("Error converting image to Base64: " + e.getMessage());
            return null;
        }
    }

    // Converts a Fid image to a Java AWT Image object
    private static Image convertFidToImage(Fid fid) {
        try {
            byte[] imageData = fid.getData(); // Get the byte data from Fid
            ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
            System.out.println(imageData);
            return ImageIO.read(bais);
        } catch (IOException e) {
            System.out.println("Error converting Fid to Image: " + e.getMessage());
            return null;
        }
    }
}
