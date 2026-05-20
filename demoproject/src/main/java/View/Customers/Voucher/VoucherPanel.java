package View.Customers.Voucher;

import Common.TokenManager;
import Controller.ProfileDAO;
import Controller.Customers.Voucher.CustomerVoucherDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * Giao diện Khuyến mãi của Khách hàng
 * Hỗ trợ: Xem số điểm tích lũy, Đổi mã khuyến mãi từ điểm, Mở ví khuyến mãi cá nhân.
 */
public class VoucherPanel extends javax.swing.JPanel {

    private CustomerVoucherDAO voucherDAO;
    private ProfileDAO.Profile customerProfile;
    private String token;

    // UI Components
    private JLabel lblPointsVal;
    private JLabel lblMemberRank;
    private JPanel pnlAvailableGrid;

    private static final DecimalFormat DF = new DecimalFormat("#,###");
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    // Colors & Theme (Modern Shopee Palette)
    private static final Color SHOPEE_ORANGE = new Color(238, 77, 45);
    private static final Color GOLD_DARK = new Color(184, 134, 11);
    private static final Color GOLD_LIGHT = new Color(244, 208, 63);
    private static final Color TEXT_DARK = new Color(51, 65, 85);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color PANEL_BG = new Color(248, 250, 252);
    private static final Color CARD_BORDER = new Color(226, 232, 240);

    public VoucherPanel() {
        voucherDAO = new CustomerVoucherDAO();
        token = TokenManager.getLocalToken();
        loadProfileData();

        initComponentsCustom();
        loadAvailablePromotions();
    }

    private void loadProfileData() {
        if (token != null && !token.isEmpty()) {
            customerProfile = ProfileDAO.getProfileByToken(token);
        }
    }

    private void refreshProfileUI() {
        loadProfileData();
        if (customerProfile != null) {
            lblPointsVal.setText(DF.format(customerProfile.diemTichLuy) + " xu");
            
            // Xác định hạng thành viên
            String rank = "THÀNH VIÊN ĐỒNG";
            Color rankColor = Color.GRAY;
            if (customerProfile.diemTichLuy >= 2000) {
                rank = "⭐ THÀNH VIÊN KIM CƯƠNG";
                rankColor = new Color(125, 60, 152);
            } else if (customerProfile.diemTichLuy >= 1000) {
                rank = "✨ THÀNH VIÊN VÀNG";
                rankColor = new Color(212, 172, 13);
            } else if (customerProfile.diemTichLuy >= 500) {
                rank = "🛡️ THÀNH VIÊN BẠC";
                rankColor = new Color(127, 140, 141);
            }
            lblMemberRank.setText(rank);
            lblMemberRank.setForeground(rankColor);
        } else {
            lblPointsVal.setText("0 xu");
            lblMemberRank.setText("HẠNG THÀNH VIÊN");
        }
    }

    private void initComponentsCustom() {
        this.setLayout(new BorderLayout(0, 0));
        this.setBackground(PANEL_BG);

        // --- 1. HEADER SECTION: POINTS & WELCOME CARD ---
        JPanel pnlHeader = new JPanel(new BorderLayout(15, 0));
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(new EmptyBorder(15, 20, 10, 20));

        // Left Title label (tab name) instead of welcome text
        JLabel lblTitle = new JLabel("Khuyến mãi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(30, 41, 59));
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        // Right Header Panel containing the Reward Card and "Ví của tôi" button next to it
        JPanel pnlRightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlRightHeader.setOpaque(false);

        // Create "Ví của tôi" (My Wallet) button next to points badge
        JButton btnMyWallet = new JButton("💼 Ví của tôi");
        btnMyWallet.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnMyWallet.setBackground(Color.WHITE);
        btnMyWallet.setForeground(SHOPEE_ORANGE);
        btnMyWallet.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SHOPEE_ORANGE, 1, true),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));
        btnMyWallet.setFocusPainted(false);
        btnMyWallet.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMyWallet.addActionListener(e -> showMyWalletDialog());
        pnlRightHeader.add(btnMyWallet);

        // Right Golden Reward Card (Wow Card)
        JPanel pnlRewardCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gold gradient fill
                GradientPaint gp = new GradientPaint(0, 0, new Color(253, 242, 208), getWidth(), getHeight(), new Color(248, 196, 113));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                
                // Add soft border outline
                g2.setColor(new Color(245, 176, 65, 100));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        pnlRewardCard.setLayout(new GridBagLayout());
        pnlRewardCard.setBorder(new EmptyBorder(10, 20, 10, 20));
        pnlRewardCard.setPreferredSize(new Dimension(340, 75));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel lblPointsTitle = new JLabel("🪙 ĐIỂM TÍCH LŨY CỦA BẠN");
        lblPointsTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblPointsTitle.setForeground(GOLD_DARK);
        gbc.gridy = 0;
        pnlRewardCard.add(lblPointsTitle, gbc);

        lblPointsVal = new JLabel("0 xu");
        lblPointsVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblPointsVal.setForeground(new Color(120, 66, 18)); // Deep bronze brown
        gbc.gridy = 1;
        pnlRewardCard.add(lblPointsVal, gbc);

        lblMemberRank = new JLabel("HẠNG THÀNH VIÊN");
        lblMemberRank.setFont(new Font("Segoe UI", Font.BOLD, 10));
        gbc.gridy = 2;
        pnlRewardCard.add(lblMemberRank, gbc);

        pnlRightHeader.add(pnlRewardCard);
        pnlHeader.add(pnlRightHeader, BorderLayout.EAST);
        this.add(pnlHeader, BorderLayout.NORTH);

        // --- 2. MAIN CENTER BODY (Voucher Grid) ---
        pnlAvailableGrid = new JPanel(new GridLayout(0, 2, 20, 20));
        pnlAvailableGrid.setOpaque(false);
        pnlAvailableGrid.setBorder(new EmptyBorder(15, 20, 20, 20));

        JPanel pnlAvailWrapper = new JPanel(new BorderLayout());
        pnlAvailWrapper.setOpaque(false);
        pnlAvailWrapper.add(pnlAvailableGrid, BorderLayout.NORTH);

        JScrollPane scrollAvail = new JScrollPane(pnlAvailWrapper);
        scrollAvail.setBorder(new EmptyBorder(0, 20, 0, 20));
        scrollAvail.getVerticalScrollBar().setUnitIncrement(16);
        scrollAvail.getViewport().setBackground(PANEL_BG);

        this.add(scrollAvail, BorderLayout.CENTER);

        // Initial UI Bindings
        refreshProfileUI();
    }

    /**
     * Tải danh sách khuyến mãi khả dụng và hiển thị
     */
    public void loadAvailablePromotions() {
        pnlAvailableGrid.removeAll();
        try {
            List<Map<String, Object>> promos = voucherDAO.getAvailablePromotions();
            if (promos.isEmpty()) {
                showEmptyPlaceholder(pnlAvailableGrid, "Hiện không có voucher khuyến mãi nào khả dụng để quy đổi!");
            } else {
                for (Map<String, Object> promo : promos) {
                    pnlAvailableGrid.add(createVoucherCard(promo, false));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showEmptyPlaceholder(pnlAvailableGrid, "Lỗi tải danh sách khuyến mãi: " + e.getMessage());
        }
        pnlAvailableGrid.revalidate();
        pnlAvailableGrid.repaint();
    }

    /**
     * Mở dialog hiển thị ví khuyến mãi cá nhân của khách hàng
     */
    private void showMyWalletDialog() {
        if (customerProfile == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng đăng nhập để xem ví khuyến mãi cá nhân!",
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create dialog
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "💼 Ví Khuyến Mãi Của Tôi", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(680, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(PANEL_BG);

        // Dialog Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblWalletTitle = new JLabel("💼 Ví Khuyến Mãi Của Tôi");
        lblWalletTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblWalletTitle.setForeground(TEXT_DARK);
        headerPanel.add(lblWalletTitle, BorderLayout.WEST);

        JLabel lblPointsBadge = new JLabel("🪙 " + DF.format(customerProfile.diemTichLuy) + " xu");
        lblPointsBadge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPointsBadge.setForeground(GOLD_DARK);
        headerPanel.add(lblPointsBadge, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Wallet Content Grid
        JPanel walletGrid = new JPanel(new GridLayout(0, 2, 15, 15));
        walletGrid.setOpaque(false);
        walletGrid.setBorder(new EmptyBorder(15, 15, 15, 15));

        try {
            List<Map<String, Object>> myVouchers = voucherDAO.getMyVouchers(customerProfile.id);
            if (myVouchers.isEmpty()) {
                showEmptyPlaceholder(walletGrid, "Kho khuyến mãi của bạn đang trống! Hãy đổi điểm tích lũy lấy voucher ngay.");
            } else {
                for (Map<String, Object> myVoucher : myVouchers) {
                    walletGrid.add(createVoucherCard(myVoucher, true));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showEmptyPlaceholder(walletGrid, "Lỗi tải kho khuyến mãi cá nhân: " + e.getMessage());
        }

        JPanel walletWrapper = new JPanel(new BorderLayout());
        walletWrapper.setOpaque(false);
        walletWrapper.add(walletGrid, BorderLayout.NORTH);

        JScrollPane scrollWallet = new JScrollPane(walletWrapper);
        scrollWallet.setBorder(null);
        scrollWallet.getVerticalScrollBar().setUnitIncrement(16);
        scrollWallet.getViewport().setBackground(PANEL_BG);

        mainPanel.add(scrollWallet, BorderLayout.CENTER);

        // Footer with close button
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, CARD_BORDER));

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnClose.setForeground(Color.WHITE);
        btnClose.setBackground(SHOPEE_ORANGE);
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));
        btnClose.addActionListener(ev -> dialog.dispose());
        footerPanel.add(btnClose);

        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);
    }

    private void showEmptyPlaceholder(JPanel container, String text) {
        JPanel placeholder = new JPanel(new GridBagLayout());
        placeholder.setOpaque(false);
        placeholder.setPreferredSize(new Dimension(500, 200));

        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lbl.setForeground(TEXT_MUTED);
        placeholder.add(lbl);

        container.setLayout(new BorderLayout());
        container.add(placeholder, BorderLayout.CENTER);
    }

    /**
     * Tạo card voucher hình vé giảm giá cực kỳ đẹp mắt với semicircular ticket cutouts!
     */
    private JPanel createVoucherCard(Map<String, Object> data, boolean isWallet) {
        int maKM = (int) data.get("MA_KM");
        String tenKM = (String) data.get("TEN_KM");
        long giaTri = (long) data.get("GIA_TRI");
        String rangBuoc = (String) data.get("RANG_BUOC");
        java.util.Date ngayKT = (java.util.Date) data.get("NGAY_KET_THUC");
        String loaiKM = (String) data.get("TEN_LOAI_KM");

        int diemDoi = data.containsKey("DIEM_DOI") ? (int) data.get("DIEM_DOI") : 0;
        int soLuongCL = data.containsKey("SO_LUONG_CL") ? (int) data.get("SO_LUONG_CL") : 0;
        int soLuongSoHuu = data.containsKey("SO_LUONG") ? (int) data.get("SO_LUONG") : 0;

        // Custom Ticket Panel
        TicketPanel card = new TicketPanel(140); // 140px left division
        card.setLayout(new BorderLayout(0, 0));
        card.setPreferredSize(new Dimension(380, 115));
        card.setBackground(Color.WHITE);

        // --- A. LEFT PANEL (Ticket Stub - Value Display) ---
        JPanel pnlStub = new JPanel(new GridBagLayout());
        pnlStub.setOpaque(false);
        pnlStub.setPreferredSize(new Dimension(140, 115));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Discount value text formatting
        String valueStr = "";
        String unitStr = "";
        if (loaiKM != null && (loaiKM.toLowerCase().contains("phần trăm") || loaiKM.toLowerCase().contains("tỷ lệ"))) {
            valueStr = giaTri + "%";
            unitStr = "GIẢM GIÁ";
        } else {
            if (giaTri >= 1000) {
                valueStr = (giaTri / 1000) + "K";
            } else {
                valueStr = String.valueOf(giaTri);
            }
            unitStr = "GIẢM GIÁ";
        }

        if (loaiKM != null && loaiKM.toLowerCase().contains("vận chuyển")) {
            unitStr = "FREE SHIP";
        }

        JLabel lblVal = new JLabel(valueStr, SwingConstants.CENTER);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblVal.setForeground(isWallet ? TEXT_MUTED : SHOPEE_ORANGE);
        gbc.gridy = 0;
        pnlStub.add(lblVal, gbc);

        JLabel lblUnit = new JLabel(unitStr, SwingConstants.CENTER);
        lblUnit.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblUnit.setForeground(TEXT_MUTED);
        gbc.gridy = 1;
        pnlStub.add(lblUnit, gbc);

        card.add(pnlStub, BorderLayout.WEST);

        // --- B. RIGHT PANEL (Ticket Main Details & Actions) ---
        JPanel pnlMain = new JPanel(new GridBagLayout());
        pnlMain.setOpaque(false);
        pnlMain.setBorder(new EmptyBorder(10, 15, 10, 15));

        GridBagConstraints gbcMain = new GridBagConstraints();
        gbcMain.fill = GridBagConstraints.HORIZONTAL;
        gbcMain.weightx = 1.0;
        gbcMain.gridx = 0;

        // Title
        JLabel lblTitle = new JLabel(tenKM);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(TEXT_DARK);
        gbcMain.gridy = 0;
        pnlMain.add(lblTitle, gbcMain);

        // Description / Expiry
        String dateStr = ngayKT != null ? SDF.format(ngayKT) : "Không giới hạn";
        JLabel lblExpiry = new JLabel("HSD: " + dateStr);
        lblExpiry.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblExpiry.setForeground(TEXT_MUTED);
        gbcMain.gridy = 1;
        gbcMain.insets = new Insets(2, 0, 0, 0);
        pnlMain.add(lblExpiry, gbcMain);

        // Secondary Info: Point Requirement or Quantity Owned
        JLabel lblInfo = new JLabel();
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        if (isWallet) {
            lblInfo.setText("📦 Đã có: " + soLuongSoHuu + " lượt sử dụng");
            lblInfo.setForeground(new Color(40, 167, 69)); // Healthy green
        } else {
            lblInfo.setText("🪙 " + diemDoi + " điểm  |  Còn lại: " + soLuongCL);
            lblInfo.setForeground(GOLD_DARK);
        }
        gbcMain.gridy = 2;
        gbcMain.insets = new Insets(3, 0, 0, 0);
        pnlMain.add(lblInfo, gbcMain);

        // Actions: Link rules + Button
        JPanel pnlActions = new JPanel(new BorderLayout(5, 0));
        pnlActions.setOpaque(false);
        pnlActions.setPreferredSize(new Dimension(0, 26));

        // Details label acts like a hyperlink
        JLabel lblRules = new JLabel("<html><u>Thể lệ</u></html>");
        lblRules.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRules.setForeground(new Color(0, 123, 255));
        lblRules.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRules.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showVoucherDetailsDialog(tenKM, loaiKM, giaTri, rangBuoc, dateStr);
            }
        });
        pnlActions.add(lblRules, BorderLayout.WEST);

        // Big action button (Đổi mã or Sử dụng)
        JButton btnAction = new JButton();
        btnAction.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnAction.setFocusPainted(false);
        btnAction.setBorderPainted(false);
        btnAction.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (isWallet) {
            btnAction.setText("Dùng ngay");
            btnAction.setBackground(new Color(0, 123, 255));
            btnAction.setForeground(Color.WHITE);
            btnAction.addActionListener(e -> {
                JOptionPane.showMessageDialog(this, 
                    "Để sử dụng Voucher, vui lòng áp dụng trực tiếp tại màn hình mua hàng (Giỏ hàng/Thanh toán) của bạn!", 
                    "Hướng dẫn sử dụng", JOptionPane.INFORMATION_MESSAGE);
            });
        } else {
            btnAction.setText("Đổi điểm");
            
            // Check condition for gray/orange styling
            boolean isOutOfStock = (soLuongCL <= 0);
            boolean isNotEnoughPoints = (customerProfile == null || customerProfile.diemTichLuy < diemDoi);

            if (isOutOfStock) {
                btnAction.setText("Hết lượt");
                btnAction.setEnabled(false);
                btnAction.setBackground(new Color(200, 200, 200));
                btnAction.setForeground(Color.WHITE);
            } else if (isNotEnoughPoints) {
                btnAction.setBackground(new Color(220, 220, 220));
                btnAction.setForeground(TEXT_MUTED);
                btnAction.addActionListener(e -> {
                    JOptionPane.showMessageDialog(this, 
                        "Không đủ điểm! Vui lòng chọn mã khác hoặc tích lũy thêm điểm từ đơn hàng.", 
                        "Không đủ điểm tích lũy", JOptionPane.WARNING_MESSAGE);
                });
            } else {
                btnAction.setBackground(SHOPEE_ORANGE);
                btnAction.setForeground(Color.WHITE);
                btnAction.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(this, 
                        "Bạn có chắc muốn quy đổi " + diemDoi + " điểm để lấy voucher '" + tenKM + "' không?", 
                        "Xác nhận đổi điểm", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            voucherDAO.redeemVoucher(customerProfile.id, maKM, diemDoi);
                            JOptionPane.showMessageDialog(this, "Đã đổi mã thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                            
                            // Cập nhật lại toàn bộ UI
                            refreshProfileUI();
                            loadAvailablePromotions();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Quy đổi thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
            }
        }
        pnlActions.add(btnAction, BorderLayout.EAST);

        gbcMain.gridy = 3;
        gbcMain.insets = new Insets(5, 0, 0, 0);
        pnlMain.add(pnlActions, gbcMain);

        card.add(pnlMain, BorderLayout.CENTER);

        return card;
    }

    /**
     * Mở modal thông báo chi tiết thể lệ chương trình khuyến mãi cực kỳ chuyên nghiệp
     */
    private void showVoucherDetailsDialog(String name, String type, long value, String rule, String expiry) {
        String detailText = "<html>"
                + "<body style='font-family: Segoe UI, sans-serif; padding: 10px; color: #334155;'>"
                + "  <h3 style='margin: 0 0 10px 0; color: #ee4d2d;'>" + name + "</h3>"
                + "  <hr style='border: 0; border-top: 1px solid #e2e8f0; margin-bottom: 10px;'>"
                + "  <p><b>🎟️ Loại khuyến mãi:</b> " + (type != null ? type : "Giảm giá đặc biệt") + "</p>"
                + "  <p><b>💰 Giá trị giảm giá:</b> " + DF.format(value) + (type != null && type.contains("trăm") ? "%" : " VNĐ") + "</p>"
                + "  <p><b>📅 Hạn sử dụng (HSD):</b> " + expiry + "</p>"
                + "  <p><b>🏷️ Điều kiện áp dụng (Thể lệ):</b> " + (rule != null && !rule.trim().isEmpty() ? rule : "Áp dụng cho mọi đơn hàng không giới hạn giá trị tối thiểu.") + "</p>"
                + "  <p style='color: #64748b; font-size: 10px; margin-top: 20px; font-style: italic;'>"
                + "     * Hệ thống giữ quyền quyết định cuối cùng trong việc giải quyết tranh chấp liên quan đến quy đổi điểm."
                + "  </p>"
                + "</body>"
                + "</html>";

        JEditorPane ep = new JEditorPane("text/html", detailText);
        ep.setEditable(false);
        ep.setBackground(Color.WHITE);
        
        JScrollPane sp = new JScrollPane(ep);
        sp.setPreferredSize(new Dimension(360, 240));
        sp.setBorder(null);

        JOptionPane.showMessageDialog(this, sp, "Chi tiết thể lệ ưu đãi", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Custom JPanel để vẽ nét Ticket Coupon xịn mịn với Semicircular bites/cutouts ở giữa!
     */
    private static class TicketPanel extends JPanel {
        private final int splitX; // Vị trí đường đứt nét cắt dọc

        public TicketPanel(int splitX) {
            this.splitX = splitX;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int radius = 16;       // Bo tròn góc thẻ
            int cutRadius = 8;     // Bán kính vết khía bánh xe (semicircular ticket bite)

            // Tạo hình dạng thẻ cơ bản (Rounded Rect)
            RoundRectangle2D rect = new RoundRectangle2D.Float(0, 0, w, h, radius, radius);
            Area area = new Area(rect);

            // Cắt hai lỗ tròn khuyết ở phía trên và phía dưới đường ranh giới
            Ellipse2D topCut = new Ellipse2D.Float(splitX - cutRadius, -cutRadius, cutRadius * 2, cutRadius * 2);
            Ellipse2D botCut = new Ellipse2D.Float(splitX - cutRadius, h - cutRadius, cutRadius * 2, cutRadius * 2);
            area.subtract(new Area(topCut));
            area.subtract(new Area(botCut));

            // Vẽ nền
            g2.setColor(Color.WHITE);
            g2.fill(area);

            // Vẽ đường viền
            g2.setColor(CARD_BORDER);
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(area);

            // Vẽ đường chỉ khâu chấm gạch đứt đoạn (dashed vertical separator line)
            g2.setColor(new Color(203, 213, 225)); // Gray line
            float[] dash = {4f, 4f};
            g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.0f, dash, 0.0f));
            g2.drawLine(splitX, cutRadius, splitX, h - cutRadius);

            g2.dispose();
        }
    }
}
