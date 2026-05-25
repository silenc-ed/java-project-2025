package View.Admin.CreateInvoice;

import Controller.Admin.DonDatHang.DonDatHangDAO;
import Model.KhachHang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateInvoicePanel extends javax.swing.JPanel {

    private DonDatHangDAO dao;

    // Cột trái
    private JTextField txtPhone, txtCustomerName;
    private Integer currentMaKH = null;

    private JTextField txtSearchSP;
    private JComboBox<ServiceItem> cbServices;

    private DefaultTableModel cartTableModel;
    private JTable cartTable;
    private List<CartItemMetadata> cartMetadata = new ArrayList<>();

    // Cột phải
    private JTextField txtPromoCode;
    private JLabel lblPromoResult;
    private int currentMaKM = -1;
    private long currentDiscount = 0;

    private JLabel lblTongSP, lblTongDVLK, lblGiamGia, lblThanhTien;

    private static final DecimalFormat DF = new DecimalFormat("#,###");
    private static final Color PURPLE_MAIN = new Color(142, 68, 173);
    private static final Color PURPLE_BORDER = new Color(175, 122, 197);
    private static final Color BG_COLOR = new Color(245, 247, 250);

    public CreateInvoicePanel() {
        dao = new DonDatHangDAO();
        setupUI();
        loadServiceCombo();
    }

    private void setupUI() {
        this.setLayout(new BorderLayout(15, 15));
        this.setBackground(BG_COLOR);
        this.setBorder(new EmptyBorder(15, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(226, 232, 240)));
        JLabel lblTitle = new JLabel("Tạo Đơn Đặt Hàng Mới");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 41, 59));
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        headerPanel.add(lblTitle, BorderLayout.WEST);
        this.add(headerPanel, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(15, 0));
        mainContent.setOpaque(false);

        mainContent.add(buildLeftColumn(), BorderLayout.CENTER);
        mainContent.add(buildRightColumn(), BorderLayout.EAST);

        this.add(mainContent, BorderLayout.CENTER);
    }

    private JPanel buildLeftColumn() {
        JPanel leftPanel = new JPanel(new BorderLayout(0, 15));
        leftPanel.setOpaque(false);

        leftPanel.add(buildCustomerSection(), BorderLayout.NORTH);

        JPanel cartCard = createCard("Chi tiết đơn hàng");
        cartCard.setLayout(new BorderLayout(0, 10));

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setOpaque(false);

        // Dòng 1: Tìm kiếm Sản phẩm
        JPanel rowSP = new JPanel(new BorderLayout(8, 0));
        rowSP.setOpaque(false);
        JLabel lblSP = makeLabel("Sản phẩm:");
        lblSP.setPreferredSize(new Dimension(75, 35));
        rowSP.add(lblSP, BorderLayout.WEST);
        
        txtSearchSP = new JTextField();
        txtSearchSP.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearchSP.setPreferredSize(new Dimension(0, 35));
        txtSearchSP.setToolTipText("Nhập Tên sản phẩm hoặc Serial/IMEI rồi ấn Enter");
        txtSearchSP.putClientProperty("JTextField.placeholderText", " Nhập tên sản phẩm hoặc Serial/IMEI để tìm kiếm...");
        
        JButton btnSearchSP = createFlatButton("Tìm / Thêm", new Color(16, 185, 129));
        btnSearchSP.setPreferredSize(new Dimension(120, 35));
        btnSearchSP.addActionListener(e -> addProduct());
        txtSearchSP.addActionListener(e -> addProduct());
        
        rowSP.add(txtSearchSP, BorderLayout.CENTER);
        rowSP.add(btnSearchSP, BorderLayout.EAST);

        // Dòng 2: Dịch vụ & Sửa chữa
        JPanel rowDV = new JPanel(new BorderLayout(8, 0));
        rowDV.setOpaque(false);
        rowDV.setBorder(new EmptyBorder(10, 0, 5, 0));

        JLabel lblDV = makeLabel("Dịch vụ:");
        lblDV.setPreferredSize(new Dimension(75, 35));
        rowDV.add(lblDV, BorderLayout.WEST);

        JPanel centerDV = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        centerDV.setOpaque(false);

        cbServices = new JComboBox<>();
        cbServices.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbServices.setPreferredSize(new Dimension(140, 35));
        cbServices.addActionListener(e -> {
            if (cbServices.getSelectedIndex() > 0) {
                addServiceOrRepair();
            }
        });
        centerDV.add(cbServices);

        rowDV.add(centerDV, BorderLayout.CENTER);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pnlBtns.setOpaque(false);
        JButton btnAddDV = createFlatButton("+ Thêm DV", new Color(59, 130, 246));
        btnAddDV.setPreferredSize(new Dimension(120, 35));
        btnAddDV.addActionListener(e -> addServiceOrRepair());
        
        JButton btnCreateRepair = createFlatButton("+ Tạo Phiếu SC", new Color(245, 158, 11));
        btnCreateRepair.setPreferredSize(new Dimension(135, 35));
        btnCreateRepair.addActionListener(e -> showRepairDialog());
        
        pnlBtns.add(btnAddDV);
        pnlBtns.add(btnCreateRepair);

        rowDV.add(pnlBtns, BorderLayout.EAST);

        inputPanel.add(rowSP);
        inputPanel.add(rowDV);
        cartCard.add(inputPanel, BorderLayout.NORTH);

        // -- Bảng Hợp Nhất --
        String[] cols = {"Loại", "Mã / Serial", "Tên mục", "SL", "Đơn giá", "Thành tiền", "Xóa"};
        cartTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 6; }
        };
        cartTable = buildTable(cartTableModel);
        
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(40);
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        cartTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        cartTable.getColumnModel().getColumn(6).setMaxWidth(40);
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        cartTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        cartTable.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        cartTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        
        cartTable.getColumnModel().getColumn(6).setCellRenderer(new DeleteBtnRenderer());
        cartTable.getColumnModel().getColumn(6).setCellEditor(new DeleteBtnEditor(cartTable, cartTableModel, this::handleRowDeleted));

        JScrollPane sp = new JScrollPane(cartTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        cartCard.add(sp, BorderLayout.CENTER);

        leftPanel.add(cartCard, BorderLayout.CENTER);
        return leftPanel;
    }

    private JPanel buildCustomerSection() {
        JPanel card = createCard("Thông tin khách hàng");
        card.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        card.add(makeLabel("Số điện thoại:"), g);

        g.gridx = 1; g.weightx = 0.5;
        txtPhone = new JTextField();
        txtPhone.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPhone.setPreferredSize(new Dimension(0, 35));
        card.add(txtPhone, g);

        g.gridx = 2; g.weightx = 0;
        JButton btnCheck = createFlatButton("Kiểm tra", PURPLE_MAIN);
        btnCheck.setPreferredSize(new Dimension(90, 35));
        btnCheck.addActionListener(e -> checkCustomer());
        txtPhone.addActionListener(e -> checkCustomer());
        card.add(btnCheck, g);

        g.gridx = 3; g.weightx = 0;
        card.add(makeLabel("  Họ tên:"), g);

        g.gridx = 4; g.weightx = 0.5;
        txtCustomerName = new JTextField();
        txtCustomerName.setEditable(false);
        txtCustomerName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtCustomerName.setPreferredSize(new Dimension(0, 35));
        txtCustomerName.setBackground(new Color(241, 245, 249));
        card.add(txtCustomerName, g);

        return card;
    }

    private JPanel buildRightColumn() {
        JPanel rightPanel = new JPanel(new BorderLayout(0, 15));
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(320, 0));

        JPanel totalCard = createCard("Tóm tắt thanh toán");
        totalCard.setLayout(new BoxLayout(totalCard, BoxLayout.Y_AXIS));

        Font fNormal = new Font("Segoe UI", Font.PLAIN, 14);

        lblTongSP = new JLabel("0đ", SwingConstants.RIGHT);
        lblTongDVLK = new JLabel("0đ", SwingConstants.RIGHT);
        lblGiamGia = new JLabel("0đ", SwingConstants.RIGHT);
        lblThanhTien = new JLabel("0đ", SwingConstants.RIGHT);

        lblTongSP.setFont(fNormal);
        lblTongDVLK.setFont(fNormal);
        lblGiamGia.setFont(fNormal); lblGiamGia.setForeground(new Color(220, 38, 38));
        lblThanhTien.setFont(new Font("Segoe UI", Font.BOLD, 22)); lblThanhTien.setForeground(PURPLE_MAIN);

        totalCard.add(makeTotalRow("Tiền sản phẩm:", lblTongSP));
        totalCard.add(Box.createVerticalStrut(10));
        totalCard.add(makeTotalRow("Dịch vụ & LK:", lblTongDVLK));
        totalCard.add(Box.createVerticalStrut(15));
        
        JPanel promoPanel = new JPanel(new BorderLayout(5, 0));
        promoPanel.setOpaque(false);
        txtPromoCode = new JTextField();
        txtPromoCode.putClientProperty("JTextField.placeholderText", "Mã KM");
        JButton btnApply = createFlatButton("Áp dụng", new Color(234, 88, 12));
        btnApply.addActionListener(e -> applyPromotion());
        promoPanel.add(txtPromoCode, BorderLayout.CENTER);
        promoPanel.add(btnApply, BorderLayout.EAST);
        totalCard.add(promoPanel);
        
        lblPromoResult = new JLabel(" ", SwingConstants.RIGHT);
        lblPromoResult.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblPromoResult.setAlignmentX(Component.RIGHT_ALIGNMENT);
        totalCard.add(lblPromoResult);

        totalCard.add(Box.createVerticalStrut(5));
        totalCard.add(makeTotalRow("Giảm giá:", lblGiamGia));
        totalCard.add(Box.createVerticalStrut(15));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        totalCard.add(sep);
        totalCard.add(Box.createVerticalStrut(15));

        totalCard.add(makeTotalRow("THÀNH TIỀN:", lblThanhTien));
        
        rightPanel.add(totalCard, BorderLayout.NORTH);

        JPanel actionPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        actionPanel.setOpaque(false);
        
        JButton btnSave = new JButton("TẠO ĐƠN HÀNG");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnSave.setBackground(PURPLE_MAIN);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.setPreferredSize(new Dimension(0, 50));
        btnSave.addActionListener(e -> saveOrder());
        if (!Controller.Admin.PermissionService.canAdd("Mua hang")) {
            btnSave.setVisible(false);
        }

        JButton btnReset = createFlatButton("Làm mới", new Color(148, 163, 184));
        btnReset.setPreferredSize(new Dimension(0, 40));
        btnReset.addActionListener(e -> resetForm());

        actionPanel.add(btnSave);
        actionPanel.add(btnReset);
        rightPanel.add(actionPanel, BorderLayout.SOUTH);

        return rightPanel;
    }

    private JPanel makeTotalRow(String labelText, JLabel valueLabel) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(new Color(71, 85, 105));
        if (labelText.equals("THÀNH TIỀN:")) {
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lbl.setForeground(new Color(30, 41, 59));
        }
        p.add(lbl, BorderLayout.WEST);
        p.add(valueLabel, BorderLayout.EAST);
        return p;
    }

    private void addProduct() {
        String keyword = txtSearchSP.getText().trim();
        if (keyword.isEmpty()) return;
        try {
            List<Map<String, Object>> res = dao.searchProductsForSale(keyword);
            if (res.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy sản phẩm khả dụng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Map<String, Object> selected = null;
            for (Map<String, Object> s : res) {
                if (keyword.equalsIgnoreCase((String) s.get("SERIAL_NUMBER"))) { selected = s; break; }
            }
            if (selected == null && res.size() == 1) selected = res.get(0);
            
            if (selected == null) {
                String[] options = new String[res.size()];
                for (int i = 0; i < res.size(); i++) {
                    Map<String, Object> s = res.get(i);
                    String snText = s.get("SERIAL_NUMBER") != null ? (String)s.get("SERIAL_NUMBER") : "SL Tồn: " + s.get("SO_LUONG_TON");
                    options[i] = snText + " | " + s.get("TEN_SP") + " | " + DF.format((long) s.get("GIA_BAN")) + "đ";
                }
                String choice = (String) JOptionPane.showInputDialog(this, 
                    "Có nhiều Sản phẩm/Serial khớp. Vui lòng chọn:", 
                    "Chọn Sản Phẩm", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                if (choice == null) return;
                selected = res.get(java.util.Arrays.asList(options).indexOf(choice));
            }

            String sn = (String) selected.get("SERIAL_NUMBER");
            int isSerial = (int) selected.get("CO_QUAN_LY_SERIAL");
            int soLuongTon = (int) selected.get("SO_LUONG_TON");
            long giaBan = (long) selected.get("GIA_BAN");
            int maBienThe = (int) selected.get("MA_BIENTHE");
            String tenHienThi = selected.get("TEN_SP") + " (" + selected.get("TEN_BIENTHE") + ")";
            int soLuong = 1;

            if (isSerial == 1) {
                for (CartItemMetadata meta : cartMetadata) {
                    if ("SP".equals(meta.type) && sn.equals(meta.serialOrIdText)) {
                        JOptionPane.showMessageDialog(this, "Serial này đã có trong giỏ hàng!", "Trùng lặp", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
                if (dao.isSerialInPendingOrder(sn, 0)) {
                    JOptionPane.showMessageDialog(this, "Serial " + sn + " đã nằm trong một đơn 'Chờ thanh toán' khác!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                String input = JOptionPane.showInputDialog(this, "Sản phẩm không quản lý Serial. Nhập số lượng (Tồn: " + soLuongTon + "):", "1");
                if (input == null) return;
                try {
                    soLuong = Integer.parseInt(input.trim());
                    if (soLuong <= 0 || soLuong > soLuongTon) {
                        JOptionPane.showMessageDialog(this, "Số lượng không hợp lệ hoặc vượt tồn kho!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            String displayId = (sn != null) ? sn : "SP#" + selected.get("MA_SP");
            cartTableModel.addRow(new Object[]{ "Sản phẩm", displayId, tenHienThi, soLuong, DF.format(giaBan) + "đ", DF.format(giaBan * soLuong) + "đ", "✕" });
            cartMetadata.add(new CartItemMetadata("SP", displayId, (int) selected.get("MA_SP"), giaBan, soLuong, maBienThe));
            
            txtSearchSP.setText("");
            updateTotals();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addServiceOrRepair() {
        boolean added = false;
        
        ServiceItem selDV = (ServiceItem) cbServices.getSelectedItem();
        if (selDV != null && selDV.id != -1) {
            boolean dup = false;
            for (CartItemMetadata m : cartMetadata) {
                if ("DV".equals(m.type) && m.maId == selDV.id) { dup = true; break; }
            }
            if (!dup) {
                cartTableModel.addRow(new Object[]{ "Dịch vụ", "DV#" + selDV.id, selDV.name, 1, DF.format(selDV.giaCuoc) + "đ", DF.format(selDV.giaCuoc) + "đ", "✕" });
                cartMetadata.add(new CartItemMetadata("DV", "DV#" + selDV.id, selDV.id, selDV.giaCuoc, 1, null));
                added = true;
                cbServices.setSelectedIndex(0);
            }
        }

        if (added) updateTotals();
    }

    private void showRepairDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Tạo Phiếu Sửa Chữa", true);
        dialog.setSize(750, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);
        
        // Header
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new EmptyBorder(15, 20, 10, 20));
        JLabel lblTitle = new JLabel("TẠO PHIẾU SỬA CHỮA");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(PURPLE_MAIN);
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        dialog.add(pnlHeader, BorderLayout.NORTH);
        
        // Main Content Split
        JPanel pnlContent = new JPanel(new BorderLayout(15, 15));
        pnlContent.setBackground(Color.WHITE);
        pnlContent.setBorder(new EmptyBorder(10, 20, 20, 20));
        
        // Top: Form info
        JPanel pnlForm = createCard("Thông tin Sửa chữa");
        pnlForm.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8); g.fill = GridBagConstraints.HORIZONTAL;
        
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        pnlForm.add(makeLabel("Mô tả lỗi / Yêu cầu:"), g);
        g.gridx = 1; g.weightx = 1.0;
        JTextField txtMoTa = new JTextField();
        txtMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMoTa.setPreferredSize(new Dimension(0, 35));
        pnlForm.add(txtMoTa, g);
        
        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        pnlForm.add(makeLabel("Tiền công sửa (VNĐ):"), g);
        g.gridx = 1; g.weightx = 1.0;
        JTextField txtGiaCuoc = new JTextField("0");
        txtGiaCuoc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtGiaCuoc.setPreferredSize(new Dimension(0, 35));
        pnlForm.add(txtGiaCuoc, g);
        pnlContent.add(pnlForm, BorderLayout.NORTH);
        
        // Center: Parts list
        JPanel pnlParts = createCard("Chi tiết sử dụng linh kiện");
        pnlParts.setLayout(new BorderLayout(0, 10));
        
        // Search bar
        JPanel pnlSearchPart = new JPanel(new BorderLayout(8, 0));
        pnlSearchPart.setOpaque(false);
        JTextField txtSearchPart = new JTextField();
        txtSearchPart.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearchPart.setPreferredSize(new Dimension(0, 35));
        txtSearchPart.putClientProperty("JTextField.placeholderText", " Nhập mã hoặc tên linh kiện...");
        JButton btnAddPart = createFlatButton("Tìm & Thêm LK", new Color(16, 185, 129));
        btnAddPart.setPreferredSize(new Dimension(140, 35));
        
        pnlSearchPart.add(txtSearchPart, BorderLayout.CENTER);
        pnlSearchPart.add(btnAddPart, BorderLayout.EAST);
        pnlParts.add(pnlSearchPart, BorderLayout.NORTH);
        
        // Parts Table
        DefaultTableModel partModel = new DefaultTableModel(new String[]{"Mã SP", "Tên Linh Kiện", "SL", "Đơn giá", "Thành tiền", "Xóa"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 5; }
        };
        JTable partTable = buildTable(partModel);
        partTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        partTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        partTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        partTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        partTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        partTable.getColumnModel().getColumn(5).setMaxWidth(50);
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        partTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        partTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        
        JScrollPane spPart = new JScrollPane(partTable);
        spPart.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        pnlParts.add(spPart, BorderLayout.CENTER);
        pnlContent.add(pnlParts, BorderLayout.CENTER);
        
        dialog.add(pnlContent, BorderLayout.CENTER);
        
        // Data structure
        List<DonDatHangDAO.RepairPartDraft> parts = new ArrayList<>();
        
        // Setup Delete button for Part Table
        partTable.getColumnModel().getColumn(5).setCellRenderer(new DeleteBtnRenderer());
        partTable.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private JButton btn;
            private int rowToDelete = -1;
            {
                btn = new JButton("✕");
                btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
                btn.setForeground(new Color(220, 38, 38));
                btn.setBorderPainted(false); btn.setContentAreaFilled(false);
                btn.addActionListener(e -> { rowToDelete = partTable.getSelectedRow(); fireEditingStopped(); });
            }
            @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { return btn; }
            @Override public Object getCellEditorValue() { return "✕"; }
            @Override protected void fireEditingStopped() {
                super.fireEditingStopped();
                if (rowToDelete >= 0 && rowToDelete < parts.size()) {
                    parts.remove(rowToDelete);
                    partModel.removeRow(rowToDelete);
                    rowToDelete = -1;
                }
            }
        });
        
        // Logic add part
        java.awt.event.ActionListener searchAction = e -> {
            String kw = txtSearchPart.getText().trim();
            if(kw.isEmpty()) return;
            try {
                List<Map<String, Object>> res = dao.searchVariantsForRepair(kw);
                if(res.isEmpty()) { JOptionPane.showMessageDialog(dialog, "Không tìm thấy linh kiện!", "Thông báo", JOptionPane.WARNING_MESSAGE); return; }
                
                String[] opts = new String[res.size()];
                for(int i=0; i<res.size(); i++) {
                    opts[i] = res.get(i).get("TEN_SP") + " (Tồn: " + res.get(i).get("SO_LUONG_TON") + ") | Giá: " + DF.format((long)res.get(i).get("GIA_BAN")) + "đ";
                }
                String ch = (String) JOptionPane.showInputDialog(dialog, "Chọn Linh Kiện cần thêm:", "Kết quả tìm kiếm", JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
                if(ch == null) return;
                
                int idx = java.util.Arrays.asList(opts).indexOf(ch);
                Map<String, Object> sel = res.get(idx);
                
                int maSP = (int) sel.get("MA_SP");
                int maBT = (int) sel.get("MA_BIENTHE");
                String tenSP = (String) sel.get("TEN_SP");
                long donGia = (long) sel.get("GIA_BAN");
                int isSerial = (int) sel.get("CO_QUAN_LY_SERIAL");
                
                int sl = 0;
                List<String> selectedSerials = new ArrayList<>();
                
                if (isSerial == 1) {
                    List<String> serials = dao.getAvailableSerialsForVariant(maBT);
                    if (serials.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "Không còn serial khả dụng cho biến thể này!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    JPanel pnlCheckbox = new JPanel();
                    pnlCheckbox.setLayout(new BoxLayout(pnlCheckbox, BoxLayout.Y_AXIS));
                    List<JCheckBox> cbList = new ArrayList<>();
                    for (String s : serials) {
                        JCheckBox cb = new JCheckBox(s);
                        cbList.add(cb);
                        pnlCheckbox.add(cb);
                    }
                    JScrollPane scroll = new JScrollPane(pnlCheckbox);
                    scroll.setPreferredSize(new Dimension(250, 200));
                    
                    int resDlg = JOptionPane.showConfirmDialog(dialog, scroll, "Chọn Serial", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (resDlg != JOptionPane.OK_OPTION) return;
                    
                    for (JCheckBox cb : cbList) {
                        if (cb.isSelected()) selectedSerials.add(cb.getText());
                    }
                    if (selectedSerials.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "Bạn chưa chọn serial nào!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    sl = selectedSerials.size();
                } else {
                    String slStr = JOptionPane.showInputDialog(dialog, "Nhập số lượng sử dụng (Tồn kho: " + sel.get("SO_LUONG_TON") + "):", "1");
                    if(slStr == null) return;
                    sl = Integer.parseInt(slStr.trim());
                    if (sl <= 0 || sl > (int)sel.get("SO_LUONG_TON")) {
                        JOptionPane.showMessageDialog(dialog, "Số lượng không hợp lệ hoặc vượt tồn kho!", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
                    }
                }
                
                DonDatHangDAO.RepairPartDraft partDraft = new DonDatHangDAO.RepairPartDraft(maSP, maBT, tenSP, sl, donGia);
                partDraft.serials = selectedSerials;
                parts.add(partDraft);
                
                partModel.addRow(new Object[]{"SP#"+maSP, tenSP, sl, DF.format(donGia)+"đ", DF.format(donGia*sl)+"đ", "✕"});
                txtSearchPart.setText("");
            } catch(Exception ex) { ex.printStackTrace(); }
        };
        btnAddPart.addActionListener(searchAction);
        txtSearchPart.addActionListener(searchAction);
        
        // Footer buttons
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlFooter.setBackground(new Color(248, 250, 252));
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));
        
        JButton btnCancel = createFlatButton("Hủy bỏ", new Color(148, 163, 184));
        btnCancel.setPreferredSize(new Dimension(100, 40));
        btnCancel.addActionListener(e -> dialog.dispose());
        
        JButton btnOk = createFlatButton("Hoàn tất & Đưa vào Giỏ", PURPLE_MAIN);
        btnOk.setPreferredSize(new Dimension(200, 40));
        btnOk.addActionListener(e -> {
            try {
                long gc = Long.parseLong(txtGiaCuoc.getText().replace(",", "").trim());
                String mt = txtMoTa.getText();
                if (mt.trim().isEmpty()) { JOptionPane.showMessageDialog(dialog, "Vui lòng nhập mô tả sửa chữa!", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }
                
                DonDatHangDAO.RepairTicketDraft draft = new DonDatHangDAO.RepairTicketDraft(mt, gc, parts);
                long totalParts = parts.stream().mapToLong(p -> p.donGia * p.soLuong).sum();
                long total = gc + totalParts;
                
                cartTableModel.addRow(new Object[]{"SC (Mới)", "NEW_SC", mt, 1, DF.format(total)+"đ", DF.format(total)+"đ", "✕"});
                cartMetadata.add(new CartItemMetadata(draft));
                updateTotals();
                dialog.dispose();
            } catch(Exception ex) { JOptionPane.showMessageDialog(dialog, "Tiền công sửa chữa phải là số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE); }
        });
        
        pnlFooter.add(btnCancel);
        pnlFooter.add(btnOk);
        dialog.add(pnlFooter, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }

    private void loadServiceCombo() {
        cbServices.addItem(new ServiceItem(-1, "- Không sử dụng -", 0));
        try {
            List<Map<String, Object>> list = dao.getAvailableServices();
            for (Map<String, Object> s : list) cbServices.addItem(new ServiceItem((int) s.get("MA_DV"), (String) s.get("TEN_DV"), (long) s.get("GIA_CUOC")));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void checkCustomer() {
        String phone = txtPhone.getText().trim();
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            KhachHang kh = dao.getCustomerByPhone(phone);
            if (kh != null) {
                currentMaKH = (int) kh.getMaKH();
                txtCustomerName.setText("✅ " + kh.getHoTen());
                txtCustomerName.setForeground(new Color(5, 122, 85));
            } else {
                currentMaKH = null;
                txtCustomerName.setText("❌ Không tìm thấy");
                txtCustomerName.setForeground(new Color(185, 28, 28));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleRowDeleted() { }

    private void updateTotals() {
        long tongSP = 0, tongDVLK = 0;
        for (CartItemMetadata m : cartMetadata) {
            long thanhTien = m.donGia * m.soLuong;
            if ("SP".equals(m.type)) tongSP += thanhTien;
            else tongDVLK += thanhTien;
        }
        
        long total = tongSP + tongDVLK;
        
        if (currentMaKM > 0) {
            try (java.sql.Connection con = ConnectDB.ConnectionUtils.getMyConnection()) {
                currentDiscount = dao.validateAndCalculateDiscount(con, currentMaKM, total);
                lblPromoResult.setText("Đã áp dụng giảm " + DF.format(currentDiscount) + "đ");
                lblPromoResult.setForeground(new Color(5, 122, 85));
            } catch (Exception e) {
                currentDiscount = 0; currentMaKM = -1;
                lblPromoResult.setText("❌ " + e.getMessage());
                lblPromoResult.setForeground(new Color(185, 28, 28));
            }
        } else {
            currentDiscount = 0;
        }

        long finalPrice = Math.max(total - currentDiscount, 0);

        lblTongSP.setText(DF.format(tongSP) + "đ");
        lblTongDVLK.setText(DF.format(tongDVLK) + "đ");
        lblGiamGia.setText("-" + DF.format(currentDiscount) + "đ");
        lblThanhTien.setText(DF.format(finalPrice) + "đ");
    }

    private void applyPromotion() {
        String code = txtPromoCode.getText().trim();
        if (code.isEmpty()) { JOptionPane.showMessageDialog(this, "Vui lòng nhập mã khuyến mãi!"); return; }
        try {
            int maKM = Integer.parseInt(code);
            currentMaKM = maKM;
            updateTotals();
        } catch (NumberFormatException ex) {
            lblPromoResult.setText("❌ Mã KM phải là số!");
            lblPromoResult.setForeground(new Color(185, 28, 28));
        }
    }

    private void saveOrder() {
        if (cartMetadata.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            List<Map<String, Object>> products = new ArrayList<>();
            List<Integer> dvIds = new ArrayList<>();
            List<Integer> scIds = new ArrayList<>();
            List<DonDatHangDAO.RepairTicketDraft> newRepairs = new ArrayList<>();

            for (CartItemMetadata m : cartMetadata) {
                if ("SP".equals(m.type)) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("serialNumber", m.serialOrIdText);
                    item.put("maSP", m.maId);
                    item.put("donGia", m.donGia);
                    item.put("soLuong", m.soLuong);
                    item.put("maBienThe", m.maBienThe);
                    products.add(item);
                } else if ("DV".equals(m.type)) {
                    dvIds.add(m.maId);
                } else if ("SC".equals(m.type)) {
                    scIds.add(m.maId);
                } else if ("NEW_SC".equals(m.type)) {
                    newRepairs.add(m.draftSC);
                }
            }

            int maNV = dao.getCurrentMaNV();
            int newHD = -1;

            if (!scIds.isEmpty() && !newRepairs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không thể kết hợp phiếu SC cũ và mới cùng lúc, vui lòng tạo riêng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!newRepairs.isEmpty() || scIds.isEmpty()) {
                newHD = dao.createOrderWithDrafts(currentMaKH, maNV, 1, products, dvIds, newRepairs);
            } else {
                newHD = dao.createOrder(currentMaKH, maNV, 1, products, dvIds, scIds);
            }

            if (currentMaKM > 0 && newHD > 0) {
                try { dao.applyPromotion(newHD, currentMaKM); } catch (Exception ignored) {}
            }
            
            JOptionPane.showMessageDialog(this, "Tạo đơn hàng #" + newHD + " thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetForm() {
        currentMaKH = null; currentMaKM = -1; currentDiscount = 0;
        txtPhone.setText(""); txtCustomerName.setText(""); txtSearchSP.setText("");
        txtPromoCode.setText(""); lblPromoResult.setText(" ");
        cbServices.setSelectedIndex(0);
        cartTableModel.setRowCount(0);
        cartMetadata.clear();
        updateTotals();
    }

    private JPanel createCard(String title) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        TitledBorder tb = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true), " " + title + " ");
        tb.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        tb.setTitleColor(new Color(71, 85, 105));
        card.setBorder(BorderFactory.createCompoundBorder(tb, new EmptyBorder(10, 10, 10, 10)));
        return card;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        l.setForeground(new Color(71, 85, 105));
        return l;
    }

    private JButton createFlatButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JTable buildTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(248, 250, 252));
        table.setSelectionForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(new Color(71, 85, 105));
        table.getTableHeader().setPreferredSize(new Dimension(0, 35));
        return table;
    }

    static class ServiceItem {
        int id; String name; long giaCuoc;
        ServiceItem(int id, String name, long giaCuoc) { this.id = id; this.name = name; this.giaCuoc = giaCuoc; }
        @Override public String toString() { return id == -1 ? name : name + " (" + DF.format(giaCuoc) + "đ)"; }
    }

    static class RepairItem {
        int id; String moTa; long tienLK;
        RepairItem(int id, String moTa, long tienLK) { this.id = id; this.moTa = moTa; this.tienLK = tienLK; }
        @Override public String toString() { return id == -1 ? moTa : "SC#" + id + " - " + moTa + " (" + DF.format(tienLK) + "đ)"; }
    }

    class CartItemMetadata {
        String type; 
        String serialOrIdText; 
        int maId; 
        long donGia;
        int soLuong;
        Integer maBienThe;
        DonDatHangDAO.RepairTicketDraft draftSC;

        CartItemMetadata(String t, String s, int id, long gia, int sl, Integer mbt) { 
            type = t; serialOrIdText = s; maId = id; donGia = gia; soLuong = sl; maBienThe = mbt;
        }
        CartItemMetadata(DonDatHangDAO.RepairTicketDraft draft) {
            type = "NEW_SC"; serialOrIdText = "SC Tạo mới"; maId = -1; donGia = draft.giaCuoc; 
            for(DonDatHangDAO.RepairPartDraft p : draft.parts) donGia += p.donGia * p.soLuong;
            soLuong = 1; draftSC = draft;
        }
    }

    static class DeleteBtnRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            JLabel lbl = new JLabel("✕", SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lbl.setForeground(new Color(220, 38, 38));
            lbl.setOpaque(true);
            lbl.setBackground(s ? new Color(248, 250, 252) : Color.WHITE);
            return lbl;
        }
    }

    class DeleteBtnEditor extends DefaultCellEditor {
        private JButton btn;
        private int rowToDelete = -1;

        DeleteBtnEditor(JTable tbl, DefaultTableModel mdl, Runnable onDeleted) {
            super(new JCheckBox());
            btn = new JButton("✕");
            btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
            btn.setForeground(new Color(220, 38, 38));
            btn.setBorderPainted(false); btn.setContentAreaFilled(false);
            btn.addActionListener(e -> {
                rowToDelete = tbl.getSelectedRow();
                fireEditingStopped();
            });
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { return btn; }
        @Override public Object getCellEditorValue() { return "✕"; }
        @Override protected void fireEditingStopped() {
            super.fireEditingStopped();
            if (rowToDelete >= 0 && rowToDelete < cartMetadata.size()) {
                cartMetadata.remove(rowToDelete);
                cartTableModel.removeRow(rowToDelete);
                updateTotals();
                rowToDelete = -1;
            }
        }
    }
}
