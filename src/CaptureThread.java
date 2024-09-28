// src/CaptureThread.java

import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.Reader.CaptureResult;
import com.digitalpersona.uareu.Reader.Status;
import com.digitalpersona.uareu.Reader.CaptureQuality;
import com.digitalpersona.uareu.Reader.ReaderStatus;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.UareUException;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CaptureThread extends Thread {
    public static final String ACT_CAPTURE = "capture_thread_captured";

    private boolean m_bCancel;
    private Reader m_reader;
    private boolean m_bStream;
    private Fid.Format m_format;
    private Reader.ImageProcessing m_proc;
    private CaptureEvent m_last_capture;
    private ActionListener m_listener; // Listener to notify

    // Custom capture event class
    public class CaptureEvent extends ActionEvent {
        private static final long serialVersionUID = 101L;
        public CaptureResult capture_result;
        public Status reader_status;
        public UareUException exception;

        public CaptureEvent(Object source, String action, CaptureResult cr, Status st, UareUException ex) {
            super(source, ActionEvent.ACTION_PERFORMED, action);
            capture_result = cr;
            reader_status = st;
            exception = ex;
        }
    }

    // Constructor accepting ActionListener
    public CaptureThread(Reader reader, boolean bStream, Fid.Format img_format, Reader.ImageProcessing img_proc, ActionListener listener) {
        m_bCancel = false;
        m_reader = reader;
        m_bStream = bStream;
        m_format = img_format;
        m_proc = img_proc;
        m_listener = listener;
    }

    // Cancel capture
    public void cancel() {
        m_bCancel = true;
        try {
            if (!m_bStream) m_reader.CancelCapture();
        } catch (UareUException e) {
            e.printStackTrace();
        }
    }

    public void join(int milliseconds){
		try{
			super.join(milliseconds);
		} 
		catch(InterruptedException e){ e.printStackTrace(); }
	}

    // Capture method
    private void capture() {
        synchronized (this) {
            try {
                boolean ready = false;
                while (!ready && !m_bCancel) {
                    Status rs = m_reader.GetStatus();
                    if (ReaderStatus.BUSY == rs.status) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    } else if (ReaderStatus.READY == rs.status || ReaderStatus.NEED_CALIBRATION == rs.status) {
                        ready = true;
                        break;
                    } else {
                        notifyListener(ACT_CAPTURE, null, rs, null);
                        break;
                    }
                }

                if (m_bCancel) {
                    CaptureResult cr = new CaptureResult();
                    cr.quality = CaptureQuality.CANCELED;
                    notifyListener(ACT_CAPTURE, cr, null, null);
                }

                if (ready) {
                    CaptureResult cr = m_reader.Capture(m_format, m_proc, m_reader.GetCapabilities().resolutions[0], -1);
                    notifyListener(ACT_CAPTURE, cr, null, null);
                }
            } catch (UareUException e) {
                notifyListener(ACT_CAPTURE, null, null, e);
            }
        }
    }

    // Streaming method
    private void stream() {
        try {
            boolean ready = false;
            while (!ready && !m_bCancel) {
                Status rs = m_reader.GetStatus();
                if (ReaderStatus.BUSY == rs.status) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                } else if (ReaderStatus.READY == rs.status || ReaderStatus.NEED_CALIBRATION == rs.status) {
                    ready = true;
                    break;
                } else {
                    notifyListener(ACT_CAPTURE, null, rs, null);
                    break;
                }
            }

            if (ready) {
                m_reader.StartStreaming();
                while (!m_bCancel) {
                    CaptureResult cr = m_reader.GetStreamImage(m_format, m_proc, 500);
                    notifyListener(ACT_CAPTURE, cr, null, null);
                }
                m_reader.StopStreaming();
            }
        } catch (UareUException e) {
            notifyListener(ACT_CAPTURE, null, null, e);
        }

        if (m_bCancel) {
            CaptureResult cr = new CaptureResult();
            cr.quality = CaptureQuality.CANCELED;
            notifyListener(ACT_CAPTURE, cr, null, null);
        }
    }

    // Notify listener method
    private void notifyListener(String action, CaptureResult cr, Status st, UareUException ex) {
        m_last_capture = new CaptureEvent(this, action, cr, st, ex);
        if (m_listener != null && action != null && !action.isEmpty()) {
            m_listener.actionPerformed(m_last_capture);
        }
    }

    @Override
    public void run() {
        if (m_bStream) {
            stream();
        } else {
            capture();
        }
    }
}
