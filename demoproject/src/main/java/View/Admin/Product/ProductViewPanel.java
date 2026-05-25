package View.Admin.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.List;

public class ProductViewPanel extends JPanel {

    private ProductManagementController controller;
    private int currentCategoryId = -1;
    private DefaultTableModel tableModel;
    private JTable dataTable;
    private JTextField txtSearch;

    public ProductViewPanel(ProductManagementController controller) {
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
        JLabel lblTitle = new JLabel("Danh sách sản phẩm");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(15, 23, 42));
        leftHeader.add(lblTitle);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightHeader.setOpaque(false);

        JButton btnDeleteSelected = new JButton("🗑️ Xóa");
        View.Admin.UIUtils.styleButton(btnDeleteSelected);
        btnDeleteSelected.addActionListener(e -> handleDeleteSelected());

        JButton btnAdd = new JButton("+ Thêm sản phẩm");
        View.Admin.UIUtils.styleButton(btnAdd);
        btnAdd.addActionListener(e -> handleAddProduct());

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
        txtSearch = new JTextField("Tìm kiếm sản phẩm...");
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
                if (text.isEmpty() || text.equals("Tìm kiếm sản phẩm...")) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1, 2));
                }
            }
        });
        centerPanel.add(txtSearch, BorderLayout.NORTH);

        // Table
        String[] columns = {"", "Mã SP", "Tên sản phẩm", "Mô tả", "Đã bán", "Giá bán", "Trạng thái", "Thao tác"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Boolean.class : super.getColumnClass(c);
            }
            @Override public boolean isCellEditable(int r, int c) {
                return c == 0 || c == 7;
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
        
        dataTable.getColumnModel().getColumn(2).setCellRenderer(new ProductSharedUtils.ProductCellRenderer());
        dataTable.getColumnModel().getColumn(6).setCellRenderer(new ProductSharedUtils.StatusBadgeRenderer());
        
        ProductSharedUtils.ActionCellEditor actionEditor = new ProductSharedUtils.ActionCellEditor(
            dataTable, 
            this::handleEditProduct, 
            this::handleViewProduct,
            true // Show "Chi tiết"
        );
        dataTable.getColumnModel().getColumn(7).setCellRenderer(new ProductSharedUtils.ActionCellRenderer(true));
        dataTable.getColumnModel().getColumn(7).setCellEditor(actionEditor);

        // Double click to view details (variants) — skip checkbox col 0 and action col 7
        dataTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = dataTable.rowAtPoint(e.getPoint());
                    int col = dataTable.columnAtPoint(e.getPoint());
                    if (row != -1 && col > 0 && col < 7) {
                        handleViewProduct();
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(dataTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void loadProductsForCategory(int categoryId) {
        this.currentCategoryId = categoryId;
        tableModel.setRowCount(0);

        try {
            List<Model.SanPham> list = Controller.SanPhamDAO.getAllSanPham();
            for (Model.SanPham sp : list) {
                if (sp.getMaLsp() == categoryId) {
                    tableModel.addRow(new Object[]{
                        Boolean.FALSE,
                        String.valueOf(sp.getMaSp()),
                        sp.getTenSp(),
                        sp.getMoTa(),
                        sp.getSoLuongDaBan(),
                        new DecimalFormat("#,###đ").format(sp.getGiaBan()),
                        sp.getTrangThai(),
                        sp.getMaSp()
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAddProduct() {
        if (currentCategoryId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Loại sản phẩm trước!");
            return;
        }
        ProductDialog dialog = new ProductDialog(SwingUtilities.getWindowAncestor(this), "Thêm sản phẩm");
        dialog.setVisible(true);
        if (dialog.isSaveClicked()) {
            try {
                Model.SanPham sp = new Model.SanPham();
                sp.setMaLsp(currentCategoryId);
                sp.setTenSp(dialog.getTenSp());
                sp.setTrangThai(dialog.getTrangThai());
                sp.setSoLuongDaBan(dialog.getSoLuongDaBan());
                sp.setDonViTinh(dialog.getDonViTinh());
                sp.setMoTa(dialog.getMoTa());
                Controller.SanPhamDAO.addSanPham(sp, dialog.getGiaBan());
                loadProductsForCategory(currentCategoryId);
            } catch (Exception e) {
                e.printStackTrace();
                tableModel.addRow(new Object[]{ Boolean.FALSE, "SP_NEW", dialog.getTenSp(), dialog.getMoTa(), dialog.getSoLuongDaBan(), dialog.getGiaBan() + "đ", dialog.getTrangThai(), -1 });
            }
        }
    }

    private void handleEditProduct() {
        try {
            int row = dataTable.getSelectedRow();
            if (row == -1) return;
            int modelRow = dataTable.convertRowIndexToModel(row);
            
            Object idObj = tableModel.getValueAt(modelRow, 1);
            int id = -1;
            if (idObj != null) {
                try {
                    id = Integer.parseInt(idObj.toString().replace("SP", "").trim());
                } catch (Exception e) {}
            }
            
            Object nameObj = tableModel.getValueAt(modelRow, 2);
            String name = nameObj != null ? nameObj.toString() : "";
            
            Object descObj = tableModel.getValueAt(modelRow, 3);
            String desc = descObj != null ? descObj.toString() : "";
            
            Object qtyObj = tableModel.getValueAt(modelRow, 4);
            int qty = 0;
            if (qtyObj != null) {
                try {
                    qty = Integer.parseInt(qtyObj.toString().trim());
                } catch (Exception e) {}
            }
            
            Object priceObj = tableModel.getValueAt(modelRow, 5);
            double price = 0;
            if (priceObj != null) {
                try {
                    String priceStr = priceObj.toString()
                        .replace("đ", "")
                        .replace(",", "")
                        .replace(".", "")
                        .trim();
                    price = Double.parseDouble(priceStr);
                } catch (Exception e) {}
            }
            
            Object statusObj = tableModel.getValueAt(modelRow, 6);
            String status = statusObj != null ? statusObj.toString() : "Đang kinh doanh";
    
            ProductDialog dialog = new ProductDialog(SwingUtilities.getWindowAncestor(this), "Sửa sản phẩm");
            dialog.setTenSp(name);
            dialog.setMoTa(desc);
            dialog.setSoLuongDaBan(qty);
            dialog.setGiaBan(price);
            dialog.setTrangThai(status);
            dialog.setVisible(true);
    
            if (dialog.isSaveClicked()) {
                if (id != -1) {
                    try {
                        Model.SanPham sp = new Model.SanPham();
                        sp.setMaSp(id);
                        sp.setMaLsp(currentCategoryId);
                        sp.setTenSp(dialog.getTenSp());
                        sp.setTrangThai(dialog.getTrangThai());
                        sp.setSoLuongDaBan(dialog.getSoLuongDaBan());
                        sp.setDonViTinh(dialog.getDonViTinh());
                        sp.setMoTa(dialog.getMoTa());
                        Controller.SanPhamDAO.updateSanPham(sp, dialog.getGiaBan());
                        loadProductsForCategory(currentCategoryId);
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
                    }
                } else {
                    tableModel.setValueAt(dialog.getTenSp(), modelRow, 2);
                    tableModel.setValueAt(dialog.getMoTa(), modelRow, 3);
                    tableModel.setValueAt(dialog.getSoLuongDaBan(), modelRow, 4);
                    tableModel.setValueAt(new DecimalFormat("#,###đ").format(dialog.getGiaBan()), modelRow, 5);
                    tableModel.setValueAt(dialog.getTrangThai(), modelRow, 6);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra: " + e.getMessage());
        }
    }

    private void handleViewProduct() {
        int row = dataTable.getSelectedRow();
        if (row == -1) return;
        int modelRow = dataTable.convertRowIndexToModel(row);
        
        int id = -1;
        try { id = Integer.parseInt(tableModel.getValueAt(modelRow, 1).toString().replace("SP", "")); } catch (Exception e) {}
        String name = tableModel.getValueAt(modelRow, 2).toString();
        
        controller.showVariant(id, name);
    }

    private void handleDeleteSelected() {
        int hasChecked = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean checked = (Boolean) tableModel.getValueAt(i, 0);
            if (checked != null && checked) {
                hasChecked++;
            }
        }
        if (hasChecked == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một sản phẩm để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "⚠️ CẢNH BÁO: Hành động này sẽ XÓA HẾT tất cả dữ liệu liên quan đến sản phẩm đó\n" +
            "(bao gồm tất cả mã serial, số lượng tồn kho, các phiên bản/biến thể liên quan)!\n\n" +
            "Bạn có chắc chắn muốn xóa không?",
            "Cảnh báo xóa dữ liệu liên quan",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        int successCount = 0;
        int failedCount = 0;
        String failReason = "";

        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            Boolean checked = (Boolean) tableModel.getValueAt(i, 0);
            if (checked != null && checked) {
                int modelRow = dataTable.convertRowIndexToModel(i);
                int id = -1;
                try { id = Integer.parseInt(tableModel.getValueAt(modelRow, 1).toString().replace("SP", "").trim()); } catch (Exception e) {}
                
                if (id != -1) {
                    try {
                        boolean success = Controller.SanPhamDAO.deleteSanPham(id);
                        if (success) {
                            successCount++;
                            tableModel.removeRow(i);
                        } else {
                            failedCount++;
                        }
                    } catch (Exception e) {
                        failedCount++;
                        String errorMsg = e.getMessage();
                        if (errorMsg != null && errorMsg.contains("ORA-02292")) {
                            failReason = "Không thể xóa Sản phẩm này vì dữ liệu đã nằm trong Hóa đơn hoặc Phiếu nhập.";
                        } else {
                            failReason = errorMsg;
                        }
                    }
                } else {
                    successCount++;
                    tableModel.removeRow(i);
                }
            }
        }
        loadProductsForCategory(currentCategoryId);

        if (failedCount > 0) {
            JOptionPane.showMessageDialog(
                this,
                "Lỗi bảo vệ dữ liệu:\n\n- Số lượng xóa thành công: " + successCount + " mục.\n- Số lượng thất bại: " + failedCount + " mục.\n\nNguyên nhân thất bại:\n" + failReason,
                "Từ chối xóa dữ liệu",
                JOptionPane.WARNING_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(this, "Đã xóa thành công tất cả các mục đã chọn!");
        }
    }

    // Dialog class for Product
    class ProductDialog extends JDialog {
        private JTextField txtTen = new JTextField();
        private JTextField txtMoTa = new JTextField();
        private JTextField txtSoLuongDaBan = new JTextField("0");
        private JTextField txtGia = new JTextField("0");
        private JTextField txtDonViTinh = new JTextField("Cái");
        private JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"Đang kinh doanh", "Ngừng kinh doanh"});
        private JButton btnSave = new JButton("Lưu");
        private JButton btnCancel = new JButton("Hủy");
        private boolean isSaveClicked = false;

        public ProductDialog(Window owner, String title) {
            super(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
            setSize(480, 580);
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

            String[][] fields = {
                {"Tên sản phẩm"},
                {"Mô tả"},
                {"Số lượng đã bán"},
                {"Giá bán (đ)"},
                {"Đơn vị tính"}
            };
            JTextField[] inputs = {txtTen, txtMoTa, txtSoLuongDaBan, txtGia, txtDonViTinh};

            for (int i = 0; i < fields.length; i++) {
                gbc.gridy = i * 2; gbc.insets = new Insets(i == 0 ? 0 : 6, 0, 4, 0);
                JLabel lbl = new JLabel(fields[i][0]);
                lbl.setFont(labelFont);
                content.add(lbl, gbc);

                gbc.gridy = i * 2 + 1; gbc.insets = new Insets(0, 0, 4, 0);
                inputs[i].setFont(fieldFont);
                inputs[i].setPreferredSize(new Dimension(400, 35));
                inputs[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                    BorderFactory.createEmptyBorder(4, 10, 4, 10)
                ));
                content.add(inputs[i], gbc);
            }

            // Vô hiệu hóa nhập thủ công số lượng đã bán (chỉ đọc)
            txtSoLuongDaBan.setEditable(false);
            txtSoLuongDaBan.setFocusable(false);
            txtSoLuongDaBan.setBackground(new Color(241, 245, 249));
            txtSoLuongDaBan.setForeground(new Color(100, 116, 139));

            // Trạng thái
            gbc.gridy = fields.length * 2; gbc.insets = new Insets(6, 0, 4, 0);
            JLabel lblStatus = new JLabel("Trạng thái");
            lblStatus.setFont(labelFont);
            content.add(lblStatus, gbc);

            gbc.gridy = fields.length * 2 + 1; gbc.insets = new Insets(0, 0, 0, 0);
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
                    JOptionPane.showMessageDialog(this, "Tên sản phẩm không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                isSaveClicked = true;
                dispose();
            });
            btnCancel.addActionListener(e -> dispose());
        }

        public String getTenSp() { return txtTen.getText().trim(); }
        public void setTenSp(String n) { txtTen.setText(n); }
        public String getMoTa() { return txtMoTa.getText().trim(); }
        public void setMoTa(String d) { txtMoTa.setText(d); }
        public int getSoLuongDaBan() { try { return Integer.parseInt(txtSoLuongDaBan.getText().trim()); } catch (Exception e) { return 0; } }
        public void setSoLuongDaBan(int v) { txtSoLuongDaBan.setText(String.valueOf(v)); }
        public double getGiaBan() { try { return Double.parseDouble(txtGia.getText().trim()); } catch (Exception e) { return 0; } }
        public void setGiaBan(double v) { txtGia.setText(String.valueOf((long)v)); }
        public String getDonViTinh() { return txtDonViTinh.getText().trim(); }
        public void setDonViTinh(String d) { txtDonViTinh.setText(d); }
        public String getTrangThai() { return cbTrangThai.getSelectedItem().toString(); }
        public void setTrangThai(String t) { cbTrangThai.setSelectedItem(t); }
        public boolean isSaveClicked() { return isSaveClicked; }
    }
}
