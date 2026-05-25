package View.Admin.Voucher;

import Controller.Admin.KhuyenMai.KhuyenMaiDAO;
import View.Admin.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

// ======================= Helper Classes =======================

class LoaiKMItem {
    private int id;
    private String name;
    public LoaiKMItem(int id, String name) { this.id = id; this.name = name; }
    public int getId() { return id; }
    public String getName() { return name; }
    @Override public String toString() { return name; }
}

class StatusBadgeRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String status = value != null ? value.toString() : "Vô hiệu lực";
        JLabel label = new JLabel(status);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setOpaque(true);

        if (isSelected) {
            label.setBackground(table.getSelectionBackground());
        } else {
            label.setBackground(Color.WHITE);
        }

        if (status.contains("Có hiệu lực") || status.contains("hiệu lực") && !status.contains("Vô")) {
            label.setForeground(new Color(21, 128, 61)); // Green
        } else {
            label.setForeground(new Color(185, 28, 28)); // Red
        }
        return label;
    }
}

class ActionPanel extends JPanel {
    public JButton btnEdit = new JButton();
    public ActionPanel() {
        setLayout(new GridBagLayout());
        setOpaque(true);
        setBackground(Color.WHITE);
        btnEdit.setText("Sửa");
        UIUtils.styleButton(btnEdit);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.anchor = GridBagConstraints.CENTER;
        add(btnEdit, gbc);
    }
}

class ActionCellRenderer extends DefaultTableCellRenderer {
    private ActionPanel panel = new ActionPanel();
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) panel.setBackground(table.getSelectionBackground());
        else panel.setBackground(Color.WHITE);
        return panel;
    }
}

class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
    private ActionPanel panel = new ActionPanel();
    public ActionCellEditor(JTable table, Runnable onEdit) {
        panel.btnEdit.addActionListener(e -> {
            stopCellEditing();
            onEdit.run();
        });
    }
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        panel.setBackground(table.getSelectionBackground());
        return panel;
    }
    @Override public Object getCellEditorValue() { return null; }
}

// ======================= Main Panel =======================

public class VoucherPanel extends JPanel {

    private KhuyenMaiDAO dao;
    private DefaultTableModel tableModel;
    private JTable dataTable;
    private JTextField txtSearch;
    private JLabel lblLastUpdate;
    private JLabel lblTitle;
    private JButton btnSwitchMode;
    private JButton btnAdd;
    private JButton btnDeleteSelected;
    
    private boolean isKhuyenMaiMode = true;
    private List<LoaiKMItem> promoTypeList = new ArrayList<>();

    private static final DecimalFormat DF = new DecimalFormat("#,###");
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public VoucherPanel() {
        dao = new KhuyenMaiDAO();
        initUI();
        // Ẩn các nút theo quyền
        btnAdd.setVisible(Controller.Admin.PermissionService.canAdd("Khuyen mai"));
        btnDeleteSelected.setVisible(Controller.Admin.PermissionService.canDelete("Khuyen mai"));
    }

    private void initUI() {
        this.setLayout(new BorderLayout(15, 15));
        this.setBackground(new Color(248, 250, 252));
        this.setBorder(new EmptyBorder(20, 20, 20, 20));

        // ================= HEADER =================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftHeader.setOpaque(false);
        lblTitle = new JLabel("Quản lý khuyến mãi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(15, 23, 42));
        
        lblLastUpdate = new JLabel("Chưa cập nhật");
        lblLastUpdate.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblLastUpdate.setForeground(new Color(148, 163, 184));
        
        leftHeader.add(lblTitle);
        leftHeader.add(lblLastUpdate);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightHeader.setOpaque(false);

        btnSwitchMode = new JButton(" Loại khuyến mãi");
        UIUtils.styleButton(btnSwitchMode);
        btnSwitchMode.setPreferredSize(new Dimension(160, 36));
        btnSwitchMode.addActionListener(e -> toggleMode());

        btnDeleteSelected = new JButton("Xóa");
        UIUtils.styleButton(btnDeleteSelected);
        btnDeleteSelected.setPreferredSize(new Dimension(100, 36));
        btnDeleteSelected.addActionListener(e -> handleDeleteSelected());

        JButton btnRefresh = new JButton("Cập nhật");
        UIUtils.styleButton(btnRefresh);
        btnRefresh.setPreferredSize(new Dimension(130, 36));
        btnRefresh.addActionListener(e -> {
            if (isKhuyenMaiMode) {
                loadPromoTypes();
                loadDataToTable(getSearchKeyword());
            } else {
                loadPromoTypesToTable();
            }
        });

        btnAdd = new JButton("+ Thêm khuyến mãi");
        UIUtils.styleButton(btnAdd);
        btnAdd.setPreferredSize(new Dimension(160, 36));
        btnAdd.addActionListener(e -> {
            if (isKhuyenMaiMode) handleAddVoucher();
            else handleAddPromoType();
        });

        rightHeader.add(btnSwitchMode);
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

        txtSearch = new JTextField("Tìm khuyến mãi...");
        txtSearch.setBorder(null);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtSearch.setForeground(Color.GRAY);
        txtSearch.setSelectionColor(new Color(210, 160, 205));
        txtSearch.setSelectedTextColor(Color.WHITE);
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                String t = txtSearch.getText();
                if (txtSearch.getForeground() == Color.GRAY && (t.equals("Tìm khuyến mãi...") || t.equals("Tìm loại khuyến mãi..."))) {
                    txtSearch.setText("");
                    txtSearch.setForeground(new Color(15, 23, 42));
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText(isKhuyenMaiMode ? "Tìm khuyến mãi..." : "Tìm loại khuyến mãi...");
                    txtSearch.setForeground(Color.GRAY);
                }
            }
        });
        
        txtSearch.addActionListener(e -> {
            if (isKhuyenMaiMode) loadDataToTable(getSearchKeyword());
        });

        searchPanel.add(txtSearch, BorderLayout.CENTER);
        centerPanel.add(searchPanel, BorderLayout.NORTH);

        // Table
        dataTable = new JTable();
        dataTable.setRowHeight(50);
        dataTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dataTable.setBackground(Color.WHITE);
        dataTable.setShowVerticalLines(false);
        dataTable.setShowHorizontalLines(true);
        dataTable.setGridColor(new Color(241, 245, 249));
        dataTable.setSelectionBackground(new Color(245, 235, 250));
        dataTable.setSelectionForeground(new Color(142, 68, 173));

        updateTableStructure();

        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        this.add(centerPanel, BorderLayout.CENTER);

        // Initial load
        loadPromoTypes();
        loadDataToTable(null);
    }

    private void updateTableStructure() {
        if (isKhuyenMaiMode) {
            String[] cols = {"", "Mã KM", "Tên KM", "Loại", "Giá trị (%)", "Ràng buộc", "Bắt đầu", "Kết thúc", "Trạng thái", "Thao tác"};
            tableModel = new DefaultTableModel(cols, 0) {
                @Override public Class<?> getColumnClass(int c) {
                    if (c == 0) return Boolean.class;
                    return super.getColumnClass(c);
                }
                @Override public boolean isCellEditable(int r, int c) {
                    return c == 0 || c == 9;
                }
            };
            dataTable.setModel(tableModel);

            // Widths
            dataTable.getColumnModel().getColumn(0).setPreferredWidth(40);
            dataTable.getColumnModel().getColumn(0).setMaxWidth(40);
            dataTable.getColumnModel().getColumn(1).setPreferredWidth(60);
            dataTable.getColumnModel().getColumn(1).setMaxWidth(80);
            dataTable.getColumnModel().getColumn(2).setPreferredWidth(150);
            dataTable.getColumnModel().getColumn(9).setPreferredWidth(80);
            dataTable.getColumnModel().getColumn(9).setMaxWidth(100);

            // Renderers
            DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
            centerR.setHorizontalAlignment(SwingConstants.CENTER);
            dataTable.getColumnModel().getColumn(1).setCellRenderer(centerR);
            dataTable.getColumnModel().getColumn(3).setCellRenderer(centerR);
            dataTable.getColumnModel().getColumn(4).setCellRenderer(centerR);
            dataTable.getColumnModel().getColumn(5).setCellRenderer(centerR);
            dataTable.getColumnModel().getColumn(6).setCellRenderer(centerR);
            dataTable.getColumnModel().getColumn(7).setCellRenderer(centerR);
            dataTable.getColumnModel().getColumn(8).setCellRenderer(new StatusBadgeRenderer());

            // Action Editor & Renderer
            ActionCellEditor actionEditor = new ActionCellEditor(dataTable, () -> handleEditVoucher());
            dataTable.getColumnModel().getColumn(9).setCellRenderer(new ActionCellRenderer());
            dataTable.getColumnModel().getColumn(9).setCellEditor(actionEditor);

        } else {
            String[] cols = {"", "Mã Loại", "Tên Loại", "Mô Tả", "Thao tác"};
            tableModel = new DefaultTableModel(cols, 0) {
                @Override public Class<?> getColumnClass(int c) {
                    if (c == 0) return Boolean.class;
                    return super.getColumnClass(c);
                }
                @Override public boolean isCellEditable(int r, int c) {
                    return c == 0 || c == 4;
                }
            };
            dataTable.setModel(tableModel);

            // Widths
            dataTable.getColumnModel().getColumn(0).setPreferredWidth(40);
            dataTable.getColumnModel().getColumn(0).setMaxWidth(40);
            dataTable.getColumnModel().getColumn(1).setPreferredWidth(80);
            dataTable.getColumnModel().getColumn(1).setMaxWidth(100);
            dataTable.getColumnModel().getColumn(4).setPreferredWidth(80);
            dataTable.getColumnModel().getColumn(4).setMaxWidth(100);

            DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
            centerR.setHorizontalAlignment(SwingConstants.CENTER);
            dataTable.getColumnModel().getColumn(1).setCellRenderer(centerR);

            // Action Editor & Renderer
            ActionCellEditor actionEditor = new ActionCellEditor(dataTable, () -> handleEditPromoType());
            dataTable.getColumnModel().getColumn(4).setCellRenderer(new ActionCellRenderer());
            dataTable.getColumnModel().getColumn(4).setCellEditor(actionEditor);
        }

        dataTable.getTableHeader().setPreferredSize(new Dimension(0, 45));
        dataTable.getTableHeader().setBackground(Color.WHITE);
        dataTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        dataTable.getTableHeader().setForeground(new Color(100, 116, 139));
        dataTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
    }

    private void toggleMode() {
        isKhuyenMaiMode = !isKhuyenMaiMode;
        if (isKhuyenMaiMode) {
            lblTitle.setText("Quản lý khuyến mãi");
            btnSwitchMode.setText(" Loại khuyến mãi");
            btnAdd.setText("+ Thêm khuyến mãi");
            txtSearch.setText("Tìm khuyến mãi...");
            txtSearch.setForeground(Color.GRAY);
            updateTableStructure();
            loadDataToTable(null);
        } else {
            lblTitle.setText("Quản lý loại khuyến mãi");
            btnSwitchMode.setText(" Khuyến mãi");
            btnAdd.setText("+ Thêm loại khuyến mãi");
            txtSearch.setText("Tìm loại khuyến mãi...");
            txtSearch.setForeground(Color.GRAY);
            updateTableStructure();
            loadPromoTypesToTable();
        }
        this.revalidate();
        this.repaint();
    }

    private String getSearchKeyword() {
        if (txtSearch.getForeground() == Color.GRAY) return null;
        String t = txtSearch.getText().trim();
        return t.isEmpty() ? null : t;
    }

    private void loadPromoTypes() {
        promoTypeList.clear();
        try {
            List<Map<String, Object>> types = dao.getAllPromoTypes();
            for (Map<String, Object> t : types) {
                promoTypeList.add(new LoaiKMItem((int) t.get("MA_LOAI_KM"), (String) t.get("TEN_LOAI_KM")));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadDataToTable(String keyword) {
        tableModel.setRowCount(0);
        updateLastUpdateTime();
        try {
            List<Map<String, Object>> list = dao.getAllPromotions(keyword);
            for (Map<String, Object> r : list) {
                Timestamp bd = (Timestamp) r.get("NGAY_BAT_DAU");
                Timestamp kt = (Timestamp) r.get("NGAY_KET_THUC");
                tableModel.addRow(new Object[]{
                    Boolean.FALSE,
                    r.get("MA_KM"),
                    r.get("TEN_KM"),
                    r.get("TEN_LOAI_KM"),
                    r.get("GIA_TRI") + "%",
                    r.get("RANG_BUOC") != null ? r.get("RANG_BUOC") : "",
                    bd != null ? SDF.format(bd) : "",
                    kt != null ? SDF.format(kt) : "",
                    r.get("TRANG_THAI"),
                    r.get("MA_KM")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage());
        }
    }

    private void loadPromoTypesToTable() {
        tableModel.setRowCount(0);
        updateLastUpdateTime();
        try {
            List<Map<String, Object>> types = dao.getAllPromoTypes();
            for (Map<String, Object> t : types) {
                tableModel.addRow(new Object[]{
                    Boolean.FALSE,
                    t.get("MA_LOAI_KM"),
                    t.get("TEN_LOAI_KM"),
                    t.get("MO_TA") != null ? t.get("MO_TA") : "",
                    t.get("MA_LOAI_KM")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateLastUpdateTime() {
        String time = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));
        if (lblLastUpdate != null) {
            lblLastUpdate.setText("Cập nhật lúc: " + time);
        }
    }

    // ================== Action Handlers ==================

    private void handleAddVoucher() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        Frame frame = parent instanceof Frame ? (Frame) parent : null;
        VoucherDialog dialog = new VoucherDialog(frame, "Thêm Khuyến Mãi", promoTypeList);
        dialog.setVisible(true);

        if (dialog.isSaveClicked()) {
            try {
                LoaiKMItem loai = dialog.getSelectedCategory();
                String ten = dialog.getTenKM();
                long giaTri = dialog.getGiaTri();
                String rangBuoc = dialog.getRangBuoc();
                Timestamp bd = new Timestamp(dialog.getNgayBD().getTime());
                Timestamp kt = new Timestamp(dialog.getNgayKT().getTime());
                String trangThai = dialog.getTrangThai();

                int newId = dao.addPromotion(loai.getId(), ten, giaTri, rangBuoc, bd, kt, trangThai);
                if (newId > 0) {
                    JOptionPane.showMessageDialog(this, "Thêm thành công!");
                    loadDataToTable(null);
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi thêm!");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEditVoucher() {
        int row = dataTable.getSelectedRow();
        if (row == -1) return;
        int modelRow = dataTable.convertRowIndexToModel(row);

        int id = (int) tableModel.getValueAt(modelRow, 1);
        String ten = (String) tableModel.getValueAt(modelRow, 2);
        String tenLoai = (String) tableModel.getValueAt(modelRow, 3);
        String giaTriStr = ((String) tableModel.getValueAt(modelRow, 4)).replace("%", "");
        String rangBuoc = (String) tableModel.getValueAt(modelRow, 5);
        String bdStr = (String) tableModel.getValueAt(modelRow, 6);
        String ktStr = (String) tableModel.getValueAt(modelRow, 7);
        String tt = (String) tableModel.getValueAt(modelRow, 8);

        Window parent = SwingUtilities.getWindowAncestor(this);
        Frame frame = parent instanceof Frame ? (Frame) parent : null;
        VoucherDialog dialog = new VoucherDialog(frame, "Sửa Khuyến Mãi", promoTypeList);

        dialog.setTenKM(ten);
        dialog.setGiaTri(giaTriStr);
        dialog.setRangBuoc(rangBuoc);
        dialog.setTrangThai(tt);

        for (LoaiKMItem item : promoTypeList) {
            if (item.getName().equalsIgnoreCase(tenLoai)) {
                dialog.setSelectedCategory(item.getId());
                break;
            }
        }

        try {
            if (!bdStr.isEmpty()) dialog.setNgayBD(SDF.parse(bdStr));
            if (!ktStr.isEmpty()) dialog.setNgayKT(SDF.parse(ktStr));
        } catch (Exception ignored) {}

        dialog.setVisible(true);

        if (dialog.isSaveClicked()) {
            try {
                LoaiKMItem loai = dialog.getSelectedCategory();
                String newTen = dialog.getTenKM();
                long giaTri = dialog.getGiaTri();
                String newRangBuoc = dialog.getRangBuoc();
                Timestamp bd = new Timestamp(dialog.getNgayBD().getTime());
                Timestamp kt = new Timestamp(dialog.getNgayKT().getTime());
                String trangThai = dialog.getTrangThai();

                dao.updatePromotion(id, loai.getId(), newTen, giaTri, newRangBuoc, bd, kt, trangThai);
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadDataToTable(null);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleAddPromoType() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        Frame frame = parent instanceof Frame ? (Frame) parent : null;
        PromoTypeDialog dialog = new PromoTypeDialog(frame, "Thêm Loại Khuyến Mãi");
        dialog.setVisible(true);

        if (dialog.isSaveClicked()) {
            try {
                boolean ok = dao.addPromoType(dialog.getTenLoai(), dialog.getMoTa());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Thêm thành công!");
                    loadPromoTypesToTable();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEditPromoType() {
        int row = dataTable.getSelectedRow();
        if (row == -1) return;
        int modelRow = dataTable.convertRowIndexToModel(row);

        int id = (int) tableModel.getValueAt(modelRow, 1);
        String ten = (String) tableModel.getValueAt(modelRow, 2);
        String moTa = (String) tableModel.getValueAt(modelRow, 3);

        Window parent = SwingUtilities.getWindowAncestor(this);
        Frame frame = parent instanceof Frame ? (Frame) parent : null;
        PromoTypeDialog dialog = new PromoTypeDialog(frame, "Sửa Loại Khuyến Mãi");
        dialog.setTenLoai(ten);
        dialog.setMoTa(moTa);
        dialog.setVisible(true);

        if (dialog.isSaveClicked()) {
            try {
                boolean ok = dao.updatePromoType(id, dialog.getTenLoai(), dialog.getMoTa());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                    loadPromoTypesToTable();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteSelected() {
        List<Integer> idsToDelete = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean checked = (Boolean) tableModel.getValueAt(i, 0);
            if (checked != null && checked) {
                idsToDelete.add((int) tableModel.getValueAt(i, 1));
            }
        }

        if (idsToDelete.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất 1 mục để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc muốn xóa " + idsToDelete.size() + " mục đã chọn?", 
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            int successCount = 0;
            for (int id : idsToDelete) {
                try {
                    if (isKhuyenMaiMode) {
                        dao.deletePromotion(id);
                    } else {
                        dao.deletePromoType(id);
                    }
                    successCount++;
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi xóa ID " + id + ": " + e.getMessage());
                }
            }
            JOptionPane.showMessageDialog(this, "Đã xóa thành công " + successCount + " mục.");
            if (isKhuyenMaiMode) loadDataToTable(null);
            else loadPromoTypesToTable();
        }
    }

    // ================== DIALOG CLASSES ==================

    class VoucherDialog extends JDialog {
        private JTextField txtTenKM = new JTextField();
        private JComboBox<LoaiKMItem> cbLoaiKM = new JComboBox<>();
        private JTextField txtGiaTri = new JTextField();
        private JTextField txtRangBuoc = new JTextField();
        private JSpinner spNgayBD = new JSpinner(new SpinnerDateModel());
        private JSpinner spNgayKT = new JSpinner(new SpinnerDateModel());
        private JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"Có hiệu lực", "Vô hiệu lực"});
        private JButton btnSave = new JButton("Lưu");
        private JButton btnCancel = new JButton("Hủy");
        private boolean isSaveClicked = false;

        public VoucherDialog(Frame owner, String title, List<LoaiKMItem> categories) {
            super(owner, title, true);
            setSize(450, 500);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());

            JPanel content = new JPanel(new GridBagLayout());
            content.setBackground(Color.WHITE);
            content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(8, 5, 8, 5);
            gbc.weightx = 1.0;

            for (LoaiKMItem cat : categories) {
                cbLoaiKM.addItem(cat);
            }

            spNgayBD.setEditor(new JSpinner.DateEditor(spNgayBD, "dd/MM/yyyy HH:mm"));
            spNgayKT.setEditor(new JSpinner.DateEditor(spNgayKT, "dd/MM/yyyy HH:mm"));

            Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
            Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

            int row = 0;
            gbc.gridy = row++; gbc.gridwidth = 2;
            JLabel lbl1 = new JLabel("Loại Khuyến Mãi"); lbl1.setFont(labelFont); content.add(lbl1, gbc);
            gbc.gridy = row++; cbLoaiKM.setFont(fieldFont); cbLoaiKM.setPreferredSize(new Dimension(0, 35)); content.add(cbLoaiKM, gbc);

            gbc.gridy = row++; 
            JLabel lbl2 = new JLabel("Tên Khuyến Mãi"); lbl2.setFont(labelFont); content.add(lbl2, gbc);
            gbc.gridy = row++; txtTenKM.setFont(fieldFont); txtTenKM.setPreferredSize(new Dimension(0, 35)); content.add(txtTenKM, gbc);

            gbc.gridy = row++;
            JLabel lbl3 = new JLabel("Giá Trị (%)"); lbl3.setFont(labelFont); content.add(lbl3, gbc);
            gbc.gridy = row++; txtGiaTri.setFont(fieldFont); txtGiaTri.setPreferredSize(new Dimension(0, 35)); content.add(txtGiaTri, gbc);

            gbc.gridy = row++;
            JLabel lbl4 = new JLabel("Ràng Buộc Tối Đa (VNĐ)"); lbl4.setFont(labelFont); content.add(lbl4, gbc);
            gbc.gridy = row++; txtRangBuoc.setFont(fieldFont); txtRangBuoc.setPreferredSize(new Dimension(0, 35)); content.add(txtRangBuoc, gbc);

            gbc.gridy = row; gbc.gridwidth = 1;
            JLabel lbl5 = new JLabel("Ngày BĐ"); lbl5.setFont(labelFont); content.add(lbl5, gbc);
            gbc.gridx = 1; JLabel lbl6 = new JLabel("Ngày KT"); lbl6.setFont(labelFont); content.add(lbl6, gbc);
            
            row++;
            gbc.gridx = 0; gbc.gridy = row; spNgayBD.setFont(fieldFont); spNgayBD.setPreferredSize(new Dimension(180, 35)); content.add(spNgayBD, gbc);
            gbc.gridx = 1; spNgayKT.setFont(fieldFont); spNgayKT.setPreferredSize(new Dimension(180, 35)); content.add(spNgayKT, gbc);

            row++;
            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
            JLabel lbl7 = new JLabel("Trạng Thái"); lbl7.setFont(labelFont); content.add(lbl7, gbc);
            row++;
            gbc.gridy = row; cbTrangThai.setFont(fieldFont); cbTrangThai.setPreferredSize(new Dimension(0, 35)); content.add(cbTrangThai, gbc);

            JScrollPane scrollPane = new JScrollPane(content);
            scrollPane.setBorder(null);
            add(scrollPane, BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
            footer.setBackground(new Color(248, 250, 252));

            UIUtils.styleButton(btnSave);
            UIUtils.styleButton(btnCancel);
            
            footer.add(btnCancel);
            footer.add(btnSave);
            add(footer, BorderLayout.SOUTH);

            btnSave.addActionListener(e -> {
                if (txtTenKM.getText().trim().isEmpty() || txtGiaTri.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ tên và giá trị!");
                    return;
                }
                try { Long.parseLong(txtGiaTri.getText().trim()); } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Giá trị phải là số hợp lệ!"); return;
                }
                Date bd = (Date) spNgayBD.getValue();
                Date kt = (Date) spNgayKT.getValue();
                if (kt.before(bd)) {
                    JOptionPane.showMessageDialog(this, "Ngày kết thúc phải sau ngày bắt đầu!"); return;
                }
                isSaveClicked = true;
                setVisible(false);
            });
            btnCancel.addActionListener(e -> setVisible(false));
        }

        public LoaiKMItem getSelectedCategory() { return (LoaiKMItem) cbLoaiKM.getSelectedItem(); }
        public void setSelectedCategory(int ma) {
            for (int i = 0; i < cbLoaiKM.getItemCount(); i++) {
                if (cbLoaiKM.getItemAt(i).getId() == ma) {
                    cbLoaiKM.setSelectedIndex(i); break;
                }
            }
        }
        public String getTenKM() { return txtTenKM.getText().trim(); }
        public void setTenKM(String v) { txtTenKM.setText(v); }
        public long getGiaTri() { return Long.parseLong(txtGiaTri.getText().trim()); }
        public void setGiaTri(String v) { txtGiaTri.setText(v); }
        public String getRangBuoc() { return txtRangBuoc.getText().trim(); }
        public void setRangBuoc(String v) { txtRangBuoc.setText(v); }
        public Date getNgayBD() { return (Date) spNgayBD.getValue(); }
        public void setNgayBD(Date d) { spNgayBD.setValue(d); }
        public Date getNgayKT() { return (Date) spNgayKT.getValue(); }
        public void setNgayKT(Date d) { spNgayKT.setValue(d); }
        public String getTrangThai() { return cbTrangThai.getSelectedItem().toString(); }
        public void setTrangThai(String v) { cbTrangThai.setSelectedItem(v); }
        public boolean isSaveClicked() { return isSaveClicked; }
    }

    class PromoTypeDialog extends JDialog {
        private JTextField txtTen = new JTextField();
        private JTextField txtMoTa = new JTextField();
        private JButton btnSave = new JButton("Lưu");
        private JButton btnCancel = new JButton("Hủy");
        private boolean isSaveClicked = false;

        public PromoTypeDialog(Frame owner, String title) {
            super(owner, title, true);
            setSize(400, 300);
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

            gbc.gridy = 0; JLabel lbl1 = new JLabel("Tên Loại"); lbl1.setFont(labelFont); content.add(lbl1, gbc);
            gbc.gridy = 1; txtTen.setFont(fieldFont); txtTen.setPreferredSize(new Dimension(0, 35)); content.add(txtTen, gbc);

            gbc.gridy = 2; JLabel lbl2 = new JLabel("Mô Tả"); lbl2.setFont(labelFont); content.add(lbl2, gbc);
            gbc.gridy = 3; txtMoTa.setFont(fieldFont); txtMoTa.setPreferredSize(new Dimension(0, 35)); content.add(txtMoTa, gbc);

            add(content, BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
            footer.setBackground(new Color(248, 250, 252));
            UIUtils.styleButton(btnSave);
            UIUtils.styleButton(btnCancel);
            footer.add(btnCancel); footer.add(btnSave);
            add(footer, BorderLayout.SOUTH);

            btnSave.addActionListener(e -> {
                if (txtTen.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Tên không được trống!"); return;
                }
                isSaveClicked = true;
                setVisible(false);
            });
            btnCancel.addActionListener(e -> setVisible(false));
        }

        public String getTenLoai() { return txtTen.getText().trim(); }
        public void setTenLoai(String v) { txtTen.setText(v); }
        public String getMoTa() { return txtMoTa.getText().trim(); }
        public void setMoTa(String v) { txtMoTa.setText(v); }
        public boolean isSaveClicked() { return isSaveClicked; }
    }
}
