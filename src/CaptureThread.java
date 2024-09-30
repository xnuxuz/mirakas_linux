// src/CaptureThread.java

import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.UareUException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * CaptureThread handles the fingerprint capture process in a separate thread.
 */
public class CaptureThread extends Thread {
    public static final String ACT_CAPTURE = "capture_thread_captured";

    /**
     * Inner class to represent a capture event.
     */
    public static class CaptureEvent extends ActionEvent {
        private static final long serialVersionUID = 101;

        public Reader.CaptureResult capture_result;
        public Reader.Status reader_status;
        public UareUException exception;

        public CaptureEvent(Object source, String action, Reader.CaptureResult cr, Reader.Status st, UareUException ex) {
            super(source, ActionEvent.ACTION_PERFORMED, action);
            this.capture_result = cr;
            this.reader_status = st;
            this.exception = ex;
        }
    }

    private ActionListener listener;
    private boolean cancel;
    private Reader reader;
    private Fid.Format format;
    private Reader.ImageProcessing imgProc;
    private CaptureEvent lastCaptureEvent;
    private static final Logger logger = Logger.getLogger(CaptureThread.class.getName());

    /**
     * Constructor for CaptureThread.
     *
     * @param reader   The fingerprint reader.
     * @param format   The FID format.
     * @param imgProc  The image processing type.
     * @param listener The ActionListener to handle capture events.
     */
    public CaptureThread(Reader reader, Fid.Format format, Reader.ImageProcessing imgProc, ActionListener listener) {
        this.reader = reader;
        this.format = format;
        this.imgProc = imgProc;
        this.listener = listener;
        this.cancel = false;
    }

    /**
     * Cancels the capture process.
     */
    public void cancelCapture() {
        this.cancel = true;
        try {
            reader.CancelCapture();
        } catch (UareUException e) {
            logger.log(Level.SEVERE, "Error canceling capture", e);
        }
    }

    /**
     * Retrieves the last capture event.
     *
     * @return The last CaptureEvent.
     */
    public CaptureEvent getLastCaptureEvent() {
        return lastCaptureEvent;
    }

    /**
     * The main run method that performs the capture.
     */
    @Override
    public void run() {
        try {
            // Wait for the reader to be ready
            while (!cancel) {
                Reader.Status status = reader.GetStatus();
                if (status.status == Reader.ReaderStatus.READY || status.status == Reader.ReaderStatus.NEED_CALIBRATION) {
                    break;
                } else if (status.status == Reader.ReaderStatus.BUSY) {
                    Thread.sleep(100);
                } else {
                    // Reader failure
                    notifyListener(ACT_CAPTURE, null, status, null);
                    return;
                }
            }

            if (cancel) {
                Reader.CaptureResult cr = new Reader.CaptureResult();
                cr.quality = Reader.CaptureQuality.CANCELED;
                notifyListener(ACT_CAPTURE, cr, null, null);
                return;
            }

            // Perform capture
            Reader.CaptureResult cr = reader.Capture(format, imgProc, reader.GetCapabilities().resolutions[0], -1);
            notifyListener(ACT_CAPTURE, cr, null, null);
        } catch (UareUException | InterruptedException e) {
            notifyListener(ACT_CAPTURE, null, null, e instanceof UareUException ? (UareUException) e : null);
        }
    }

    /**
     * Notifies the listener with the capture event.
     *
     * @param action The action command.
     * @param cr     The capture result.
     * @param st     The reader status.
     * @param ex     The exception, if any.
     */
    private void notifyListener(String action, Reader.CaptureResult cr, Reader.Status st, UareUException ex) {
        lastCaptureEvent = new CaptureEvent(this, action, cr, st, ex);
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.actionPerformed(lastCaptureEvent));
        }
    }
}
