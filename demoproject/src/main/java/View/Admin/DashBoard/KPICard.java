package View.Admin.DashBoard;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Professional KPI Card component for BI dashboard.
 * Displays a metric with title, value, trend badge (^/v %) and accent color.
 */
public class KPICard extends JPanel {

    private final JLabel lblTitle;
    private final JLabel lblValue;
    private final JLabel lblTrend;
    private final JLabel lblIcon;
    private final JPanel accentBar;

    private final Color accentColor;
    private final Color tintColor;

    /**
     * @param title       Card title (e.g. "Tổng doanh thu")
     * @param icon        Emoji or text icon (e.g. "")
     * @param accentColor Primary accent color
     * @param tintColor   Light tint for the accent bar
     */
    public KPICard(String title, String icon, Color accentColor, Color tintColor) {
        this.accentColor = accentColor;
        this.tintColor = tintColor;

        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);
        setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 16, 14, 16)));

        // Left accent bar
        accentBar = new JPanel();
        accentBar.setBackground(tintColor);
        accentBar.setPreferredSize(new Dimension(5, 0));
        add(accentBar, BorderLayout.WEST);

        // Center content
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(0, 12, 0, 0));

        // Title
        lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitle.setForeground(new Color(100, 116, 139));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Value
        lblValue = new JLabel("—");
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblValue.setForeground(accentColor);
        lblValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblValue.setBorder(new EmptyBorder(2, 0, 4, 0));

        // Trend badge
        lblTrend = new JLabel(" ");
        lblTrend.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTrend.setForeground(new Color(100, 116, 139));
        lblTrend.setAlignmentX(Component.LEFT_ALIGNMENT);

        centerPanel.add(lblTitle);
        centerPanel.add(lblValue);
        centerPanel.add(lblTrend);
        add(centerPanel, BorderLayout.CENTER);

        // Icon at top-right
        lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        lblIcon.setForeground(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 160));
        lblIcon.setVerticalAlignment(SwingConstants.TOP);
        lblIcon.setBorder(new EmptyBorder(0, 8, 0, 0));
        add(lblIcon, BorderLayout.EAST);
    }

    /**
     * Update the displayed metric value.
     */
    public void setValue(String text) {
        lblValue.setText(text);
    }

    /**
     * Update the trend indicator.
     * @param percent  growth percentage (positive = up, negative = down)
     * @param label    comparison label, e.g. "so với tháng trước"
     */
    public void setTrend(double percent, String label) {
        if (percent == 0) {
            lblTrend.setText("— Không thay đổi " + label);
            lblTrend.setForeground(new Color(100, 116, 139));
            return;
        }
        String arrow = percent > 0 ? "^" : "v";
        Color color = percent > 0 ? new Color(5, 150, 105) : new Color(220, 38, 38);
        String text = String.format("%s %.1f%% %s", arrow, Math.abs(percent), label);
        lblTrend.setText(text);
        lblTrend.setForeground(color);
    }

    /**
     * Clear the trend label (when no comparison available).
     */
    public void clearTrend() {
        lblTrend.setText(" ");
    }
}
