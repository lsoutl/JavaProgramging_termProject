import java.awt.*;
import javax.swing.JPanel;

public class RoundedPanel extends JPanel {
    private final int arc;
    private final Color fill;
    private final Color outline;

    public RoundedPanel(int arc, Color fill, Color outline) {
        this.arc = arc;
        this.fill = fill;
        this.outline = outline;
        setOpaque(false);
    }

    public RoundedPanel(int arc, Color fill) {
        this(arc, fill, null);
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        if (outline != null) {
            g2.setColor(outline);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
