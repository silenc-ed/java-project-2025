package View.Admin.Voucher;

import Controller.Admin.KhuyenMai.KhuyenMaiDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * Panel quản lý khuyến mãi — CRUD
 */
public class VoucherPanel extends javax.swing.JPanel {

    private KhuyenMaiDAO dao;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch;

    // Form fields
    private JTextField txtTenKM, txtGiaTri, txtRangBuoc;
    private JComboBox<LoaiKMItem> cbLoaiKM;
    private JComboBox<String> cbTrangThai;
    private JSpinner spNgayBD, spNgayKT;
    private int selectedMaKM = -1;

    private static final DecimalFormat DF = new DecimalFormat("#,###");
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final Color PURPLE = new Color(142, 68, 173);
    private static final Color PURPLE_LIGHT = new Color(175, 122, 197);
    private static final Color BG = new Color(248, 250, 252);

    public VoucherPanel() {
        dao = new KhuyenMaiDAO();
        initUI();
        loadData(null);
    }

    private void initUI() {
        this.setLayout(new BorderLayout(0, 0));
        this.setBackground(BG);

        // Header
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, PURPLE_LIGHT),
            new EmptyBorder(14, 20, 14, 20)
        ));
        JLabel lblTitle = new JLabel("Quản lý khuyến mãi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(30, 41, 59));
        header.add(lblTitle, BorderLayout.WEST);

        // Search
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setOpaque(false);
        searchPanel.setPreferredSize(new Dimension(280, 35));
        txtSearch = new JTextField("Tìm khuyến mãi...");
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setForeground(Color.GRAY);
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (txtSearch.getForeground() == Color.GRAY) { txtSearch.setText(""); txtSearch.setForeground(new Color(30, 41, 59)); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) { txtSearch.setText("Tìm khuyến mãi..."); txtSearch.setForeground(Color.GRAY); }
            }
        });
        txtSearch.addActionListener(e -> loadData(getSearchKeyword()));
        JButton btnSearch = new JButton("🔍");
        btnSearch.setPreferredSize(new Dimension(40, 35));
        btnSearch.setBackground(new Color(0, 123, 255));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setBorderPainted(false);
        btnSearch.setFocusPainted(false);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearch.addActionListener(e -> loadData(getSearchKeyword()));
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.add(btnSearch, BorderLayout.EAST);
        header.add(searchPanel, BorderLayout.EAST);
        this.add(header, BorderLayout.NORTH);

        // Split: Left = Table, Right = Form
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(580);
        splitPane.setResizeWeight(0.65);
        splitPane.setBorder(null);

        // LEFT: Table
        splitPane.setLeftComponent(buildTablePanel());

        // RIGHT: Form
        splitPane.setRightComponent(buildFormPanel());

        this.add(splitPane, BorderLayout.CENTER);
    }

    // ======================== TABLE ========================

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(15, 20, 15, 5));

        String[] cols = {"Mã KM", "Tên KM", "Loại", "Giá trị (%)", "Ràng buộc", "Bắt đầu", "Kết thúc", "Trạng thái"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(245, 235, 250));
        table.setSelectionForeground(PURPLE);
        table.setIntercellSpacing(new Dimension(0, 1));

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(248, 248, 252));
        table.getTableHeader().setForeground(new Color(100, 100, 130));
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));

        table.setRowSorter(new TableRowSorter<>(tableModel));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(0).setMaxWidth(65);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(75);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);

        // Center
        DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
        centerR.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerR);
        table.getColumnModel().getColumn(3).setCellRenderer(centerR);
        table.getColumnModel().getColumn(4).setCellRenderer(centerR);
        table.getColumnModel().getColumn(5).setCellRenderer(centerR);
        table.getColumnModel().getColumn(6).setCellRenderer(centerR);

        // Status badge
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lbl.setOpaque(true);
                String status = v != null ? v.toString() : "";
                if (!s) {
                    if (status.contains("Có hiệu lực") || status.contains("hiệu lực") && !status.contains("Vô")) {
                        lbl.setForeground(new Color(5, 122, 85));
                        lbl.setBackground(new Color(220, 252, 231));
                    } else {
                        lbl.setForeground(new Color(185, 28, 28));
                        lbl.setBackground(new Color(254, 226, 226));
                    }
                }
                return lbl;
            }
        });

        // Row selection → fill form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0) fillFormFromTable(table.convertRowIndexToModel(row));
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        sp.getViewport().setBackground(Color.WHITE);
        panel.add(sp, BorderLayout.CENTER);

        return panel;
    }

    // ======================== FORM ========================

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 2, 0, 0, PURPLE_LIGHT),
            new EmptyBorder(15, 15, 15, 20)
        ));

        // Form title
        JLabel lblForm = new JLabel("Thông tin khuyến mãi");
        lblForm.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblForm.setForeground(PURPLE);
        lblForm.setBorder(new EmptyBorder(0, 0, 12, 0));
        panel.add(lblForm, BorderLayout.NORTH);

        // Form fields
        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 5, 5, 5);

        int row = 0;

        // Loại KM
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        formGrid.add(makeLabel("Loại KM:"), g);
        g.gridx = 1; g.weightx = 1;
        cbLoaiKM = new JComboBox<>();
        cbLoaiKM.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbLoaiKM.setPreferredSize(new Dimension(0, 32));
        formGrid.add(cbLoaiKM, g);
        loadPromoTypes();

        // Tên KM
        row++;
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        formGrid.add(makeLabel("Tên KM:"), g);
        g.gridx = 1; g.weightx = 1;
        txtTenKM = makeTextField();
        formGrid.add(txtTenKM, g);

        // Giá trị (%)
        row++;
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        formGrid.add(makeLabel("Giá trị (%):"), g);
        g.gridx = 1; g.weightx = 1;
        txtGiaTri = makeTextField();
        formGrid.add(txtGiaTri, g);

        // Ràng buộc
        row++;
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        formGrid.add(makeLabel("Ràng buộc:"), g);
        g.gridx = 1; g.weightx = 1;
        txtRangBuoc = makeTextField();
        txtRangBuoc.setToolTipText("Số tiền tối đa áp dụng KM (VD: 5000000)");
        formGrid.add(txtRangBuoc, g);

        // Ngày bắt đầu
        row++;
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        formGrid.add(makeLabel("Ngày BĐ:"), g);
        g.gridx = 1; g.weightx = 1;
        spNgayBD = new JSpinner(new SpinnerDateModel());
        spNgayBD.setEditor(new JSpinner.DateEditor(spNgayBD, "dd/MM/yyyy HH:mm"));
        spNgayBD.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formGrid.add(spNgayBD, g);

        // Ngày kết thúc
        row++;
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        formGrid.add(makeLabel("Ngày KT:"), g);
        g.gridx = 1; g.weightx = 1;
        spNgayKT = new JSpinner(new SpinnerDateModel());
        spNgayKT.setEditor(new JSpinner.DateEditor(spNgayKT, "dd/MM/yyyy HH:mm"));
        spNgayKT.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formGrid.add(spNgayKT, g);

        // Trạng thái
        row++;
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        formGrid.add(makeLabel("Trạng thái:"), g);
        g.gridx = 1; g.weightx = 1;
        cbTrangThai = new JComboBox<>(new String[]{"Vô hiệu lực", "Có hiệu lực"});
        cbTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbTrangThai.setPreferredSize(new Dimension(0, 32));
        formGrid.add(cbTrangThai, g);

        // Spacer
        row++;
        g.gridx = 0; g.gridy = row; g.weighty = 1; g.gridwidth = 2;
        formGrid.add(Box.createVerticalGlue(), g);

        panel.add(formGrid, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 4, 8, 0));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(12, 0, 0, 0));

        JButton btnAdd = makeButton("Thêm", new Color(40, 167, 69));
        JButton btnUpdate = makeButton("Sửa", new Color(0, 123, 255));
        JButton btnDelete = makeButton("Xóa", new Color(220, 53, 69));
        JButton btnClear = makeButton("Mới", new Color(148, 163, 184));

        btnAdd.addActionListener(e -> addPromotion());
        btnUpdate.addActionListener(e -> updatePromotion());
        btnDelete.addActionListener(e -> deletePromotion());
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ======================== DATA ========================

    private void loadData(String keyword) {
        tableModel.setRowCount(0);
        try {
            List<Map<String, Object>> list = dao.getAllPromotions(keyword);
            for (Map<String, Object> r : list) {
                Timestamp bd = (Timestamp) r.get("NGAY_BAT_DAU");
                Timestamp kt = (Timestamp) r.get("NGAY_KET_THUC");
                tableModel.addRow(new Object[]{
                    r.get("MA_KM"),
                    r.get("TEN_KM"),
                    r.get("TEN_LOAI_KM"),
                    r.get("GIA_TRI") + "%",
                    r.get("RANG_BUOC") != null ? r.get("RANG_BUOC") : "",
                    bd != null ? SDF.format(bd) : "",
                    kt != null ? SDF.format(kt) : "",
                    r.get("TRANG_THAI")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage());
        }
    }

    private void loadPromoTypes() {
        try {
            List<Map<String, Object>> types = dao.getAllPromoTypes();
            for (Map<String, Object> t : types) {
                cbLoaiKM.addItem(new LoaiKMItem((int) t.get("MA_LOAI_KM"), (String) t.get("TEN_LOAI_KM")));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void fillFormFromTable(int modelRow) {
        selectedMaKM = (int) tableModel.getValueAt(modelRow, 0);
        txtTenKM.setText((String) tableModel.getValueAt(modelRow, 1));

        // Set loại KM
        String loai = (String) tableModel.getValueAt(modelRow, 2);
        for (int i = 0; i < cbLoaiKM.getItemCount(); i++) {
            if (cbLoaiKM.getItemAt(i).name.equals(loai)) { cbLoaiKM.setSelectedIndex(i); break; }
        }

        // Giá trị
        String giaTriStr = ((String) tableModel.getValueAt(modelRow, 3)).replace("%", "");
        txtGiaTri.setText(giaTriStr);

        // Ràng buộc
        txtRangBuoc.setText(tableModel.getValueAt(modelRow, 4) != null ? tableModel.getValueAt(modelRow, 4).toString() : "");

        // Ngày
        try {
            String bdStr = (String) tableModel.getValueAt(modelRow, 5);
            String ktStr = (String) tableModel.getValueAt(modelRow, 6);
            if (!bdStr.isEmpty()) spNgayBD.setValue(SDF.parse(bdStr));
            if (!ktStr.isEmpty()) spNgayKT.setValue(SDF.parse(ktStr));
        } catch (Exception ignored) {}

        // Trạng thái
        String tt = (String) tableModel.getValueAt(modelRow, 7);
        cbTrangThai.setSelectedItem(tt);
    }

    private void addPromotion() {
        try {
            validateForm();
            LoaiKMItem loai = (LoaiKMItem) cbLoaiKM.getSelectedItem();
            long giaTri = Long.parseLong(txtGiaTri.getText().trim());
            Timestamp bd = new Timestamp(((java.util.Date) spNgayBD.getValue()).getTime());
            Timestamp kt = new Timestamp(((java.util.Date) spNgayKT.getValue()).getTime());

            int newId = dao.addPromotion(loai.id, txtTenKM.getText().trim(), giaTri,
                    txtRangBuoc.getText().trim(), bd, kt, (String) cbTrangThai.getSelectedItem());

            JOptionPane.showMessageDialog(this, "Thêm KM #" + newId + " thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadData(getSearchKeyword());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePromotion() {
        if (selectedMaKM <= 0) {
            JOptionPane.showMessageDialog(this, "Chọn 1 KM để sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            validateForm();
            LoaiKMItem loai = (LoaiKMItem) cbLoaiKM.getSelectedItem();
            long giaTri = Long.parseLong(txtGiaTri.getText().trim());
            Timestamp bd = new Timestamp(((java.util.Date) spNgayBD.getValue()).getTime());
            Timestamp kt = new Timestamp(((java.util.Date) spNgayKT.getValue()).getTime());

            dao.updatePromotion(selectedMaKM, loai.id, txtTenKM.getText().trim(), giaTri,
                    txtRangBuoc.getText().trim(), bd, kt, (String) cbTrangThai.getSelectedItem());

            JOptionPane.showMessageDialog(this, "Cập nhật KM #" + selectedMaKM + " thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadData(getSearchKeyword());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePromotion() {
        if (selectedMaKM <= 0) {
            JOptionPane.showMessageDialog(this, "Chọn 1 KM để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa KM #" + selectedMaKM + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            dao.deletePromotion(selectedMaKM);
            JOptionPane.showMessageDialog(this, "Đã xóa!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadData(getSearchKeyword());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void validateForm() throws Exception {
        if (txtTenKM.getText().trim().isEmpty()) throw new Exception("Tên KM không được trống!");
        if (txtGiaTri.getText().trim().isEmpty()) throw new Exception("Giá trị không được trống!");
        try { Long.parseLong(txtGiaTri.getText().trim()); } catch (NumberFormatException e) { throw new Exception("Giá trị phải là số!"); }
        if (cbLoaiKM.getSelectedItem() == null) throw new Exception("Chọn loại KM!");
        java.util.Date bd = (java.util.Date) spNgayBD.getValue();
        java.util.Date kt = (java.util.Date) spNgayKT.getValue();
        if (kt.before(bd)) throw new Exception("Ngày kết thúc phải sau ngày bắt đầu!");
    }

    private void clearForm() {
        selectedMaKM = -1;
        txtTenKM.setText("");
        txtGiaTri.setText("");
        txtRangBuoc.setText("");
        cbTrangThai.setSelectedIndex(0);
        if (cbLoaiKM.getItemCount() > 0) cbLoaiKM.setSelectedIndex(0);
        spNgayBD.setValue(new java.util.Date());
        spNgayKT.setValue(new java.util.Date());
        table.clearSelection();
    }

    private String getSearchKeyword() {
        if (txtSearch.getForeground() == Color.GRAY) return null;
        String t = txtSearch.getText().trim();
        return t.isEmpty() ? null : t;
    }

    // ======================== UI HELPERS ========================

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(new Color(60, 60, 80));
        return l;
    }

    private JTextField makeTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setPreferredSize(new Dimension(0, 32));
        return tf;
    }

    private JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 36));
        return btn;
    }

    // ======================== INNER ========================

    static class LoaiKMItem {
        int id; String name;
        LoaiKMItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }
}
