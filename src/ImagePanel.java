import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Image;

public class ImagePanel extends JPanel {
    private Image image;

    public void showImage(Image img) {
        this.image = img;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(image != null){
            int x = (getWidth() - image.getWidth(null)) / 2;
            int y = (getHeight() - image.getHeight(null)) / 2;
            g.drawImage(image, x, y, this);
        }
    }
}
