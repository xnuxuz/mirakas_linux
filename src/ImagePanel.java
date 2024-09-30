import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fid.Fiv;

public class ImagePanel extends JPanel {
    private static final long serialVersionUID = 5;
    private BufferedImage m_image;

    // Method to display the image
    public void showImage(Fid image) {
        Fiv view = image.getViews()[0];
        m_image = new BufferedImage(view.getWidth(), view.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        // Fill the buffered image with fingerprint data
        m_image.getRaster().setDataElements(0, 0, view.getWidth(), view.getHeight(), view.getImageData());

        repaint(); // Repaint to display the new image
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (m_image != null) {
            g.drawImage(m_image, 0, 0, this.getWidth(), this.getHeight(), null); // Scale the image to fit the panel
        }
    }
}
