package View.Admin.Procurement;

import View.Admin.UIUtils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.DefaultTableCellRenderer;
//import javax.swing.table.AbstractCellEditor;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import ConnectDB.ConnectionUtils;

class DBItem {
    private int id;
    private String name;
    public DBItem(int id, String name) { this.id = id; this.name = name; }
    public int getId() { return id; }
    @Override public String toString() { return name; }
}

public class ProcurementPanel extends javax.swing.JPanel {

    private DefaultTableModel tableModel;
    private JTable dataTable;
    private JLabel lblLastUpdate;
    private JComboBox<String> cbFilter;
    private JTextField txtSearch;

    public ProcurementPanel() {
        initComponents();
        ensureStatusColumnExists();
        setupCustomUI();
    }

    private void ensureStatusColumnExists() {
        try (Connection con = ConnectionUtils.getMyConnection()) {
            java.sql.DatabaseMetaData md = con.getMetaData();
            try (ResultSet rs = md.getColumns(null, null, "HOA_DON", "TRANG_THAI")) {
                if (!rs.next()) {
                    try (java.sql.Statement st = con.createStatement()) {
                        st.execute("ALTER TABLE HOA_DON ADD TRANG_THAI NVARCHAR2(50) DEFAULT 'Hoàn thành'");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setSelectedComboItem(JComboBox<DBItem> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            DBItem item = combo.getItemAt(i);
            if (item.getId() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void applyFilters(String searchText, String selectedStatus) {
        RowFilter<DefaultTableModel, Object> searchFilter = null;
        if (searchText != null && !searchText.isEmpty() && !searchText.equals("Tìm kiếm theo Mã HD, Khách hàng, Mã nhân viên...")) {
            final String searchLower = searchText.toLowerCase();
            searchFilter = new RowFilter<DefaultTableModel, Object>() {
                @Override
                public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Object> entry) {
                    String maHd = entry.getStringValue(1).toLowerCase();
                    String kh = entry.getStringValue(2).toLowerCase();
                    String maNv = entry.getStringValue(3).toLowerCase();
                    return maHd.contains(searchLower) || kh.contains(searchLower) || maNv.contains(searchLower);
                }
            };
        }

        RowFilter<DefaultTableModel, Object> statusFilter = null;
        if (selectedStatus != null && !selectedStatus.equals("Tất cả trạng thái")) {
            statusFilter = RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(selectedStatus) + "$", 7);
        }

        java.util.List<RowFilter<DefaultTableModel, Object>> filters = new java.util.ArrayList<>();
        if (searchFilter != null) filters.add(searchFilter);
        if (statusFilter != null) filters.add(statusFilter);

        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) dataTable.getRowSorter();
        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    // ==================== SHOW ORDER DIALOG (Add / Edit) ====================
    private void showOrderDialog(boolean isEdit, int maHd) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), 
            isEdit ? "Chỉnh sửa hóa đơn" : "Thêm hóa đơn mới", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(620, 700);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(248, 250, 252), 0, getHeight(), new Color(237, 233, 254));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        // ---- Title bar ----
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setPreferredSize(new Dimension(0, 56));
        titleBar.setBackground(isEdit ? new Color(0, 123, 255) : new Color(40, 167, 69));
        titleBar.setBorder(new EmptyBorder(0, 24, 0, 24));

        JLabel lblDialogTitle = new JLabel(isEdit ? "Chỉnh sửa hóa đơn #" + maHd : "Thêm hóa đơn mới");
        lblDialogTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblDialogTitle.setForeground(Color.WHITE);
        titleBar.add(lblDialogTitle, BorderLayout.WEST);
        mainPanel.add(titleBar, BorderLayout.NORTH);

        // ---- Form content ----
        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 28, 10, 28));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Create local form components
        JComboBox<DBItem> dlgCbKhachHang = new JComboBox<>();
        JComboBox<DBItem> dlgCbNhanVien = new JComboBox<>();
        JComboBox<DBItem> dlgCbChiNhanh = new JComboBox<>();
        JComboBox<DBItem> dlgCbKhuyenMai = new JComboBox<>();
        JComboBox<String> dlgCbPhuongThuc = new JComboBox<>(new String[]{"Tiền mặt", "Chuyển khoản", "Thẻ tín dụng", "Ví điện tử"});
        JComboBox<String> dlgCbTrangThai = new JComboBox<>(new String[]{"Hoàn thành", "Đang xử lý", "Đang chuẩn bị hàng", "Chờ thanh toán", "Đã hủy"});
        JTextField dlgTxtTongTien = new JTextField("0");
        JTextField dlgTxtGiamGia = new JTextField("0");
        JTextField dlgTxtThanhTien = new JTextField("0");
        dlgTxtThanhTien.setEditable(false);
        dlgTxtThanhTien.setBackground(new Color(241, 245, 249));

        // Style all combos and textfields
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);
        Dimension fieldSize = new Dimension(230, 34);
        JComponent[] fields = {dlgCbKhachHang, dlgCbNhanVien, dlgCbChiNhanh, dlgCbKhuyenMai, 
                              dlgCbPhuongThuc, dlgCbTrangThai, dlgTxtTongTien, dlgTxtGiamGia, dlgTxtThanhTien};
        for (JComponent f : fields) {
            f.setFont(fieldFont);
            f.setPreferredSize(fieldSize);
        }

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Color labelColor = new Color(51, 65, 85);

        // Row 0
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel l1 = new JLabel("Khách hàng"); l1.setFont(labelFont); l1.setForeground(labelColor);
        formPanel.add(l1, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(dlgCbKhachHang, gbc);

        // Row 1
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel l2 = new JLabel("Nhân viên"); l2.setFont(labelFont); l2.setForeground(labelColor);
        formPanel.add(l2, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(dlgCbNhanVien, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        JLabel l3 = new JLabel("Chi nhánh"); l3.setFont(labelFont); l3.setForeground(labelColor);
        formPanel.add(l3, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(dlgCbChiNhanh, gbc);

        // Row 3
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        JLabel l4 = new JLabel("Khuyến mãi"); l4.setFont(labelFont); l4.setForeground(labelColor);
        formPanel.add(l4, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(dlgCbKhuyenMai, gbc);

        // Row 4
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        JLabel l5 = new JLabel("Tổng tiền hàng"); l5.setFont(labelFont); l5.setForeground(labelColor);
        formPanel.add(l5, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(dlgTxtTongTien, gbc);

        // Row 5
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0;
        JLabel l6 = new JLabel("Giảm giá"); l6.setFont(labelFont); l6.setForeground(labelColor);
        formPanel.add(l6, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(dlgTxtGiamGia, gbc);

        // Row 6
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0;
        JLabel l7 = new JLabel("Thành tiền"); l7.setFont(labelFont); l7.setForeground(new Color(40, 167, 69));
        formPanel.add(l7, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(dlgTxtThanhTien, gbc);

        // Row 7
        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0;
        JLabel l8 = new JLabel("PT Thanh toán"); l8.setFont(labelFont); l8.setForeground(labelColor);
        formPanel.add(l8, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(dlgCbPhuongThuc, gbc);

        // Row 8
        gbc.gridx = 0; gbc.gridy = 8; gbc.weightx = 0;
        JLabel l9 = new JLabel("Trạng thái"); l9.setFont(labelFont); l9.setForeground(labelColor);
        formPanel.add(l9, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(dlgCbTrangThai, gbc);

        // Row 9: Chi tiết sản phẩm
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        DefaultTableModel detailTableModel = new DefaultTableModel(new String[]{"Tên SP", "Số lượng", "Đơn giá", "Thành tiền", "Chi nhánh"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable detailTable = new JTable(detailTableModel);
        detailTable.setRowHeight(28);
        detailTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane detailScroll = new JScrollPane(detailTable);
        detailScroll.setPreferredSize(new Dimension(550, 150));
        detailScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), "Danh sách sản phẩm trong đơn", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), new Color(100, 100, 100)));
        formPanel.add(detailScroll, gbc);
        gbc.gridwidth = 1; gbc.weighty = 0; // reset

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // ---- Live calculation: Thành tiền = Tổng tiền - Giảm giá ----
        javax.swing.event.DocumentListener calcListener = new javax.swing.event.DocumentListener() {
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { calc(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { calc(); }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { calc(); }
            private void calc() {
                try {
                    double t = Double.parseDouble(dlgTxtTongTien.getText().trim());
                    double g = Double.parseDouble(dlgTxtGiamGia.getText().trim());
                    dlgTxtThanhTien.setText(String.valueOf((long)(t - g)));
                } catch (Exception ex) { dlgTxtThanhTien.setText("0"); }
            }
        };
        dlgTxtTongTien.getDocument().addDocumentListener(calcListener);
        dlgTxtGiamGia.getDocument().addDocumentListener(calcListener);

        // ---- Button bar ----
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(8, 28, 18, 28));

        JButton btnSave = new JButton(isEdit ? "Cập nhật" : "Lưu");
        UIUtils.styleButton(btnSave);
        btnSave.setPreferredSize(new Dimension(140, 38));
        JButton btnCancel = new JButton("Hủy");
        UIUtils.styleButton(btnCancel);
        btnCancel.setPreferredSize(new Dimension(140, 38));

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);

        // ---- Load combo data synchronously ----
        loadComboDataSync(dlgCbKhachHang, "KHACH_HANG", "MA_KH", "HO_TEN");
        loadComboDataSync(dlgCbNhanVien, "NHAN_VIEN", "MA_NV", "HO_TEN");
        loadComboDataSync(dlgCbChiNhanh, "CHI_NHANH", "MA_CN", "TEN_CN");
        loadComboDataSync(dlgCbKhuyenMai, "KHUYEN_MAI", "MA_KM", "TEN_KM");

        // ---- If edit mode, pre-fill data from DB ----
        if (isEdit && maHd > 0) {
            try {
                java.util.Map<String, Object> data = Controller.Admin.HoaDonDAO.getHoaDonById(maHd);
                if (data != null) {
                    dlgTxtTongTien.setText(String.valueOf((long) (double) data.get("TONG_TIEN")));
                    dlgTxtGiamGia.setText(String.valueOf((long) (double) data.get("GIAM_GIA")));
                    dlgTxtThanhTien.setText(String.valueOf((long) (double) data.get("THANH_TIEN")));
                    dlgCbPhuongThuc.setSelectedItem(data.get("PHUONG_THUC_TT"));

                    String trangThai = (String) data.get("TRANG_THAI");
                    if (trangThai == null || trangThai.trim().isEmpty()) trangThai = "Hoàn thành";
                    // Map old status values
                    if ("Chờ xử lý".equals(trangThai)) trangThai = "Chờ thanh toán";
                    dlgCbTrangThai.setSelectedItem(trangThai);
                    
                    if ("Đã hủy".equals(trangThai) || "Hoàn thành".equals(trangThai)) {
                        btnSave.setEnabled(false);
                        btnSave.setToolTipText("Không thể chỉnh sửa hóa đơn đã Hủy hoặc Hoàn thành");
                        
                        dlgCbKhachHang.setEnabled(false);
                        dlgCbNhanVien.setEnabled(false);
                        dlgCbChiNhanh.setEnabled(false);
                        dlgCbKhuyenMai.setEnabled(false);
                        dlgCbPhuongThuc.setEnabled(false);
                        dlgCbTrangThai.setEnabled(false);
                        dlgTxtTongTien.setEditable(false);
                        dlgTxtGiamGia.setEditable(false);
                    }

                    Integer maKh = (Integer) data.get("MA_KH");
                    if (maKh != null) setSelectedComboItem(dlgCbKhachHang, maKh);
                    Integer maNv = (Integer) data.get("MA_NV");
                    if (maNv != null) setSelectedComboItem(dlgCbNhanVien, maNv);
                    Integer maCn = (Integer) data.get("MA_CN");
                    if (maCn != null) setSelectedComboItem(dlgCbChiNhanh, maCn);
                    Integer maKm = (Integer) data.get("MA_KM");
                    if (maKm != null && maKm > 0) {
                        setSelectedComboItem(dlgCbKhuyenMai, maKm);
                    } else {
                        setSelectedComboItem(dlgCbKhuyenMai, -1);
                    }
                    
                    // Khoá Khuyến mãi (Không cho đổi)
                    dlgCbKhuyenMai.setEnabled(false);
                    dlgCbKhuyenMai.setToolTipText("Không được phép thay đổi khuyến mãi của hóa đơn");

                    // Load products into the table
                    try {
                        java.util.List<java.util.Map<String, Object>> details = Controller.Admin.HoaDonDAO.getChiTietHoaDon(maHd);
                                                for (java.util.Map<String, Object> d : details) {
                            String tenSp = (String) d.get("TEN_SP");
                            String serial = (String) d.get("SERIAL_NUMBER");
                            if (serial != null && !serial.isEmpty()) tenSp += " (" + serial + ")";
                            String rowBranchName = d.get("TEN_CN") != null ? (String) d.get("TEN_CN") : (dlgCbChiNhanh.getSelectedItem() != null ? dlgCbChiNhanh.getSelectedItem().toString() : "Không xác định");
                            
                            detailTableModel.addRow(new Object[]{
                                tenSp,
                                d.get("SO_LUONG"),
                                String.format("%,dđ", ((Number)d.get("DON_GIA")).longValue()),
                                String.format("%,dđ", ((Number)d.get("THANH_TIEN")).longValue()),
                                rowBranchName
                            });
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    
                    // Tuyệt đối không cho sửa khuyến mãi của bất kỳ đơn nào sau khi đã tạo
                    dlgCbKhuyenMai.setEnabled(false);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Lỗi khi lấy thông tin hóa đơn: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }

        // ---- Save action ----
        btnSave.addActionListener(e -> {
            try {
                Integer maKh = null;
                if (dlgCbKhachHang.getSelectedItem() != null) {
                    maKh = ((DBItem) dlgCbKhachHang.getSelectedItem()).getId();
                }
                int maNv = 1;
                if (dlgCbNhanVien.getSelectedItem() != null) {
                    maNv = ((DBItem) dlgCbNhanVien.getSelectedItem()).getId();
                }
                int maCn = 1;
                if (dlgCbChiNhanh.getSelectedItem() != null) {
                    maCn = ((DBItem) dlgCbChiNhanh.getSelectedItem()).getId();
                }
                Integer maKm = null;
                if (dlgCbKhuyenMai.getSelectedItem() != null) {
                    int kmId = ((DBItem) dlgCbKhuyenMai.getSelectedItem()).getId();
                    if (kmId > 0) maKm = kmId;
                }
                double tongTien = Double.parseDouble(dlgTxtTongTien.getText().trim());
                double giamGia = Double.parseDouble(dlgTxtGiamGia.getText().trim());
                double thanhTien = Double.parseDouble(dlgTxtThanhTien.getText().trim());
                String phuongThuc = dlgCbPhuongThuc.getSelectedItem().toString();
                String trangThai = dlgCbTrangThai.getSelectedItem().toString();

                boolean success = Controller.Admin.HoaDonDAO.saveHoaDon(
                    maHd, maKh, maNv, maCn, maKm, tongTien, giamGia, thanhTien, phuongThuc, trangThai, isEdit
                );

                if (success) {
                    dialog.dispose();
                    loadDataToTable(tableModel);
                    JOptionPane.showMessageDialog(this, isEdit ? "Cập nhật hóa đơn thành công!" : "Thêm hóa đơn thành công!");
                } else {
                    throw new Exception("Không lưu được hóa đơn");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Lỗi lưu hóa đơn: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    // ==================== SETUP UI ====================
    private void setupCustomUI() {
        this.removeAll();
        this.setLayout(new BorderLayout(15, 15));
        this.setBackground(new Color(248, 250, 252));
        this.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Table Model — 9 columns including "Chỉnh sửa"
        String[] columns = {"", "Mã HD", "Khách hàng", "Mã nhân viên", "Thời gian", "Thành tiền", "Thanh toán", "Trạng thái", "Chỉnh sửa"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : Object.class; }
            @Override public boolean isCellEditable(int r, int c) { return c == 0 || c == 8; }
        };

        // ================= HEADER =================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản lý hóa đơn bán hàng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(30, 41, 59));

        lblLastUpdate = new JLabel("Chưa cập nhật");
        lblLastUpdate.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblLastUpdate.setForeground(new Color(148, 163, 184));

        leftHeader.add(lblTitle);
        leftHeader.add(lblLastUpdate);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightHeader.setOpaque(false);

        String[] statusFilterItems = {"Tất cả trạng thái", "Hoàn thành", "Đang xử lý", "Đang chuẩn bị hàng", "Chờ thanh toán", "Đã hủy"};
        cbFilter = new JComboBox<>(statusFilterItems);
        cbFilter.setPreferredSize(new Dimension(180, 35));
        cbFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton btnRefresh = new JButton("Cập nhật");
        UIUtils.styleButton(btnRefresh);
        btnRefresh.setPreferredSize(new Dimension(130, 36));
        btnRefresh.addActionListener(e -> loadDataToTable(tableModel));

        JButton btnDelete = new JButton("Xóa");
        UIUtils.styleButton(btnDelete);
        btnDelete.setPreferredSize(new Dimension(140, 36));
        btnDelete.addActionListener(e -> deleteSelectedRows());

        // Ẩn nút theo quyền
        btnDelete.setVisible(Controller.Admin.PermissionService.canDelete("Don hang"));
        // Nếu không có quyền Sửa, ẩn cột Chỉnh sửa
        if (!Controller.Admin.PermissionService.canEdit("Don hang")) {
            dataTable.getColumnModel().getColumn(8).setMinWidth(0);
            dataTable.getColumnModel().getColumn(8).setMaxWidth(0);
            dataTable.getColumnModel().getColumn(8).setWidth(0);
        }

        rightHeader.add(btnDelete);
        rightHeader.add(cbFilter);
        rightHeader.add(btnRefresh);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);

        this.add(headerPanel, BorderLayout.NORTH);

        // ================= CENTER (search + table) =================
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);

        dataTable = new JTable(tableModel);
        dataTable.setRowHeight(45);
        dataTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dataTable.setSelectionBackground(new Color(245, 235, 250));
        dataTable.setSelectionForeground(new Color(142, 68, 173));

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        dataTable.setRowSorter(sorter);
        // Prevent sorting on checkbox and edit button columns
        sorter.setSortable(0, false);
        sorter.setSortable(8, false);

        // Search Bar
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(175, 122, 197), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        txtSearch = new JTextField("Tìm kiếm theo Mã HD, Khách hàng, Mã nhân viên...");
        txtSearch.setBorder(null);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtSearch.setForeground(Color.GRAY);
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals("Tìm kiếm theo Mã HD, Khách hàng, Mã nhân viên...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(new Color(30, 41, 59));
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText("Tìm kiếm theo Mã HD, Khách hàng, Mã nhân viên...");
                    txtSearch.setForeground(Color.GRAY);
                }
            }
        });

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { search(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { search(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { search(); }
            private void search() {
                SwingUtilities.invokeLater(() -> {
                    applyFilters(txtSearch.getText().trim(), cbFilter.getSelectedItem().toString());
                });
            }
        });

        searchPanel.add(txtSearch, BorderLayout.CENTER);
        centerPanel.add(searchPanel, BorderLayout.NORTH);

        // Table header styling
        dataTable.getTableHeader().setPreferredSize(new Dimension(0, 45));
        dataTable.getTableHeader().setBackground(Color.WHITE);
        dataTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        dataTable.getTableHeader().setForeground(new Color(100, 116, 139));

        class CenteredHeaderRenderer implements javax.swing.table.TableCellRenderer {
            private javax.swing.table.TableCellRenderer delegate;
            public CenteredHeaderRenderer(javax.swing.table.TableCellRenderer delegate) {
                this.delegate = delegate;
            }
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JLabel) {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                    ((JLabel) c).setFont(new Font("Segoe UI", Font.BOLD, 13));
                }
                return c;
            }
        }
        dataTable.getTableHeader().setDefaultRenderer(new CenteredHeaderRenderer(dataTable.getTableHeader().getDefaultRenderer()));

        // Cell renderers
        DefaultTableCellRenderer centerCellRenderer = new DefaultTableCellRenderer();
        centerCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer leftCellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
                return label;
            }
        };
        leftCellRenderer.setHorizontalAlignment(SwingConstants.LEFT);

        DefaultTableCellRenderer rightCellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
                return label;
            }
        };
        rightCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        // Status column renderer with colored badges
        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String status = value != null ? value.toString() : "";
                if (!isSelected) {
                    switch (status) {
                        case "Hoàn thành":
                            label.setForeground(new Color(21, 128, 61));
                            break;
                        case "Đang xử lý":
                            label.setForeground(new Color(217, 119, 6));
                            break;
                        case "Đang chuẩn bị hàng":
                            label.setForeground(new Color(147, 51, 234));
                            break;
                        case "Chờ thanh toán":
                            label.setForeground(new Color(37, 99, 235));
                            break;
                        case "Đã hủy":
                            label.setForeground(new Color(220, 38, 38));
                            break;
                        default:
                            label.setForeground(new Color(100, 116, 139));
                    }
                }
                return label;
            }
        };

        dataTable.getColumnModel().getColumn(1).setCellRenderer(centerCellRenderer);
        dataTable.getColumnModel().getColumn(2).setCellRenderer(leftCellRenderer);
        dataTable.getColumnModel().getColumn(3).setCellRenderer(centerCellRenderer);
        dataTable.getColumnModel().getColumn(4).setCellRenderer(centerCellRenderer);
        dataTable.getColumnModel().getColumn(5).setCellRenderer(rightCellRenderer);
        dataTable.getColumnModel().getColumn(6).setCellRenderer(centerCellRenderer);
        dataTable.getColumnModel().getColumn(7).setCellRenderer(statusRenderer);

        // Column widths
        dataTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        dataTable.getColumnModel().getColumn(0).setMaxWidth(55);
        dataTable.getColumnModel().getColumn(1).setPreferredWidth(75);
        dataTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        dataTable.getColumnModel().getColumn(3).setPreferredWidth(115);
        dataTable.getColumnModel().getColumn(4).setPreferredWidth(135);
        dataTable.getColumnModel().getColumn(5).setPreferredWidth(115);
        dataTable.getColumnModel().getColumn(6).setPreferredWidth(110);
        dataTable.getColumnModel().getColumn(7).setPreferredWidth(120);
        dataTable.getColumnModel().getColumn(8).setPreferredWidth(90);
        dataTable.getColumnModel().getColumn(8).setMaxWidth(100);

        // ---- Edit button renderer & editor for column 8 ----
        dataTable.getColumnModel().getColumn(8).setCellRenderer(new OrderActionCellRenderer());
        dataTable.getColumnModel().getColumn(8).setCellEditor(new OrderActionCellEditor());

        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(175, 122, 197), 2));
        scrollPane.getViewport().setBackground(Color.WHITE);

        centerPanel.add(scrollPane, BorderLayout.CENTER);
        this.add(centerPanel, BorderLayout.CENTER);

        // Removed bottom buttons since they are now in the header

        // Filter action
        cbFilter.addActionListener(e -> {
            applyFilters(txtSearch.getText().trim(), cbFilter.getSelectedItem().toString());
        });

        // Load initial data
        loadDataToTable(tableModel);
    }

    // ==================== Delete selected rows ====================
    private void deleteSelectedRows() {
        java.util.List<Integer> toDelete = new java.util.ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean checked = (Boolean) tableModel.getValueAt(i, 0);
            if (checked != null && checked) {
                toDelete.add((int) tableModel.getValueAt(i, 1));
            }
        }
        if (toDelete.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn xóa " + toDelete.size() + " hóa đơn đã chọn?",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean success = Controller.Admin.HoaDonDAO.deleteHoaDons(toDelete);
            if (success) {
                loadDataToTable(tableModel);
                JOptionPane.showMessageDialog(this, "Đã xóa " + toDelete.size() + " hóa đơn thành công!");
            } else {
                throw new Exception("Không xóa được hóa đơn");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== Row-level Edit Button Renderer ====================
    private class OrderActionCellRenderer extends JPanel implements TableCellRenderer {
        private final JButton btnEdit;

        public OrderActionCellRenderer() {
            setLayout(new GridBagLayout());
            setOpaque(true);
            btnEdit = new JButton("Sửa");
            UIUtils.styleButton(btnEdit);
            btnEdit.setPreferredSize(new Dimension(72, 30));
            add(btnEdit);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            int modelRow = table.convertRowIndexToModel(row);
            Object statusObj = table.getModel().getValueAt(modelRow, 7);
            String currentStatus = (statusObj instanceof String) ? (String) statusObj : "";
            if ("Đã xóa".equals(currentStatus)) {
                btnEdit.setText("Khôi phục");
                btnEdit.setForeground(Color.WHITE);
            } else {
                btnEdit.setText("Sửa");
                btnEdit.setForeground(Color.WHITE);
            }
            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }

    // ==================== Row-level Edit Button Editor ====================
    private class OrderActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private final JButton btnEdit;
        private int currentMaHd = -1;
        private String currentStatus = "";

        public OrderActionCellEditor() {
            panel = new JPanel(new GridBagLayout());
            panel.setOpaque(true);
            btnEdit = new JButton("Sửa");
            UIUtils.styleButton(btnEdit);
            btnEdit.setPreferredSize(new Dimension(72, 30));
            panel.add(btnEdit);

            btnEdit.addActionListener(e -> {
                fireEditingStopped();
                if (currentMaHd > 0) {
                    if ("Đã xóa".equals(currentStatus)) {
                        int confirm = JOptionPane.showConfirmDialog(panel, "Bạn có muốn khôi phục hóa đơn #" + currentMaHd + " không?", "Khôi phục hóa đơn", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            try {
                                if (Controller.Admin.HoaDonDAO.restoreHoaDon(currentMaHd)) {
                                    JOptionPane.showMessageDialog(panel, "Khôi phục thành công!");
                                    loadDataToTable(tableModel);
                                } else {
                                    JOptionPane.showMessageDialog(panel, "Lỗi khôi phục!");
                                }
                            } catch (Exception ex) { ex.printStackTrace(); }
                        }
                    } else {
                        SwingUtilities.invokeLater(() -> showOrderDialog(true, currentMaHd));
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            int modelRow = table.convertRowIndexToModel(row);
            Object maHdObj = table.getModel().getValueAt(modelRow, 1);
            Object statusObj = table.getModel().getValueAt(modelRow, 7);
            currentMaHd = (maHdObj instanceof Integer) ? (int) maHdObj : -1;
            currentStatus = (statusObj instanceof String) ? (String) statusObj : "";
            
            if ("Đã xóa".equals(currentStatus)) {
                btnEdit.setText("Khôi phục");
                btnEdit.setForeground(Color.WHITE);
            } else {
                btnEdit.setText("Sửa");
                btnEdit.setForeground(Color.WHITE);
            }

            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    // ==================== Load data ====================
    private void loadDataToTable(DefaultTableModel model) {
        model.setRowCount(0);
        String time = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));
        if (lblLastUpdate != null) {
            lblLastUpdate.setText("Cập nhật lúc: " + time);
        }
        DecimalFormat df = new DecimalFormat("#,### đ");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try {
            boolean canEdit = Controller.Admin.PermissionService.canEdit("Don hang");
            java.util.List<java.util.Map<String, Object>> dataList = Controller.Admin.HoaDonDAO.getAllHoaDon(canEdit);
            for (java.util.Map<String, Object> row : dataList) {
                int maNv = (Integer) row.get("MA_NV");
                String empCode = String.valueOf(maNv);
                String trangThai = (String) row.get("TRANG_THAI");
                if (trangThai == null || trangThai.trim().isEmpty()) {
                    trangThai = "Hoàn thành";
                }
                // Map old status
                if ("Chờ xử lý".equals(trangThai)) trangThai = "Chờ thanh toán";
                
                int isDeleted = ((Number) row.getOrDefault("IS_DELETED", 0)).intValue();
                if (isDeleted == 1) {
                    trangThai = "Đã xóa";
                }
                
                java.sql.Timestamp thoiGianLap = (java.sql.Timestamp) row.get("THOI_GIAN_LAP");
                String formattedTime = thoiGianLap != null ? sdf.format(thoiGianLap) : "";
                
                model.addRow(new Object[]{
                    false,
                    row.get("MA_HD"),
                    row.get("TEN_KH"),
                    empCode,
                    formattedTime,
                    df.format((Double) row.get("THANH_TIEN")),
                    row.get("PHUONG_THUC_TT"),
                    trangThai,
                    ""  // Edit button column placeholder
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ==================== Load combo data synchronously ====================
    private void loadComboDataSync(JComboBox<DBItem> combo, String table, String idCol, String nameCol) {
        combo.removeAllItems();
        try {
            if ("KHUYEN_MAI".equals(table)) {
                combo.addItem(new DBItem(-1, "(Không có khuyến mãi)"));
            }
            java.util.List<java.util.Map<String, Object>> items = Controller.Admin.HoaDonDAO.getComboData(table, idCol, nameCol);
            for (java.util.Map<String, Object> item : items) {
                int id = (Integer) item.get("ID");
                String name = (String) item.get("NAME");
                if ("NHAN_VIEN".equals(table)) {
                    name = id + " - " + name;
                }
                combo.addItem(new DBItem(id, name));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================== Styled button helpers ====================


    @SuppressWarnings("unchecked")
    private void initComponents() {
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 400, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 300, Short.MAX_VALUE));
    }
}
