package View.Admin.Service;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

// ================= Helper classes (scoped to this file) =================



class ServiceStatusRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String status = value != null ? value.toString() : "Ngừng hoạt động";
        JLabel label = new JLabel(status);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setOpaque(false);

        if ("Hoạt động".equalsIgnoreCase(status)) {
            label.setForeground(new Color(21, 128, 61));
        } else {
            label.setForeground(new Color(185, 28, 28));
        }
        return label;
    }
}

class ServiceActionPanel extends JPanel {
    public JButton btnEdit = new JButton();

    public ServiceActionPanel() {
        setLayout(new GridBagLayout());
        setOpaque(true);
        setBackground(Color.WHITE);

        btnEdit.setText("Sửa");
        View.Admin.UIUtils.styleButton(btnEdit);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(btnEdit, gbc);
    }
}

class ServiceActionRenderer extends DefaultTableCellRenderer {
    private ServiceActionPanel panel = new ServiceActionPanel();
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            panel.setBackground(table.getSelectionBackground());
        } else {
            panel.setBackground(Color.WHITE);
        }
        return panel;
    }
}

// ================= Main Panel =================

public class ServicePanel extends javax.swing.JPanel {

    private DefaultTableModel tableModel;
    private JTable dataTable;
    private JLabel lblLastUpdate;
    private JButton btnDeleteSelected;
    private JButton btnAdd;
    private JLabel lblTitle;
    private JTextField txtSearch;

    public ServicePanel() {
        initComponents();
        setupCustomUI();
        // Ẩn các nút theo quyền
        btnAdd.setVisible(Controller.Admin.PermissionService.canAdd("Dich vu"));
        btnDeleteSelected.setVisible(Controller.Admin.PermissionService.canDelete("Dich vu"));
    }

    private void setupCustomUI() {
        this.removeAll();
        this.setLayout(new BorderLayout(15, 15));
        this.setBackground(new Color(248, 250, 252));
        this.setBorder(new EmptyBorder(20, 20, 20, 20));

        // ================= HEADER =================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftHeader.setOpaque(false);

        lblTitle = new JLabel("Quản lý dịch vụ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(15, 23, 42));

        lblLastUpdate = new JLabel("Chưa cập nhật");
        lblLastUpdate.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblLastUpdate.setForeground(new Color(148, 163, 184));

        leftHeader.add(lblTitle);
        leftHeader.add(lblLastUpdate);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightHeader.setOpaque(false);

        btnDeleteSelected = new JButton("Xóa");
        View.Admin.UIUtils.styleButton(btnDeleteSelected);
        btnDeleteSelected.setPreferredSize(new Dimension(140, 36));
        btnDeleteSelected.addActionListener(e -> handleDeleteSelected());

        JButton btnRefresh = new JButton("Cập nhật");
        View.Admin.UIUtils.styleButton(btnRefresh);
        btnRefresh.setPreferredSize(new Dimension(130, 36));
        btnRefresh.addActionListener(e -> loadDataToTable());

        btnAdd = new JButton("+ Thêm dịch vụ");
        View.Admin.UIUtils.styleButton(btnAdd);
        btnAdd.setPreferredSize(new Dimension(160, 36));
        btnAdd.addActionListener(e -> handleAddService());

        rightHeader.add(btnDeleteSelected);
        rightHeader.add(btnRefresh);
        rightHeader.add(btnAdd);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);

        this.add(headerPanel, BorderLayout.NORTH);

        // ================= CENTER =================
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);

        // Search Bar
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        txtSearch = new JTextField("Tìm kiếm dịch vụ...");
        txtSearch.setBorder(null);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtSearch.setForeground(Color.GRAY);
        txtSearch.setSelectionColor(new Color(210, 160, 205));
        txtSearch.setSelectedTextColor(Color.WHITE);
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals("Tìm kiếm dịch vụ...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(new Color(15, 23, 42));
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText("Tìm kiếm dịch vụ...");
                    txtSearch.setForeground(Color.GRAY);
                }
            }
        });
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        centerPanel.add(searchPanel, BorderLayout.NORTH);

        // Table initialization
        String[] columns = {"", "Mã dịch vụ", "Tên dịch vụ", "Mô tả", "Giá cước", "Trạng thái", "Thao tác"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public Class<?> getColumnClass(int c) {
                if (c == 0) return Boolean.class;
                return super.getColumnClass(c);
            }
            @Override public boolean isCellEditable(int r, int c) {
                return c == 0;
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
        dataTable.setSelectionForeground(new Color(142, 68, 173));

        setupTableColumns();

        // Xử lý sự kiện click nút Sửa tức thì trên cột Thao tác (cột 6)
        dataTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int col = dataTable.columnAtPoint(e.getPoint());
                if (col == 6) { // Cột Thao tác
                    int row = dataTable.rowAtPoint(e.getPoint());
                    if (row != -1) {
                        dataTable.setRowSelectionInterval(row, row);
                        int modelRow = dataTable.convertRowIndexToModel(row);
                        handleEditService(modelRow);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        this.add(centerPanel, BorderLayout.CENTER);

        // Search filtering
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = txtSearch.getText().trim();
                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
                dataTable.setRowSorter(sorter);
                if (text.isEmpty() || text.equals("Tìm kiếm dịch vụ...")) {
                    sorter.setRowFilter(null);
                } else {
                    final String searchLower = text.toLowerCase();
                    sorter.setRowFilter(new RowFilter<DefaultTableModel, Object>() {
                        @Override
                        public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Object> entry) {
                            String maDv = entry.getStringValue(1).toLowerCase();
                            String tenDv = entry.getStringValue(2).toLowerCase();
                            String moTa = entry.getStringValue(3).toLowerCase();
                            return maDv.contains(searchLower) || tenDv.contains(searchLower) || moTa.contains(searchLower);
                        }
                    });
                }
            }
        });

        // Load data from DB in background
        loadDataToTable();
    }

    private void setupTableColumns() {
        // Checkbox column
        dataTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        dataTable.getColumnModel().getColumn(0).setMaxWidth(40);

        // Status column
        dataTable.getColumnModel().getColumn(5).setCellRenderer(new ServiceStatusRenderer());

        // Center align columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        dataTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        dataTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // Action Renderer (sự kiện click được xử lý trực tiếp qua MouseListener)
        dataTable.getColumnModel().getColumn(6).setCellRenderer(new ServiceActionRenderer());

        // General styling for the header
        dataTable.getTableHeader().setPreferredSize(new Dimension(0, 45));
        dataTable.getTableHeader().setBackground(Color.WHITE);
        dataTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        dataTable.getTableHeader().setForeground(new Color(100, 116, 139));
        dataTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);
        String time = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));
        if (lblLastUpdate != null) {
            lblLastUpdate.setText("Cập nhật lúc: " + time);
        }
        DecimalFormat df = new DecimalFormat("#,###đ");
        try {
            List<Model.DichVu> list = Controller.DichVuDAO.getAllDichVu();
            boolean hasData = false;
            for (Model.DichVu dv : list) {
                hasData = true;
                int id = dv.getMaDv();
                String name = dv.getTenDv();
                String desc = dv.getMoTa();
                double price = dv.getGiaCuoc();
                int status = dv.getTrangThai();

                tableModel.addRow(new Object[]{
                    Boolean.FALSE,
                    String.valueOf(id),
                    name,
                    desc != null ? desc : "",
                    df.format(price),
                    status == 1 ? "Hoạt động" : "Ngừng hoạt động",
                    id
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kết nối hoặc không thể lấy dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }





    // ================= CRUD Handlers =================

    private void handleAddService() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        Frame frame = parent instanceof Frame ? (Frame) parent : null;
        ServiceDialog dialog = new ServiceDialog(frame, "Thêm dịch vụ mới");
        dialog.setVisible(true);

        if (dialog.isSaveClicked()) {
            String name = dialog.getTenDv();
            String desc = dialog.getMoTa();
            double price = dialog.getGiaCuoc();
            int status = dialog.getTrangThai();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng điền tên dịch vụ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Model.DichVu dv = new Model.DichVu();
                dv.setTenDv(name);
                dv.setMoTa(desc);
                dv.setGiaCuoc(price);
                dv.setTrangThai(status);

                boolean success = Controller.DichVuDAO.addDichVu(dv);
                if (success) {
                    loadDataToTable();
                    JOptionPane.showMessageDialog(this, "Thêm dịch vụ thành công!");
                } else {
                    throw new Exception("Không thể thêm dịch vụ");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi hệ thống: Không thể thêm vào Database!\n" + ex.getMessage(), "Lỗi Database", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEditService(int modelRow) {
        String currentName = tableModel.getValueAt(modelRow, 2).toString();
        String currentDesc = tableModel.getValueAt(modelRow, 3).toString();
        String currentPriceStr = tableModel.getValueAt(modelRow, 4).toString().replace(",", "").replace(".", "").replace("đ", "");
        double currentPrice = 0;
        try { currentPrice = Double.parseDouble(currentPriceStr); } catch (Exception ignored) {}
        String currentStatusStr = tableModel.getValueAt(modelRow, 5).toString();
        int currentStatus = "Hoạt động".equalsIgnoreCase(currentStatusStr) ? 1 : 0;
        
        // Lấy ID an toàn bằng cách parse Mã dịch vụ từ cột 1 (không bao giờ bị ghi đè thành null)
        int id = -1;
        try {
            id = Integer.parseInt(tableModel.getValueAt(modelRow, 1).toString());
        } catch (Exception ignored) {}

        Window parent = SwingUtilities.getWindowAncestor(this);
        Frame frame = parent instanceof Frame ? (Frame) parent : null;
        ServiceDialog dialog = new ServiceDialog(frame, "Cập nhật dịch vụ");
        dialog.setTenDv(currentName);
        dialog.setMoTa(currentDesc);
        dialog.setGiaCuoc(currentPrice);
        dialog.setTrangThai(currentStatus);

        dialog.setVisible(true);

        if (dialog.isSaveClicked()) {
            String name = dialog.getTenDv();
            String desc = dialog.getMoTa();
            double price = dialog.getGiaCuoc();
            int status = dialog.getTrangThai();

            if (name.isEmpty()) return;

            if (id != -1) {
                try {
                    Model.DichVu dv = new Model.DichVu();
                    dv.setMaDv(id);
                    dv.setTenDv(name);
                    dv.setMoTa(desc);
                    dv.setGiaCuoc(price);
                    dv.setTrangThai(status);

                    boolean success = Controller.DichVuDAO.updateDichVu(dv);
                    if (success) {
                        loadDataToTable();
                        JOptionPane.showMessageDialog(this, "Cập nhật dịch vụ thành công!");
                    } else {
                        throw new Exception("Không thể cập nhật dịch vụ");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật cơ sở dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi dữ liệu: Không tìm thấy mã dịch vụ hợp lệ để cập nhật!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteSelected() {
        if (dataTable.isEditing()) {
            dataTable.getCellEditor().stopCellEditing();
        }

        List<Integer> selectedIds = new ArrayList<>();
        List<Integer> selectedModelRows = new ArrayList<>();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean checked = (Boolean) tableModel.getValueAt(i, 0);
            if (checked != null && checked) {
                selectedModelRows.add(i);
                Object idObj = tableModel.getValueAt(i, 6);
                if (idObj instanceof Integer) {
                    selectedIds.add((Integer) idObj);
                }
            }
        }

        if (selectedIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một dịch vụ để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc chắn muốn xóa " + selectedIds.size() + " dịch vụ đã chọn không?",
            "Xác nhận xóa hàng loạt",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            int successCount = 0;
            int failedCount = 0;
            String failReason = "";

            for (int id : selectedIds) {
                if (id != -1) {
                    try {
                        boolean success = Controller.DichVuDAO.deleteDichVu(id);
                        if (success) {
                            successCount++;
                        } else {
                            failedCount++;
                        }
                    } catch (Exception ex) {
                        failedCount++;
                        failReason = ex.getMessage();
                    }
                }
            }

            loadDataToTable();

            if (failedCount > 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "Đã xóa thành công " + successCount + " mục.\nThất bại " + failedCount + " mục (lỗi khóa ngoại hoặc ràng buộc dữ liệu: " + failReason + ").",
                    "Kết quả xóa",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(this, "Đã xóa thành công tất cả các dịch vụ đã chọn!");
            }
        }
    }

    // ================= Service Dialog =================

    class ServiceDialog extends JDialog {
        private JTextField txtTen = new JTextField();
        private JTextField txtMoTa = new JTextField();
        private JTextField txtGiaCuoc = new JTextField("0");
        private JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"Hoạt động", "Ngừng hoạt động"});
        private JButton btnSave = new JButton("Lưu dịch vụ");
        private JButton btnCancel = new JButton("Hủy");
        private boolean isSaveClicked = false;

        public ServiceDialog(Frame owner, String title) {
            super(owner, title, true);
            setSize(420, 400);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());

            JPanel content = new JPanel(new GridBagLayout());
            content.setBackground(Color.WHITE);
            content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(8, 0, 8, 0);
            gbc.weightx = 1.0;

            Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
            Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

            gbc.gridy = 0;
            JLabel lblTen = new JLabel("Tên dịch vụ"); lblTen.setFont(labelFont); content.add(lblTen, gbc);
            gbc.gridy = 1;
            txtTen.setFont(fieldFont); txtTen.setPreferredSize(new Dimension(360, 35)); content.add(txtTen, gbc);

            gbc.gridy = 2;
            JLabel lblMoTa = new JLabel("Mô tả"); lblMoTa.setFont(labelFont); content.add(lblMoTa, gbc);
            gbc.gridy = 3;
            txtMoTa.setFont(fieldFont); txtMoTa.setPreferredSize(new Dimension(360, 35)); content.add(txtMoTa, gbc);

            gbc.gridy = 4;
            JLabel lblGia = new JLabel("Giá cước (đ)"); lblGia.setFont(labelFont); content.add(lblGia, gbc);
            gbc.gridy = 5;
            txtGiaCuoc.setFont(fieldFont); txtGiaCuoc.setPreferredSize(new Dimension(360, 35)); content.add(txtGiaCuoc, gbc);

            gbc.gridy = 6;
            JLabel lblStatus = new JLabel("Trạng thái"); lblStatus.setFont(labelFont); content.add(lblStatus, gbc);
            gbc.gridy = 7;
            cbTrangThai.setFont(fieldFont); cbTrangThai.setPreferredSize(new Dimension(360, 35)); content.add(cbTrangThai, gbc);

            add(content, BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
            footer.setBackground(new Color(248, 250, 252));

            View.Admin.UIUtils.styleButton(btnSave);
            View.Admin.UIUtils.styleButton(btnCancel);

            footer.add(btnCancel);
            footer.add(btnSave);
            add(footer, BorderLayout.SOUTH);

            btnSave.addActionListener(e -> {
                isSaveClicked = true;
                setVisible(false);
            });
            btnCancel.addActionListener(e -> setVisible(false));
        }

        public String getTenDv() { return txtTen.getText().trim(); }
        public void setTenDv(String name) { txtTen.setText(name); }
        public String getMoTa() { return txtMoTa.getText().trim(); }
        public void setMoTa(String desc) { txtMoTa.setText(desc); }
        public double getGiaCuoc() {
            try { return Double.parseDouble(txtGiaCuoc.getText().trim()); } catch (Exception e) { return 0.0; }
        }
        public void setGiaCuoc(double price) { txtGiaCuoc.setText(String.valueOf((long) price)); }
        public int getTrangThai() {
            return cbTrangThai.getSelectedIndex() == 0 ? 1 : 0;
        }
        public void setTrangThai(int status) {
            cbTrangThai.setSelectedIndex(status == 1 ? 0 : 1);
        }
        public boolean isSaveClicked() { return isSaveClicked; }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
}
