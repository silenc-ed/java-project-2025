package View.Customers.Warranty;

import Controller.Customers.Warranty.WarrantyCheckingProcess;
import ConnectDB.ConnectionUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class WarrantyPanel extends javax.swing.JPanel {

    // Premium Color Palette
    private static final Color BG_PRIMARY = new Color(245, 246, 250);
    private static final Color BG_WHITE = Color.WHITE;
    private static final Color ACCENT_PRIMARY = new Color(79, 70, 229);    // Indigo
    private static final Color ACCENT_SECONDARY = new Color(99, 102, 241); // Light Indigo
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BORDER_LIGHT = new Color(226, 232, 240);
    
    // Status colors
    private static final Color STATUS_ACTIVE_BG = new Color(236, 253, 245);
    private static final Color STATUS_ACTIVE_FG = new Color(16, 185, 129);
    private static final Color STATUS_EXPIRED_BG = new Color(254, 242, 242);
    private static final Color STATUS_EXPIRED_FG = new Color(239, 68, 68);
    private static final Color STATUS_VOID_BG = new Color(255, 247, 237);
    private static final Color STATUS_VOID_FG = new Color(249, 115, 22);

    private JTextField sdtField;
    private JTextField imeiField;
    private JButton confirmButton;
    
    // Results elements
    private JPanel pnlResultCard;
    private JLabel lblResultTitle;
    private JLabel lblResultProductName;
    private JLabel lblResultProductVariant;
    private JLabel lblResultStatusBadge;
    private JLabel lblResultActivationDate;
    private JLabel lblResultExpirationDate;
    private JLabel lblResultImei;
    private JLabel lblResultRemainTime;

    public WarrantyPanel() {
        initComponentsCustom();
    }

    private void initComponentsCustom() {
        setLayout(new BorderLayout());
        setBackground(BG_PRIMARY);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        // ================== HEADER ==================
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("Tra cứu bảo hành");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subLabel = new JLabel("Nhập số điện thoại và số sê-ri (IMEI) cúa thiết bị để kiểm tra thời hạn bảo hành");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLabel.setForeground(TEXT_SECONDARY);
        subLabel.setBorder(new EmptyBorder(5, 0, 5, 0));

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_LIGHT);

        pnlHeader.add(titleLabel, BorderLayout.NORTH);
        pnlHeader.add(subLabel, BorderLayout.CENTER);
        pnlHeader.add(sep, BorderLayout.SOUTH);
        add(pnlHeader, BorderLayout.NORTH);

        // ================== MAIN CONTENT AREA ==================
        JPanel pnlMainBody = new JPanel();
        pnlMainBody.setLayout(new BoxLayout(pnlMainBody, BoxLayout.Y_AXIS));
        pnlMainBody.setOpaque(false);

        // 1. Search Box Panel (Card Layout)
        JPanel pnlSearchCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pnlSearchCard.setOpaque(false);
        pnlSearchCard.setBorder(new EmptyBorder(25, 30, 25, 30));
        pnlSearchCard.setMaximumSize(new Dimension(850, 160));
        pnlSearchCard.setPreferredSize(new Dimension(850, 160));
        pnlSearchCard.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.4;
        JLabel lblPhone = new JLabel("Số điện thoại / Email:");
        lblPhone.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPhone.setForeground(TEXT_SECONDARY);
        pnlSearchCard.add(lblPhone, gbc);

        gbc.gridx = 1; gbc.weightx = 0.4;
        JLabel lblImei = new JLabel("Số sê-ri (Serial / IMEI):");
        lblImei.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblImei.setForeground(TEXT_SECONDARY);
        pnlSearchCard.add(lblImei, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.4;
        sdtField = createStyledTextField("Số điện thoại hoặc Email");
        pnlSearchCard.add(sdtField, gbc);

        gbc.gridx = 1; gbc.weightx = 0.4;
        imeiField = createStyledTextField("Mã sê-ri cúa máy");
        pnlSearchCard.add(imeiField, gbc);

        gbc.gridx = 2; gbc.weightx = 0.2;
        confirmButton = new JButton("Xác nhận") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 123, 255));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        confirmButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFocusPainted(false);
        confirmButton.setContentAreaFilled(false);
        confirmButton.setBorder(new EmptyBorder(8, 20, 8, 20));
        confirmButton.setPreferredSize(new Dimension(120, 36));
        confirmButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                confirmButton.setBackground(new Color(0, 105, 217));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                confirmButton.setBackground(new Color(0, 123, 255));
            }
        });
        confirmButton.addActionListener(this::confirmButtonActionPerformed);
        pnlSearchCard.add(confirmButton, gbc);

        pnlMainBody.add(pnlSearchCard);
        pnlMainBody.add(Box.createVerticalStrut(20));

        // 2. Search Result Panel (Diagnostic Receipt Card)
        pnlResultCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                
                // Draw a beautiful blue receipt dashed line inside
                g2.setColor(BORDER_LIGHT);
                float[] dash = {4f, 4f};
                g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.0f, dash, 2f));
                g2.drawLine(20, 50, getWidth() - 20, 50);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pnlResultCard.setOpaque(false);
        pnlResultCard.setBorder(new EmptyBorder(15, 25, 20, 25));
        pnlResultCard.setMaximumSize(new Dimension(850, 280));
        pnlResultCard.setPreferredSize(new Dimension(850, 280));
        pnlResultCard.setLayout(new BorderLayout(0, 15));
        pnlResultCard.setVisible(false);

        // Result Card Header
        JPanel pnlResultHeader = new JPanel(new BorderLayout());
        pnlResultHeader.setOpaque(false);
        lblResultTitle = new JLabel("BIÊN NHẬN TRA CỨU BẢO HÀNH");
        lblResultTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblResultTitle.setForeground(TEXT_SECONDARY);
        pnlResultHeader.add(lblResultTitle, BorderLayout.WEST);

        lblResultStatusBadge = new JLabel("Còn hiệu lực") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblResultStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblResultStatusBadge.setBorder(new EmptyBorder(4, 12, 4, 12));
        lblResultStatusBadge.setOpaque(false);
        lblResultStatusBadge.setHorizontalAlignment(SwingConstants.CENTER);
        pnlResultHeader.add(lblResultStatusBadge, BorderLayout.EAST);
        pnlResultCard.add(pnlResultHeader, BorderLayout.NORTH);

        // Result Card Body (Fields details)
        JPanel pnlResultBody = new JPanel(new GridBagLayout());
        pnlResultBody.setOpaque(false);
        
        GridBagConstraints gbcRes = new GridBagConstraints();
        gbcRes.fill = GridBagConstraints.HORIZONTAL;
        gbcRes.insets = new Insets(8, 10, 8, 10);

        // Product Name Row
        gbcRes.gridx = 0; gbcRes.gridy = 0; gbcRes.weightx = 0.3;
        JLabel lblResProd = new JLabel("Thiết bị / Sản phẩm:");
        lblResProd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblResProd.setForeground(TEXT_SECONDARY);
        pnlResultBody.add(lblResProd, gbcRes);

        gbcRes.gridx = 1; gbcRes.weightx = 0.7;
        lblResultProductName = new JLabel("Điện thoại iPhone 15 Pro Max");
        lblResultProductName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblResultProductName.setForeground(TEXT_PRIMARY);
        pnlResultBody.add(lblResultProductName, gbcRes);

        // Product Variant Row
        gbcRes.gridx = 0; gbcRes.gridy++; gbcRes.weightx = 0.3;
        JLabel lblResVar = new JLabel("Phiên bản / Cấu hình:");
        lblResVar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblResVar.setForeground(TEXT_SECONDARY);
        pnlResultBody.add(lblResVar, gbcRes);

        gbcRes.gridx = 1; gbcRes.weightx = 0.7;
        lblResultProductVariant = new JLabel("Titan tự nhiên - 256GB");
        lblResultProductVariant.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblResultProductVariant.setForeground(TEXT_PRIMARY);
        pnlResultBody.add(lblResultProductVariant, gbcRes);

        // Serial Row
        gbcRes.gridx = 0; gbcRes.gridy++; gbcRes.weightx = 0.3;
        JLabel lblResSn = new JLabel("Mã số sê-ri / IMEI:");
        lblResSn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblResSn.setForeground(TEXT_SECONDARY);
        pnlResultBody.add(lblResSn, gbcRes);

        gbcRes.gridx = 1; gbcRes.weightx = 0.7;
        lblResultImei = new JLabel("123456789012345");
        lblResultImei.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblResultImei.setForeground(TEXT_PRIMARY);
        pnlResultBody.add(lblResultImei, gbcRes);

        // Activation Date Row
        gbcRes.gridx = 0; gbcRes.gridy++; gbcRes.weightx = 0.3;
        JLabel lblResAct = new JLabel("Ngày kích hoạt:");
        lblResAct.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblResAct.setForeground(TEXT_SECONDARY);
        pnlResultBody.add(lblResAct, gbcRes);

        gbcRes.gridx = 1; gbcRes.weightx = 0.7;
        lblResultActivationDate = new JLabel("20/10/2025");
        lblResultActivationDate.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblResultActivationDate.setForeground(TEXT_PRIMARY);
        pnlResultBody.add(lblResultActivationDate, gbcRes);

        // Expiration Date Row
        gbcRes.gridx = 0; gbcRes.gridy++; gbcRes.weightx = 0.3;
        JLabel lblResExp = new JLabel("Ngày hết hạn:");
        lblResExp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblResExp.setForeground(TEXT_SECONDARY);
        pnlResultBody.add(lblResExp, gbcRes);

        gbcRes.gridx = 1; gbcRes.weightx = 0.7;
        lblResultExpirationDate = new JLabel("20/10/2026");
        lblResultExpirationDate.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblResultExpirationDate.setForeground(TEXT_PRIMARY);
        pnlResultBody.add(lblResultExpirationDate, gbcRes);

        // Expiration Time Remaining Row
        gbcRes.gridx = 0; gbcRes.gridy++; gbcRes.weightx = 0.3;
        JLabel lblResRem = new JLabel("Thời gian bảo hành:");
        lblResRem.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblResRem.setForeground(TEXT_SECONDARY);
        pnlResultBody.add(lblResRem, gbcRes);

        gbcRes.gridx = 1; gbcRes.weightx = 0.7;
        lblResultRemainTime = new JLabel("Còn lại 150 ngày");
        lblResultRemainTime.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblResultRemainTime.setForeground(STATUS_ACTIVE_FG);
        pnlResultBody.add(lblResultRemainTime, gbcRes);

        pnlResultCard.add(pnlResultBody, BorderLayout.CENTER);
        pnlMainBody.add(pnlResultCard);

        add(pnlMainBody, BorderLayout.CENTER);
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_PRIMARY);
        tf.putClientProperty("JTextField.placeholderText", placeholder);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT, 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_SECONDARY, 1),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)
                ));
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)
                ));
            }
        });
        return tf;
    }

    private void confirmButtonActionPerformed(java.awt.event.ActionEvent evt) {
        String sdt = sdtField.getText().trim();
        String imei = imeiField.getText().trim();
        
        if (sdt.isEmpty() || imei.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin tra cứu!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show loading dialog
        final JDialog progressDlg = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), "Đang tra cứu", true);
        progressDlg.setLayout(new BorderLayout());
        JLabel lblStatus = new JLabel("Đang kết nối hệ thống bảo hành...", JLabel.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStatus.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        progressDlg.add(lblStatus, BorderLayout.CENTER);
        progressDlg.setSize(350, 100);
        progressDlg.setLocationRelativeTo(this);

        Date[] thoiGian = new Date[2];
        WarrantyCheckingProcess checkingWarranty = new WarrantyCheckingProcess();

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            private String prodName = "Thiết bị di động";
            private String prodConfig = "Tiêu chuẩn";
            
            @Override
            protected String doInBackground() throws Exception {
                // Fetch product details using BaoHanhDAO
                java.util.Map<String, String> details = Controller.Admin.BaoHanh.BaoHanhDAO.getProductDetailsByImei(imei);
                if (details != null && !details.isEmpty()) {
                    if (details.containsKey("TEN_SP")) {
                        prodName = details.get("TEN_SP");
                    }
                    if (details.containsKey("TEN_BIENTHE")) {
                        prodConfig = details.get("TEN_BIENTHE");
                    }
                }
                
                return checkingWarranty.checking(sdt, imei, thoiGian);
            }

            @Override
            protected void done() {
                progressDlg.dispose();
                try {
                    String result = get();
                    
                    if ("Không tìm thấy".equals(result)) {
                        pnlResultCard.setVisible(false);
                        JOptionPane.showMessageDialog(WarrantyPanel.this, 
                            "Không tìm thấy thông tin bảo hành cúa thiết bị này trong hệ thống!\nVui lòng kiểm tra lại Số sê-ri.", 
                            "Không tìm thấy", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    
                    // Format dates
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    String actDate = thoiGian[0] != null ? sdf.format(thoiGian[0]) : "Chưa kích hoạt";
                    String expDate = thoiGian[1] != null ? sdf.format(thoiGian[1]) : "Chưa kích hoạt";
                    
                    lblResultProductName.setText(prodName);
                    lblResultProductVariant.setText(prodConfig);
                    lblResultImei.setText(imei);
                    lblResultActivationDate.setText(actDate);
                    lblResultExpirationDate.setText(expDate);

                    if ("Còn hiệu lực".equals(result)) {
                        lblResultStatusBadge.setText("Còn hiệu lực");
                        lblResultStatusBadge.setBackground(STATUS_ACTIVE_BG);
                        lblResultStatusBadge.setForeground(STATUS_ACTIVE_FG);
                        
                        long diff = thoiGian[1].getTime() - System.currentTimeMillis();
                        long days = diff / (1000 * 60 * 60 * 24);
                        if (days > 0) {
                            lblResultRemainTime.setText("Còn lại " + days + " ngày bảo hành");
                        } else {
                            lblResultRemainTime.setText("Còn hiệu lực bảo hành");
                        }
                        lblResultRemainTime.setForeground(STATUS_ACTIVE_FG);
                        
                    } else if ("Hết hiệu lực".equals(result)) {
                        lblResultStatusBadge.setText("Hết hiệu lực");
                        lblResultStatusBadge.setBackground(STATUS_EXPIRED_BG);
                        lblResultStatusBadge.setForeground(STATUS_EXPIRED_FG);
                        
                        long diff = System.currentTimeMillis() - thoiGian[1].getTime();
                        long days = diff / (1000 * 60 * 60 * 24);
                        lblResultRemainTime.setText("Đã hết hạn cách đây " + days + " ngày");
                        lblResultRemainTime.setForeground(STATUS_EXPIRED_FG);
                        
                    } else {
                        lblResultStatusBadge.setText("Chưa kích hoạt / Vô hiệu lực");
                        lblResultStatusBadge.setBackground(STATUS_VOID_BG);
                        lblResultStatusBadge.setForeground(STATUS_VOID_FG);
                        lblResultRemainTime.setText("Thiết bị chưa được kích hoạt bảo hành");
                        lblResultRemainTime.setForeground(STATUS_VOID_FG);
                    }

                    pnlResultCard.setVisible(true);
                    WarrantyPanel.this.revalidate();
                    WarrantyPanel.this.repaint();
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(WarrantyPanel.this, "Lỗi khi xử lý dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
        progressDlg.setVisible(true);
    }
}
