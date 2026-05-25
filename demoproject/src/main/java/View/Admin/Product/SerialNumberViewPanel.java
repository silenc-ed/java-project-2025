package View.Admin.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class SerialNumberViewPanel extends JPanel {

    private ProductManagementController controller;
    private int currentVariantId = -1;
    private DefaultTableModel tableModel;
    private JTable dataTable;
    private JTextField txtSearch;

    public SerialNumberViewPanel(ProductManagementController controller) {
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
        JLabel lblTitle = new JLabel("Danh sách Serial Number");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(15, 23, 42));
        leftHeader.add(lblTitle);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightHeader.setOpaque(false);

        JButton btnDeleteSelected = new JButton("Xóa");
        View.Admin.UIUtils.styleButton(btnDeleteSelected);
        btnDeleteSelected.addActionListener(e -> handleDeleteSelected());

        JButton btnAdd = new JButton("+ Thêm Serial");
        View.Admin.UIUtils.styleButton(btnAdd);
        btnAdd.addActionListener(e -> handleAddSerial());

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
        txtSearch = new JTextField("Tìm kiếm Serial...");
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
                if (text.isEmpty() || text.equals("Tìm kiếm Serial...")) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1));
                }
            }
        });
        centerPanel.add(txtSearch, BorderLayout.NORTH);

        // Table
        String[] columns = {"", "Mã Serial Number", "Chi nhánh", "Trạng thái", "Ngày nhập", "Thao tác"};
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
            this::handleEditSerial, 
            null,
            false // No "Chi tiết" button at leaf node
        );
        dataTable.getColumnModel().getColumn(5).setCellRenderer(new ProductSharedUtils.ActionCellRenderer(false));
        dataTable.getColumnModel().getColumn(5).setCellEditor(actionEditor);

        JScrollPane scrollPane = new JScrollPane(dataTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void loadSerialsForVariant(int variantId) {
        this.currentVariantId = variantId;
        tableModel.setRowCount(0);
        try {
            java.util.List<Model.SerialNumber> list = Controller.SerialNumberDAO.getSerialNumbersByMaBienThe(variantId);
            for (Model.SerialNumber sn : list) {
                tableModel.addRow(new Object[]{
                    Boolean.FALSE,
                    sn.getMaSerial(),
                    sn.getTenCn() != null ? sn.getTenCn() : "Chưa có",
                    sn.getTrangThai(),
                    sn.getNgayNhap() != null ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(sn.getNgayNhap()) : "",
                    sn.getMaSerial()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAddSerial() {
        if (currentVariantId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Biến thể trước!");
            return;
        }
        SerialDialog dialog = new SerialDialog(SwingUtilities.getWindowAncestor(this), "Thêm Serial");
        
        try {
            java.util.List<Model.ChiNhanh> chiNhanhList = Controller.ChiNhanhDAO.getAllChiNhanh();
            dialog.setChiNhanhList(chiNhanhList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        dialog.setVisible(true);
        if (dialog.isSaveClicked()) {
            try {
                Model.SerialNumber sn = new Model.SerialNumber();
                sn.setMaSerial(dialog.getMaSerial());
                sn.setMaBienThe(currentVariantId);
                
                Model.ChiNhanh selectedCn = dialog.getSelectedChiNhanh();
                if (selectedCn != null) sn.setMaCn(selectedCn.getMaCn());
                
                sn.setTrangThai(dialog.getTrangThai());
                sn.setNgayNhap(new java.sql.Date(System.currentTimeMillis()));
                Controller.SerialNumberDAO.addSerialNumber(sn);
                loadSerialsForVariant(currentVariantId);
            } catch (Exception e) {
                e.printStackTrace();
                tableModel.addRow(new Object[]{ Boolean.FALSE, dialog.getMaSerial(), dialog.getSelectedChiNhanh() != null ? dialog.getSelectedChiNhanh().getTenCn() : "", dialog.getTrangThai(), "Hôm nay", -1 });
            }
        }
    }

    private void handleEditSerial() {
        try {
            int row = dataTable.getSelectedRow();
            if (row == -1) return;
            int modelRow = dataTable.convertRowIndexToModel(row);
            
            Object serialObj = tableModel.getValueAt(modelRow, 1);
            String maSerial = serialObj != null ? serialObj.toString() : "";
            
            Object statusObj = tableModel.getValueAt(modelRow, 3);
            String trangThai = statusObj != null ? statusObj.toString() : "";
            
            Object branchObj = tableModel.getValueAt(modelRow, 2);
            String branchName = branchObj != null ? branchObj.toString() : "";
    
            SerialDialog dialog = new SerialDialog(SwingUtilities.getWindowAncestor(this), "Sửa Serial");
            
            try {
                java.util.List<Model.ChiNhanh> chiNhanhList = Controller.ChiNhanhDAO.getAllChiNhanh();
                dialog.setChiNhanhList(chiNhanhList);
                dialog.setSelectedChiNhanhByName(branchName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            dialog.setMaSerial(maSerial);
            dialog.setTrangThai(trangThai);
            dialog.setVisible(true);
    
            if (dialog.isSaveClicked()) {
                try {
                    Model.SerialNumber sn = new Model.SerialNumber();
                    sn.setMaSerial(dialog.getMaSerial());
                    sn.setMaBienThe(currentVariantId);
                    
                    Model.ChiNhanh selectedCn = dialog.getSelectedChiNhanh();
                    if (selectedCn != null) sn.setMaCn(selectedCn.getMaCn());
                    
                    sn.setTrangThai(dialog.getTrangThai());
                    Controller.SerialNumberDAO.updateSerialNumber(sn, maSerial);
                    loadSerialsForVariant(currentVariantId);
                } catch (Exception e) {
                    e.printStackTrace();
                    tableModel.setValueAt(dialog.getMaSerial(), modelRow, 1);
                    tableModel.setValueAt(dialog.getSelectedChiNhanh() != null ? dialog.getSelectedChiNhanh().getTenCn() : "", modelRow, 2);
                    tableModel.setValueAt(dialog.getTrangThai(), modelRow, 3);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra: " + e.getMessage());
        }
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
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một mã Serial Number để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc chắn muốn xóa các mã Serial Number đã chọn không?",
            "Xác nhận xóa Serial Number",
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
                String maSerial = tableModel.getValueAt(modelRow, 1).toString();
                try {
                    boolean success = Controller.SerialNumberDAO.deleteSerialNumber(maSerial);
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
                        failReason = "Không thể xóa Serial Number này vì dữ liệu đã nằm trong Hóa đơn hoặc Phiếu nhập.";
                    } else {
                        failReason = errorMsg;
                    }
                }
            }
        }
        loadSerialsForVariant(currentVariantId);

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

    // Dialog class for Serial Number
    class SerialDialog extends JDialog {
        private JTextField txtMaSerial = new JTextField();
        private JComboBox<Model.ChiNhanh> cbChiNhanh = new JComboBox<>();
        private JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"Trong kho", "Đã bán", "Lỗi"});
        private JButton btnSave = new JButton("Lưu");
        private JButton btnCancel = new JButton("Hủy");
        private boolean isSaveClicked = false;

        public SerialDialog(Window owner, String title) {
            super(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
            setSize(460, 320);
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

            // Mã Serial
            gbc.gridy = 0; gbc.insets = new Insets(0, 0, 4, 0);
            JLabel lblMa = new JLabel("Mã Serial Number");
            lblMa.setFont(labelFont);
            content.add(lblMa, gbc);

            gbc.gridy = 1; gbc.insets = new Insets(0, 0, 14, 0);
            txtMaSerial.setFont(fieldFont);
            txtMaSerial.setPreferredSize(new Dimension(400, 35));
            txtMaSerial.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
            content.add(txtMaSerial, gbc);

            // Chi nhánh
            gbc.gridy = 2; gbc.insets = new Insets(0, 0, 4, 0);
            JLabel lblChiNhanh = new JLabel("Chi nhánh");
            lblChiNhanh.setFont(labelFont);
            content.add(lblChiNhanh, gbc);

            gbc.gridy = 3; gbc.insets = new Insets(0, 0, 14, 0);
            cbChiNhanh.setFont(fieldFont);
            cbChiNhanh.setPreferredSize(new Dimension(400, 35));
            cbChiNhanh.setBackground(Color.WHITE);
            content.add(cbChiNhanh, gbc);

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
                if (txtMaSerial.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Mã Serial không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                isSaveClicked = true;
                dispose();
            });
            btnCancel.addActionListener(e -> dispose());
        }

        public String getMaSerial() { return txtMaSerial.getText().trim(); }
        public void setMaSerial(String n) { txtMaSerial.setText(n); }
        public String getTrangThai() { return cbTrangThai.getSelectedItem().toString(); }
        public void setTrangThai(String t) { cbTrangThai.setSelectedItem(t); }
        
        public void setChiNhanhList(java.util.List<Model.ChiNhanh> list) {
            cbChiNhanh.removeAllItems();
            for (Model.ChiNhanh cn : list) {
                cbChiNhanh.addItem(cn);
            }
        }
        public Model.ChiNhanh getSelectedChiNhanh() {
            return (Model.ChiNhanh) cbChiNhanh.getSelectedItem();
        }
        public void setSelectedChiNhanhByName(String name) {
            for (int i = 0; i < cbChiNhanh.getItemCount(); i++) {
                if (cbChiNhanh.getItemAt(i).getTenCn().equals(name)) {
                    cbChiNhanh.setSelectedIndex(i);
                    break;
                }
            }
        }
        
        public boolean isSaveClicked() { return isSaveClicked; }
    }
}
