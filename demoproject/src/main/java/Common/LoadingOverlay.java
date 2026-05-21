package Common;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;

/**
 * Overlay bán trong suốt với spinner hình tròn xoay.
 * Đặt lên JLayeredPane hoặc add trực tiếp vào container.
 */
public class LoadingOverlay extends JPanel {

    private javax.swing.Timer spinTimer;
    private int angle = 0;
    private static final int SPINNER_SIZE = 48;
    private static final int ARC_STROKE = 5;
    private static final Color BG_COLOR = new Color(30, 30, 30, 120);
    private static final Color SPINNER_COLOR_START = new Color(99, 102, 241); // indigo-500
    private static final Color SPINNER_COLOR_END = new Color(168, 85, 247);  // purple-500

    public LoadingOverlay() {
        setOpaque(false);
        setVisible(false);

        spinTimer = new javax.swing.Timer(40, e -> {
            angle = (angle + 10) % 360;
            repaint();
        });
    }

    /**
     * Hiển thị overlay xoay trên parent container.
     * Tự động resize theo kích thước parent.
     */
    public void showLoading(Container parent) {
        if (parent == null) return;

        // Đảm bảo overlay nằm đúng vị trí và kích thước
        setBounds(0, 0, parent.getWidth(), parent.getHeight());

        // Thêm vào parent nếu chưa có
        if (getParent() != parent) {
            parent.add(this, 0); // index 0 = trên cùng
        }

        setVisible(true);
        angle = 0;
        spinTimer.start();
        parent.repaint();
    }

    /**
     * Ẩn overlay, dừng timer.
     */
    public void hideLoading() {
        spinTimer.stop();
        setVisible(false);
        Container parent = getParent();
        if (parent != null) {
            parent.remove(this);
            parent.repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // --- Vẽ nền bán trong suốt ---
        g2.setColor(BG_COLOR);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // --- Vẽ spinner (arc gradient) ---
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        int half = SPINNER_SIZE / 2;

        // Tạo gradient cho arc
        GradientPaint gradient = new GradientPaint(
                cx - half, cy - half, SPINNER_COLOR_START,
                cx + half, cy + half, SPINNER_COLOR_END
        );
        g2.setPaint(gradient);
        g2.setStroke(new BasicStroke(ARC_STROKE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Arc xoay
        Arc2D arc = new Arc2D.Double(
                cx - half, cy - half,
                SPINNER_SIZE, SPINNER_SIZE,
                angle, 270,
                Arc2D.OPEN
        );
        g2.draw(arc);

        // --- Vẽ chấm tròn ở đuôi arc cho đẹp hơn ---
        double tailAngle = Math.toRadians(angle);
        int dotX = cx + (int) (half * Math.cos(tailAngle));
        int dotY = cy - (int) (half * Math.sin(tailAngle));
        g2.setColor(SPINNER_COLOR_END);
        g2.fillOval(dotX - 4, dotY - 4, 8, 8);

        g2.dispose();
    }
}
