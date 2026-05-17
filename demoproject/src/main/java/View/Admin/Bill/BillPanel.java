/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package View.Admin.Bill;

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
import java.sql.Statement;

import ConnectDB.ConnectionUtils;

/**
 *
 * @author DELL
 */
public class BillPanel extends javax.swing.JPanel {

    // === KHAI BÁO CÁC BIẾN TOÀN CỤC BỊ THIẾU ===
    private boolean isEdit = false;
    private int editingMaPn = -1;

    /**
     * Creates new form BillPanel
     */
    public BillPanel() {
        initComponents();
        setupCustomUI();
    }

    private void setupCustomUI() {
        JPanel mainContainer = new JPanel(new BorderLayout(10, 10));
        mainContainer.setBackground(new Color(245, 247, 250));
        mainContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        // ================= HEADER =================
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản lý đơn nhập hàng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator separator = new JSeparator();
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        separator.setForeground(new Color(200, 200, 200));
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(separator);
        headerPanel.add(Box.createVerticalStrut(5));

        mainContainer.add(headerPanel, BorderLayout.NORTH);

        // ================= CENTER =================
        JPanel centerPanel = new JPanel(new BorderLayout(0, 0));
        centerPanel.setBackground(new Color(215, 215, 215));
        centerPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Phần Search Bar
        JPanel topSearchPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                Color colorTop = new Color(175, 122, 197); 
                Color colorBottom = new Color(210, 160, 205); 
                GradientPaint gp = new GradientPaint(0, 0, colorTop, 0, getHeight(), colorBottom);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        topSearchPanel.setOpaque(false);
        topSearchPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField txtSearch = new JTextField("Tìm kiếm theo Mã PN");
        txtSearch.setPreferredSize(new Dimension(0, 30));
        txtSearch.setBackground(new Color(225, 225, 225));
        txtSearch.setForeground(Color.GRAY);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        txtSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals("Tìm kiếm theo Mã PN")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setForeground(Color.GRAY);
                    txtSearch.setText("Tìm kiếm theo Mã PN");
                }
            }
        });

        topSearchPanel.add(txtSearch, BorderLayout.CENTER);
        centerPanel.add(topSearchPanel, BorderLayout.NORTH);

        // Bảng dữ liệu
        String[] columns = {"", "Mã PN", "Nhà cung cấp", "Mã NV", "Mã CN", "Ngày nhập", "Số lượng", "Đơn giá", "Tổng tiền", "Trạng thái", "Ghi chú"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class; 
                return super.getColumnClass(columnIndex);
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; 
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(230, 230, 230));
        table.getTableHeader().setPreferredSize(new Dimension(0, 35));
        table.setBackground(new Color(215, 215, 215)); 
        table.setShowGrid(false);

        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(0).setMinWidth(40);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(215, 215, 215)); 
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 1; i < table.getColumnCount(); i++) {
            if (i != 2) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
        
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { search(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { search(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { search(); }
            private void search() {
                SwingUtilities.invokeLater(() -> {
                    String text = txtSearch.getText();
                    if (text.trim().isEmpty() || text.equals("Tìm kiếm theo mã hóa đơn") || txtSearch.getForeground() == Color.GRAY) {
                        sorter.setRowFilter(null);
                    } else {
                        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1));
                    }
                });
            }
        });
        
        // ================= ADD FORM PANEL =================
        JPanel addFormPanel = new JPanel(new BorderLayout(10, 10));
        addFormPanel.setBackground(new Color(230, 240, 250));
        addFormPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JPanel inputGrid = new JPanel(new GridLayout(2, 8, 10, 5));
        inputGrid.setOpaque(false);
        
        JTextField txtSupplier = new JTextField();
        JTextField txtMaNV = new JTextField();
        JTextField txtMaCN = new JTextField();
        JTextField txtSoLuong = new JTextField("0");
        JTextField txtDonGia = new JTextField("0");
        JTextField txtTongTien = new JTextField("0");
        txtTongTien.setEditable(false);
        JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"-1", "0", "1"});
        cbTrangThai.setSelectedIndex(1); 
        JTextField txtGhiChu = new JTextField();
        
        javax.swing.event.DocumentListener autoCalc = new javax.swing.event.DocumentListener() {
            private void calculate() {
                try {
                    double qty = Double.parseDouble(txtSoLuong.getText().trim());
                    double price = Double.parseDouble(txtDonGia.getText().trim());
                    txtTongTien.setText(String.valueOf((long)(qty * price)));
                } catch (Exception e) {
                    txtTongTien.setText("0");
                }
            }
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { calculate(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { calculate(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { calculate(); }
        };
        txtSoLuong.getDocument().addDocumentListener(autoCalc);
        txtDonGia.getDocument().addDocumentListener(autoCalc);

        inputGrid.add(new JLabel("Nhà cung cấp"));
        inputGrid.add(new JLabel("Mã NV"));
        inputGrid.add(new JLabel("Mã CN"));
        inputGrid.add(new JLabel("Số lượng"));
        inputGrid.add(new JLabel("Đơn giá"));
        inputGrid.add(new JLabel("Tổng tiền"));
        inputGrid.add(new JLabel("Trạng thái"));
        inputGrid.add(new JLabel("Ghi chú"));
        
        inputGrid.add(txtSupplier);
        inputGrid.add(txtMaNV);
        inputGrid.add(txtMaCN);
        inputGrid.add(txtSoLuong);
        inputGrid.add(txtDonGia);
        inputGrid.add(txtTongTien);
        inputGrid.add(cbTrangThai);
        inputGrid.add(txtGhiChu);
        
        JPanel formButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        formButtons.setOpaque(false);
        JButton btnSaveAdd = createGradientButton("Lưu");
        btnSaveAdd.setPreferredSize(new Dimension(80, 35));
        JButton btnCancelAdd = createGradientButton("Hủy");
        btnCancelAdd.setPreferredSize(new Dimension(80, 35));
        
        formButtons.add(btnSaveAdd);
        formButtons.add(btnCancelAdd);
        
        addFormPanel.add(inputGrid, BorderLayout.CENTER);
        addFormPanel.add(formButtons, BorderLayout.EAST);
        addFormPanel.setVisible(false);
        
        centerPanel.add(addFormPanel, BorderLayout.SOUTH);
        
        btnCancelAdd.addActionListener(e -> {
            addFormPanel.setVisible(false);
            txtSupplier.setText("");
            txtMaNV.setText("");
            txtMaCN.setText("");
            txtSoLuong.setText("0");
            txtDonGia.setText("0");
            txtTongTien.setText("0");
            cbTrangThai.setSelectedIndex(1);
            txtGhiChu.setText("");
        });
        
        btnSaveAdd.addActionListener(e -> {
            String supplierName = txtSupplier.getText().trim();
            if (supplierName.isEmpty() || txtMaNV.getText().trim().isEmpty() || txtMaCN.getText().trim().isEmpty() || txtTongTien.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection con = ConnectionUtils.getMyConnection()) {
                con.setAutoCommit(false); 
                int maNcc = -1;
                String checkSql = "SELECT MA_NCC FROM NHACUNGCAP WHERE UPPER(TEN_NCC) = UPPER(?)";
                try (PreparedStatement psCheck = con.prepareStatement(checkSql)) {
                    psCheck.setString(1, supplierName);
                    try (ResultSet rsCheck = psCheck.executeQuery()) {
                        if (rsCheck.next()) maNcc = rsCheck.getInt("MA_NCC");
                    }
                }

                if (maNcc == -1) {
                    String insNccSql = "INSERT INTO NHACUNGCAP (TEN_NCC) VALUES (?)";
                    try (PreparedStatement psInsNcc = con.prepareStatement(insNccSql, Statement.RETURN_GENERATED_KEYS)) {
                        psInsNcc.setString(1, supplierName);
                        psInsNcc.executeUpdate();
                        try (ResultSet rsKey = psInsNcc.getGeneratedKeys()) {
                            if (rsKey.next()) maNcc = rsKey.getInt(1);
                        }
                    }
                }

                if (!isEdit) {
                    String sql = "INSERT INTO PHIEU_NHAP (MA_NCC, MA_NV, MA_CN, TONG_TIEN, TRANG_THAI, GHI_CHU) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, maNcc);
                        ps.setInt(2, Integer.parseInt(txtMaNV.getText().trim()));
                        ps.setInt(3, Integer.parseInt(txtMaCN.getText().trim()));
                        ps.setDouble(4, Double.parseDouble(txtTongTien.getText().trim()));
                        ps.setInt(5, Integer.parseInt(cbTrangThai.getSelectedItem().toString()));
                        ps.setString(6, txtGhiChu.getText().trim());
                        ps.executeUpdate();
                        
                        try (ResultSet rsPn = ps.getGeneratedKeys()) {
                            if (rsPn.next()) {
                                int newMaPn = rsPn.getInt(1);
                                int maBienthe = 1;
                                try (PreparedStatement psBt = con.prepareStatement("SELECT NVL(MIN(MA_BIENTHE), 1) FROM BIENTHE_SANPHAM")) {
                                    try (ResultSet rsBt = psBt.executeQuery()) {
                                        if (rsBt.next()) maBienthe = rsBt.getInt(1);
                                    }
                                }
                                String sqlCt = "INSERT INTO CHITIET_PHIEUNHAP (MA_PN, MA_BIENTHE, SO_LUONG, DON_GIA_NHAP) VALUES (?, ?, ?, ?)";
                                try (PreparedStatement psCt = con.prepareStatement(sqlCt)) {
                                    psCt.setInt(1, newMaPn);
                                    psCt.setInt(2, maBienthe);
                                    psCt.setInt(3, Integer.parseInt(txtSoLuong.getText().trim()));
                                    psCt.setDouble(4, Double.parseDouble(txtDonGia.getText().trim()));
                                    psCt.executeUpdate();
                                }
                            }
                        }
                    }
                } else {
                    String sql = "UPDATE PHIEU_NHAP SET MA_NCC=?, MA_NV=?, MA_CN=?, TONG_TIEN=?, TRANG_THAI=?, GHI_CHU=? WHERE MA_PN=?";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setInt(1, maNcc);
                        ps.setInt(2, Integer.parseInt(txtMaNV.getText().trim()));
                        ps.setInt(3, Integer.parseInt(txtMaCN.getText().trim()));
                        ps.setDouble(4, Double.parseDouble(txtTongTien.getText().trim()));
                        ps.setInt(5, Integer.parseInt(cbTrangThai.getSelectedItem().toString()));
                        ps.setString(6, txtGhiChu.getText().trim());
                        ps.setInt(7, editingMaPn);
                        ps.executeUpdate();
                    }
                    
                    String sqlCtCheck = "SELECT COUNT(*) FROM CHITIET_PHIEUNHAP WHERE MA_PN = ?";
                    boolean hasDetail = false;
                    try (PreparedStatement psCtCheck = con.prepareStatement(sqlCtCheck)) {
                        psCtCheck.setInt(1, editingMaPn);
                        try (ResultSet rsCt = psCtCheck.executeQuery()) {
                            if (rsCt.next() && rsCt.getInt(1) > 0) hasDetail = true;
                        }
                    }
                    
                    if (hasDetail) {
                        String sqlCtUpd = "UPDATE CHITIET_PHIEUNHAP SET SO_LUONG=?, DON_GIA_NHAP=? WHERE MA_PN=?";
                        try (PreparedStatement psCtUpd = con.prepareStatement(sqlCtUpd)) {
                            psCtUpd.setInt(1, Integer.parseInt(txtSoLuong.getText().trim()));
                            psCtUpd.setDouble(2, Double.parseDouble(txtDonGia.getText().trim()));
                            psCtUpd.setInt(3, editingMaPn);
                            psCtUpd.executeUpdate();
                        }
                    } else {
                        int maBienthe = 1;
                        try (PreparedStatement psBt = con.prepareStatement("SELECT NVL(MIN(MA_BIENTHE), 1) FROM BIENTHE_SANPHAM")) {
                            try (ResultSet rsBt = psBt.executeQuery()) {
                                if (rsBt.next()) maBienthe = rsBt.getInt(1);
                            }
                        }
                        String sqlCtIns = "INSERT INTO CHITIET_PHIEUNHAP (MA_PN, MA_BIENTHE, SO_LUONG, DON_GIA_NHAP) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement psCtIns = con.prepareStatement(sqlCtIns)) {
                            psCtIns.setInt(1, editingMaPn);
                            psCtIns.setInt(2, maBienthe);
                            psCtIns.setInt(3, Integer.parseInt(txtSoLuong.getText().trim()));
                            psCtIns.setDouble(4, Double.parseDouble(txtDonGia.getText().trim()));
                            psCtIns.executeUpdate();
                        }
                    }
                }

                con.commit();
                JOptionPane.showMessageDialog(this, isEdit ? "Cập nhật thành công!" : "Thêm phiếu nhập thành công!");
                btnCancelAdd.doClick();
                loadDataToTable(model);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi xử lý: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        mainContainer.add(centerPanel, BorderLayout.CENTER);

        // ================= BOTTOM =================
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton btnEdit = createGradientButton("Sửa");
        btnEdit.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu nhập để sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            isEdit = true;
            btnSaveAdd.setText("Cập nhật");
            int modelRow = table.convertRowIndexToModel(selectedRow);
            
            editingMaPn = (int) model.getValueAt(modelRow, 1);
            txtSupplier.setText(model.getValueAt(modelRow, 2).toString());
            txtMaNV.setText(model.getValueAt(modelRow, 3).toString());
            txtMaCN.setText(model.getValueAt(modelRow, 4).toString());
            txtSoLuong.setText(model.getValueAt(modelRow, 6).toString());
            
            String donGiaStr = model.getValueAt(modelRow, 7).toString().replace(",", "");
            String tongTienStr = model.getValueAt(modelRow, 8).toString().replace(",", "");
            txtDonGia.setText(donGiaStr);
            txtTongTien.setText(tongTienStr);
            
            cbTrangThai.setSelectedItem(model.getValueAt(modelRow, 9).toString());
            txtGhiChu.setText(model.getValueAt(modelRow, 10) != null ? model.getValueAt(modelRow, 10).toString() : "");

            addFormPanel.setVisible(true);
            txtSupplier.requestFocus();
        });
        bottomPanel.add(btnEdit, BorderLayout.WEST);

        JPanel rightButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightButtonsPanel.setOpaque(false);
        JButton btnAdd = createGradientButton("Thêm");
        btnAdd.addActionListener(e -> {
            isEdit = false;
            editingMaPn = -1;
            btnSaveAdd.setText("Lưu");
            txtSupplier.setText("");
            txtMaNV.setText("");
            txtMaCN.setText("");
            txtSoLuong.setText("0");
            txtDonGia.setText("0");
            txtTongTien.setText("0");
            cbTrangThai.setSelectedIndex(1);
            txtGhiChu.setText("");
            addFormPanel.setVisible(true);
            txtSupplier.requestFocus();
        });
        
        JButton btnDelete = createGradientButton("Xóa");
        btnDelete.addActionListener(e -> {
            boolean hasChecked = false;
            for (int i = model.getRowCount() - 1; i >= 0; i--) {
                Boolean isChecked = (Boolean) model.getValueAt(i, 0);
                if (isChecked != null && isChecked) {
                    hasChecked = true;
                    break;
                }
            }

            if (hasChecked) {
                int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa các phiếu nhập đã chọn?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try (Connection con = ConnectionUtils.getMyConnection()) {
                        con.setAutoCommit(false);
                        try (PreparedStatement psCt = con.prepareStatement("DELETE FROM CHITIET_PHIEUNHAP WHERE MA_PN = ?");
                             PreparedStatement psPn = con.prepareStatement("DELETE FROM PHIEU_NHAP WHERE MA_PN = ?")) {
                            for (int i = model.getRowCount() - 1; i >= 0; i--) {
                                Boolean isChecked = (Boolean) model.getValueAt(i, 0);
                                if (isChecked != null && isChecked) {
                                    int maPn = (int) model.getValueAt(i, 1);
                                    
                                    psCt.setInt(1, maPn);
                                    psCt.executeUpdate();
                                    
                                    psPn.setInt(1, maPn);
                                    psPn.executeUpdate();
                                    
                                    model.removeRow(i);
                                }
                            }
                            con.commit();
                            JOptionPane.showMessageDialog(this, "Xóa thành công!");
                        } catch (Exception ex) {
                            con.rollback();
                            throw ex;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Lỗi xóa dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa phiếu nhập này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        int modelRow = table.convertRowIndexToModel(selectedRow);
                        int maPn = (int) model.getValueAt(modelRow, 1);
                        try (Connection con = ConnectionUtils.getMyConnection()) {
                            con.setAutoCommit(false);
                            try (PreparedStatement psCt = con.prepareStatement("DELETE FROM CHITIET_PHIEUNHAP WHERE MA_PN = ?");
                                 PreparedStatement psPn = con.prepareStatement("DELETE FROM PHIEU_NHAP WHERE MA_PN = ?")) {
                                psCt.setInt(1, maPn);
                                psCt.executeUpdate();
                                
                                psPn.setInt(1, maPn);
                                psPn.executeUpdate();
                                
                                con.commit();
                                model.removeRow(modelRow);
                                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                            } catch (Exception ex) {
                                con.rollback();
                                throw ex;
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(this, "Lỗi xóa dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn hoặc đánh dấu phiếu nhập cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        rightButtonsPanel.add(btnAdd);
        rightButtonsPanel.add(btnDelete);
        bottomPanel.add(rightButtonsPanel, BorderLayout.EAST);
        mainContainer.add(bottomPanel, BorderLayout.SOUTH);
        
        this.setLayout(new BorderLayout());
        this.add(mainContainer, BorderLayout.CENTER);
        
        this.revalidate();
        this.repaint();
        loadDataToTable(model);
    }

    // === HÀM TẠO NÚT BẤM GRADIENT BỊ THIẾU ===
    private JButton createGradientButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(142, 68, 173), 0, getHeight(), new Color(175, 122, 197));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    // === HÀM LOAD DỮ LIỆU TỪ DATABASE BỊ THIẾU ===
    private void loadDataToTable(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT p.MA_PN, n.TEN_NCC, p.MA_NV, p.MA_CN, p.NGAY_NHAP, c.SO_LUONG, c.DON_GIA_NHAP, p.TONG_TIEN, p.TRANG_THAI, p.GHI_CHU " +
                     "FROM PHIEU_NHAP p " +
                     "LEFT JOIN NHACUNGCAP n ON p.MA_NCC = n.MA_NCC " +
                     "LEFT JOIN CHITIET_PHIEUNHAP c ON p.MA_PN = c.MA_PN " +
                     "ORDER BY p.MA_PN DESC";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    false, // Ô checkbox cột 0
                    rs.getInt("MA_PN"),
                    rs.getString("TEN_NCC"),
                    rs.getInt("MA_NV"),
                    rs.getInt("MA_CN"),
                    rs.getTimestamp("NGAY_NHAP"),
                    rs.getInt("SO_LUONG"),
                    rs.getDouble("DON_GIA_NHAP"),
                    rs.getDouble("TONG_TIEN"),
                    rs.getInt("TRANG_THAI"),
                    rs.getString("GHI_CHU")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
   
 
   

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel2.setText("Đơn hàng");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addContainerGap(313, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(250, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables
}

