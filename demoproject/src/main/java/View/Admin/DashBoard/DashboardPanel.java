package View.Admin.DashBoard;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class DashboardPanel extends JPanel {

    private JButton btnDoanhThu;
    private JButton btnSpThinhHanh;
    private JButton btnThongKeDonHang;
    private CardLayout cardLayout;
    private JPanel contentPanel;

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250));
        add(buildHeader(), BorderLayout.NORTH);
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);
        contentPanel.add(new RevenuePanel(), "DOANH_THU");
        contentPanel.add(new PopularProductPanel(), "SP_THINH_HANH");
        contentPanel.add(new StatisticsPanel(), "THONG_KE");
        add(contentPanel, BorderLayout.CENTER);
        activateTab(btnDoanhThu, "DOANH_THU");
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                new EmptyBorder(0, 20, 0, 20)));
        header.setPreferredSize(new Dimension(0, 52));

        JLabel lblTitle = new JLabel("Tổng quan");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(new Color(30, 41, 59));
        lblTitle.setBorder(new EmptyBorder(0, 0, 0, 28));

        btnDoanhThu = makeTabBtn("Doanh thu");
        btnSpThinhHanh = makeTabBtn("SP thịnh hành");
        btnThongKeDonHang = makeTabBtn("Thống kê đơn hàng");

        btnDoanhThu.addActionListener(e -> activateTab(btnDoanhThu, "DOANH_THU"));
        btnSpThinhHanh.addActionListener(e -> activateTab(btnSpThinhHanh, "SP_THINH_HANH"));
        btnThongKeDonHang.addActionListener(e -> activateTab(btnThongKeDonHang, "THONG_KE"));

        header.add(lblTitle);
        header.add(btnDoanhThu);
        header.add(btnSpThinhHanh);
        header.add(btnThongKeDonHang);
        return header;
    }

    private JButton makeTabBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(100, 116, 139));
        btn.setBorder(new EmptyBorder(16, 16, 14, 16));
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE,
                FlatClientProperties.BUTTON_TYPE_BORDERLESS);
        return btn;
    }

    private void activateTab(JButton active, String card) {
        for (JButton b : new JButton[]{btnDoanhThu, btnSpThinhHanh, btnThongKeDonHang}) {
            b.setForeground(new Color(100, 116, 139));
            b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            b.setBorder(new EmptyBorder(16, 16, 14, 16));
        }
        active.setForeground(new Color(37, 99, 235));
        active.setFont(new Font("Segoe UI", Font.BOLD, 13));
        active.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 2, 0, new Color(37, 99, 235)),
                new EmptyBorder(16, 16, 12, 16)));
        cardLayout.show(contentPanel, card);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
