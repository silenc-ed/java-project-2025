package View.Admin.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VariantViewPanel extends JPanel {

    private ProductManagementController controller;
    private int currentProductId = -1;
    private DefaultTableModel tableModel;
    private JTable dataTable;
    private JTextField txtSearch;

    public VariantViewPanel(ProductManagementController controller) {
        this.controller = controller;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("Danh sách biến thể (Phiên bản)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(15, 23, 42));
        leftHeader.add(lblTitle);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightHeader.setOpaque(false);

        JButton btnDeleteSelected = new JButton("🗑️ Xóa");
        View.Admin.UIUtils.styleButton(btnDeleteSelected);
        btnDeleteSelected.addActionListener(e -> handleDeleteSelected());

        JButton btnAdd = new JButton("+ Thêm biến thể");
        View.Admin.UIUtils.styleButton(btnAdd);
        btnAdd.addActionListener(e -> handleAddVariant());

        // Ẩn các nút theo quyền
        btnDeleteSelected.setVisible(Controller.Admin.PermissionService.canDelete("Quan ly SP"));
        btnAdd.setVisible(Controller.Admin.PermissionService.canAdd("Quan ly SP"));

        rightHeader.add(btnDeleteSelected);
        rightHeader.add(btnAdd);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Panel
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);

        // Search Bar
        txtSearch = new JTextField("Tìm kiếm biến thể...");
        txtSearch.setPreferredSize(new Dimension(300, 35));
        txtSearch.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = txtSearch.getText().trim();
                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
                dataTable.setRowSorter(sorter);
                if (text.isEmpty() || text.equals("Tìm kiếm biến thể...")) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1, 2));
                }
            }
        });
        centerPanel.add(txtSearch, BorderLayout.NORTH);

        // Table
        String[] columns = {"", "Mã biến thể", "Tên biến thể (Màu/RAM/ROM)", "Giá cộng thêm", "Số lượng tồn", "Thao tác"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Boolean.class : super.getColumnClass(c);
            }
            @Override public boolean isCellEditable(int r, int c) {
                return c == 0 || c == 5;
            }
        };

        dataTable = new JTable(tableModel);
        dataTable.setRowHeight(60);
        dataTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dataTable.setBackground(Color.WHITE);
        dataTable.setShowVerticalLines(false);
        dataTable.setShowHorizontalLines(true);
        dataTable.setGridColor(new Color(241, 245, 249));
        dataTable.setSelectionBackground(new Color(245, 235, 250));
        dataTable.setSelectionForeground(new Color(15, 23, 42));

        dataTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        dataTable.getColumnModel().getColumn(0).setMaxWidth(40);

        ProductSharedUtils.ActionCellEditor actionEditor = new ProductSharedUtils.ActionCellEditor(
            dataTable, 
            this::handleEditVariant, 
            this::handleViewVariant,
            true // Show "Chi tiết"
        );
        dataTable.getColumnModel().getColumn(5).setCellRenderer(new ProductSharedUtils.ActionCellRenderer(true));
        dataTable.getColumnModel().getColumn(5).setCellEditor(actionEditor);

        // Double click to view details (Serials) — skip checkbox col 0 and action col 5
        dataTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = dataTable.rowAtPoint(e.getPoint());
                    int col = dataTable.columnAtPoint(e.getPoint());
                    if (row != -1 && col > 0 && col < 5) {
                        handleViewVariant();
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(dataTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void loadVariantsForProduct(int productId) {
        this.currentProductId = productId;
        tableModel.setRowCount(0);
        try {
            java.util.List<Model.BienTheSanPham> list = Controller.SanPhamDAO.getBienTheByMaSp(productId);
            boolean hasData = false;
            for (Model.BienTheSanPham bt : list) {
                hasData = true;
                tableModel.addRow(new Object[]{
                    Boolean.FALSE,
                    String.valueOf(bt.getMaBienThe()),
                    bt.getTenBienThe(),
                    new java.text.DecimalFormat("#,###đ").format(bt.getGiaBan()),
                    0, // Số lượng tồn (mock or load from somewhere else)
                    bt.getMaBienThe()
                });
            }
            if (!hasData) loadSampleVariants();
        } catch (Exception e) {
            e.printStackTrace();
            loadSampleVariants();
        }
    }

    private void loadSampleVariants() {
        Object[][] samples = {
            {"VAR01", "Màu Bạc - 16GB RAM - 512GB SSD", "0đ", 50},
            {"VAR02", "Màu Đen - 32GB RAM - 1TB SSD", "+5,000,000đ", 20}
        };
        for (Object[] row : samples) {
            tableModel.addRow(new Object[]{ Boolean.FALSE, row[0], row[1], row[2], row[3], -1 });
        }
    }

    private void handleAddVariant() {
        if (currentProductId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Sản phẩm trước!");
            return;
        }
        VariantDialog dialog = new VariantDialog(SwingUtilities.getWindowAncestor(this), "Thêm biến thể");
        dialog.setVisible(true);
        if (dialog.isSaveClicked()) {
            try {
                Model.BienTheSanPham bt = new Model.BienTheSanPham();
                bt.setMaSp(currentProductId);
                bt.setTenBienThe(dialog.getTenBienThe());
                bt.setGiaBan(dialog.getGiaBan());
                bt.setTrangThai(dialog.getTrangThai());
                Controller.BienTheSanPhamDAO.addBienTheSanPham(bt);
                loadVariantsForProduct(currentProductId);
            } catch (Exception e) {
                e.printStackTrace();
                tableModel.addRow(new Object[]{ Boolean.FALSE, "VAR_NEW", dialog.getTenBienThe(), dialog.getGiaBan() + "đ", 0, -1 });
            }
        }
    }

    private void handleEditVariant() {
        try {
            int row = dataTable.getSelectedRow();
            if (row == -1) return;
            int modelRow = dataTable.convertRowIndexToModel(row);
            
            Object idObj = tableModel.getValueAt(modelRow, 1);
            int id = -1;
            if (idObj != null) {
                try {
                    id = Integer.parseInt(idObj.toString().replace("VAR", "").trim());
                } catch (Exception e) {}
            }
            
            Object nameObj = tableModel.getValueAt(modelRow, 2);
            String name = nameObj != null ? nameObj.toString() : "";
            
            Object priceObj = tableModel.getValueAt(modelRow, 3);
            double price = 0;
            if (priceObj != null) {
                try {
                    String priceStr = priceObj.toString()
                        .replace("đ", "")
                        .replace(",", "")
                        .replace(".", "")
                        .replace("+", "")
                        .trim();
                    price = Double.parseDouble(priceStr);
                } catch (Exception e) {}
            }
            
            String status = "Đang kinh doanh"; // Có thể thêm cột trạng thái vào bảng nếu cần
    
            VariantDialog dialog = new VariantDialog(SwingUtilities.getWindowAncestor(this), "Sửa biến thể");
            dialog.setTenBienThe(name);
            dialog.setGiaBan(price);
            dialog.setTrangThai(status);
            dialog.setVisible(true);
    
            if (dialog.isSaveClicked()) {
                if (id != -1) {
                    try {
                        Model.BienTheSanPham bt = new Model.BienTheSanPham();
                        bt.setMaBienThe(id);
                        bt.setMaSp(currentProductId);
                        bt.setTenBienThe(dialog.getTenBienThe());
                        bt.setGiaBan(dialog.getGiaBan());
                        bt.setTrangThai(dialog.getTrangThai());
                        Controller.BienTheSanPhamDAO.updateBienTheSanPham(bt);
                        loadVariantsForProduct(currentProductId);
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật biến thể: " + e.getMessage());
                    }
                } else {
                    tableModel.setValueAt(dialog.getTenBienThe(), modelRow, 2);
                    tableModel.setValueAt(new java.text.DecimalFormat("#,###đ").format(dialog.getGiaBan()), modelRow, 3);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra: " + e.getMessage());
        }
    }

    private void handleViewVariant() {
        int row = dataTable.getSelectedRow();
        if (row == -1) return;
        int modelRow = dataTable.convertRowIndexToModel(row);
        
        int id = -1;
        try { id = Integer.parseInt(tableModel.getValueAt(modelRow, 1).toString().replace("VAR", "")); } catch (Exception e) {}
        String name = tableModel.getValueAt(modelRow, 2).toString();
        
        controller.showSerialNumber(id, name);
    }

    private void handleDeleteSelected() {
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            Boolean checked = (Boolean) tableModel.getValueAt(i, 0);
            if (checked != null && checked) {
                int modelRow = dataTable.convertRowIndexToModel(i);
                int id = -1;
                try { id = Integer.parseInt(tableModel.getValueAt(modelRow, 1).toString().replace("VAR", "")); } catch (Exception e) {}
                
                if (id != -1) {
                    try {
                        Controller.BienTheSanPhamDAO.deleteBienTheSanPham(id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                tableModel.removeRow(i);
            }
        }
        loadVariantsForProduct(currentProductId);
    }

    // Dialog class for Variant
    class VariantDialog extends JDialog {
        private JTextField txtTen = new JTextField();
        private JTextField txtGia = new JTextField("0");
        private JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"Đang kinh doanh", "Ngừng kinh doanh"});
        private JButton btnSave = new JButton("Lưu");
        private JButton btnCancel = new JButton("Hủy");
        private boolean isSaveClicked = false;

        public VariantDialog(Window owner, String title) {
            super(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
            setSize(460, 300);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());
            setResizable(false);

            // ---- Content Panel ----
            JPanel content = new JPanel(new GridBagLayout());
            content.setBackground(Color.WHITE);
            content.setBorder(BorderFactory.createEmptyBorder(24, 28, 16, 28));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;

            Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
            Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

            // Tên biến thể
            gbc.gridy = 0; gbc.insets = new Insets(0, 0, 4, 0);
            JLabel lblTen = new JLabel("Tên biến thể (Màu / RAM / ROM)");
            lblTen.setFont(labelFont);
            content.add(lblTen, gbc);

            gbc.gridy = 1; gbc.insets = new Insets(0, 0, 14, 0);
            txtTen.setFont(fieldFont);
            txtTen.setPreferredSize(new Dimension(400, 35));
            txtTen.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
            content.add(txtTen, gbc);

            // Giá cộng thêm
            gbc.gridy = 2; gbc.insets = new Insets(0, 0, 4, 0);
            JLabel lblGia = new JLabel("Giá cộng thêm (đ)");
            lblGia.setFont(labelFont);
            content.add(lblGia, gbc);

            gbc.gridy = 3; gbc.insets = new Insets(0, 0, 14, 0);
            txtGia.setFont(fieldFont);
            txtGia.setPreferredSize(new Dimension(400, 35));
            txtGia.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
            content.add(txtGia, gbc);

            // Trạng thái
            gbc.gridy = 4; gbc.insets = new Insets(0, 0, 4, 0);
            JLabel lblStatus = new JLabel("Trạng thái");
            lblStatus.setFont(labelFont);
            content.add(lblStatus, gbc);

            gbc.gridy = 5; gbc.insets = new Insets(0, 0, 0, 0);
            cbTrangThai.setFont(fieldFont);
            cbTrangThai.setPreferredSize(new Dimension(400, 35));
            content.add(cbTrangThai, gbc);

            add(content, BorderLayout.CENTER);

            // ---- Footer ----
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
            footer.setBackground(new Color(248, 250, 252));
            footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

            View.Admin.UIUtils.styleButton(btnCancel);
            View.Admin.UIUtils.styleButton(btnSave);

            footer.add(btnCancel);
            footer.add(btnSave);
            add(footer, BorderLayout.SOUTH);

            btnSave.addActionListener(e -> {
                if (txtTen.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Tên biến thể không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                isSaveClicked = true;
                dispose();
            });
            btnCancel.addActionListener(e -> dispose());
        }

        public String getTenBienThe() { return txtTen.getText().trim(); }
        public void setTenBienThe(String n) { txtTen.setText(n); }
        public double getGiaBan() { try { return Double.parseDouble(txtGia.getText().trim()); } catch (Exception e) { return 0; } }
        public void setGiaBan(double v) { txtGia.setText(String.valueOf((long)v)); }
        public String getTrangThai() { return cbTrangThai.getSelectedItem().toString(); }
        public void setTrangThai(String t) { cbTrangThai.setSelectedItem(t); }
        public boolean isSaveClicked() { return isSaveClicked; }
    }
}
