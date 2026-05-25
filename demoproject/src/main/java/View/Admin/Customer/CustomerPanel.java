package View.Admin.Customer;

import Controller.Admin.KhachHangDAO;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class CustomerPanel extends javax.swing.JPanel {

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch;
    private JLabel lblLastUpdate;

    public CustomerPanel() {
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

        JLabel title = new JLabel("Quản lý khách hàng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(30, 41, 59));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        txtSearch = new JTextField(18);
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm theo mã KH, tên, SĐT, email...");
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(220, 34));
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { searchData(); }
        });

        JButton btnAdd = new JButton("+ Thêm khách hàng");
        View.Admin.UIUtils.styleButton(btnAdd);
        btnAdd.addActionListener(e -> showAddDialog());
        // Ẩn nếu không có quyền Thêm
        btnAdd.setVisible(Controller.Admin.PermissionService.canAdd("Khach hang"));
        JButton btnRefresh = new JButton("Cập nhật");
        View.Admin.UIUtils.styleButton(btnRefresh);
        btnRefresh.addActionListener(e -> loadData());

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

        String[] cols = {"Mã KH", "Họ tên", "SĐT", "Địa chỉ", "Email", "Điểm TL", "Trạng thái TK"};
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
        table.getColumnModel().getColumn(5).setCellRenderer(center);
        table.getColumnModel().getColumn(5).setPreferredWidth(70);
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(6).setPreferredWidth(120);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row < 0) return;
                long maKh = ((Number) tableModel.getValueAt(row, 0)).longValue();
                showDetailDialog(maKh);
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
                return KhachHangDAO.getAllKhachHang();
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
                return kw.isEmpty() ? KhachHangDAO.getAllKhachHang() : KhachHangDAO.timKiemKhachHang(kw);
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
            tableModel.addRow(new Object[]{r[0], r[1], r[2], r[3], r[4], r[5], r[6]});
        }
    }

    private void toggleTrangThai(long maKh, String currentStatus, Runnable onDone) {
        String action = "Hoạt động".equals(currentStatus) ? "khóa" : "mở khóa";
        int opt = JOptionPane.showConfirmDialog(
                SwingUtilities.getWindowAncestor(this),
                "Bạn có chắc muốn " + action + " tài khoản khách hàng #" + maKh + "?",
                "Xác nhận " + action,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opt != JOptionPane.YES_OPTION) return;

        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() { return KhachHangDAO.toggleTrangThaiTK(maKh); }
            @Override protected void done() {
                try {
                    String newStatus = get();
                    if (newStatus != null) {
                        String msg = "Hoạt động".equals(newStatus)
                                ? "Đã mở khóa tài khoản."
                                : "Đã khóa tài khoản.";
                        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(CustomerPanel.this),
                                msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        loadData();
                        if (onDone != null) onDone.run();
                    } else {
                        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(CustomerPanel.this),
                                "Khách hàng chưa có tài khoản.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    // ─── Add Dialog ─────────────────────────────────────────────────

    private void showAddDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Thêm khách hàng", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(460, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel dlgTitle = new JLabel("Thêm khách hàng mới");
        dlgTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        dlgTitle.setForeground(new Color(30, 41, 59));
        dlgTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField tfHoTen = makeField("Họ tên *");
        JTextField tfSdt = makeField("Số điện thoại *");
        JTextField tfDiaChi = makeField("Địa chỉ");
        JTextField tfEmail = makeField("Email");

        JLabel lblTK = new JLabel("Tài khoản đăng nhập (tùy chọn)");
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

        JButton btnSave = new JButton("Lưu");
        View.Admin.UIUtils.styleButton(btnSave);
        btnSave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnSave.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSave.addActionListener(e -> {
            String hoTen = tfHoTen.getText().trim();
            String sdt = tfSdt.getText().trim();
            if (hoTen.isEmpty() || sdt.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Họ tên và SĐT không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() {
                    return KhachHangDAO.themKhachHang(hoTen, sdt, tfDiaChi.getText().trim(),
                            tfEmail.getText().trim(), tfUser.getText().trim(), new String(tfPass.getPassword()));
                }
                @Override protected void done() {
                    try {
                        if (get()) {
                            dialog.dispose();
                            loadData();
                            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(CustomerPanel.this),
                                    "Thêm khách hàng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(dialog, "Lỗi: SĐT hoặc Username đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }.execute();
        });

        content.add(dlgTitle);
        content.add(Box.createVerticalStrut(16));
        content.add(tfHoTen); content.add(Box.createVerticalStrut(8));
        content.add(tfSdt); content.add(Box.createVerticalStrut(8));
        content.add(tfDiaChi); content.add(Box.createVerticalStrut(8));
        content.add(tfEmail); content.add(Box.createVerticalStrut(8));
        content.add(lblTK); content.add(Box.createVerticalStrut(4));
        content.add(tfUser); content.add(Box.createVerticalStrut(8));
        content.add(tfPass); content.add(Box.createVerticalStrut(20));
        content.add(btnSave);

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    // ─── Detail/Edit Dialog ─────────────────────────────────────────

    private void showDetailDialog(long maKh) {
        Object[] data = KhachHangDAO.getKhachHangById(maKh);
        if (data == null) return;

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Chi tiết khách hàng", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel dlgTitle = new JLabel("Khách hàng #" + maKh);
        dlgTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        dlgTitle.setForeground(new Color(30, 41, 59));
        dlgTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField tfHoTen = makeField("Họ tên"); tfHoTen.setText(str(data[1])); tfHoTen.setEditable(false);
        JTextField tfSdt = makeField("SĐT"); tfSdt.setText(str(data[2])); tfSdt.setEditable(false);
        JTextField tfDiaChi = makeField("Địa chỉ"); tfDiaChi.setText(str(data[3])); tfDiaChi.setEditable(false);
        JTextField tfEmail = makeField("Email"); tfEmail.setText(str(data[4])); tfEmail.setEditable(false);

        JLabel lblDiem = new JLabel("Điểm tích lũy: " + data[5]);
        lblDiem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDiem.setForeground(new Color(71, 85, 105));
        lblDiem.setAlignmentX(Component.LEFT_ALIGNMENT);

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

        JTextField[] khFields = {tfHoTen, tfSdt, tfDiaChi, tfEmail};

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnToggle = new JButton();
        View.Admin.UIUtils.styleButton(btnToggle);
        boolean isActive = "Hoạt động".equals(trangThaiTk);
        if (hasAccount) {
            btnToggle.setText(isActive ? "Khóa TK" : "Mở khóa TK");
            btnToggle.setBackground(isActive ? new Color(220, 53, 69) : new Color(40, 167, 69));
        } else {
            btnToggle.setText("Chưa có TK");
            btnToggle.setBackground(new Color(148, 163, 184));
            btnToggle.setEnabled(false);
        }
        btnToggle.addActionListener(e -> {
            dialog.dispose();
            toggleTrangThai(maKh, trangThaiTk, null);
        });

        JButton btnEdit = new JButton("Chỉnh sửa");
        View.Admin.UIUtils.styleButton(btnEdit);
        btnEdit.addActionListener(e -> {
            if ("Chỉnh sửa".equals(btnEdit.getText())) {
                for (JTextField f : khFields) {
                    f.setEditable(true);
                    f.setBackground(new Color(255, 255, 240));
                }
                tfUsername.setEditable(true);
                tfUsername.setBackground(new Color(255, 255, 240));
                tfNewPass.setEditable(true);
                tfNewPass.setBackground(new Color(255, 255, 240));
                btnEdit.setText("Hoàn tất");
                btnEdit.setBackground(new Color(40, 167, 69));
            } else {
                String hoTen = tfHoTen.getText().trim();
                String sdt = tfSdt.getText().trim();
                if (hoTen.isEmpty() || sdt.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Họ tên và SĐT không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String newUser = tfUsername.getText().trim();
                String newPass = new String(tfNewPass.getPassword());
                new SwingWorker<Boolean, Void>() {
                    @Override protected Boolean doInBackground() {
                        boolean khOk = KhachHangDAO.capNhatKhachHang(maKh, hoTen, sdt,
                                tfDiaChi.getText().trim(), tfEmail.getText().trim());
                        if (hasAccount && !newUser.isEmpty()) {
                            KhachHangDAO.capNhatTaiKhoan(maKh, newUser, newPass);
                        }
                        return khOk;
                    }
                    @Override protected void done() {
                        try {
                            if (get()) {
                                dialog.dispose();
                                loadData();
                                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(CustomerPanel.this),
                                        "Cập nhật thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                            }
                        } catch (Exception ex) { ex.printStackTrace(); }
                    }
                }.execute();
            }
        });

        btnRow.add(btnToggle);
        // Ẩn nút Chỉnh sửa và Khóa/Mở khóa nếu không có quyền Sửa
        boolean canEditCustomer = Controller.Admin.PermissionService.canEdit("Khach hang");
        btnToggle.setVisible(canEditCustomer);
        btnEdit.setVisible(canEditCustomer);
        btnRow.add(btnEdit);

        content.add(dlgTitle);
        content.add(Box.createVerticalStrut(14));
        content.add(tfHoTen); content.add(Box.createVerticalStrut(8));
        content.add(tfSdt); content.add(Box.createVerticalStrut(8));
        content.add(tfDiaChi); content.add(Box.createVerticalStrut(8));
        content.add(tfEmail); content.add(Box.createVerticalStrut(8));
        content.add(lblDiem);
        content.add(Box.createVerticalStrut(12));
        content.add(lblTkSection);
        content.add(Box.createVerticalStrut(2));
        content.add(lblTrangThai);
        content.add(Box.createVerticalStrut(6));
        content.add(tfUsername); content.add(Box.createVerticalStrut(8));
        content.add(tfNewPass);
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
