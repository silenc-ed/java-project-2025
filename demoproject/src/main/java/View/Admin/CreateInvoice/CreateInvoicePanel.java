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

/**
 * Panel tạo đơn đặt hàng — hiển thị tại Admin menu "Mua hàng" (index 1)
 */
public class CreateInvoicePanel extends javax.swing.JPanel {

    private DonDatHangDAO dao;

    // Khách hàng
    private JTextField txtPhone, txtCustomerName;
    private Integer currentMaKH = null;

    // Sản phẩm (Serial)
    private DefaultTableModel productTableModel;
    private JTable productTable;
    private JTextField txtSerial;

    // Dịch vụ
    private DefaultTableModel serviceTableModel;
    private JTable serviceTable;
    private JComboBox<ServiceItem> cbServices;

    // Phiếu sửa chữa
    private DefaultTableModel repairTableModel;
    private JTable repairTable;
    private JComboBox<RepairItem> cbRepairs;

    // Khuyến mãi
    private JTextField txtPromoCode;
    private JLabel lblPromoResult;
    private int currentMaKM = -1;
    private long currentDiscount = 0;

    // Tổng cộng
    private JLabel lblTongSP, lblTongDV, lblTongLK, lblGiamGia, lblThanhTien;

    private static final DecimalFormat DF = new DecimalFormat("#,###");
    private static final Color PURPLE_MAIN = new Color(142, 68, 173);
    private static final Color PURPLE_BORDER = new Color(175, 122, 197);
    private static final Color BG_COLOR = new Color(248, 250, 252);

    public CreateInvoicePanel() {
        dao = new DonDatHangDAO();
        setupUI();
        loadServiceCombo();
        loadRepairCombo();
    }

    private void setupUI() {
        this.setLayout(new BorderLayout(0, 0));
        this.setBackground(BG_COLOR);

        // ===== HEADER =====
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, PURPLE_BORDER),
            new EmptyBorder(18, 25, 18, 25)
        ));
        JLabel lblTitle = new JLabel("Tạo đơn đặt hàng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(30, 41, 59));
        headerPanel.add(lblTitle, BorderLayout.WEST);
        
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightHeader.setOpaque(false);
        
        JButton btnSave = createGradientButton("Lưu đơn hàng");
        JButton btnReset = createFlatButton("Làm mới", new Color(148, 163, 184));
        
        btnSave.addActionListener(e -> saveOrder());
        btnReset.addActionListener(e -> resetForm());
        
        // Ẩn nút Lưu nếu không có quyền Thêm
        btnSave.setVisible(Controller.Admin.PermissionService.canAdd("Mua hang"));

        rightHeader.add(btnReset);
        rightHeader.add(btnSave);
        headerPanel.add(rightHeader, BorderLayout.EAST);
        
        this.add(headerPanel, BorderLayout.NORTH);

        // ===== SCROLLABLE MAIN CONTENT =====
        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setBackground(BG_COLOR);
        mainContent.setBorder(new EmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 14, 0);

        gbc.gridy = 0;
        mainContent.add(buildCustomerSection(), gbc);

        gbc.gridy = 1;
        mainContent.add(buildProductSection(), gbc);

        gbc.gridy = 2;
        mainContent.add(buildServiceSection(), gbc);

        gbc.gridy = 3;
        mainContent.add(buildRepairSection(), gbc);

        gbc.gridy = 4;
        mainContent.add(buildPromoSection(), gbc);

        gbc.gridy = 5;
        mainContent.add(buildTotalSection(), gbc);

        // Spacer to push everything up
        gbc.gridy = 6;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        mainContent.add(new JPanel() {{ setOpaque(false); }}, gbc);

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        this.add(scrollPane, BorderLayout.CENTER);

        // Removed bottom buttons since they are now in the header
    }

    // ================= SECTION: KHÁCH HÀNG =================

    private JPanel buildCustomerSection() {
        JPanel card = createCard("Khách hàng");
        card.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        // Row 1: SĐT
        g.gridx = 0; g.gridy = 0; g.weightx = 0; g.gridwidth = 1;
        card.add(makeLabel("Số điện thoại:"), g);

        g.gridx = 1; g.weightx = 1;
        txtPhone = new JTextField();
        txtPhone.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPhone.setPreferredSize(new Dimension(0, 35));
        card.add(txtPhone, g);

        g.gridx = 2; g.weightx = 0;
        JButton btnCheck = createFlatButton("Kiểm tra", PURPLE_MAIN);
        btnCheck.setPreferredSize(new Dimension(110, 35));
        btnCheck.addActionListener(e -> checkCustomer());
        txtPhone.addActionListener(e -> checkCustomer());
        card.add(btnCheck, g);

        // Row 2: Tên KH
        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        card.add(makeLabel("Tên KH:"), g);

        g.gridx = 1; g.gridwidth = 2; g.weightx = 1;
        txtCustomerName = new JTextField();
        txtCustomerName.setEditable(false);
        txtCustomerName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtCustomerName.setPreferredSize(new Dimension(0, 35));
        txtCustomerName.setBackground(new Color(248, 250, 252));
        card.add(txtCustomerName, g);

        return card;
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
                txtCustomerName.setText("✅  " + kh.getHoTen());
                txtCustomerName.setForeground(new Color(5, 122, 85));
            } else {
                currentMaKH = null;
                txtCustomerName.setText("❌  Không tìm thấy khách hàng!");
                txtCustomerName.setForeground(new Color(185, 28, 28));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tra cứu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= SECTION: SẢN PHẨM =================

    private JPanel buildProductSection() {
        JPanel card = createCard("Sản phẩm (Serial/IMEI)");
        card.setLayout(new BorderLayout(0, 8));

        // Input row
        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setOpaque(false);
        txtSerial = new JTextField();
        txtSerial.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSerial.setPreferredSize(new Dimension(0, 35));
        txtSerial.setToolTipText("Nhập Serial/IMEI hoặc tên sản phẩm");
        JButton btnAdd = createFlatButton("+ Thêm SP", new Color(16, 185, 129));
        btnAdd.setPreferredSize(new Dimension(120, 35));
        btnAdd.addActionListener(e -> addProductBySerial());
        txtSerial.addActionListener(e -> addProductBySerial());
        inputRow.add(txtSerial, BorderLayout.CENTER);
        inputRow.add(btnAdd, BorderLayout.EAST);
        card.add(inputRow, BorderLayout.NORTH);

        // Table
        String[] cols = {"Serial/IMEI", "Sản phẩm", "Đơn giá", ""};
        productTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 3; }
        };
        productTable = buildTable(productTableModel);
        productTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(50);
        productTable.getColumnModel().getColumn(3).setMaxWidth(55);
        productTable.getColumnModel().getColumn(3).setCellRenderer(new DeleteBtnRenderer());
        productTable.getColumnModel().getColumn(3).setCellEditor(new DeleteBtnEditor(productTable, productTableModel, this::updateTotals));

        // Right-align price
        DefaultTableCellRenderer priceRenderer = new DefaultTableCellRenderer();
        priceRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        productTable.getColumnModel().getColumn(2).setCellRenderer(priceRenderer);

        JScrollPane sp = new JScrollPane(productTable);
        sp.setPreferredSize(new Dimension(0, 140));
        sp.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        card.add(sp, BorderLayout.CENTER);

        return card;
    }

    private void addProductBySerial() {
        String keyword = txtSerial.getText().trim();
        if (keyword.isEmpty()) return;
        try {
            List<Map<String, Object>> serials = dao.searchAvailableSerials(keyword);
            if (serials.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy serial/sản phẩm khả dụng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Map<String, Object> selected = null;
            for (Map<String, Object> s : serials) {
                if (keyword.equalsIgnoreCase((String) s.get("SERIAL_NUMBER"))) { selected = s; break; }
            }
            if (selected == null && serials.size() == 1) selected = serials.get(0);
            if (selected == null) {
                String[] options = new String[serials.size()];
                for (int i = 0; i < serials.size(); i++) {
                    Map<String, Object> s = serials.get(i);
                    options[i] = s.get("SERIAL_NUMBER") + " | " + s.get("TEN_SP") + " (" + s.get("TEN_BIENTHE") + ") | " + DF.format((long) s.get("GIA_BAN")) + "đ";
                }
                String choice = (String) JOptionPane.showInputDialog(this, "Chọn sản phẩm:", "Kết quả", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                if (choice == null) return;
                selected = serials.get(java.util.Arrays.asList(options).indexOf(choice));
            }

            String sn = (String) selected.get("SERIAL_NUMBER");
            for (int i = 0; i < productTableModel.getRowCount(); i++) {
                if (sn.equals(productTableModel.getValueAt(i, 0))) {
                    JOptionPane.showMessageDialog(this, "Serial này đã được thêm!", "Trùng lặp", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            if (dao.isSerialInPendingOrder(sn, 0)) {
                JOptionPane.showMessageDialog(this, "Serial " + sn + " đã nằm trong đơn 'Chờ thanh toán' khác!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            productTableModel.addRow(new Object[]{ sn, selected.get("TEN_SP") + " (" + selected.get("TEN_BIENTHE") + ")", DF.format((long) selected.get("GIA_BAN")) + "đ", "✕" });
            int r = productTableModel.getRowCount() - 1;
            productTable.putClientProperty("maSP_" + r, selected.get("MA_SP"));
            productTable.putClientProperty("donGia_" + r, selected.get("GIA_BAN"));
            txtSerial.setText("");
            updateTotals();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= SECTION: DỊCH VỤ =================

    private JPanel buildServiceSection() {
        JPanel card = createCard("Dịch vụ phần mềm / tiện ích");
        card.setLayout(new BorderLayout(0, 8));

        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setOpaque(false);
        cbServices = new JComboBox<>();
        cbServices.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbServices.setPreferredSize(new Dimension(0, 35));
        JButton btnAdd = createFlatButton("+ Thêm DV", new Color(59, 130, 246));
        btnAdd.setPreferredSize(new Dimension(120, 35));
        btnAdd.addActionListener(e -> addService());
        inputRow.add(cbServices, BorderLayout.CENTER);
        inputRow.add(btnAdd, BorderLayout.EAST);
        card.add(inputRow, BorderLayout.NORTH);

        String[] cols = {"Dịch vụ", "Phí dịch vụ", ""};
        serviceTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 2; }
        };
        serviceTable = buildTable(serviceTableModel);
        serviceTable.getColumnModel().getColumn(2).setMaxWidth(55);
        serviceTable.getColumnModel().getColumn(2).setCellRenderer(new DeleteBtnRenderer());
        serviceTable.getColumnModel().getColumn(2).setCellEditor(new DeleteBtnEditor(serviceTable, serviceTableModel, this::updateTotals));
        DefaultTableCellRenderer pr = new DefaultTableCellRenderer();
        pr.setHorizontalAlignment(SwingConstants.RIGHT);
        serviceTable.getColumnModel().getColumn(1).setCellRenderer(pr);

        JScrollPane sp = new JScrollPane(serviceTable);
        sp.setPreferredSize(new Dimension(0, 110));
        sp.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private void addService() {
        ServiceItem sel = (ServiceItem) cbServices.getSelectedItem();
        if (sel == null) return;
        for (int i = 0; i < serviceTableModel.getRowCount(); i++) {
            if (sel.name.equals(serviceTableModel.getValueAt(i, 0))) {
                JOptionPane.showMessageDialog(this, "Dịch vụ này đã được thêm!", "Trùng lặp", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        serviceTableModel.addRow(new Object[]{ sel.name, DF.format(sel.giaCuoc) + "đ", "✕" });
        int r = serviceTableModel.getRowCount() - 1;
        serviceTable.putClientProperty("maDV_" + r, sel.id);
        serviceTable.putClientProperty("giaDV_" + r, sel.giaCuoc);
        updateTotals();
    }

    private void loadServiceCombo() {
        try {
            List<Map<String, Object>> list = dao.getAvailableServices();
            for (Map<String, Object> s : list) cbServices.addItem(new ServiceItem((int) s.get("MA_DV"), (String) s.get("TEN_DV"), (long) s.get("GIA_CUOC")));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ================= SECTION: PHIẾU SỬA CHỮA =================

    private JPanel buildRepairSection() {
        JPanel card = createCard("Phiếu sửa chữa (nếu có)");
        card.setLayout(new BorderLayout(0, 8));

        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setOpaque(false);
        cbRepairs = new JComboBox<>();
        cbRepairs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbRepairs.setPreferredSize(new Dimension(0, 35));
        JButton btnAdd = createFlatButton("+ Thêm SC", new Color(245, 158, 11));
        btnAdd.setPreferredSize(new Dimension(120, 35));
        btnAdd.addActionListener(e -> addRepair());
        inputRow.add(cbRepairs, BorderLayout.CENTER);
        inputRow.add(btnAdd, BorderLayout.EAST);
        card.add(inputRow, BorderLayout.NORTH);

        String[] cols = {"Mã phiếu", "Mô tả", "Tiền LK", ""};
        repairTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 3; }
        };
        repairTable = buildTable(repairTableModel);
        repairTable.getColumnModel().getColumn(3).setMaxWidth(55);
        repairTable.getColumnModel().getColumn(3).setCellRenderer(new DeleteBtnRenderer());
        repairTable.getColumnModel().getColumn(3).setCellEditor(new DeleteBtnEditor(repairTable, repairTableModel, this::updateTotals));

        JScrollPane sp = new JScrollPane(repairTable);
        sp.setPreferredSize(new Dimension(0, 90));
        sp.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private void addRepair() {
        RepairItem sel = (RepairItem) cbRepairs.getSelectedItem();
        if (sel == null) return;
        repairTableModel.addRow(new Object[]{ sel.id, sel.moTa, DF.format(sel.tienLK) + "đ", "✕" });
        int r = repairTableModel.getRowCount() - 1;
        repairTable.putClientProperty("maPhieuSC_" + r, sel.id);
        repairTable.putClientProperty("tienLK_" + r, sel.tienLK);
        updateTotals();
    }

    private void loadRepairCombo() {
        try {
            List<Map<String, Object>> list = dao.getRepairTickets();
            for (Map<String, Object> r : list) cbRepairs.addItem(new RepairItem((int) r.get("MA_PHIEU_SC"), (String) r.get("MO_TA"), (long) r.get("TIEN_LK")));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ================= SECTION: KHUYẾN MÃI =================

    private JPanel buildPromoSection() {
        JPanel card = createCard("Khuyến mãi");
        card.setLayout(new BorderLayout(0, 8));

        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setOpaque(false);
        txtPromoCode = new JTextField();
        txtPromoCode.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPromoCode.setPreferredSize(new Dimension(0, 35));
        txtPromoCode.setToolTipText("Nhập mã khuyến mãi (MA_KM)");
        JButton btnApply = createFlatButton("Áp dụng", new Color(234, 88, 12));
        btnApply.setPreferredSize(new Dimension(110, 35));
        btnApply.addActionListener(e -> applyPromotion());
        inputRow.add(txtPromoCode, BorderLayout.CENTER);
        inputRow.add(btnApply, BorderLayout.EAST);
        card.add(inputRow, BorderLayout.NORTH);

        lblPromoResult = new JLabel(" ");
        lblPromoResult.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPromoResult.setBorder(new EmptyBorder(4, 5, 0, 0));
        card.add(lblPromoResult, BorderLayout.CENTER);
        return card;
    }

    private void applyPromotion() {
        String code = txtPromoCode.getText().trim();
        if (code.isEmpty()) { JOptionPane.showMessageDialog(this, "Vui lòng nhập mã khuyến mãi!"); return; }
        try {
            int maKM = Integer.parseInt(code);
            long tongTien = calcTotalBeforeDiscount();
            try (java.sql.Connection con = ConnectDB.ConnectionUtils.getMyConnection()) {
                currentDiscount = dao.validateAndCalculateDiscount(con, maKM, tongTien);
                currentMaKM = maKM;
                lblPromoResult.setText("✅  Giảm " + DF.format(currentDiscount) + "đ");
                lblPromoResult.setForeground(new Color(5, 122, 85));
                updateTotals();
            }
        } catch (NumberFormatException ex) {
            lblPromoResult.setText("❌  Mã KM phải là số!"); lblPromoResult.setForeground(new Color(185, 28, 28));
        } catch (Exception ex) {
            currentMaKM = -1; currentDiscount = 0;
            lblPromoResult.setText("❌  " + ex.getMessage()); lblPromoResult.setForeground(new Color(185, 28, 28));
            updateTotals();
        }
    }

    // ================= SECTION: TỔNG CỘNG =================

    private JPanel buildTotalSection() {
        JPanel card = createCard("Tổng cộng");
        card.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 10, 4, 10);

        Font normal = new Font("Segoe UI", Font.PLAIN, 15);
        Font bold = new Font("Segoe UI", Font.BOLD, 18);

        lblTongSP = new JLabel("0đ"); lblTongDV = new JLabel("0đ"); lblTongLK = new JLabel("0đ");
        lblGiamGia = new JLabel("0đ"); lblThanhTien = new JLabel("0đ");
        lblTongSP.setFont(normal); lblTongDV.setFont(normal); lblTongLK.setFont(normal);
        lblGiamGia.setFont(normal); lblGiamGia.setForeground(new Color(220, 38, 38));
        lblThanhTien.setFont(bold); lblThanhTien.setForeground(PURPLE_MAIN);

        JLabel[] labels = { makeLabel("Tổng tiền sản phẩm:"), makeLabel("Tổng phí dịch vụ:"),
                            makeLabel("Tổng tiền linh kiện:"), makeLabel("Giảm giá:"), makeBoldLabel("THÀNH TIỀN:") };
        JLabel[] values = { lblTongSP, lblTongDV, lblTongLK, lblGiamGia, lblThanhTien };

        for (int i = 0; i < labels.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0.4;
            card.add(labels[i], g);
            g.gridx = 1; g.weightx = 0.6;
            values[i].setHorizontalAlignment(SwingConstants.RIGHT);
            card.add(values[i], g);

            if (i == 2) { // Separator after LK
                g.gridx = 0; g.gridy = i + 1; g.gridwidth = 2; g.weightx = 1;
                g.insets = new Insets(8, 10, 8, 10);
                JSeparator sep = new JSeparator();
                sep.setForeground(new Color(220, 220, 220));
                card.add(sep, g);
                g.gridwidth = 1;
                g.insets = new Insets(4, 10, 4, 10);
                // Shift remaining rows
                labels[3] = labels[3]; // no-op
                g.gridx = 0; g.gridy = i + 2; g.weightx = 0.4;
                card.add(labels[3], g);
                g.gridx = 1; g.weightx = 0.6;
                card.add(values[3], g);
                g.gridx = 0; g.gridy = i + 3; g.weightx = 0.4;
                g.insets = new Insets(8, 10, 8, 10);
                card.add(labels[4], g);
                g.gridx = 1; g.weightx = 0.6;
                card.add(values[4], g);
                break;
            }
        }

        return card;
    }

    private void updateTotals() {
        long tongSP = 0, tongDV = 0, tongLK = 0;
        for (int i = 0; i < productTableModel.getRowCount(); i++) {
            Object v = productTable.getClientProperty("donGia_" + i); if (v != null) tongSP += (long) v;
        }
        for (int i = 0; i < serviceTableModel.getRowCount(); i++) {
            Object v = serviceTable.getClientProperty("giaDV_" + i); if (v != null) tongDV += (long) v;
        }
        for (int i = 0; i < repairTableModel.getRowCount(); i++) {
            Object v = repairTable.getClientProperty("tienLK_" + i); if (v != null) tongLK += (long) v;
        }
        long total = tongSP + tongDV + tongLK;
        long finalPrice = Math.max(total - currentDiscount, 0);
        lblTongSP.setText(DF.format(tongSP) + "đ");
        lblTongDV.setText(DF.format(tongDV) + "đ");
        lblTongLK.setText(DF.format(tongLK) + "đ");
        lblGiamGia.setText("-" + DF.format(currentDiscount) + "đ");
        lblThanhTien.setText(DF.format(finalPrice) + "đ");
    }

    private long calcTotalBeforeDiscount() {
        long t = 0;
        for (int i = 0; i < productTableModel.getRowCount(); i++) { Object v = productTable.getClientProperty("donGia_" + i); if (v != null) t += (long) v; }
        for (int i = 0; i < serviceTableModel.getRowCount(); i++) { Object v = serviceTable.getClientProperty("giaDV_" + i); if (v != null) t += (long) v; }
        for (int i = 0; i < repairTableModel.getRowCount(); i++) { Object v = repairTable.getClientProperty("tienLK_" + i); if (v != null) t += (long) v; }
        return t;
    }

    // ================= SAVE / RESET =================

    private void saveOrder() {
        if (productTableModel.getRowCount() == 0 && serviceTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Phải có ít nhất một sản phẩm hoặc dịch vụ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            List<Map<String, Object>> serials = new ArrayList<>();
            for (int i = 0; i < productTableModel.getRowCount(); i++) {
                Map<String, Object> item = new HashMap<>();
                item.put("serialNumber", productTableModel.getValueAt(i, 0).toString());
                item.put("maSP", productTable.getClientProperty("maSP_" + i));
                item.put("donGia", productTable.getClientProperty("donGia_" + i));
                serials.add(item);
            }
            List<Integer> dvIds = new ArrayList<>();
            for (int i = 0; i < serviceTableModel.getRowCount(); i++) {
                Object v = serviceTable.getClientProperty("maDV_" + i); if (v != null) dvIds.add((int) v);
            }
            List<Integer> scIds = new ArrayList<>();
            for (int i = 0; i < repairTableModel.getRowCount(); i++) {
                Object v = repairTable.getClientProperty("maPhieuSC_" + i); if (v != null) scIds.add((int) v);
            }

            int maNV = dao.getCurrentMaNV();
            int newHD = dao.createOrder(currentMaKH, maNV, 1, serials, dvIds, scIds);
            if (currentMaKM > 0 && newHD > 0) {
                try { dao.applyPromotion(newHD, currentMaKM); } catch (Exception ignored) {}
            }
            JOptionPane.showMessageDialog(this, "Tạo đơn hàng #" + newHD + " thành công!\nTrạng thái: Chờ thanh toán", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetForm() {
        currentMaKH = null; currentMaKM = -1; currentDiscount = 0;
        txtPhone.setText(""); txtCustomerName.setText(""); txtSerial.setText("");
        if (txtPromoCode != null) txtPromoCode.setText("");
        if (lblPromoResult != null) lblPromoResult.setText(" ");
        productTableModel.setRowCount(0); serviceTableModel.setRowCount(0); repairTableModel.setRowCount(0);
        updateTotals();
    }

    // ================= UI FACTORY =================

    /** Card panel with titled border + white background + rounded feel */
    private JPanel createCard(String title) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        TitledBorder tb = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(215, 205, 225), 1, true), "  " + title + "  ");
        tb.setTitleFont(new Font("Segoe UI", Font.BOLD, 13));
        tb.setTitleColor(PURPLE_MAIN);
        card.setBorder(BorderFactory.createCompoundBorder(tb, new EmptyBorder(10, 12, 12, 12)));
        return card;
    }

    private JTable buildTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(34);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(245, 235, 250));
        table.setSelectionForeground(PURPLE_MAIN);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(248, 248, 252));
        table.getTableHeader().setForeground(new Color(100, 100, 130));
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));
        return table;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        l.setForeground(new Color(60, 60, 80));
        return l;
    }

    private JLabel makeBoldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 17));
        l.setForeground(new Color(30, 30, 50));
        return l;
    }

    private JButton createFlatButton(String text, Color bg) {
        JButton btn = new JButton(text);
        View.Admin.UIUtils.styleButton(btn);
        return btn;
    }

    private JButton createGradientButton(String text) {
        JButton btn = new JButton(text);
        View.Admin.UIUtils.styleButton(btn);
        btn.setPreferredSize(new Dimension(200, 45));
        return btn;
    }

    // ================= INNER CLASSES =================

    static class ServiceItem {
        int id; String name; long giaCuoc;
        ServiceItem(int id, String name, long giaCuoc) { this.id = id; this.name = name; this.giaCuoc = giaCuoc; }
        @Override public String toString() { return name + " (" + DF.format(giaCuoc) + "đ)"; }
    }

    static class RepairItem {
        int id; String moTa; long tienLK;
        RepairItem(int id, String moTa, long tienLK) { this.id = id; this.moTa = moTa; this.tienLK = tienLK; }
        @Override public String toString() { return "SC#" + id + " - " + (moTa != null ? moTa : "") + " (" + DF.format(tienLK) + "đ)"; }
    }

    static class DeleteBtnRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            JLabel lbl = new JLabel("✕", SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lbl.setForeground(new Color(220, 38, 38));
            lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
            lbl.setOpaque(true);
            lbl.setBackground(s ? new Color(245, 235, 250) : Color.WHITE);
            return lbl;
        }
    }

    static class DeleteBtnEditor extends DefaultCellEditor {
        private JButton btn;
        private JTable tbl;
        private DefaultTableModel mdl;
        private Runnable cb;
        private boolean clicked;

        DeleteBtnEditor(JTable tbl, DefaultTableModel mdl, Runnable cb) {
            super(new JCheckBox());
            this.tbl = tbl; this.mdl = mdl; this.cb = cb;
            btn = new JButton("✕");
            btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
            btn.setForeground(new Color(220, 38, 38));
            btn.setBorderPainted(false); btn.setContentAreaFilled(false);
            btn.addActionListener(e -> { clicked = true; fireEditingStopped(); });
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { clicked = false; return btn; }
        @Override public Object getCellEditorValue() { return "✕"; }
        @Override public boolean stopCellEditing() { clicked = false; return super.stopCellEditing(); }
        @Override protected void fireEditingStopped() {
            if (clicked) {
                int row = tbl.getSelectedRow();
                if (row >= 0 && row < mdl.getRowCount()) {
                    SwingUtilities.invokeLater(() -> { mdl.removeRow(row); if (cb != null) cb.run(); });
                }
            }
            super.fireEditingStopped();
        }
    }
}
