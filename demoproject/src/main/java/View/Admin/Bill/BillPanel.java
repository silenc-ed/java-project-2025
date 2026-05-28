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

public class BillPanel extends javax.swing.JPanel {

    private boolean isEdit = false;
    private int editingMaPn = -1;
    private JLabel lblLastUpdate;
    
    private boolean canAdd;
    private boolean canEdit;
    private boolean canDelete;

    private CardLayout cardLayout;
    private JPanel cardContainer;
    
    private DefaultTableModel listModel;
    private JTable listTable;
    
    // For detail panel
    private DefaultTableModel detailModel;
    private JTable detailTable;
    private JLabel lblDetailTitle;
    private int currentViewMaPn = -1;

    public BillPanel() {
        // Lấy quyền từ PermissionService
        canAdd    = Controller.Admin.PermissionService.canAdd("Nhap kho");
        canEdit   = Controller.Admin.PermissionService.canEdit("Nhap kho");
        canDelete = Controller.Admin.PermissionService.canDelete("Nhap kho");

        initComponents();
        setupCustomUI();
    }

    private void setupCustomUI() {
        this.removeAll();
        this.setLayout(new BorderLayout());
        
        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        
        cardContainer.add(buildListPanel(), "list");
        cardContainer.add(buildDetailPanel(), "detail");
        
        this.add(cardContainer, BorderLayout.CENTER);
        
        cardLayout.show(cardContainer, "list");
    }

    private JPanel buildListPanel() {
        JPanel mainContainer = new JPanel(new BorderLayout(10, 10));
        mainContainer.setBackground(Color.WHITE);
        mainContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] columns = {"", "Mã PN", "Nhà cung cấp", "Mã NV", "Mã CN", "Ngày nhập", "Số lượng", "Đơn giá", "Tổng tiền", "Trạng thái", "Ghi chú"};
        listModel = new DefaultTableModel(columns, 0) {
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

        JButton btnAdd = createGradientButton("Thêm");
        btnAdd.setPreferredSize(new Dimension(130, 36));
        btnAdd.addActionListener(e -> showOrderDialog(false, -1));

        JButton btnEdit = createGradientButton("Sửa");
        btnEdit.setPreferredSize(new Dimension(130, 36));
        btnEdit.addActionListener(e -> {
            int selectedRow = listTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu nhập để sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = listTable.convertRowIndexToModel(selectedRow);
            int maPn = (int) listModel.getValueAt(modelRow, 1);
            showOrderDialog(true, maPn);
        });

        JButton btnDelete = createGradientButton("Xóa");
        btnDelete.setPreferredSize(new Dimension(130, 36));
        btnDelete.addActionListener(e -> deleteSelectedRows());

        JButton btnRefresh = createGradientButton("Cập nhật");
        btnRefresh.setPreferredSize(new Dimension(130, 36));
        btnRefresh.addActionListener(e -> loadDataToTable(listModel));

        if (canAdd)    rightHeader.add(btnAdd);
        if (canEdit)   rightHeader.add(btnEdit);
        if (canDelete) rightHeader.add(btnDelete);
        rightHeader.add(btnRefresh);

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

        listTable = new JTable(listModel);
        listTable.setRowHeight(35);
        listTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        listTable.getTableHeader().setBackground(new Color(241, 245, 249));
        listTable.getTableHeader().setForeground(new Color(100, 116, 139));
        listTable.getTableHeader().setPreferredSize(new Dimension(0, 35));
        listTable.setBackground(Color.WHITE); 
        listTable.setShowGrid(false);

        listTable.getColumnModel().getColumn(0).setMaxWidth(40);
        listTable.getColumnModel().getColumn(0).setMinWidth(40);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 1; i < listTable.getColumnCount(); i++) {
            if (i != 2) {
                listTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
        
        listTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = listTable.getSelectedRow();
                    if (row != -1) {
                        int modelRow = listTable.convertRowIndexToModel(row);
                        int maPn = (int) listModel.getValueAt(modelRow, 1);
                        String ncc = (String) listModel.getValueAt(modelRow, 2);
                        showDetailPanel(maPn, ncc);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(listTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE); 
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(listModel);
        listTable.setRowSorter(sorter);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { search(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { search(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { search(); }
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

        mainContainer.add(centerPanel, BorderLayout.CENTER);
        loadDataToTable(listModel);
        return mainContainer;
    }

    private JPanel buildDetailPanel() {
        JPanel detailContainer = new JPanel(new BorderLayout(10, 10));
        detailContainer.setBackground(Color.WHITE);
        detailContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftHeader.setOpaque(false);
        
        JButton btnBack = createGradientButton("<- Quay lại");
        btnBack.setPreferredSize(new Dimension(120, 36));
        btnBack.addActionListener(e -> {
            loadDataToTable(listModel); 
            cardLayout.show(cardContainer, "list");
        });
        
        lblDetailTitle = new JLabel("Chi tiết phiếu nhập");
        lblDetailTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblDetailTitle.setForeground(new Color(30, 41, 59));
        
        leftHeader.add(btnBack);
        leftHeader.add(lblDetailTitle);
        headerPanel.add(leftHeader, BorderLayout.WEST);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightHeader.setOpaque(false);

        JButton btnAddDetail = createGradientButton("Thêm mặt hàng");
        btnAddDetail.setPreferredSize(new Dimension(140, 36));
        btnAddDetail.addActionListener(e -> showDetailDialog(false, currentViewMaPn, -1));

        JButton btnEditDetail = createGradientButton("Sửa mặt hàng");
        btnEditDetail.setPreferredSize(new Dimension(140, 36));
        btnEditDetail.addActionListener(e -> {
            int selectedRow = detailTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một mặt hàng để sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = detailTable.convertRowIndexToModel(selectedRow);
            int maBienthe = (int) detailModel.getValueAt(modelRow, 2);
            showDetailDialog(true, currentViewMaPn, maBienthe);
        });

        JButton btnDeleteDetail = createGradientButton("Xóa mặt hàng");
        btnDeleteDetail.setPreferredSize(new Dimension(140, 36));
        btnDeleteDetail.addActionListener(e -> deleteSelectedDetails());

        if (canAdd)    rightHeader.add(btnAddDetail);
        if (canEdit)   rightHeader.add(btnEditDetail);
        if (canDelete) rightHeader.add(btnDeleteDetail);
        
        headerPanel.add(rightHeader, BorderLayout.EAST);

        detailContainer.add(headerPanel, BorderLayout.NORTH);

        String[] cols = {"", "Mã PN", "Mã BT", "Sản phẩm", "Biến thể", "Số lượng", "Đơn giá", "Thành tiền"};
        detailModel = new DefaultTableModel(cols, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class;
                return super.getColumnClass(columnIndex);
            }
            @Override public boolean isCellEditable(int r, int c) { return c == 0; }
        };

        // Bọc phần bảng vào một Center Panel để chứa thanh tìm kiếm ở trên
        JPanel centerPanel = new JPanel(new BorderLayout(0, 0));
        centerPanel.setBackground(Color.WHITE);

        // Thanh tìm kiếm cho panel chi tiết
        JPanel detailSearchPanel = new JPanel(new BorderLayout());
        detailSearchPanel.setBackground(Color.WHITE);
        detailSearchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JTextField txtDetailSearch = new JTextField("Tìm kiếm theo Mã BT, Sản phẩm, Biến thể...");
        txtDetailSearch.setPreferredSize(new Dimension(0, 35));
        txtDetailSearch.setBackground(new Color(248, 250, 252));
        txtDetailSearch.setForeground(Color.GRAY);
        txtDetailSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDetailSearch.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        txtDetailSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtDetailSearch.getText().equals("Tìm kiếm theo Mã BT, Sản phẩm, Biến thể...")) {
                    txtDetailSearch.setText("");
                    txtDetailSearch.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtDetailSearch.getText().isEmpty()) {
                    txtDetailSearch.setForeground(Color.GRAY);
                    txtDetailSearch.setText("Tìm kiếm theo Mã BT, Sản phẩm, Biến thể...");
                }
            }
        });

        detailSearchPanel.add(txtDetailSearch, BorderLayout.CENTER);
        centerPanel.add(detailSearchPanel, BorderLayout.NORTH);

        detailTable = new JTable(detailModel);
        detailTable.setRowHeight(35);
        detailTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        detailTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        detailTable.getTableHeader().setBackground(new Color(241, 245, 249));
        detailTable.getTableHeader().setForeground(new Color(100, 116, 139));
        detailTable.getTableHeader().setPreferredSize(new Dimension(0, 35));
        detailTable.setBackground(Color.WHITE);

        detailTable.getColumnModel().getColumn(0).setMaxWidth(40);
        detailTable.getColumnModel().getColumn(0).setMinWidth(40);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 1; i < detailTable.getColumnCount(); i++) {
            if (i != 3 && i != 4) {
                detailTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
        
        TableRowSorter<DefaultTableModel> detailSorter = new TableRowSorter<>(detailModel);
        detailTable.setRowSorter(detailSorter);

        txtDetailSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { searchDetail(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { searchDetail(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { searchDetail(); }
            private void searchDetail() {
                SwingUtilities.invokeLater(() -> {
                    String text = txtDetailSearch.getText().trim();
                    if (text.isEmpty() || text.equals("Tìm kiếm theo Mã BT, Sản phẩm, Biến thể...") || txtDetailSearch.getForeground() == Color.GRAY) {
                        detailSorter.setRowFilter(null);
                    } else {
                        final String searchLower = text.toLowerCase();
                        detailSorter.setRowFilter(new RowFilter<DefaultTableModel, Object>() {
                            @Override
                            public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Object> entry) {
                                String maBt = entry.getStringValue(2).toLowerCase();
                                String sanPham = entry.getStringValue(3).toLowerCase();
                                String bienThe = entry.getStringValue(4).toLowerCase();
                                return maBt.contains(searchLower) || sanPham.contains(searchLower) || bienThe.contains(searchLower);
                            }
                        });
                    }
                });
            }
        });

        JScrollPane sp = new JScrollPane(detailTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        sp.getViewport().setBackground(Color.WHITE);
        centerPanel.add(sp, BorderLayout.CENTER);

        detailContainer.add(centerPanel, BorderLayout.CENTER);

        return detailContainer;
    }

    private void showDetailPanel(int maPn, String ncc) {
        currentViewMaPn = maPn;
        lblDetailTitle.setText("Chi tiết phiếu nhập #" + maPn + " — " + ncc);
        loadDetailData(maPn);
        cardLayout.show(cardContainer, "detail");
    }

    private void loadDetailData(int maPn) {
        detailModel.setRowCount(0);
        try {
            java.util.List<java.util.Map<String, Object>> chiTiet = Controller.Admin.PhieuNhap.PhieuNhapDAO.getChiTietPhieuNhap(maPn);
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,### đ");
            for (java.util.Map<String, Object> r : chiTiet) {
                detailModel.addRow(new Object[]{
                    false,
                    r.get("MA_PN"),
                    r.get("MA_BIENTHE"),
                    r.get("TEN_SP"),
                    r.get("TEN_BIENTHE") != null ? r.get("TEN_BIENTHE") : "Mặc định",
                    r.get("SO_LUONG"),
                    df.format(r.get("DON_GIA_NHAP")),
                    df.format(r.get("THANH_TIEN"))
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteSelectedDetails() {
        java.util.List<Integer> toDelete = new java.util.ArrayList<>();
        java.util.List<Integer> modelRows = new java.util.ArrayList<>();

        for (int i = detailModel.getRowCount() - 1; i >= 0; i--) {
            Boolean isChecked = (Boolean) detailModel.getValueAt(i, 0);
            if (isChecked != null && isChecked) {
                toDelete.add((int) detailModel.getValueAt(i, 2)); // MA_BIENTHE
                modelRows.add(i);
            }
        }

        if (toDelete.isEmpty()) {
            int selectedRow = detailTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = detailTable.convertRowIndexToModel(selectedRow);
                toDelete.add((int) detailModel.getValueAt(modelRow, 2));
                modelRows.add(modelRow);
            }
        }

        if (toDelete.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hoặc đánh dấu mặt hàng cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc chắn muốn xóa " + toDelete.size() + " mặt hàng khỏi phiếu nhập này?", 
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean success = Controller.Admin.PhieuNhap.PhieuNhapDAO.deleteChiTietPhieuNhaps(currentViewMaPn, toDelete);
            if (success) {
                loadDetailData(currentViewMaPn);
                JOptionPane.showMessageDialog(this, "Xóa mặt hàng thành công!");
            } else {
                throw new Exception("Lỗi khi xóa mặt hàng chi tiết");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi xóa dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDetailDialog(boolean isEditMode, int maPn, int editMaBienthe) {
        JDialog dialog = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), 
            isEditMode ? "Sửa mặt hàng" : "Thêm mặt hàng", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 14);

        JTextField txtMaBienthe = new JTextField();
        if (isEditMode) {
            txtMaBienthe.setText(String.valueOf(editMaBienthe));
            txtMaBienthe.setEditable(false);
            txtMaBienthe.setBackground(new Color(248, 250, 252));
        }

        JTextField txtSoLuong = new JTextField("1");
        JTextField txtDonGia = new JTextField("0");

        JComponent[] inputs = {txtMaBienthe, txtSoLuong, txtDonGia};
        String[] labels = {"Mã biến thể:", "Số lượng:", "Đơn giá nhập:"};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(labelFont);
            mainPanel.add(lbl, gbc);

            gbc.gridx = 1; gbc.weightx = 1.0;
            inputs[i].setFont(inputFont);
            inputs[i].setPreferredSize(new Dimension(200, 32));
            mainPanel.add(inputs[i], gbc);
        }

        if (isEditMode) {
            int selectedRow = detailTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = detailTable.convertRowIndexToModel(selectedRow);
                txtSoLuong.setText(detailModel.getValueAt(modelRow, 5).toString());
                txtDonGia.setText(detailModel.getValueAt(modelRow, 6).toString().replaceAll("[^\\d.]", ""));
            }
        }

        dialog.add(mainPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton btnSave = createGradientButton("Lưu");
        btnSave.setPreferredSize(new Dimension(100, 36));
        JButton btnCancel = createGradientButton("Hủy");
        btnCancel.setPreferredSize(new Dimension(100, 36));

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            try {
                int bienthe = Integer.parseInt(txtMaBienthe.getText().trim());
                int qty = Integer.parseInt(txtSoLuong.getText().trim());
                double price = Double.parseDouble(txtDonGia.getText().trim().replace(",", ""));

                boolean success = Controller.Admin.PhieuNhap.PhieuNhapDAO.saveChiTietPhieuNhap(
                    maPn, bienthe, qty, price, isEditMode
                );

                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Thành công!");
                    dialog.dispose();
                    loadDetailData(maPn);
                } else {
                    throw new Exception("Lỗi trùng lặp mặt hàng, hãy chọn sửa để");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đúng định dạng số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    private void deleteSelectedRows() {
        java.util.List<Integer> toDelete = new java.util.ArrayList<>();
        java.util.List<Integer> modelRows = new java.util.ArrayList<>();

        for (int i = listModel.getRowCount() - 1; i >= 0; i--) {
            Boolean isChecked = (Boolean) listModel.getValueAt(i, 0);
            if (isChecked != null && isChecked) {
                toDelete.add((int) listModel.getValueAt(i, 1));
                modelRows.add(i);
            }
        }

        if (toDelete.isEmpty()) {
            int selectedRow = listTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = listTable.convertRowIndexToModel(selectedRow);
                toDelete.add((int) listModel.getValueAt(modelRow, 1));
                modelRows.add(modelRow);
            }
        }

        if (toDelete.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hoặc đánh dấu phiếu nhập cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc chắn muốn xóa mềm " + toDelete.size() + " phiếu nhập đã chọn? (Cập nhật trạng thái về -1)", 
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean success = Controller.Admin.PhieuNhap.PhieuNhapDAO.softDeletePhieuNhaps(toDelete);
            if (success) {
                loadDataToTable(listModel);
                JOptionPane.showMessageDialog(this, "Xóa mềm thành công!");
            } else {
                throw new Exception("Lỗi khi xóa phiếu nhập");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi xóa dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showOrderDialog(boolean isEditMode, int maPn) {
        JDialog dialog = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), 
            isEditMode ? "Sửa phiếu nhập" : "Thêm phiếu nhập", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 14);

        JTextField txtSupplier = new JTextField();
        JTextField txtMaNV = new JTextField();
        JTextField txtMaCN = new JTextField();
        JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"Đã hủy", "Chờ xác nhận", "Đã nhập"});
        if (!isEditMode) {
            cbTrangThai.setSelectedItem("Chờ xác nhận");
            cbTrangThai.setEnabled(false);
        } else {
            cbTrangThai.setSelectedItem("Đã nhập");
        }
        JTextField txtGhiChu = new JTextField();

        JComponent[] inputs = {txtSupplier, txtMaNV, txtMaCN, cbTrangThai, txtGhiChu};
        String[] labels = {"Nhà cung cấp:", "Mã NV:", "Mã CN:", "Trạng thái:", "Ghi chú:"};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(labelFont);
            mainPanel.add(lbl, gbc);

            gbc.gridx = 1; gbc.weightx = 1.0;
            inputs[i].setFont(inputFont);
            inputs[i].setPreferredSize(new Dimension(250, 32));
            mainPanel.add(inputs[i], gbc);
        }

        if (isEditMode) {
            int selectedRow = listTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = listTable.convertRowIndexToModel(selectedRow);
                txtSupplier.setText(listModel.getValueAt(modelRow, 2).toString());
                txtMaNV.setText(listModel.getValueAt(modelRow, 3).toString());
                txtMaCN.setText(listModel.getValueAt(modelRow, 4).toString());
                String statusStr = listModel.getValueAt(modelRow, 9).toString();
                cbTrangThai.setSelectedItem(statusStr);
                
                if (!"Chờ xác nhận".equals(statusStr)) {
                    cbTrangThai.setEnabled(false);
                }
                
                Object noteObj = listModel.getValueAt(modelRow, 10);
                txtGhiChu.setText(noteObj != null ? noteObj.toString() : "");
            }
        }

        dialog.add(mainPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton btnSave = createGradientButton("Lưu");
        btnSave.setPreferredSize(new Dimension(100, 36));
        JButton btnCancel = createGradientButton("Hủy");
        btnCancel.setPreferredSize(new Dimension(100, 36));

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            String supplierName = txtSupplier.getText().trim();
            if (supplierName.isEmpty() || txtMaNV.getText().trim().isEmpty() || txtMaCN.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin bắt buộc", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int maNv = Integer.parseInt(txtMaNV.getText().trim());
                int maCn = Integer.parseInt(txtMaCN.getText().trim());
                int status = cbTrangThai.getSelectedIndex() - 1; // "Đã hủy" -> -1, "Chờ xác nhận" -> 0, "Đã nhập" -> 1
                String note = txtGhiChu.getText().trim();

                boolean success = Controller.Admin.PhieuNhap.PhieuNhapDAO.savePhieuNhap(
                    maPn, supplierName, maNv, maCn, 0, 0, 0, status, note, isEditMode
                );

                if (success) {
                    JOptionPane.showMessageDialog(dialog, isEditMode ? "Cập nhật thành công!" : "Thêm phiếu nhập thành công (Hãy vào chi tiết để thêm mặt hàng)!");
                    dialog.dispose();
                    loadDataToTable(listModel);
                } else {
                    throw new Exception("Lỗi khi lưu phiếu nhập");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Lỗi xử lý: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    private JButton createGradientButton(String text) {
        JButton button = new JButton(text);
        View.Admin.UIUtils.styleButton(button);
        return button;
    }

    private void loadDataToTable(DefaultTableModel model) {
        model.setRowCount(0);
        String time = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));
        if (lblLastUpdate != null) {
            lblLastUpdate.setText("Cập nhật lúc: " + time);
        }
        try {
            // Truyền biến hasEditRole để lọc các trạng thái xóa theo yêu cầu
            java.util.List<java.util.Map<String, Object>> list = Controller.Admin.PhieuNhap.PhieuNhapDAO.getAllPhieuNhap(canEdit);
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
            for (java.util.Map<String, Object> row : list) {
                model.addRow(new Object[]{
                    false,
                    row.get("MA_PN"),
                    row.get("TEN_NCC"),
                    row.get("MA_NV"),
                    row.get("MA_CN"),
                    row.get("NGAY_NHAP"),
                    row.get("SO_LUONG"),
                    df.format(row.get("DON_GIA_NHAP")),
                    df.format(row.get("TONG_TIEN")),
                    ((Number)row.get("TRANG_THAI")).intValue() == 0 ? "Chờ xác nhận" : (((Number)row.get("TRANG_THAI")).intValue() == 1 ? "Đã nhập" : "Đã hủy"),
                    row.get("GHI_CHU")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initComponents() {
    }
}

