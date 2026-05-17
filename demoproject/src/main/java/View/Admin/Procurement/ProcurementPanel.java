package View.Admin.Procurement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.DefaultTableCellRenderer;
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

    private boolean isEdit = false;
    private int editingMaHd = -1;
    private DefaultTableModel tableModel;
    private JTable dataTable;
    
    // Form components
    private JComboBox<DBItem> cbKhachHang, cbNhanVien, cbChiNhanH, cbKhuyenMai;
    private JTextField txtTongTienHang, txtGiamGia, txtThanhTien;
    private JComboBox<String> cbPhuongThuc;
    private JPanel addFormPanel;
    private JButton btnSaveForm;

    public ProcurementPanel() {
        initComponents();
        setupCustomUI();
    }

    private void setupCustomUI() {
        this.removeAll();
        this.setLayout(new BorderLayout(15, 15));
        this.setBackground(new Color(248, 250, 252));
        this.setBorder(new EmptyBorder(20, 20, 20, 20));

        // ================= HEADER =================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản lý hóa đơn bán hàng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(30, 41, 59));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        String[] statusFilter = {"Tất cả trạng thái", "Hoàn thành", "Đang xử lý", "Chờ xử lý", "Đã hủy"};
        JComboBox<String> cbFilter = new JComboBox<>(statusFilter);
        cbFilter.setPreferredSize(new Dimension(180, 35));
        headerPanel.add(cbFilter, BorderLayout.EAST);

        this.add(headerPanel, BorderLayout.NORTH);

        // ================= CENTER =================
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);

        // Table Model and Sorter
        String[] columns = {"", "Mã HD", "Khách hàng", "Mã nhân viên", "Thời gian", "Tổng tiền", "Giảm giá", "Thành tiền", "Thanh toán"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : Object.class; }
            @Override public boolean isCellEditable(int r, int c) { return c == 0; }
        };
        dataTable = new JTable(tableModel);
        dataTable.setRowHeight(45);
        dataTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dataTable.setSelectionBackground(new Color(245, 235, 250)); // Orchid pink-purple soft background
        dataTable.setSelectionForeground(new Color(142, 68, 173));  // Dark orchid text
        
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        dataTable.setRowSorter(sorter);

        // Search Bar
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(175, 122, 197), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        JTextField txtSearch = new JTextField("Tìm kiếm theo Mã HD, Khách hàng, Mã nhân viên...");
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
                    String text = txtSearch.getText().trim();
                    if (text.isEmpty() || text.equals("Tìm kiếm theo Mã HD, Khách hàng, Mã nhân viên...") || txtSearch.getForeground() == Color.GRAY) {
                        sorter.setRowFilter(null);
                    } else {
                        final String searchLower = text.toLowerCase();
                        sorter.setRowFilter(new RowFilter<DefaultTableModel, Object>() {
                            @Override
                            public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Object> entry) {
                                String maHd = entry.getStringValue(1).toLowerCase();
                                String kh = entry.getStringValue(2).toLowerCase();
                                String maNv = entry.getStringValue(3).toLowerCase();
                                return maHd.contains(searchLower) || kh.contains(searchLower) || maNv.contains(searchLower);
                            }
                        });
                    }
                });
            }
        });

        searchPanel.add(txtSearch, BorderLayout.CENTER);
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Table Header styling & centering
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

        // Cell Alignment & Padding
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

        // Apply Renderers to Columns
        dataTable.getColumnModel().getColumn(1).setCellRenderer(centerCellRenderer); // Mã HD
        dataTable.getColumnModel().getColumn(2).setCellRenderer(leftCellRenderer);   // Khách hàng
        dataTable.getColumnModel().getColumn(3).setCellRenderer(centerCellRenderer); // Mã nhân viên
        dataTable.getColumnModel().getColumn(4).setCellRenderer(centerCellRenderer); // Thời gian
        dataTable.getColumnModel().getColumn(5).setCellRenderer(rightCellRenderer);  // Tổng tiền
        dataTable.getColumnModel().getColumn(6).setCellRenderer(rightCellRenderer);  // Giảm giá
        dataTable.getColumnModel().getColumn(7).setCellRenderer(rightCellRenderer);  // Thành tiền
        dataTable.getColumnModel().getColumn(8).setCellRenderer(centerCellRenderer); // Thanh toán

        // Column Widths
        dataTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        dataTable.getColumnModel().getColumn(0).setMaxWidth(55);
        dataTable.getColumnModel().getColumn(1).setPreferredWidth(85);
        dataTable.getColumnModel().getColumn(2).setPreferredWidth(170);
        dataTable.getColumnModel().getColumn(3).setPreferredWidth(125);
        dataTable.getColumnModel().getColumn(4).setPreferredWidth(140);
        dataTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        dataTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        dataTable.getColumnModel().getColumn(7).setPreferredWidth(120);
        dataTable.getColumnModel().getColumn(8).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(175, 122, 197), 2));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        // Form Panel
        addFormPanel = new JPanel(new BorderLayout(10, 10));
        addFormPanel.setBackground(new Color(241, 245, 249));
        addFormPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        addFormPanel.setVisible(false);
        
        JPanel inputGrid = new JPanel(new GridLayout(2, 5, 15, 10));
        inputGrid.setOpaque(false);
        
        cbKhachHang = new JComboBox<>();
        cbNhanVien = new JComboBox<>();
        cbChiNhanH = new JComboBox<>();
        cbKhuyenMai = new JComboBox<>();
        cbPhuongThuc = new JComboBox<>(new String[]{"Tiền mặt", "Chuyển khoản", "Thẻ tín dụng", "Ví điện tử"});
        txtTongTienHang = new JTextField("0");
        txtGiamGia = new JTextField("0");
        txtThanhTien = new JTextField("0");
        txtThanhTien.setEditable(false);

        inputGrid.add(new JLabel("Khách hàng")); inputGrid.add(new JLabel("Mã nhân viên")); inputGrid.add(new JLabel("Chi nhánh")); inputGrid.add(new JLabel("Khuyến mãi")); inputGrid.add(new JLabel("PT Thanh toán"));
        inputGrid.add(cbKhachHang); inputGrid.add(cbNhanVien); inputGrid.add(cbChiNhanH); inputGrid.add(cbKhuyenMai); inputGrid.add(cbPhuongThuc);

        JPanel inputGrid2 = new JPanel(new GridLayout(1, 6, 15, 10));
        inputGrid2.setOpaque(false);
        inputGrid2.add(new JLabel("Tổng tiền")); inputGrid2.add(txtTongTienHang);
        inputGrid2.add(new JLabel("Giảm giá")); inputGrid2.add(txtGiamGia);
        inputGrid2.add(new JLabel("Thành tiền")); inputGrid2.add(txtThanhTien);

        JPanel formContent = new JPanel(new BorderLayout(10, 10));
        formContent.setOpaque(false);
        formContent.add(inputGrid, BorderLayout.NORTH);
        formContent.add(inputGrid2, BorderLayout.SOUTH);

        JPanel formButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        formButtons.setOpaque(false);
        btnSaveForm = createStyledButton("Lưu hóa đơn", new Color(16, 185, 129), Color.WHITE);
        JButton btnCancelForm = createStyledButton("Hủy", new Color(148, 163, 184), Color.WHITE);
        formButtons.add(btnSaveForm); formButtons.add(btnCancelForm);
        
        addFormPanel.add(formContent, BorderLayout.CENTER);
        addFormPanel.add(formButtons, BorderLayout.SOUTH);

        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(addFormPanel, BorderLayout.SOUTH);
        this.add(centerPanel, BorderLayout.CENTER);

        // ================= BOTTOM ACTIONS =================
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftActions.setOpaque(false);
        JButton btnAdd = createGradientButton("Thêm mới");
        JButton btnEdit = createGradientButton("Sửa");
        JButton btnDelete = createGradientButton("Xóa");
        leftActions.add(btnAdd); leftActions.add(btnEdit); leftActions.add(btnDelete);
        bottomPanel.add(leftActions, BorderLayout.WEST);
        this.add(bottomPanel, BorderLayout.SOUTH);

        // Events
        btnAdd.addActionListener(e -> {
            isEdit = false; editingMaHd = -1; btnSaveForm.setText("Lưu hóa đơn");
            txtTongTienHang.setText("0"); txtGiamGia.setText("0"); txtThanhTien.setText("0");
            addFormPanel.setVisible(true); this.revalidate();
        });

        btnEdit.addActionListener(e -> {
            int row = dataTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn hóa đơn để sửa!"); return; }
            isEdit = true; btnSaveForm.setText("Cập nhật");
            int modelRow = dataTable.convertRowIndexToModel(row);
            editingMaHd = (int) tableModel.getValueAt(modelRow, 1);
            txtTongTienHang.setText(tableModel.getValueAt(modelRow, 5).toString().replace(",", "").replace(" đ", ""));
            txtGiamGia.setText(tableModel.getValueAt(modelRow, 6).toString().replace(",", "").replace(" đ", ""));
            cbPhuongThuc.setSelectedItem(tableModel.getValueAt(modelRow, 8).toString());
            addFormPanel.setVisible(true); this.revalidate();
        });

        btnCancelForm.addActionListener(e -> { addFormPanel.setVisible(false); this.revalidate(); });

        btnSaveForm.addActionListener(e -> {
            try (Connection con = ConnectionUtils.getMyConnection()) {
                String sql = !isEdit ? 
                    "INSERT INTO HOADON (MA_KH, MA_NV, MA_CN, MA_KM, TONG_TIEN_HANG, GIAM_GIA, THANH_TIEN, PHUONG_THUC_TT) VALUES (?, ?, ?, ?, ?, ?, ?, ?)" :
                    "UPDATE HOADON SET MA_KH=?, MA_NV=?, MA_CN=?, MA_KM=?, TONG_TIEN_HANG=?, GIAM_GIA=?, THANH_TIEN=?, PHUONG_THUC_TT=? WHERE MA_HD=?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    if (cbKhachHang.getSelectedItem() != null) {
                        ps.setInt(1, ((DBItem) cbKhachHang.getSelectedItem()).getId());
                    } else {
                        ps.setNull(1, java.sql.Types.INTEGER);
                    }
                    
                    if (cbNhanVien.getSelectedItem() != null) {
                        ps.setInt(2, ((DBItem) cbNhanVien.getSelectedItem()).getId());
                    } else {
                        ps.setInt(2, 1);
                    }
                    
                    if (cbChiNhanH.getSelectedItem() != null) {
                        ps.setInt(3, ((DBItem) cbChiNhanH.getSelectedItem()).getId());
                    } else {
                        ps.setInt(3, 1);
                    }
                    
                    if (cbKhuyenMai.getSelectedItem() != null) {
                        ps.setInt(4, ((DBItem) cbKhuyenMai.getSelectedItem()).getId());
                    } else {
                        ps.setNull(4, java.sql.Types.INTEGER);
                    }
                    
                    ps.setDouble(5, Double.parseDouble(txtTongTienHang.getText().trim()));
                    ps.setDouble(6, Double.parseDouble(txtGiamGia.getText().trim()));
                    ps.setDouble(7, Double.parseDouble(txtThanhTien.getText().trim()));
                    ps.setString(8, cbPhuongThuc.getSelectedItem().toString());
                    
                    if (isEdit) {
                        ps.setInt(9, editingMaHd);
                    }
                    
                    ps.executeUpdate();
                    addFormPanel.setVisible(false);
                    loadDataToTable(tableModel);
                    JOptionPane.showMessageDialog(this, isEdit ? "Cập nhật hóa đơn thành công!" : "Lưu hóa đơn thành công!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi lưu hóa đơn: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnDelete.addActionListener(e -> {
            int row = dataTable.getSelectedRow();
            if (row == -1) return;
            if (JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa hóa đơn này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                int maHd = (int) tableModel.getValueAt(dataTable.convertRowIndexToModel(row), 1);
                try (Connection con = ConnectionUtils.getMyConnection()) {
                    con.setAutoCommit(false);
                    try (PreparedStatement psCthd = con.prepareStatement("DELETE FROM CHITIET_HOADON WHERE MA_HD = ?");
                         PreparedStatement psPdv = con.prepareStatement("DELETE FROM PHIEU_DICH_VU WHERE MA_HD = ?");
                         PreparedStatement psBh = con.prepareStatement("DELETE FROM BAOHANH WHERE MA_HD = ?");
                         PreparedStatement psHd = con.prepareStatement("DELETE FROM HOADON WHERE MA_HD = ?")) {
                        
                        psCthd.setInt(1, maHd);
                        psCthd.executeUpdate();
                        
                        psPdv.setInt(1, maHd);
                        psPdv.executeUpdate();
                        
                        psBh.setInt(1, maHd);
                        psBh.executeUpdate();
                        
                        psHd.setInt(1, maHd);
                        psHd.executeUpdate();
                        
                        con.commit();
                        loadDataToTable(tableModel);
                        JOptionPane.showMessageDialog(this, "Xóa hóa đơn thành công!");
                    } catch (Exception ex) {
                        con.rollback();
                        throw ex;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Lỗi khi xóa hóa đơn: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Auto Calc
        txtTongTienHang.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { calc(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { calc(); }
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { calc(); }
            private void calc() {
                try {
                    double t = Double.parseDouble(txtTongTienHang.getText().trim());
                    double g = Double.parseDouble(txtGiamGia.getText().trim());
                    txtThanhTien.setText(String.valueOf((long)(t - g)));
                } catch (Exception ex) { txtThanhTien.setText("0"); }
            }
        });

        // Load data in background
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                loadComboData(cbKhachHang, "KHACHHANG", "MA_KH", "HO_TEN");
                loadComboData(cbNhanVien, "NHANVIEN", "MA_NV", "HO_TEN");
                loadComboData(cbChiNhanH, "CHINHANH", "MA_CN", "TEN_CN");
                loadComboData(cbKhuyenMai, "KHUYENMAI", "MA_KM", "TEN_KM");
                return null;
            }
        }.execute();

        loadDataToTable(tableModel);
    }

    private void loadDataToTable(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT H.MA_HD, K.HO_TEN as TEN_KH, H.MA_NV, H.THOI_GIAN_LAP, H.TONG_TIEN_HANG, H.GIAM_GIA, H.THANH_TIEN, H.PHUONG_THUC_TT FROM HOADON H LEFT JOIN KHACHHANG K ON H.MA_KH = K.MA_KH ORDER BY H.MA_HD DESC";
        DecimalFormat df = new DecimalFormat("#,### đ");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            boolean has = false;
            while (rs.next()) {
                has = true;
                int maNv = rs.getInt("MA_NV");
                String empCode = maNv > 0 ? "NV" + String.format("%03d", maNv) : "NV001";
                model.addRow(new Object[]{false, rs.getInt("MA_HD"), rs.getString("TEN_KH"), empCode, rs.getTimestamp("THOI_GIAN_LAP") != null ? sdf.format(rs.getTimestamp("THOI_GIAN_LAP")) : "", df.format(rs.getDouble("TONG_TIEN_HANG")), df.format(rs.getDouble("GIAM_GIA")), df.format(rs.getDouble("THANH_TIEN")), rs.getString("PHUONG_THUC_TT")});
            }
            if (!has) {
                model.addRow(new Object[]{false, 1, "Dữ liệu mẫu A", "NV001", "16/05/2026", df.format(100000), "0", df.format(100000), "Tiền mặt"});
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadComboData(JComboBox<DBItem> combo, String table, String idCol, String nameCol) {
        try (Connection con = ConnectionUtils.getMyConnection(); PreparedStatement ps = con.prepareStatement("SELECT " + idCol + ", " + nameCol + " FROM " + table); ResultSet rs = ps.executeQuery()) {
            SwingUtilities.invokeLater(() -> {
                combo.removeAllItems();
                try {
                    while (rs.next()) {
                        int id = rs.getInt(1);
                        String name = rs.getString(2);
                        if ("NHANVIEN".equals(table)) {
                            name = "NV" + String.format("%03d", id) + " - " + name;
                        }
                        combo.addItem(new DBItem(id, name));
                    }
                } catch (Exception ex) {}
            });
        } catch (Exception e) {}
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg); btn.setForeground(fg);
        btn.setFocusPainted(false); btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createGradientButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color colorTop = new Color(175, 122, 197); 
                Color colorBottom = new Color(210, 160, 205); 
                
                GradientPaint gp = new GradientPaint(0, 0, colorTop, 0, getHeight(), colorBottom);
                g2d.setPaint(gp);
                
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.dispose();
                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(120, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 400, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 300, Short.MAX_VALUE));
    }
}
