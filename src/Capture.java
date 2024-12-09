// src/Capture.java

import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.UareUException;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
// import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Base64;
/**
 * Capture class handles the fingerprint capture process.
 */
public class Capture {
    private static final Logger logger = Logger.getLogger(Capture.class.getName());

    /**
     * Runs the capture process and returns the captured fingerprint data.
     *
     * @param reader The fingerprint reader.
     * @return FingerprintData object containing the image and FMD, or null if capture failed or timed out.
     * @throws UareUException If an error occurs during capture.
     */
    public static Main.FingerprintData Run(Reader reader) throws UareUException {
        final CountDownLatch latch = new CountDownLatch(1);
        final FingerprintDataHolder holder = new FingerprintDataHolder();

        // Initialize CaptureThread with ActionListener using a lambda expression
        CaptureThread captureThread = new CaptureThread(reader, Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT, e -> {
            CaptureThread.CaptureEvent captureEvent = (CaptureThread.CaptureEvent) e;
            if (captureEvent.getActionCommand().equals(CaptureThread.ACT_CAPTURE)) {
                if (captureEvent.capture_result != null && captureEvent.capture_result.image != null) {
                    BufferedImage capturedImage = convertFidToImage(captureEvent.capture_result.image);
                    if (capturedImage != null) {
                        logger.info("Fingerprint captured successfully.");
                    } else {
                        logger.warning("Failed to convert Fid to Image.");
                    }

                    // Check the quality of the capture
                    if (captureEvent.capture_result.quality == Reader.CaptureQuality.GOOD) {
                        logger.info("Capture quality is good.");
                    } else {
                        logger.warning("Capture quality is not optimal: " + captureEvent.capture_result.quality);
                    }

                    // base64String
                    String base64Encode = imageToBase64(capturedImage);
                    // Extract FMDs if available
                    byte[] fmd = extractFMD(captureEvent.capture_result);
                    // Set FingerprintData even if fmd is null
                    holder.setFingerprintData(new Main.FingerprintData(capturedImage, fmd, base64Encode));
                } else if (captureEvent.exception != null) {
                    // Handle exceptions that occurred during capture
                    logger.severe("Error capturing fingerprint: " + captureEvent.exception.getMessage());
                    if (captureEvent.reader_status != null) {
                        logger.severe("Reader Status: " + captureEvent.reader_status.status);
                    }
                }

                // Notify the main thread that capture is complete
                latch.countDown();
            }
        });

        // Start the capture thread
        captureThread.start();

        // Wait for the capture to complete or timeout (1 minute)
        try {
            boolean completed = latch.await(60 * 60 * 2, java.util.concurrent.TimeUnit.SECONDS);
            if (!completed) {
                logger.warning("Capture timed out after: "+ completed);
                captureThread.cancelCapture();
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.SEVERE, "Capture process was interrupted.", e);
            captureThread.cancelCapture();
            return null;
        }

        // Retrieve the captured data
        return holder.getFingerprintData();
    }

    /**
     * Helper class to hold the captured fingerprint data.
     */
    private static class FingerprintDataHolder {
        private Main.FingerprintData fingerprintData;

        public synchronized void setFingerprintData(Main.FingerprintData data) {
            this.fingerprintData = data;
        }

        public synchronized Main.FingerprintData getFingerprintData() {
            return fingerprintData;
        }
    }

    /**
     * Converts an Image object to a Base64 encoded string.
     *
     * @param image The image to convert.
     * @return Base64 encoded string or null if conversion fails.
     */
    private static String imageToBase64(Image image) {
        if (image == null) {
            logger.warning("Cannot convert null image to Base64.");
            return null;
        }
        try {
            BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics g = bufferedImage.getGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", baos);
            byte[] bytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error converting image to Base64: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Converts a Fid image to a Java AWT BufferedImage object.
     *
     * @param fid The Fid object containing the image data.
     * @return BufferedImage or null if conversion fails.
     */
    private static BufferedImage convertFidToImage(Fid fid) {
        try {
            byte[] imageData = fid.getData(); // Get the byte data from Fid
            // logger.info("Image data length: " + imageData.length);

            // Determine the image format
            // String format = determineImageFormat(imageData);
            // logger.info("Detected image format: " + format);

            // Save to file for inspection (optional)
            // String fileExtension = format.equals("unknown") ? "bmp" : format;
            // java.nio.file.Files.write(java.nio.file.Paths.get("captured_fingerprint." + fileExtension), imageData);
            // logger.info("Image data saved to captured_fingerprint." + fileExtension);

            // Read the image
            ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
            BufferedImage bufferedImage = ImageIO.read(bais);
            if (bufferedImage == null) {
                // logger.warning("ImageIO.read() returned null. Attempting manual conversion.");
                bufferedImage = manualConvertFidToBufferedImage(fid);
            }
            return bufferedImage;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error converting Fid to Image: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Determines the image format based on byte signatures.
     *
     * @param imageData The byte array of the image.
     * @return The image format as a string.
     */
    // private static String determineImageFormat(byte[] imageData) {
    //     if (imageData.length > 4) {
    //         // BMP signature
    //         if (imageData[0] == 'B' && imageData[1] == 'M') {
    //             return "bmp";
    //         }
    //         // PNG signature
    //         if (imageData[0] == (byte) 137 && imageData[1] == 80 && imageData[2] == 78 && imageData[3] == 71) {
    //             return "png";
    //         }
    //         // JPEG signature
    //         if (imageData[0] == (byte) 0xFF && imageData[1] == (byte) 0xD8 &&
    //             imageData[imageData.length - 2] == (byte) 0xFF &&
    //             imageData[imageData.length - 1] == (byte) 0xD9) {
    //             return "jpg";
    //         }
    //     }
    //     return "unknown";
    // }

    /**
     * Manual conversion if ImageIO fails.
     *
     * @param fid The Fid object containing the image data.
     * @return BufferedImage or null if conversion fails.
     */
    private static BufferedImage manualConvertFidToBufferedImage(Fid fid) {
        try {
            Fid.Fiv view = fid.getViews()[0];
            byte[] imageData = view.getImageData();
            int width = view.getWidth();
            int height = view.getHeight();
            // logger.info("Manual Conversion - Width: " + width + ", Height: " + height);

            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            bufferedImage.getRaster().setDataElements(0, 0, width, height, imageData);

            // Save as PNG for verification
            // ImageIO.write(bufferedImage, "png", new File("fingerprint_manual.png"));
            // logger.info("Manually converted image saved as fingerprint_manual.png");
            return bufferedImage;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during manual conversion: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extracts the FMD (Fingerprint Minutiae Data) from the capture result.
     * Adjust this method based on how your SDK provides FMDs.
     *
     * @param captureResult The capture result from the reader.
     * @return Byte array representing the FMD, or null if extraction fails.
     */
    private static byte[] extractFMD(Reader.CaptureResult captureResult) {
        try {
            // Implement FMD extraction based on your SDK's capabilities
            // Example:
            // return captureResult.getFmd(); // Adjust based on SDK
            return null; // Replace with actual extraction if possible
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to extract FMD: " + e.getMessage(), e);
            return null;
        }
    }
}
