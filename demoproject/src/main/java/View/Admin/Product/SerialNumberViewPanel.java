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

        JButton btnDeleteSelected = new JButton("🗑️ Xóa");
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
        String[] columns = {"", "Mã Serial Number", "Trạng thái", "Ngày nhập", "Thao tác"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Boolean.class : super.getColumnClass(c);
            }
            @Override public boolean isCellEditable(int r, int c) {
                return c == 0 || c == 4;
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
        dataTable.getColumnModel().getColumn(4).setCellRenderer(new ProductSharedUtils.ActionCellRenderer(false));
        dataTable.getColumnModel().getColumn(4).setCellEditor(actionEditor);

        JScrollPane scrollPane = new JScrollPane(dataTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void loadSerialsForVariant(int variantId) {
        this.currentVariantId = variantId;
        tableModel.setRowCount(0);
        try {
            java.util.List<Model.SerialNumber> list = Controller.SerialNumberDAO.getSerialNumbersByMaBienThe(variantId);
            boolean hasData = false;
            for (Model.SerialNumber sn : list) {
                hasData = true;
                tableModel.addRow(new Object[]{
                    Boolean.FALSE,
                    sn.getMaSerial(),
                    sn.getTrangThai(),
                    new java.text.SimpleDateFormat("dd/MM/yyyy").format(sn.getNgayNhap()),
                    sn.getMaSerial()
                });
            }
            if (!hasData) loadSampleSerials();
        } catch (Exception e) {
            e.printStackTrace();
            loadSampleSerials();
        }
    }

    private void loadSampleSerials() {
        Object[][] samples = {
            {"SN-2023-XYZ01", "Trong kho", "15/05/2026"},
            {"SN-2023-XYZ02", "Đã bán", "10/05/2026"},
            {"SN-2023-XYZ03", "Lỗi", "01/05/2026"}
        };
        for (Object[] row : samples) {
            tableModel.addRow(new Object[]{ Boolean.FALSE, row[0], row[1], row[2], -1 });
        }
    }

    private void handleAddSerial() {
        if (currentVariantId == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Biến thể trước!");
            return;
        }
        SerialDialog dialog = new SerialDialog(SwingUtilities.getWindowAncestor(this), "Thêm Serial");
        dialog.setVisible(true);
        if (dialog.isSaveClicked()) {
            try {
                Model.SerialNumber sn = new Model.SerialNumber();
                sn.setMaSerial(dialog.getMaSerial());
                sn.setMaBienThe(currentVariantId);
                sn.setTrangThai(dialog.getTrangThai());
                sn.setNgayNhap(new java.sql.Date(System.currentTimeMillis()));
                Controller.SerialNumberDAO.addSerialNumber(sn);
                loadSerialsForVariant(currentVariantId);
            } catch (Exception e) {
                e.printStackTrace();
                tableModel.addRow(new Object[]{ Boolean.FALSE, dialog.getMaSerial(), dialog.getTrangThai(), "Hôm nay", -1 });
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
            
            Object statusObj = tableModel.getValueAt(modelRow, 2);
            String trangThai = statusObj != null ? statusObj.toString() : "";
    
            SerialDialog dialog = new SerialDialog(SwingUtilities.getWindowAncestor(this), "Sửa Serial");
            dialog.setMaSerial(maSerial);
            dialog.setTrangThai(trangThai);
            dialog.setVisible(true);
    
            if (dialog.isSaveClicked()) {
                try {
                    Model.SerialNumber sn = new Model.SerialNumber();
                    sn.setMaSerial(dialog.getMaSerial());
                    sn.setMaBienThe(currentVariantId);
                    sn.setTrangThai(dialog.getTrangThai());
                    Controller.SerialNumberDAO.updateSerialNumber(sn, maSerial);
                    loadSerialsForVariant(currentVariantId);
                } catch (Exception e) {
                    e.printStackTrace();
                    tableModel.setValueAt(dialog.getMaSerial(), modelRow, 1);
                    tableModel.setValueAt(dialog.getTrangThai(), modelRow, 2);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra: " + e.getMessage());
        }
    }

    private void handleDeleteSelected() {
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            Boolean checked = (Boolean) tableModel.getValueAt(i, 0);
            if (checked != null && checked) {
                int modelRow = dataTable.convertRowIndexToModel(i);
                String maSerial = tableModel.getValueAt(modelRow, 1).toString();
                try {
                    Controller.SerialNumberDAO.deleteSerialNumber(maSerial);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                tableModel.removeRow(i);
            }
        }
        loadSerialsForVariant(currentVariantId);
    }

    // Dialog class for Serial Number
    class SerialDialog extends JDialog {
        private JTextField txtMaSerial = new JTextField();
        private JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"Trong kho", "Đã bán", "Lỗi"});
        private JButton btnSave = new JButton("Lưu");
        private JButton btnCancel = new JButton("Hủy");
        private boolean isSaveClicked = false;

        public SerialDialog(Window owner, String title) {
            super(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
            setSize(460, 260);
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

            // Trạng thái
            gbc.gridy = 2; gbc.insets = new Insets(0, 0, 4, 0);
            JLabel lblStatus = new JLabel("Trạng thái");
            lblStatus.setFont(labelFont);
            content.add(lblStatus, gbc);

            gbc.gridy = 3; gbc.insets = new Insets(0, 0, 0, 0);
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
        public boolean isSaveClicked() { return isSaveClicked; }
    }
}
