package View.Admin.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class CategoryViewPanel extends JPanel {

    private ProductManagementController controller;
    private DefaultTableModel tableModel;
    private JTable dataTable;
    private JTextField txtSearch;
    private JLabel lblLastUpdate;

    public CategoryViewPanel(ProductManagementController controller) {
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
        JLabel lblTitle = new JLabel("Danh sách loại sản phẩm");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(15, 23, 42));
        lblLastUpdate = new JLabel("Chưa cập nhật");
        lblLastUpdate.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblLastUpdate.setForeground(new Color(148, 163, 184));
        leftHeader.add(lblTitle);
        leftHeader.add(lblLastUpdate);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightHeader.setOpaque(false);

        JButton btnDeleteSelected = new JButton("Xóa");
        View.Admin.UIUtils.styleButton(btnDeleteSelected);
        btnDeleteSelected.setPreferredSize(new Dimension(100, 36));
        btnDeleteSelected.addActionListener(e -> handleDeleteSelected());

        JButton btnRefresh = new JButton("Cập nhật");
        View.Admin.UIUtils.styleButton(btnRefresh);
        btnRefresh.setPreferredSize(new Dimension(110, 36));
        btnRefresh.addActionListener(e -> refreshData());

        JButton btnAdd = new JButton("+ Thêm loại sản phẩm");
        View.Admin.UIUtils.styleButton(btnAdd);
        btnAdd.setPreferredSize(new Dimension(160, 36));
        btnAdd.addActionListener(e -> handleAddCategory());

        // Ẩn các nút theo quyền
        btnDeleteSelected.setVisible(Controller.Admin.PermissionService.canDelete("Quan ly SP"));
        btnAdd.setVisible(Controller.Admin.PermissionService.canAdd("Quan ly SP"));

        rightHeader.add(btnDeleteSelected);
        rightHeader.add(btnRefresh);
        rightHeader.add(btnAdd);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Panel
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);

        // Search Bar
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        txtSearch = new JTextField("Tìm kiếm loại sản phẩm...");
        txtSearch.setBorder(null);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtSearch.setForeground(Color.GRAY);
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (txtSearch.getText().equals("Tìm kiếm loại sản phẩm...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(new Color(15, 23, 42));
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText("Tìm kiếm loại sản phẩm...");
                    txtSearch.setForeground(Color.GRAY);
                }
            }
        });
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        centerPanel.add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"", "Mã loại sản phẩm", "Tên loại sản phẩm", "Mô tả", "Tổng số mặt hàng", "Thao tác"};
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

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        dataTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        dataTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        ProductSharedUtils.ActionCellEditor actionEditor = new ProductSharedUtils.ActionCellEditor(
            dataTable, 
            this::handleEditCategory, 
            this::handleViewCategory,
            true // Show "Chi tiết" button
        );
        dataTable.getColumnModel().getColumn(5).setCellRenderer(new ProductSharedUtils.ActionCellRenderer(true));
        dataTable.getColumnModel().getColumn(5).setCellEditor(actionEditor);

        dataTable.getTableHeader().setPreferredSize(new Dimension(0, 45));
        dataTable.getTableHeader().setBackground(Color.WHITE);
        dataTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Double click to view details (skip checkbox col 0 and action col 5)
        dataTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = dataTable.rowAtPoint(e.getPoint());
                    int col = dataTable.columnAtPoint(e.getPoint());
                    if (row != -1 && col > 0 && col < 5) {
                        handleViewCategory();
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Search Filter
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = txtSearch.getText().trim();
                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
                dataTable.setRowSorter(sorter);
                if (text.isEmpty() || text.equals("Tìm kiếm loại sản phẩm...")) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1, 2));
                }
            }
        });
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        String time = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));
        lblLastUpdate.setText("Cập nhật lúc: " + time);
        try {
            List<Model.LoaiSanPham> list = Controller.LoaiSanPhamDAO.getAllLoaiSanPham();
            for (Model.LoaiSanPham lsp : list) {
                tableModel.addRow(new Object[]{
                    Boolean.FALSE,
                    String.valueOf(lsp.getMaLsp()),
                    lsp.getTenLsp(),
                    lsp.getMoTa() != null ? lsp.getMoTa() : "",
                    lsp.getTongSoMatHang(),
                    lsp.getMaLsp()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAddCategory() {
        CategoryDialog dialog = new CategoryDialog(SwingUtilities.getWindowAncestor(this), "Thêm loại sản phẩm");
        dialog.setVisible(true);
        if (dialog.isSaveClicked()) {
            try {
                Model.LoaiSanPham lsp = new Model.LoaiSanPham();
                lsp.setTenLsp(dialog.getTenLsp());
                lsp.setMoTa(dialog.getMoTa());
                Controller.LoaiSanPhamDAO.addLoaiSanPham(lsp);
                refreshData();
            } catch (Exception e) {
                tableModel.addRow(new Object[]{ Boolean.FALSE, String.valueOf(System.currentTimeMillis() % 1000), dialog.getTenLsp(), dialog.getMoTa(), 0, -1 });
            }
        }
    }

    private void handleEditCategory() {
        try {
            int row = dataTable.getSelectedRow();
            if (row == -1) return;
            int modelRow = dataTable.convertRowIndexToModel(row);
            
            Object nameObj = tableModel.getValueAt(modelRow, 2);
            String name = nameObj != null ? nameObj.toString() : "";
            
            Object descObj = tableModel.getValueAt(modelRow, 3);
            String desc = descObj != null ? descObj.toString() : "";
            
            CategoryDialog dialog = new CategoryDialog(SwingUtilities.getWindowAncestor(this), "Sửa loại sản phẩm");
            dialog.setTenLsp(name);
            dialog.setMoTa(desc);
            dialog.setVisible(true);
            
            if (dialog.isSaveClicked()) {
                tableModel.setValueAt(dialog.getTenLsp(), modelRow, 2);
                tableModel.setValueAt(dialog.getMoTa(), modelRow, 3);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra: " + e.getMessage());
        }
    }

    private void handleViewCategory() {
        int row = dataTable.getSelectedRow();
        if (row == -1) return;
        int modelRow = dataTable.convertRowIndexToModel(row);
        
        int id = Integer.parseInt(tableModel.getValueAt(modelRow, 1).toString());
        String name = tableModel.getValueAt(modelRow, 2).toString();
        
        controller.showProduct(id, name);
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
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một loại sản phẩm để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            " CẢNH BÁO: Hành động này sẽ XÓA HẾT tất cả dữ liệu liên quan đến loại sản phẩm đó\n" +
            "(bao gồm tất cả sản phẩm, mã serial, số lượng tồn kho, các phiên bản/biến thể liên quan)!\n\n" +
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
                try { id = Integer.parseInt(tableModel.getValueAt(modelRow, 1).toString().trim()); } catch (Exception e) {}
                
                if (id != -1) {
                    try {
                        boolean success = Controller.LoaiSanPhamDAO.deleteLoaiSanPham(id);
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
                            failReason = "Không thể xóa Loại sản phẩm này vì vẫn còn Sản phẩm bên trong (Vui lòng xóa sản phẩm trước).";
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
        refreshData();

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

    // Dialog class
    class CategoryDialog extends JDialog {
        private JTextField txtTen = new JTextField();
        private JTextField txtMoTa = new JTextField();
        private JButton btnSave = new JButton("Lưu");
        private JButton btnCancel = new JButton("Hủy");
        private boolean isSaveClicked = false;

        public CategoryDialog(Window owner, String title) {
            super(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
            setSize(440, 280);
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

            // Row 0: Label Tên
            gbc.gridy = 0; gbc.insets = new Insets(0, 0, 4, 0);
            JLabel lblTen = new JLabel("Tên loại sản phẩm");
            lblTen.setFont(labelFont);
            content.add(lblTen, gbc);

            // Row 1: Field Tên
            gbc.gridy = 1; gbc.insets = new Insets(0, 0, 14, 0);
            txtTen.setFont(fieldFont);
            txtTen.setPreferredSize(new Dimension(380, 35));
            txtTen.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
            content.add(txtTen, gbc);

            // Row 2: Label Mô tả
            gbc.gridy = 2; gbc.insets = new Insets(0, 0, 4, 0);
            JLabel lblMoTa = new JLabel("Mô tả");
            lblMoTa.setFont(labelFont);
            content.add(lblMoTa, gbc);

            // Row 3: Field Mô tả
            gbc.gridy = 3; gbc.insets = new Insets(0, 0, 0, 0);
            txtMoTa.setFont(fieldFont);
            txtMoTa.setPreferredSize(new Dimension(380, 35));
            txtMoTa.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
            content.add(txtMoTa, gbc);

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
                    JOptionPane.showMessageDialog(this, "Tên loại sản phẩm không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                isSaveClicked = true;
                dispose();
            });
            btnCancel.addActionListener(e -> dispose());
        }

        public String getTenLsp() { return txtTen.getText().trim(); }
        public void setTenLsp(String n) { txtTen.setText(n); }
        public String getMoTa() { return txtMoTa.getText().trim(); }
        public void setMoTa(String d) { txtMoTa.setText(d); }
        public boolean isSaveClicked() { return isSaveClicked; }
    }
}
