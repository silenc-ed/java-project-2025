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
    private JLabel lblLastUpdate;

    /**
     * Creates new form BillPanel
     */
    public BillPanel() {
        initComponents();
        setupCustomUI();
    }

    private void setupCustomUI() {
        JPanel mainContainer = new JPanel(new BorderLayout(10, 10));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

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

        // ================= HEADER =================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftHeader.setOpaque(false);
        
        JLabel lblTitle = new JLabel("Quản lý đơn nhập hàng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(30, 41, 59));
        
        lblLastUpdate = new JLabel("Chưa cập nhật");
        lblLastUpdate.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblLastUpdate.setForeground(new Color(148, 163, 184));
        
        leftHeader.add(lblTitle);
        leftHeader.add(lblLastUpdate);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightHeader.setOpaque(false);

        JButton btnRefresh = createGradientButton("↻ Cập nhật");
        btnRefresh.setPreferredSize(new Dimension(130, 36));
        btnRefresh.addActionListener(e -> loadDataToTable(model));
        
        // btnAdd, btnEdit, btnDelete will be added below after their logic is initialized

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);

        mainContainer.add(headerPanel, BorderLayout.NORTH);

        // ================= CENTER =================
        JPanel centerPanel = new JPanel(new BorderLayout(0, 0));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        // Phần Search Bar
        JPanel topSearchPanel = new JPanel(new BorderLayout());
        topSearchPanel.setBackground(Color.WHITE);
        topSearchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JTextField txtSearch = new JTextField("Tìm kiếm theo Mã PN, Nhà cung cấp, Mã NV, Mã CN...");
        txtSearch.setPreferredSize(new Dimension(0, 35));
        txtSearch.setBackground(new Color(248, 250, 252));
        txtSearch.setForeground(Color.GRAY);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        txtSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals("Tìm kiếm theo Mã PN, Nhà cung cấp, Mã NV, Mã CN...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setForeground(Color.GRAY);
                    txtSearch.setText("Tìm kiếm theo Mã PN, Nhà cung cấp, Mã NV, Mã CN...");
                }
            }
        });

        topSearchPanel.add(txtSearch, BorderLayout.CENTER);
        centerPanel.add(topSearchPanel, BorderLayout.NORTH);

        // Bảng dữ liệu đã khởi tạo ở trên

        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(new Color(100, 116, 139));
        table.getTableHeader().setPreferredSize(new Dimension(0, 35));
        table.setBackground(Color.WHITE); 
        table.setShowGrid(false);

        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(0).setMinWidth(40);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE); 
        
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
                    String text = txtSearch.getText().trim();
                    if (text.isEmpty() || text.equals("Tìm kiếm theo Mã PN, Nhà cung cấp, Mã NV, Mã CN...") || txtSearch.getForeground() == Color.GRAY) {
                        sorter.setRowFilter(null);
                    } else {
                        final String searchLower = text.toLowerCase();
                        sorter.setRowFilter(new RowFilter<DefaultTableModel, Object>() {
                            @Override
                            public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Object> entry) {
                                String maPn = entry.getStringValue(1).toLowerCase();
                                String ncc = entry.getStringValue(2).toLowerCase();
                                String maNv = entry.getStringValue(3).toLowerCase();
                                String maCn = entry.getStringValue(4).toLowerCase();
                                return maPn.contains(searchLower) || ncc.contains(searchLower) || maNv.contains(searchLower) || maCn.contains(searchLower);
                            }
                        });
                    }
                });
            }
        });
        
        // ================= ADD FORM PANEL =================
        JPanel addFormPanel = new JPanel(new BorderLayout(10, 10));
        addFormPanel.setBackground(new Color(248, 250, 252));
        addFormPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
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

            try {
                int maNv = Integer.parseInt(txtMaNV.getText().trim());
                int maCn = Integer.parseInt(txtMaCN.getText().trim());
                int qty = Integer.parseInt(txtSoLuong.getText().trim());
                double price = Double.parseDouble(txtDonGia.getText().trim());
                double total = Double.parseDouble(txtTongTien.getText().trim());
                int status = Integer.parseInt(cbTrangThai.getSelectedItem().toString());
                String note = txtGhiChu.getText().trim();

                boolean success = Controller.Admin.PhieuNhap.PhieuNhapDAO.savePhieuNhap(
                    editingMaPn, supplierName, maNv, maCn, qty, price, total, status, note, isEdit
                );

                if (success) {
                    JOptionPane.showMessageDialog(this, isEdit ? "Cập nhật thành công!" : "Thêm phiếu nhập thành công!");
                    btnCancelAdd.doClick();
                    loadDataToTable(model);
                } else {
                    throw new Exception("Lỗi khi lưu phiếu nhập");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi xử lý: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        mainContainer.add(centerPanel, BorderLayout.CENTER);

        JButton btnEdit = createGradientButton("Sửa");
        btnEdit.setPreferredSize(new Dimension(130, 36));
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

        JButton btnAdd = createGradientButton("Thêm");
        btnAdd.setPreferredSize(new Dimension(130, 36));
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
        btnDelete.setPreferredSize(new Dimension(130, 36));
        btnDelete.addActionListener(e -> {
            java.util.List<Integer> toDelete = new java.util.ArrayList<>();
            java.util.List<Integer> modelRows = new java.util.ArrayList<>();

            for (int i = model.getRowCount() - 1; i >= 0; i--) {
                Boolean isChecked = (Boolean) model.getValueAt(i, 0);
                if (isChecked != null && isChecked) {
                    toDelete.add((int) model.getValueAt(i, 1));
                    modelRows.add(i);
                }
            }

            if (toDelete.isEmpty()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    int modelRow = table.convertRowIndexToModel(selectedRow);
                    toDelete.add((int) model.getValueAt(modelRow, 1));
                    modelRows.add(modelRow);
                }
            }

            if (toDelete.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn hoặc đánh dấu phiếu nhập cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc chắn muốn xóa " + toDelete.size() + " phiếu nhập đã chọn?", 
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            try {
                boolean success = Controller.Admin.PhieuNhap.PhieuNhapDAO.deletePhieuNhaps(toDelete);
                if (success) {
                    java.util.Collections.sort(modelRows, java.util.Collections.reverseOrder());
                    for (int r : modelRows) {
                        model.removeRow(r);
                    }
                    JOptionPane.showMessageDialog(this, "Xóa thành công!");
                } else {
                    throw new Exception("Lỗi khi xóa phiếu nhập");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi xóa dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        rightHeader.add(btnAdd);
        rightHeader.add(btnEdit);
        rightHeader.add(btnDelete);
        rightHeader.add(btnRefresh);
        
        this.setLayout(new BorderLayout());
        this.add(mainContainer, BorderLayout.CENTER);
        
        this.revalidate();
        this.repaint();
        loadDataToTable(model);
    }

    // === HÀM TẠO NÚT BẤM GRADIENT BỊ THIẾU ===
    private JButton createGradientButton(String text) {
        JButton button = new JButton(text);
        View.Admin.UIUtils.styleButton(button);
        return button;
    }

    // === HÀM LOAD DỮ LIỆU TỪ DATABASE BỊ THIẾU ===
    private void loadDataToTable(DefaultTableModel model) {
        model.setRowCount(0);
        String time = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));
        if (lblLastUpdate != null) {
            lblLastUpdate.setText("Cập nhật lúc: " + time);
        }
        try {
            java.util.List<java.util.Map<String, Object>> list = Controller.Admin.PhieuNhap.PhieuNhapDAO.getAllPhieuNhap();
            for (java.util.Map<String, Object> row : list) {
                model.addRow(new Object[]{
                    false, // Ô checkbox cột 0
                    row.get("MA_PN"),
                    row.get("TEN_NCC"),
                    row.get("MA_NV"),
                    row.get("MA_CN"),
                    row.get("NGAY_NHAP"),
                    row.get("SO_LUONG"),
                    row.get("DON_GIA_NHAP"),
                    row.get("TONG_TIEN"),
                    row.get("TRANG_THAI"),
                    row.get("GHI_CHU")
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

