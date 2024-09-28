// src/MessageBox.java

import javax.swing.JOptionPane;
import com.digitalpersona.uareu.Reader;

public class MessageBox {
    public static void BadQuality(Reader.CaptureQuality quality){
        JOptionPane.showMessageDialog(null, "Bad capture quality: " + quality, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    public static void DpError(String context, Exception e){
        JOptionPane.showMessageDialog(null, "Error in " + context + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void Warning(String message){
        JOptionPane.showMessageDialog(null, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    public static void BadStatus(Reader.Status status){
        JOptionPane.showMessageDialog(null, "Reader status bad: " + status, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
