package View.Customers.Promotion;

import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.BorderLayout;

/**
 * Placeholder for future promotion/discount features.
 */
public class PromotionPanel extends JPanel {
    public PromotionPanel() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("Chức năng Khuyến Mãi (Đang phát triển)", JLabel.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        add(label, BorderLayout.CENTER);
    }
}
