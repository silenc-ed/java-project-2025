package View.Customers.Cart;

import View.Customers.Main;
import View.Customers.StockPanel.PacketPanel;
import Model.CartItem;
import Model.CartManager;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.text.DecimalFormat;

public class CartDrawer extends JPanel {
    private final int DRAWER_WIDTH = 350;
    private int currentX;
    private Timer animationTimer;
    private boolean isShowingDrawer = false;
    private JPanel contentPanel;
    private Main mainFrame;

    public CartDrawer(Main mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(200, 200, 200)));
        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel title = new JLabel("Giỏ hàng của bạn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        JButton closeBtn = new JButton("X");
        closeBtn.setFocusPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> hideDrawer());
        
        header.add(title, BorderLayout.CENTER);
        header.add(closeBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
        
        // Content
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JButton processBtn = new JButton("Xử lý giỏ hàng");
        processBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        processBtn.setBackground(new Color(40, 167, 69));
        processBtn.setForeground(Color.WHITE);
        processBtn.setFocusPainted(false);
        processBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        processBtn.addActionListener(e -> {
            hideDrawer();
            mainFrame.getMenu().setSelectedIndex(1);
            mainFrame.showForm(new PacketPanel());
        });
        footer.add(processBtn);
        add(footer, BorderLayout.SOUTH);
    }
    
    public boolean isShowingDrawer() {
        return isShowingDrawer;
    }
    
    public void toggleDrawer() {
        if (isShowingDrawer) hideDrawer();
        else showDrawer();
    }

    public void showDrawer() {
        if (isShowingDrawer) return;
        updateCartContent();
        isShowingDrawer = true;
        setVisible(true);
        if (animationTimer != null) animationTimer.stop();
        
        int parentWidth = mainFrame.getContentPane().getWidth();
        int targetX = parentWidth - DRAWER_WIDTH;
        int yOffset = mainFrame.getContentPane().getY();
        int h = mainFrame.getContentPane().getHeight();
        
        // Make sure it starts from outside if it was invisible
        currentX = parentWidth;
        setBounds(currentX, yOffset, DRAWER_WIDTH, h);
        
        animationTimer = new Timer(10, e -> {
            currentX -= 40; // speed
            if (currentX <= targetX) {
                currentX = targetX;
                animationTimer.stop();
            }
            setBounds(currentX, yOffset, DRAWER_WIDTH, h);
        });
        animationTimer.start();
    }

    public void hideDrawer() {
        if (!isShowingDrawer) return;
        isShowingDrawer = false;
        if (animationTimer != null) animationTimer.stop();
        
        int parentWidth = mainFrame.getContentPane().getWidth();
        int yOffset = mainFrame.getContentPane().getY();
        int h = mainFrame.getContentPane().getHeight();
        
        animationTimer = new Timer(10, e -> {
            currentX += 40; // speed
            if (currentX >= parentWidth) {
                currentX = parentWidth;
                animationTimer.stop();
                setVisible(false);
            }
            setBounds(currentX, yOffset, DRAWER_WIDTH, h);
        });
        animationTimer.start();
    }
    
    public void updateCartContent() {
        contentPanel.removeAll();
        java.util.List<CartItem> items = CartManager.getInstance().getItems();
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        
        if (items.isEmpty()) {
            JLabel emptyLabel = new JLabel("Giỏ hàng trống");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(Box.createRigidArea(new Dimension(0, 50)));
            contentPanel.add(emptyLabel);
        } else {
            for (CartItem item : items) {
                JPanel itemPanel = new JPanel(new BorderLayout(10, 10));
                itemPanel.setBackground(Color.WHITE);
                itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                    new EmptyBorder(10, 10, 10, 10)
                ));
                
                // Info
                JPanel info = new JPanel();
                info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
                info.setBackground(Color.WHITE);
                
                JLabel nameLabel = new JLabel("<html><body style='width: 200px'>" + item.getProduct().getTenSp() + "</body></html>");
                nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                info.add(nameLabel);
                
                if (item.getVariant() != null) {
                    JLabel varLabel = new JLabel(item.getVariant().getTenBienThe());
                    varLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    varLabel.setForeground(Color.GRAY);
                    info.add(varLabel);
                }
                
                JLabel priceLabel = new JLabel(formatter.format(item.getTotalPrice()) + "₫ (SL: " + item.getQuantity() + ")");
                priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                priceLabel.setForeground(new Color(220, 53, 69));
                info.add(priceLabel);
                
                // Remove button
                JButton removeBtn = new JButton("Xóa");
                removeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                removeBtn.setBackground(new Color(220, 53, 69));
                removeBtn.setForeground(Color.WHITE);
                removeBtn.setFocusPainted(false);
                removeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                removeBtn.addActionListener(e -> {
                    CartManager.getInstance().getItems().remove(item);
                    updateCartContent();
                });
                info.add(removeBtn);
                
                itemPanel.add(info, BorderLayout.CENTER);
                contentPanel.add(itemPanel);
            }
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
