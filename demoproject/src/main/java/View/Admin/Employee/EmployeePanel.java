package View.Admin.Employee;

import Controller.Admin.NhanVienDAO;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class EmployeePanel extends javax.swing.JPanel {

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch;
    private JLabel lblLastUpdate;

    // Lớp hỗ trợ cho ComboBox Vai trò
    private static class RoleItem {
        long id;
        String name;
        RoleItem(long id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    public EmployeePanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250));

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(new Color(245, 246, 250));
        wrapper.setBorder(new EmptyBorder(20, 24, 20, 24));

        wrapper.add(buildHeader());
        wrapper.add(Box.createVerticalStrut(16));
        wrapper.add(buildTablePanel());

        add(wrapper, BorderLayout.CENTER);
        loadData();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JLabel title = new JLabel("Quản lý nhân viên");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(30, 41, 59));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        txtSearch = new JTextField(18);
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm theo mã NV, tên, SĐT, CCCD, email...");
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(280, 34));
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { searchData(); }
        });

        JButton btnRefresh = new JButton("↻ Cập nhật");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.setBackground(new Color(5, 150, 105));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadData());

        JButton btnAdd = new JButton("+ Thêm nhân viên");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdd.setBackground(new Color(37, 99, 235));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setBorder(new EmptyBorder(8, 18, 8, 18));
        btnAdd.setFocusPainted(false);
        btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> showAddDialog());

        lblLastUpdate = new JLabel("Chưa cập nhật");
        lblLastUpdate.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblLastUpdate.setForeground(new Color(148, 163, 184));

        right.add(txtSearch);
        right.add(btnRefresh);
        right.add(btnAdd);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(title);
        left.add(lblLastUpdate);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(0, 0, 0, 0)));

        String[] cols = {"Mã NV", "Họ tên", "SĐT", "CCCD", "Vai trò", "Lương CB", "Trạng thái TK"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(new Color(30, 41, 59));

        JTableHeader hdr = table.getTableHeader();
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 12));
        hdr.setBackground(new Color(248, 250, 252));
        hdr.setForeground(new Color(71, 85, 105));
        hdr.setBorder(new MatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
        hdr.setPreferredSize(new Dimension(0, 40));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(6).setPreferredWidth(120);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row < 0) return;
                long maNv = ((Number) tableModel.getValueAt(row, 0)).longValue();
                showDetailDialog(maNv);
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void loadData() {
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override protected List<Object[]> doInBackground() {
                return NhanVienDAO.getAllNhanVien();
            }
            @Override protected void done() {
                try {
                    populateTable(get());
                    String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));
                    lblLastUpdate.setText("Cập nhật lúc: " + time);
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void searchData() {
        String kw = txtSearch.getText().trim();
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override protected List<Object[]> doInBackground() {
                return kw.isEmpty() ? NhanVienDAO.getAllNhanVien() : NhanVienDAO.timKiemNhanVien(kw);
            }
            @Override protected void done() {
                try { populateTable(get()); } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void populateTable(List<Object[]> list) {
        tableModel.setRowCount(0);
        for (Object[] r : list) {
            // cols: Mã NV(0), Họ tên(1), SĐT(2), CCCD(4), Vai trò(8), Lương CB(5), Trạng thái TK(6)
            tableModel.addRow(new Object[]{r[0], r[1], r[2], r[4], r[8], r[5], r[6]});
        }
    }

    private void toggleTrangThai(long maNv, String currentStatus) {
        String action = "Hoạt động".equals(currentStatus) ? "khóa" : "mở khóa";
        int opt = JOptionPane.showConfirmDialog(
                SwingUtilities.getWindowAncestor(this),
                "Bạn có chắc muốn " + action + " tài khoản nhân viên #" + maNv + "?",
                "Xác nhận " + action,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opt != JOptionPane.YES_OPTION) return;

        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() { return NhanVienDAO.toggleTrangThaiTK(maNv); }
            @Override protected void done() {
                try {
                    String newStatus = get();
                    if (newStatus != null) {
                        String msg = "Hoạt động".equals(newStatus)
                                ? "Đã mở khóa tài khoản."
                                : "Đã khóa tài khoản.";
                        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(EmployeePanel.this),
                                msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(EmployeePanel.this),
                                "Nhân viên chưa có tài khoản.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    // ─── Add Dialog ─────────────────────────────────────────────────

    private void showAddDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Thêm nhân viên", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel dlgTitle = new JLabel("Thêm nhân viên mới");
        dlgTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        dlgTitle.setForeground(new Color(30, 41, 59));
        dlgTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField tfHoTen = makeField("Họ tên *");
        JTextField tfSdt = makeField("Số điện thoại *");
        JTextField tfCccd = makeField("CCCD *");
        JTextField tfEmail = makeField("Email");
        JTextField tfLuong = makeField("Lương cơ bản");

        JLabel lblTK = new JLabel("Tài khoản đăng nhập & Vai trò (tùy chọn)");
        lblTK.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTK.setForeground(new Color(100, 116, 139));
        lblTK.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTK.setBorder(new EmptyBorder(10, 0, 4, 0));

        JTextField tfUser = makeField("Username");
        JPasswordField tfPass = new JPasswordField();
        tfPass.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Mật khẩu");
        tfPass.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tfPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        tfPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<RoleItem> cbRole = new JComboBox<>();
        cbRole.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cbRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbRole.addItem(new RoleItem(-1, "— Chọn vai trò —"));
        for (Object[] r : NhanVienDAO.getAllRoleGroups()) {
            cbRole.addItem(new RoleItem((long) r[0], (String) r[1]));
        }

        JButton btnSave = new JButton("Lưu");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setBackground(new Color(37, 99, 235));
        btnSave.setForeground(Color.WHITE);
        btnSave.setBorder(new EmptyBorder(10, 0, 10, 0));
        btnSave.setFocusPainted(false);
        btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnSave.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSave.addActionListener(e -> {
            String hoTen = tfHoTen.getText().trim();
            String sdt = tfSdt.getText().trim();
            String cccd = tfCccd.getText().trim();
            if (hoTen.isEmpty() || sdt.isEmpty() || cccd.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Họ tên, SĐT và CCCD không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            long luong = 0;
            try { if (!tfLuong.getText().trim().isEmpty()) luong = Long.parseLong(tfLuong.getText().trim()); }
            catch (Exception ex) { JOptionPane.showMessageDialog(dialog, "Lương phải là số.", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }
            
            long fLuong = luong;
            RoleItem roleItem = (RoleItem) cbRole.getSelectedItem();
            Long maRole = (roleItem != null && roleItem.id != -1) ? roleItem.id : null;

            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() {
                    return NhanVienDAO.themNhanVien(hoTen, sdt, tfEmail.getText().trim(), cccd, fLuong,
                            tfUser.getText().trim(), new String(tfPass.getPassword()), maRole);
                }
                @Override protected void done() {
                    try {
                        if (get()) {
                            dialog.dispose();
                            loadData();
                            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(EmployeePanel.this),
                                    "Thêm nhân viên thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(dialog, "Lỗi: SĐT, CCCD hoặc Username đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }.execute();
        });

        content.add(dlgTitle);
        content.add(Box.createVerticalStrut(14));
        content.add(tfHoTen); content.add(Box.createVerticalStrut(8));
        content.add(tfSdt); content.add(Box.createVerticalStrut(8));
        content.add(tfCccd); content.add(Box.createVerticalStrut(8));
        content.add(tfEmail); content.add(Box.createVerticalStrut(8));
        content.add(tfLuong); content.add(Box.createVerticalStrut(8));
        content.add(lblTK); content.add(Box.createVerticalStrut(4));
        content.add(tfUser); content.add(Box.createVerticalStrut(8));
        content.add(tfPass); content.add(Box.createVerticalStrut(8));
        content.add(cbRole); content.add(Box.createVerticalStrut(20));
        content.add(btnSave);

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    // ─── Detail/Edit Dialog ─────────────────────────────────────────

    private void showDetailDialog(long maNv) {
        Object[] data = NhanVienDAO.getNhanVienById(maNv);
        if (data == null) return;

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Chi tiết nhân viên", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 580);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel dlgTitle = new JLabel("Nhân viên #" + maNv);
        dlgTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        dlgTitle.setForeground(new Color(30, 41, 59));
        dlgTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField tfHoTen = makeField("Họ tên"); tfHoTen.setText(str(data[1])); tfHoTen.setEditable(false);
        JTextField tfSdt = makeField("SĐT"); tfSdt.setText(str(data[2])); tfSdt.setEditable(false);
        JTextField tfEmail = makeField("Email"); tfEmail.setText(str(data[3])); tfEmail.setEditable(false);
        JTextField tfCccd = makeField("CCCD"); tfCccd.setText(str(data[4])); tfCccd.setEditable(false);
        JTextField tfLuong = makeField("Lương CB"); tfLuong.setText(str(data[5])); tfLuong.setEditable(false);

        String trangThaiTk = str(data[6]);
        boolean hasAccount = !"Chưa có TK".equals(trangThaiTk);

        JLabel lblTkSection = new JLabel("Thông tin tài khoản");
        lblTkSection.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTkSection.setForeground(new Color(100, 116, 139));
        lblTkSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTkSection.setBorder(new EmptyBorder(6, 0, 4, 0));

        JLabel lblTrangThai = new JLabel("Trạng thái: " + trangThaiTk);
        lblTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTrangThai.setForeground("Hoạt động".equals(trangThaiTk) ? new Color(5, 150, 105) : new Color(220, 38, 38));
        lblTrangThai.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField tfUsername = makeField("Username");
        tfUsername.setText(str(data[7]));
        tfUsername.setEditable(false);

        JPasswordField tfNewPass = new JPasswordField();
        tfNewPass.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Mật khẩu mới (để trống nếu không đổi)");
        tfNewPass.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tfNewPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        tfNewPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        tfNewPass.setEditable(false);

        JComboBox<RoleItem> cbRole = new JComboBox<>();
        cbRole.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cbRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbRole.setEnabled(false);
        cbRole.addItem(new RoleItem(-1, "— Chọn vai trò —"));
        
        Long currentRole = data[8] == null ? null : ((Number)data[8]).longValue();
        for (Object[] r : NhanVienDAO.getAllRoleGroups()) {
            RoleItem item = new RoleItem((long) r[0], (String) r[1]);
            cbRole.addItem(item);
            if (currentRole != null && item.id == currentRole) {
                cbRole.setSelectedItem(item);
            }
        }

        JTextField[] nvFields = {tfHoTen, tfSdt, tfEmail, tfCccd, tfLuong};

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnToggle = new JButton();
        boolean isActive = "Hoạt động".equals(trangThaiTk);
        if (hasAccount) {
            btnToggle.setText(isActive ? "Khóa TK" : "Mở khóa TK");
            btnToggle.setBackground(isActive ? new Color(220, 38, 38) : new Color(5, 150, 105));
        } else {
            btnToggle.setText("Chưa có TK");
            btnToggle.setBackground(new Color(148, 163, 184));
            btnToggle.setEnabled(false);
        }
        btnToggle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnToggle.setForeground(Color.WHITE);
        btnToggle.setBorder(new EmptyBorder(8, 18, 8, 18));
        btnToggle.setFocusPainted(false);
        btnToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnToggle.addActionListener(e -> {
            dialog.dispose();
            toggleTrangThai(maNv, trangThaiTk);
        });

        JButton btnEdit = new JButton("Chỉnh sửa");
        btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnEdit.setBackground(new Color(37, 99, 235));
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setBorder(new EmptyBorder(8, 18, 8, 18));
        btnEdit.setFocusPainted(false);
        btnEdit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEdit.addActionListener(e -> {
            if ("Chỉnh sửa".equals(btnEdit.getText())) {
                for (JTextField f : nvFields) {
                    f.setEditable(true);
                    f.setBackground(new Color(255, 255, 240));
                }
                tfUsername.setEditable(true);
                tfUsername.setBackground(new Color(255, 255, 240));
                tfNewPass.setEditable(true);
                tfNewPass.setBackground(new Color(255, 255, 240));
                cbRole.setEnabled(true);
                cbRole.setBackground(new Color(255, 255, 240));
                
                btnEdit.setText("Hoàn tất");
                btnEdit.setBackground(new Color(5, 150, 105));
            } else {
                String hoTen = tfHoTen.getText().trim();
                String sdt = tfSdt.getText().trim();
                String cccd = tfCccd.getText().trim();
                if (hoTen.isEmpty() || sdt.isEmpty() || cccd.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Họ tên, SĐT và CCCD không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                long luong = 0;
                try { if (!tfLuong.getText().trim().isEmpty()) luong = Long.parseLong(tfLuong.getText().trim()); }
                catch (Exception ex) { JOptionPane.showMessageDialog(dialog, "Lương phải là số.", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }
                
                long fLuong = luong;
                String newUser = tfUsername.getText().trim();
                String newPass = new String(tfNewPass.getPassword());
                RoleItem selRole = (RoleItem) cbRole.getSelectedItem();
                Long maRole = (selRole != null && selRole.id != -1) ? selRole.id : null;

                new SwingWorker<Boolean, Void>() {
                    @Override protected Boolean doInBackground() {
                        boolean ok = NhanVienDAO.capNhatNhanVien(maNv, hoTen, sdt, tfEmail.getText().trim(), cccd, fLuong);
                        if (!newUser.isEmpty()) {
                            NhanVienDAO.capNhatTaiKhoan(maNv, newUser, newPass, maRole);
                        }
                        return ok;
                    }
                    @Override protected void done() {
                        try {
                            if (get()) {
                                dialog.dispose();
                                loadData();
                                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(EmployeePanel.this),
                                        "Cập nhật thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                            }
                        } catch (Exception ex) { ex.printStackTrace(); }
                    }
                }.execute();
            }
        });

        btnRow.add(btnToggle);
        btnRow.add(btnEdit);

        content.add(dlgTitle);
        content.add(Box.createVerticalStrut(14));
        content.add(tfHoTen); content.add(Box.createVerticalStrut(8));
        content.add(tfSdt); content.add(Box.createVerticalStrut(8));
        content.add(tfEmail); content.add(Box.createVerticalStrut(8));
        content.add(tfCccd); content.add(Box.createVerticalStrut(8));
        content.add(tfLuong); content.add(Box.createVerticalStrut(12));
        content.add(lblTkSection);
        content.add(Box.createVerticalStrut(2));
        content.add(lblTrangThai);
        content.add(Box.createVerticalStrut(6));
        content.add(tfUsername); content.add(Box.createVerticalStrut(8));
        content.add(tfNewPass); content.add(Box.createVerticalStrut(8));
        content.add(cbRole);
        content.add(Box.createVerticalStrut(20));
        content.add(btnRow);

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    // ─── Helpers ────────────────────────────────────────────────────

    private JTextField makeField(String placeholder) {
        JTextField tf = new JTextField();
        tf.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tf;
    }

    private String str(Object o) { return o == null ? "" : o.toString(); }

    // ─── Custom Renderer ────────────────────────────────────────────

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, focus, r, c);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            String s = v == null ? "" : v.toString();
            if ("Hoạt động".equals(s)) {
                lbl.setForeground(new Color(5, 150, 105));
            } else if ("Bị khóa".equals(s)) {
                lbl.setForeground(new Color(220, 38, 38));
            } else {
                lbl.setForeground(new Color(148, 163, 184));
            }
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            return lbl;
        }
    }
}
