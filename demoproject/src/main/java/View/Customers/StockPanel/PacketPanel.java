package View.Customers.StockPanel;

import Controller.Customers.Order.CustomerOrderDAO;
import java.awt.*;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class PacketPanel extends javax.swing.JPanel {

    private final Color COLOR_BG = new Color(245, 247, 250);

    // ================= COMPONENTS =================
    private JPanel productPanel;
    private JLabel lblSub;
    private JLabel lblTotalVal;
    private double discountFixed = 0;
    private double discountPercent = 0;

    private JTextField txtName, txtPhone, txtAddress;
    private JComboBox<VoucherItem> cbPromo;
    private Integer selectedMaKm = null;
    private JTextArea txtNote;
    private Preferences prefs;
    private JRadioButton cod;
    private JRadioButton bank;

    // ================= CONSTRUCTOR =================
    public PacketPanel() {
        prefs = Preferences.userRoot().node(this.getClass().getName());
        setupUI();
        loadPreferences();
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

        // LEFT SIDE: Only Products
        productPanel = new JPanel();
        productPanel.setLayout(new BoxLayout(productPanel, BoxLayout.Y_AXIS));
        productPanel.setBackground(Color.WHITE);

        JPanel productWrapper = new JPanel(new BorderLayout());
        productWrapper.setOpaque(false);
        productWrapper.add(productPanel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(productWrapper);
        scroll.setBorder(createCardBorder("Sản phẩm"));
        scroll.getViewport().setBackground(COLOR_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        main.add(scroll, BorderLayout.CENTER);

        // RIGHT SIDE: Shipping, Promo, Summary
        JPanel right = createRightPanel();
        
        // Wrap right side in scroll pane in case it gets too tall
        JScrollPane rightScroll = new JScrollPane(right);
        rightScroll.setBorder(null);
        rightScroll.getViewport().setBackground(COLOR_BG);
        rightScroll.getVerticalScrollBar().setUnitIncrement(16);
        rightScroll.setPreferredSize(new Dimension(420, 0));
        rightScroll.setMinimumSize(new Dimension(420, 0));

        main.add(rightScroll, BorderLayout.EAST);
        add(main, BorderLayout.CENTER);
        
        updateCartData();
    }

    // =========================================================
    // RIGHT PANEL (Shipping + Promo + Summary)
    // =========================================================
    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // ================= SHIPPING FORM =================
        JPanel shippingPanel = new JPanel(new GridBagLayout());
        shippingPanel.setBackground(Color.WHITE);
        shippingPanel.setBorder(createCardBorder("Thông tin đặt hàng"));
        shippingPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        shippingPanel.setMaximumSize(new Dimension(400, 420));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        txtName = new JTextField();
        txtPhone = new JTextField();
        txtAddress = new JTextField();
        txtNote = new JTextArea(3, 20);
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);

        gbc.gridx = 0; gbc.gridy = 0; shippingPanel.add(new JLabel("Họ và tên *"), gbc);
        gbc.gridy = 1; txtName.setPreferredSize(new Dimension(0, 42)); shippingPanel.add(txtName, gbc);
        
        gbc.gridy = 2; shippingPanel.add(new JLabel("Số điện thoại *"), gbc);
        gbc.gridy = 3; txtPhone.setPreferredSize(new Dimension(0, 42)); shippingPanel.add(txtPhone, gbc);
        
        gbc.gridy = 4; shippingPanel.add(new JLabel("Địa chỉ chi tiết *"), gbc);
        gbc.gridy = 5; txtAddress.setPreferredSize(new Dimension(0, 42)); shippingPanel.add(txtAddress, gbc);
        
        gbc.gridy = 6; shippingPanel.add(new JLabel("Ghi chú đơn hàng"), gbc);
        gbc.gridy = 7; 
        JScrollPane noteScroll = new JScrollPane(txtNote);
        noteScroll.setPreferredSize(new Dimension(0, 100));
        shippingPanel.add(noteScroll, gbc);

        panel.add(shippingPanel);
        panel.add(Box.createVerticalStrut(20));

        // ================= PROMO =================
        JPanel promo = new JPanel(new BorderLayout(10, 0));
        promo.setBackground(Color.WHITE);
        promo.setBorder(createCardBorder("Mã giảm giá"));
        promo.setAlignmentX(Component.CENTER_ALIGNMENT);
        promo.setMaximumSize(new Dimension(400, 80));

        cbPromo = new JComboBox<>();
        cbPromo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbPromo.setPreferredSize(new Dimension(0, 42));
        cbPromo.addItem(new VoucherItem(0, "Chọn mã giảm giá...", 0, ""));
        
        // Load user's vouchers
        try {
            Controller.ProfileDAO.Profile profile = Controller.ProfileDAO.getProfileByToken(Common.TokenManager.getLocalToken());
            if (profile != null) {
                Controller.Customers.Voucher.CustomerVoucherDAO vDao = new Controller.Customers.Voucher.CustomerVoucherDAO();
                java.util.List<java.util.Map<String, Object>> vouchers = vDao.getMyVouchers(profile.id);
                for (java.util.Map<String, Object> v : vouchers) {
                    int soLuong = (int) v.get("SO_LUONG");
                    if (soLuong > 0) {
                        cbPromo.addItem(new VoucherItem(
                            (int) v.get("MA_KM"),
                            (String) v.get("TEN_KM"),
                            (long) v.get("GIA_TRI"),
                            (String) v.get("TEN_LOAI_KM")
                        ));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        GradientButton btnApply = new GradientButton("Áp dụng", new Color(0, 123, 255), new Color(51, 153, 255));
        btnApply.setBorder(new EmptyBorder(5, 15, 5, 15));
        btnApply.addActionListener(e -> {
            VoucherItem selected = (VoucherItem) cbPromo.getSelectedItem();
            if (selected == null || selected.maKm == 0) {
                discountFixed = 0; discountPercent = 0; selectedMaKm = null;
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một mã giảm giá!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                updateCartData();
                return;
            }
            
            selectedMaKm = selected.maKm;
            if (selected.loaiKm != null && (selected.loaiKm.toLowerCase().contains("phần trăm") || selected.loaiKm.toLowerCase().contains("tỷ lệ"))) {
                discountPercent = selected.giaTri / 100.0;
                discountFixed = 0;
            } else {
                discountFixed = selected.giaTri;
                discountPercent = 0;
            }
            JOptionPane.showMessageDialog(this, "Áp dụng thành công Voucher: " + selected.tenKm, "Thành công", JOptionPane.INFORMATION_MESSAGE);
            updateCartData();
        });

        promo.add(cbPromo, BorderLayout.CENTER);
        promo.add(btnApply, BorderLayout.EAST);
        panel.add(promo);
        panel.add(Box.createVerticalStrut(20));

        // ================= SUMMARY & PAYMENT =================
        JPanel summary = new JPanel();
        summary.setLayout(new BoxLayout(summary, BoxLayout.Y_AXIS));
        summary.setBackground(Color.WHITE);
        summary.setBorder(createCardBorder("Thanh toán"));
        summary.setAlignmentX(Component.CENTER_ALIGNMENT);
        summary.setMaximumSize(new Dimension(400, 280));
        
        JPanel innerSummary = new JPanel();
        innerSummary.setLayout(new BoxLayout(innerSummary, BoxLayout.Y_AXIS));
        innerSummary.setOpaque(false);
        innerSummary.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        JLabel lblTotalText = new JLabel("Tổng cộng");
        lblTotalText.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalVal = new JLabel("0đ");
        lblTotalVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTotalVal.setForeground(new Color(26, 115, 232));

        totalRow.add(lblTotalText, BorderLayout.WEST);
        totalRow.add(lblTotalVal, BorderLayout.EAST);
        innerSummary.add(totalRow);
        innerSummary.add(Box.createVerticalStrut(25));

        JPanel paymentHeader = new JPanel(new BorderLayout());
        paymentHeader.setOpaque(false);
        JLabel paymentTitle = new JLabel("Phương thức thanh toán");
        paymentTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        paymentHeader.add(paymentTitle, BorderLayout.WEST);
        innerSummary.add(paymentHeader);
        innerSummary.add(Box.createVerticalStrut(10));

        cod = new JRadioButton("Tiền mặt (COD)");
        bank = new JRadioButton("Chuyển khoản");
        cod.setSelected(true);
        cod.setOpaque(false);
        bank.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        group.add(cod); group.add(bank);
        
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        radioPanel.setOpaque(false);
        radioPanel.add(cod);
        radioPanel.add(Box.createHorizontalStrut(20));
        radioPanel.add(bank);
        innerSummary.add(radioPanel);
        innerSummary.add(Box.createVerticalStrut(25));

        GradientButton btnOrder = new GradientButton("Đặt hàng ngay", new Color(40, 167, 69), new Color(46, 204, 113));
        btnOrder.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnOrder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btnOrder.addActionListener(e -> placeOrder());
        innerSummary.add(btnOrder);

        summary.add(innerSummary);

        panel.add(summary);
        return panel;
    }

    private void placeOrder() {
        if (txtName.getText().trim().isEmpty() || txtPhone.getText().trim().isEmpty() || txtAddress.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ các thông tin giao hàng bắt buộc (*)", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (Model.CartManager.getInstance().getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng của bạn đang trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Controller.ProfileDAO.Profile profile = Controller.ProfileDAO.getProfileByToken(Common.TokenManager.getLocalToken());
            long maKh = profile != null ? profile.id : 0;
            String paymentMethod = cod.isSelected() ? "Tiền mặt" : "Chuyển khoản";
            
            boolean success = CustomerOrderDAO.placeOrder(
                maKh, discountPercent, discountFixed, 
                Model.CartManager.getInstance().getItems(), paymentMethod, selectedMaKm
            );
            
            if (success) {
                savePreferences();
                JOptionPane.showMessageDialog(this, "Đặt hàng thành công! Đơn hàng đang được chuẩn bị.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                Model.CartManager.getInstance().getItems().clear();
                discountFixed = 0; discountPercent = 0; selectedMaKm = null;
                cbPromo.setSelectedIndex(0);
                updateCartData();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi đặt hàng", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void savePreferences() {
        prefs.put("name", txtName.getText().trim());
        prefs.put("phone", txtPhone.getText().trim());
        prefs.put("address", txtAddress.getText().trim());
    }

    private void loadPreferences() {
        txtName.setText(prefs.get("name", ""));
        txtPhone.setText(prefs.get("phone", ""));
        txtAddress.setText(prefs.get("address", ""));
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
        
        if (discountPercent > 0) total = total - (total * discountPercent);
        total = total - discountFixed;
        if (total < 0) total = 0;
        
        lblSub.setText("Bạn có " + totalQuantity + " sản phẩm trong giỏ hàng");
        lblTotalVal.setText(formatter.format(total) + "đ");
        
        productPanel.revalidate();
        productPanel.repaint();
    }

    private TitledBorder createCardBorder(String title) {
        Border line = BorderFactory.createLineBorder(new Color(220, 220, 220), 1);
        TitledBorder border = BorderFactory.createTitledBorder(line, title);
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 16));
        return border;
    }

    private static class GradientButton extends JButton {
        private Color color1 = new Color(106, 76, 156);
        private Color color2 = new Color(230, 190, 210);

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
            GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), 0, color2);
            g2d.setPaint(gp);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            super.paintComponent(g);
        }
    }

    private static class VoucherItem {
        int maKm;
        String tenKm;
        long giaTri;
        String loaiKm;
        public VoucherItem(int maKm, String tenKm, long giaTri, String loaiKm) {
            this.maKm = maKm; this.tenKm = tenKm; this.giaTri = giaTri; this.loaiKm = loaiKm;
        }
        @Override
        public String toString() {
            if (maKm == 0) return tenKm;
            String typeStr = (loaiKm != null && loaiKm.toLowerCase().contains("phần trăm")) ? "%" : "K";
            long displayVal = (typeStr.equals("K") && giaTri >= 1000) ? (giaTri / 1000) : giaTri;
            return tenKm + " (Giảm " + displayVal + typeStr + ")";
        }
    }
}
