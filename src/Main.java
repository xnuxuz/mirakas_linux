// src/Main.java

import com.digitalpersona.uareu.UareUGlobal;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;

import java.util.Scanner;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static Reader selectedReader;

    public static void main(String[] args) {
        try {
            // Initialize the UareU SDK
            ReaderCollection readers = UareUGlobal.GetReaderCollection();
            readers.GetReaders();

            if (readers.size() == 0) {
                logger.warning("No fingerprint readers found.");
                return;
            }

            // Display the list of available readers
            System.out.println("Available Fingerprint Readers:");
            for (int i = 0; i < readers.size(); i++) {
                Reader reader = readers.get(i);
                System.out.println((i + 1) + ". " + reader.GetDescription().name);
            }

            // Prompt user to select a reader
            Scanner scanner = new Scanner(System.in);
            System.out.print("Select a reader by number: ");
            int choice = scanner.nextInt();

            if (choice < 1 || choice > readers.size()) {
                System.out.println("Invalid selection.");
                return;
            }

            selectedReader = readers.get(choice - 1);
            selectedReader.Open(Reader.Priority.EXCLUSIVE);

            // Start fingerprint capture
            Capture.Run(selectedReader, false);

        } catch (UareUException e) {
            logger.log(Level.SEVERE, "UareUException occurred", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An unexpected error occurred", e);
        }
    }
}
