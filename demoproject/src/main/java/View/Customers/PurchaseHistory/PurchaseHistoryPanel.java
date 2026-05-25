package View.Customers.PurchaseHistory;

import Controller.Customers.PurchaseHistory.PurchaseHistoryDAO;
import Controller.Customers.PurchaseHistory.PurchaseHistoryDAO.Order;
import Controller.Customers.PurchaseHistory.PurchaseHistoryDAO.OrderItem;
import Controller.ProfileDAO;
import Common.TokenManager;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class PurchaseHistoryPanel extends JPanel {

    private final Color COLOR_BG = new Color(245, 247, 250);
    private final Color COLOR_PRIMARY = new Color(26, 115, 232);
    private PurchaseHistoryDAO dao;
    private long currentCustomerId = -1;
    
    private JPanel listContainer;
    private ButtonGroup tabGroup;
    private String currentStatusFilter = "Tất cả";

    public PurchaseHistoryPanel() {
        dao = new PurchaseHistoryDAO();
        loadCustomerId();
        setupUI();
        loadOrders();
    }

    private void loadCustomerId() {
        ProfileDAO.Profile profile = ProfileDAO.getProfileByToken(TokenManager.getLocalToken());
        if (profile != null && "CUSTOMER".equals(profile.role)) {
            currentCustomerId = profile.id;
        }
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Lịch sử mua hàng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        headerPanel.add(lblTitle, BorderLayout.NORTH);

        // Status Tabs (Buttons)
        JPanel tabsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tabsPanel.setOpaque(false);
        tabsPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        String[] statuses = {"Tất cả", "Chờ thanh toán", "Đã thanh toán", "Đang chuẩn bị hàng", "Đang giao", "Hoàn thành", "Đã hủy"};
        tabGroup = new ButtonGroup();

        for (String status : statuses) {
            JToggleButton btnTab = new JToggleButton(status);
            btnTab.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            btnTab.setFocusPainted(false);
            btnTab.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnTab.setBackground(Color.WHITE);
            btnTab.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220)), 
                new EmptyBorder(8, 15, 8, 15)
            ));
            
            btnTab.addActionListener(e -> {
                currentStatusFilter = status;
                loadOrders();
            });
            
            tabGroup.add(btnTab);
            tabsPanel.add(btnTab);
            if (status.equals("Tất cả")) {
                btnTab.setSelected(true);
            }
        }
        
        headerPanel.add(tabsPanel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // List Container
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(COLOR_BG);

        // Wrap listContainer inside a wrapper with BorderLayout to prevent stretching
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(listContainer, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(COLOR_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private void loadOrders() {
        listContainer.removeAll();
        
        if (currentCustomerId == -1) {
            listContainer.add(createEmptyState("Vui lòng đăng nhập để xem lịch sử."));
            refreshUI();
            return;
        }

        List<Order> orders = dao.getCustomerOrders(currentCustomerId, currentStatusFilter);

        if (orders.isEmpty()) {
            listContainer.add(createEmptyState("Chưa có đơn hàng nào."));
        } else {
            for (Order order : orders) {
                listContainer.add(createOrderCard(order));
                listContainer.add(Box.createVerticalStrut(15));
            }
        }

        refreshUI();
    }

    private JPanel createEmptyState(String message) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(50, 0, 50, 0));
        JLabel lbl = new JLabel(message);
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        lbl.setForeground(Color.GRAY);
        p.add(lbl);
        return p;
    }

    private JPanel createOrderCard(Order order) {
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            new EmptyBorder(15, 20, 15, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        // Top: ID, Date, Status
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel lblIdDate = new JLabel("Đơn hàng #" + order.orderId + "  •  " + (order.orderDate != null ? sdf.format(order.orderDate) : ""));
        lblIdDate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JLabel lblStatus = new JLabel(order.status == null ? "Không rõ" : order.status);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblStatus.setForeground(getStatusColor(order.status));

        top.add(lblIdDate, BorderLayout.WEST);
        top.add(lblStatus, BorderLayout.EAST);
        
        // Mid: Items
        JPanel mid = new JPanel();
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
        mid.setOpaque(false);
        
        for (OrderItem item : order.items) {
            JPanel itemRow = new JPanel(new BorderLayout());
            itemRow.setOpaque(false);
            itemRow.setBorder(new EmptyBorder(5, 0, 5, 0));
            
            JLabel lblName = new JLabel("<html><body style='width: 300px'>" + item.productName + "</body></html>");
            lblName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            
            JLabel lblPriceQty = new JLabel(formatter.format(item.price) + "đ x " + item.quantity);
            lblPriceQty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblPriceQty.setForeground(Color.GRAY);
            
            itemRow.add(lblName, BorderLayout.WEST);
            itemRow.add(lblPriceQty, BorderLayout.EAST);
            mid.add(itemRow);
        }
        
        // Bottom: Total
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(240, 240, 240)),
            new EmptyBorder(10, 0, 0, 0)
        ));
        
        JLabel lblTotalText = new JLabel("Thành tiền:");
        lblTotalText.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        JLabel lblTotalVal = new JLabel(formatter.format(order.finalAmount) + "đ");
        lblTotalVal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalVal.setForeground(new Color(220, 53, 69));
        
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        totalPanel.setOpaque(false);
        totalPanel.add(lblTotalText);
        totalPanel.add(lblTotalVal);
        
        bottom.add(totalPanel, BorderLayout.EAST);

        card.add(top, BorderLayout.NORTH);
        card.add(mid, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    private Color getStatusColor(String status) {
        if (status == null) return Color.GRAY;
        switch (status) {
            case "Hoàn thành": 
            case "Đã giao":
            case "Đã thanh toán": return new Color(40, 167, 69); // Green
            case "Đang giao": 
            case "Đang chuẩn bị hàng":
            case "Đang xử lý": return new Color(0, 123, 255); // Blue
            case "Chờ thanh toán":
            case "Chờ xác nhận": return new Color(255, 193, 7); // Yellow/Orange
            case "Đã hủy": return new Color(220, 53, 69); // Red
            default: return Color.GRAY;
        }
    }

    private void refreshUI() {
        listContainer.revalidate();
        listContainer.repaint();
    }
}
