/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package View.Customers.StockPanel;

/**
 *
 * @author DELL
 */
import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
public class PacketPanel extends javax.swing.JPanel {

    /**
     * Creates new form TongQuanPanel
     */
  private final Color COLOR_BG = new Color(245, 247, 250);
  

    // ================= COMPONENTS =================

    private JPanel productPanel;
    private JLabel lblSub;
    private JLabel lblTotalVal;
    private double discountFixed = 0;
    private double discountPercent = 0;

    // ================= CONSTRUCTOR =================

    public PacketPanel() {

        setupUI();
    }

    // =========================================================
    // MAIN UI
    // =========================================================

    private void setupUI() {

        setLayout(new BorderLayout(20, 20));

        setBackground(COLOR_BG);

        setBorder(new EmptyBorder(20, 20, 20, 20));

        // ================= HEADER =================

        JPanel headerContainer = new JPanel(new BorderLayout(15, 0));
        headerContainer.setOpaque(false);

        // Panel chứa text tiêu đề
        JPanel titleTextPanel = new JPanel();
        titleTextPanel.setLayout(new BoxLayout(titleTextPanel, BoxLayout.Y_AXIS));
        titleTextPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Giỏ hàng của bạn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));

        lblSub = new JLabel("Bạn có 0 sản phẩm trong giỏ hàng");
        lblSub.setForeground(Color.GRAY);

        titleTextPanel.add(lblTitle);
        titleTextPanel.add(lblSub);

        headerContainer.add(titleTextPanel, BorderLayout.CENTER);

        add(headerContainer, BorderLayout.NORTH);
        // ================= MAIN =================

        JPanel main = new JPanel(new BorderLayout(20, 0));

        main.setOpaque(false);

        // =====================================================
        // LEFT SIDE
        // =====================================================

        JPanel left = new JPanel();

        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        left.setOpaque(false);

        // PRODUCT SECTION

        productPanel = new JPanel();

        productPanel.setLayout(new BoxLayout(productPanel, BoxLayout.Y_AXIS));

        productPanel.setBackground(Color.WHITE);

        productPanel.setBorder(createCardBorder("Sản phẩm"));

        left.add(productPanel);

        left.add(Box.createVerticalStrut(20));

        // SHIPPING SECTION

        left.add(createShippingSection());

        JScrollPane scroll = new JScrollPane(left);

        scroll.setBorder(null);

        scroll.getViewport().setBackground(COLOR_BG);

        scroll.getVerticalScrollBar().setUnitIncrement(16);

        main.add(scroll, BorderLayout.CENTER);

        // =====================================================
        // RIGHT SIDE
        // =====================================================

        JPanel right = createRightPanel();

        main.add(right, BorderLayout.EAST);

        add(main, BorderLayout.CENTER);
        
        updateCartData();
    }

    // =========================================================
    // SHIPPING PANEL
    // =========================================================

    private JPanel createShippingSection() {

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBackground(Color.WHITE);

        panel.setBorder(createCardBorder("Thông tin giao hàng"));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.weightx = 1;

        JTextField txtName = new JTextField();

        JTextField txtPhone = new JTextField();

        JTextField txtAddress = new JTextField();

        JTextArea txtNote = new JTextArea(4, 20);

        txtNote.setLineWrap(true);

        txtNote.setWrapStyleWord(true);

        JLabel lblName = new JLabel("Họ và tên *");

        JLabel lblPhone = new JLabel("Số điện thoại *");

        JLabel lblAddress = new JLabel("Địa chỉ chi tiết *");

        JLabel lblNote = new JLabel("Ghi chú đơn hàng");

        // ===== ROW 1 =====

        gbc.gridx = 0;
        gbc.gridy = 0;

        panel.add(lblName, gbc);

        gbc.gridx = 1;

        panel.add(lblPhone, gbc);

        // ===== ROW 2 =====

        gbc.gridy = 1;
        gbc.gridx = 0;

        txtName.setPreferredSize(new Dimension(250, 40));

        panel.add(txtName, gbc);

        gbc.gridx = 1;

        txtPhone.setPreferredSize(new Dimension(250, 40));

        panel.add(txtPhone, gbc);

        // ===== ADDRESS =====

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        panel.add(lblAddress, gbc);

        gbc.gridy = 3;

        txtAddress.setPreferredSize(new Dimension(250, 40));

        panel.add(txtAddress, gbc);

        // ===== NOTE =====

        gbc.gridy = 4;

        panel.add(lblNote, gbc);

        gbc.gridy = 5;

        JScrollPane noteScroll = new JScrollPane(txtNote);

        noteScroll.setPreferredSize(new Dimension(300, 100));

        panel.add(noteScroll, gbc);

        // ===== SAVE BUTTON =====

        gbc.gridy = 6;
        gbc.insets = new Insets(15, 8, 8, 8); // Tăng khoảng cách phía trên nút

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);

        GradientButton btnSaveShipping = new GradientButton("Lưu thông tin", new Color(40, 167, 69), new Color(46, 204, 113));
        btnSaveShipping.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSaveShipping.setPreferredSize(new Dimension(150, 40));
        btnSaveShipping.addActionListener(e -> {
            // Validate data if needed, then show success message
            if (txtName.getText().trim().isEmpty() || txtPhone.getText().trim().isEmpty() || txtAddress.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Vui lòng nhập đầy đủ các thông tin bắt buộc (*)", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(panel, "Đã lưu thông tin giao hàng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });

        btnPanel.add(btnSaveShipping);
        panel.add(btnPanel, gbc);

        return panel;
    }

    // =========================================================
    // RIGHT PANEL
    // =========================================================

    private JPanel createRightPanel() {

        JPanel panel = new JPanel();

        panel.setPreferredSize(new Dimension(350, 0));

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.setOpaque(false);

        // ================= PROMO =================

        JPanel promo = new JPanel(new BorderLayout(10, 0));

        promo.setBackground(Color.WHITE);

        promo.setBorder(createCardBorder("Mã giảm giá"));

        promo.setPreferredSize(new Dimension(350, 100));

        promo.setMaximumSize(new Dimension(350, 100));

        String[] discountCodes = {
            "Chọn mã giảm giá...",
            "GIAM10K - Giảm 10.000đ",
            "FREESHIP - Miễn phí vận chuyển",
            "GIAM20% - Giảm 20% đơn hàng",
            "NEWUSER - Giảm 50.000đ cho KH mới"
        };
        JComboBox<String> cbPromo = new JComboBox<>(discountCodes);
        cbPromo.setBackground(Color.WHITE);
        cbPromo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbPromo.setCursor(new Cursor(Cursor.HAND_CURSOR));

        GradientButton btnApply = new GradientButton("Áp dụng", new Color(0, 123, 255), new Color(51, 153, 255));
        btnApply.setBorder(new EmptyBorder(5, 15, 5, 15));
        btnApply.addActionListener(e -> {
            int selected = cbPromo.getSelectedIndex();
            if (selected == 1) { // GIAM10K
                discountFixed = 10000;
                discountPercent = 0;
            } else if (selected == 2) { // FREESHIP
                discountFixed = 0;
                discountPercent = 0; 
            } else if (selected == 3) { // GIAM20%
                discountFixed = 0;
                discountPercent = 0.20;
            } else if (selected == 4) { // NEWUSER
                discountFixed = 50000;
                discountPercent = 0;
            } else { // Chọn mã giảm giá...
                discountFixed = 0;
                discountPercent = 0;
            }
            updateCartData(); // Cập nhật lại tổng tiền
            JOptionPane.showMessageDialog(panel, "Đã áp dụng mã giảm giá!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });

        promo.add(cbPromo, BorderLayout.CENTER);

        promo.add(btnApply, BorderLayout.EAST);

        panel.add(promo);

        panel.add(Box.createVerticalStrut(20));

        // ================= SUMMARY =================
        JPanel summary = new JPanel();
        summary.setLayout(new BoxLayout(summary, BoxLayout.Y_AXIS));
        summary.setOpaque(false);

        JLabel lblShippingVal = new JLabel("Miễn phí");
        lblShippingVal.setForeground(new Color(40, 167, 69));
        summary.add(createSummaryRow("Phí vận chuyển", lblShippingVal));
        summary.add(Box.createVerticalStrut(15));

        // Thêm đường kẻ ngang mảnh
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(230, 230, 230)); 
        summary.add(sep);
        summary.add(Box.createVerticalStrut(15));

        // Phần Tổng cộng (Làm to và đổi màu xanh)
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        JLabel lblTotalText = new JLabel("Tổng cộng");
        lblTotalText.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalVal = new JLabel("0đ");
        lblTotalVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTotalVal.setForeground(new Color(26, 115, 232)); // Màu xanh chủ đạo

        totalRow.add(lblTotalText, BorderLayout.WEST);
        totalRow.add(lblTotalVal, BorderLayout.EAST);
        summary.add(totalRow);

        summary.add(Box.createVerticalStrut(25));

        // ================= PAYMENT =================

        JLabel paymentTitle = new JLabel("Phương thức thanh toán");

        paymentTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));

        summary.add(paymentTitle);

        summary.add(Box.createVerticalStrut(10));

        JRadioButton cod = new JRadioButton("Tiền mặt (COD)");

        JRadioButton bank = new JRadioButton("Chuyển khoản");

        ButtonGroup group = new ButtonGroup();

        group.add(cod);

        group.add(bank);

        summary.add(cod);

        summary.add(bank);

        summary.add(Box.createVerticalStrut(25));

      GradientButton btnOrder = new GradientButton("Đặt hàng ngay", new Color(40, 167, 69), new Color(46, 204, 113));
      btnOrder.setFont(new Font("Segoe UI", Font.BOLD, 18));
      btnOrder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        btnOrder.setForeground(Color.WHITE);

        btnOrder.setFont(new Font("Segoe UI", Font.BOLD, 18));

        btnOrder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        btnOrder.setFocusPainted(false);

        summary.add(btnOrder);

        panel.add(summary);

        return panel;
    }

    // =========================================================
    // SUMMARY ROW
    // =========================================================

    private JPanel createSummaryRow(String left, JLabel rightLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel lblLeft = new JLabel(left);
        lblLeft.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblLeft.setForeground(new Color(120, 120, 120)); // Màu xám cho nhãn bên trái

        rightLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));

        row.add(lblLeft, BorderLayout.WEST);
        row.add(rightLabel, BorderLayout.EAST);

        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35)); // Tăng chiều cao dòng một chút
        return row;
    }

    // =========================================================
    // UPDATE CART DATA
    // =========================================================

    private void updateCartData() {
        productPanel.removeAll();
        java.util.List<Model.CartItem> items = Model.CartManager.getInstance().getItems();
        java.text.DecimalFormat formatter = new java.text.DecimalFormat("###,###,###");
        
        double total = 0;
        int totalQuantity = 0;
        
        if (items.isEmpty()) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setOpaque(false);
            emptyPanel.setBorder(new EmptyBorder(50, 20, 50, 20));
            JLabel emptyLabel = new JLabel("Chưa có sản phẩm");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            emptyLabel.setForeground(Color.GRAY);
            emptyPanel.add(emptyLabel);
            productPanel.add(emptyPanel);
        } else {
            for (Model.CartItem item : items) {
                total += item.getTotalPrice();
                totalQuantity += item.getQuantity();
                
                JPanel itemPanel = new JPanel(new BorderLayout(15, 15));
                itemPanel.setBackground(Color.WHITE);
                itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                    new EmptyBorder(15, 15, 15, 15)
                ));
                
                // Info
                JPanel info = new JPanel();
                info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
                info.setOpaque(false);
                
                JLabel nameLabel = new JLabel("<html><body style='width: 200px'>" + item.getProduct().getTenSp() + "</body></html>");
                nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                info.add(nameLabel);
                
                if (item.getVariant() != null) {
                    JLabel varLabel = new JLabel("Phân loại: " + item.getVariant().getTenBienThe());
                    varLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    varLabel.setForeground(Color.GRAY);
                    info.add(varLabel);
                }
                
                JLabel priceLabel = new JLabel("Giá: " + formatter.format(item.getTotalPrice()) + "đ (SL: " + item.getQuantity() + ")");
                priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                priceLabel.setForeground(new Color(220, 53, 69));
                info.add(priceLabel);
                
                // Actions (Right side of item)
                JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                actionPanel.setOpaque(false);
                JButton removeBtn = new JButton("Xóa");
                removeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                removeBtn.setBackground(new Color(220, 53, 69));
                removeBtn.setForeground(Color.WHITE);
                removeBtn.setFocusPainted(false);
                removeBtn.setBorder(new EmptyBorder(5, 15, 5, 15));
                removeBtn.addActionListener(e -> {
                    Model.CartManager.getInstance().getItems().remove(item);
                    updateCartData();
                });
                actionPanel.add(removeBtn);
                
                itemPanel.add(info, BorderLayout.CENTER);
                itemPanel.add(actionPanel, BorderLayout.EAST);
                
                productPanel.add(itemPanel);
            }
        }
        
        if (discountPercent > 0) {
            total = total - (total * discountPercent);
        }
        total = total - discountFixed;
        if (total < 0) total = 0;
        
        lblSub.setText("Bạn có " + totalQuantity + " sản phẩm trong giỏ hàng");
        lblTotalVal.setText(formatter.format(total) + "đ");
        
        // Revalidate and repaint
        productPanel.revalidate();
        productPanel.repaint();
    }

    // =========================================================
    // CARD BORDER
    // =========================================================

    private TitledBorder createCardBorder(String title) {

        Border line = BorderFactory.createLineBorder(
                new Color(220, 220, 220), 1);

        TitledBorder border = BorderFactory.createTitledBorder(
                line,
                title);

        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 16));

        return border;
    }
    private static class GradientButton extends JButton {
        private Color color1 = new Color(106, 76, 156); // Tím
        private Color color2 = new Color(230, 190, 210); // Hồng nhạt

        public GradientButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorder(new EmptyBorder(10, 20, 10, 20));
            setForeground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        public GradientButton(String text, Color c1, Color c2) {
            this(text);
            this.color1 = c1;
            this.color2 = c2;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Vẽ Gradient từ trái sang phải
            GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), 0, color2);
            g2d.setPaint(gp);
            
            // Vẽ hình chữ nhật bo góc (15px)
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

            super.paintComponent(g);
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        jLabel1.setText("Quản lí giỏ hàng");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(506, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 487, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    // End of variables declaration//GEN-END:variables
}
