package View.Schedule;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * ScheduleManagementPanel - Quản lý Lịch làm việc & Chấm công
 * Panel chính chứa 3 tab: Lịch làm việc, Chấm công, Cấu hình Ca
 * Sử dụng Mock Data để demo giao diện.
 */
public class ScheduleManagementPanel extends JPanel {

    private static final Logger logger = Logger.getLogger(ScheduleManagementPanel.class.getName());

    // ==================== COLOR PALETTE ====================
    private static final Color PRIMARY = new Color(37, 99, 235);
    private static final Color PRIMARY_DARK = new Color(29, 78, 216);
    private static final Color BG_MAIN = new Color(241, 245, 249);
    private static final Color BG_CARD = Color.WHITE;
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color GREEN = new Color(22, 163, 74);
    private static final Color RED = new Color(220, 38, 38);
    private static final Color HEADER_BG = new Color(248, 250, 252);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_TABLE = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_TABLE_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);

    public ScheduleManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_MAIN);
        initUI();
    }

    private void initUI() {
        // ===== HEADER =====
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_MAIN);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(18, 24, 8, 24));
        JLabel titleLabel = new JLabel("Quản lý Lịch làm việc & Chấm công");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(TEXT_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // ===== TABBED PANE =====
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(BG_MAIN);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(4, 16, 16, 16));

        tabbedPane.addTab("Lịch làm việc", createScheduleTab());
        tabbedPane.addTab("Chấm công", createAttendanceTab());
        tabbedPane.addTab("Cấu hình Ca", createShiftConfigTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // =====================================================
    //  TAB 1: LỊCH LÀM VIỆC (Schedule Matrix)
    // =====================================================

    /** Danh sách ca làm việc chuẩn dùng cho dropdown */
    private static final String[] SHIFT_OPTIONS = {"OFF", "Ca sáng", "Ca trưa", "Ca tối"};

    /** Màu tương ứng với từng ca */
    private static final Color COLOR_OFF     = new Color(220, 38, 38);   // Đỏ
    private static final Color COLOR_SANG    = new Color(22, 163, 74);   // Xanh lá
    private static final Color COLOR_TRUA    = new Color(234, 138, 0);   // Cam
    private static final Color COLOR_TOI     = new Color(124, 58, 237);  // Tím

    private JPanel createScheduleTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 8, 8, 8));

        // --- Toolbar: Lọc theo chức vụ & tuần + Nút Lưu ---
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(BG_CARD);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));

        // Phần bên trái: bộ lọc
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        filterPanel.setOpaque(false);

        filterPanel.add(createLabel("Bộ phận:"));
        JComboBox<String> cboPosition = new JComboBox<>(
                new String[]{"Tất cả", "NV bán hàng", "NV kĩ thuật"});
        cboPosition.setFont(FONT_LABEL);
        filterPanel.add(cboPosition);

        filterPanel.add(Box.createHorizontalStrut(16));
        filterPanel.add(createLabel("Tuần:"));
        JComboBox<String> cboWeek = new JComboBox<>(
                new String[]{"12/05 - 18/05/2026", "19/05 - 25/05/2026", "26/05 - 01/06/2026"});
        cboWeek.setFont(FONT_LABEL);
        filterPanel.add(cboWeek);

        toolbar.add(filterPanel, BorderLayout.WEST);

        // Phần bên phải: nút Lưu lịch làm việc
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        actionPanel.setOpaque(false);
        JButton btnSaveSchedule = createStyledButton("Lưu lịch làm việc");
        btnSaveSchedule.setBackground(GREEN);
        actionPanel.add(btnSaveSchedule);
        toolbar.add(actionPanel, BorderLayout.EAST);

        panel.add(toolbar, BorderLayout.NORTH);

        // --- Mock Data: 5 nhân viên với ca rải rác ---
        String[] cols = {"Nhân Viên", "Bộ Phận", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "CN"};
        Object[][] data = {
                {"Nguyễn Văn An",   "NV kĩ thuật",   "Ca sáng", "Ca sáng", "Ca trưa", "OFF",     "Ca sáng", "Ca tối",  "OFF"},
                {"Trần Thị Bích",   "NV bán hàng",   "Ca trưa", "Ca trưa", "Ca sáng", "Ca sáng", "OFF",     "OFF",     "Ca sáng"},
                {"Lê Hoàng Cường",  "NV kĩ thuật",   "Ca tối",  "OFF",     "Ca tối",  "Ca tối",  "Ca trưa", "Ca sáng", "OFF"},
                {"Phạm Minh Đức",   "NV bán hàng",   "Ca sáng", "Ca tối",  "OFF",     "Ca trưa", "Ca trưa", "Ca trưa", "Ca sáng"},
                {"Hoàng Thị Em",    "NV kĩ thuật",   "OFF",     "Ca sáng", "Ca sáng", "Ca sáng", "Ca tối",  "OFF",     "Ca tối"},
        };

        // --- Table Model: cột 0 (Nhân Viên) & cột 1 (Bộ Phận) không cho sửa ---
        DefaultTableModel scheduleModel = new DefaultTableModel(data, cols) {
            @Override
            public boolean isCellEditable(int row, int col) { return col > 1; }
        };

        JTable scheduleTable = createStyledTable(scheduleModel);

        // --- Row Sorter: dùng để lọc dòng theo bộ phận ---
        TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<>(scheduleModel);
        scheduleTable.setRowSorter(rowSorter);

        // --- Event lọc theo Bộ Phận khi chọn combo box ---
        cboPosition.addActionListener(e -> {
            String selected = (String) cboPosition.getSelectedItem();
            if (selected == null || "Tất cả".equals(selected)) {
                rowSorter.setRowFilter(null); // Hiện tất cả
            } else {
                // Lọc chính xác theo cột index 1 (Bộ Phận)
                rowSorter.setRowFilter(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(selected) + "$", 1));
            }
        });

        // --- Cell Editor: JComboBox dropdown cho cột Thứ 2 → CN ---
        JComboBox<String> shiftCombo = new JComboBox<>(SHIFT_OPTIONS);
        shiftCombo.setFont(FONT_TABLE);
        shiftCombo.setEditable(false); // Không cho gõ phím linh tinh
        DefaultCellEditor shiftEditor = new DefaultCellEditor(shiftCombo);

        // Áp dụng editor cho cột index 2 đến 8 (Thứ 2 → CN)
        for (int colIdx = 2; colIdx < cols.length; colIdx++) {
            scheduleTable.getColumnModel().getColumn(colIdx).setCellEditor(shiftEditor);
        }

        // --- Cell Renderer: tô màu chữ theo ca ---
        scheduleTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                setHorizontalAlignment(c <= 1 ? LEFT : CENTER);
                // Reset font về mặc định trước khi tô
                setFont(FONT_TABLE);

                if (!sel && c > 1 && val != null) {
                    String s = val.toString();
                    switch (s) {
                        case "OFF":
                            setForeground(COLOR_OFF);
                            setFont(FONT_TABLE.deriveFont(Font.BOLD));
                            break;
                        case "Ca sáng":
                            setForeground(COLOR_SANG);
                            break;
                        case "Ca trưa":
                            setForeground(COLOR_TRUA);
                            break;
                        case "Ca tối":
                            setForeground(COLOR_TOI);
                            break;
                        default:
                            setForeground(TEXT_DARK);
                    }
                } else if (!sel) {
                    setForeground(TEXT_DARK);
                }
                setBackground(sel ? new Color(219, 234, 254) : (r % 2 == 0 ? BG_CARD : HEADER_BG));
                return this;
            }
        });

        // Cột widths
        scheduleTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        scheduleTable.getColumnModel().getColumn(1).setPreferredWidth(140);

        panel.add(wrapInScrollPane(scheduleTable), BorderLayout.CENTER);

        // --- Event Nút "Lưu lịch làm việc" ---
        btnSaveSchedule.addActionListener(e -> {
            // Dừng chỉnh sửa nếu đang edit ô nào đó (để commit giá trị mới nhất)
            if (scheduleTable.isEditing()) {
                scheduleTable.getCellEditor().stopCellEditing();
            }

            // In ra Console dữ liệu dòng đầu tiên để chứng minh lấy được data
            int colCount = scheduleModel.getColumnCount();
            System.out.println("===== DỮ LIỆU LỊCH - DÒNG ĐẦU TIÊN =====");
            for (int c = 0; c < colCount; c++) {
                System.out.printf("  %-12s : %s%n",
                        scheduleModel.getColumnName(c),
                        scheduleModel.getValueAt(0, c));
            }
            System.out.println("==========================================");

            // Hiển thị thông báo thành công
            JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(panel),
                    "Đã lưu lịch thành công!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        return panel;
    }

    // =====================================================
    //  TAB 2: CHẤM CÔNG (Attendance Log)
    // =====================================================
    private JPanel createAttendanceTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 8, 8, 8));

        // ========== TOOLBAR: Bộ lọc ngày + trạng thái ==========
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        toolbar.setBackground(BG_CARD);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));

        // Từ ngày
        toolbar.add(createLabel("Từ ngày:"));
        JTextField txtFrom = new JTextField("12/05/2026", 10);
        txtFrom.setFont(FONT_LABEL);
        toolbar.add(txtFrom);

        // Đến ngày
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(createLabel("Đến ngày:"));
        JTextField txtTo = new JTextField("16/05/2026", 10);
        txtTo.setFont(FONT_LABEL);
        toolbar.add(txtTo);

        // Lọc trạng thái
        toolbar.add(Box.createHorizontalStrut(16));
        toolbar.add(createLabel("Trạng thái:"));
        JComboBox<String> cboStatus = new JComboBox<>(
                new String[]{"Tất cả", "Đúng giờ", "Đi trễ / Về sớm", "Thiếu check-in/out", "Vắng mặt"});
        cboStatus.setFont(FONT_LABEL);
        toolbar.add(cboStatus);

        // Nút lọc
        toolbar.add(Box.createHorizontalStrut(12));
        JButton btnFilter = createStyledButton("Lọc dữ liệu");
        toolbar.add(btnFilter);

        panel.add(toolbar, BorderLayout.NORTH);

        // ========== BẢNG CHẤM CÔNG (khớp CSDL: CHAM_CONG + LICH_LAM_VIEC) ==========
        // Cột: Ngày (NGAY_LAM), Mã Ca (MA_CA), Mã NV (MA_NV), Giờ Vào, Giờ Ra, Trạng Thái, Ghi Chú
        String[] cols = {"Ngày", "Mã Ca", "Mã NV", "Giờ Vào", "Giờ Ra", "Trạng Thái", "Ghi Chú"};

        // Mock Data: 5 dòng mô phỏng thực tế, tên NV khớp với Tab 1
        Object[][] data = {
                // Dòng 1: Đi làm chuẩn
                {"12/05/2026", "Ca sáng", "Nguyễn Văn An",  "07:55", "17:05", "Đúng giờ",          ""},
                // Dòng 2: Đi trễ
                {"12/05/2026", "Ca trưa", "Trần Thị Bích",  "08:30", "17:00", "Đi trễ / Về sớm",   ""},
                // Dòng 3: Quên chấm công lúc về
                {"13/05/2026", "Ca tối",  "Lê Hoàng Cường", "17:10", "",      "Thiếu check-in/out", "Quên quẹt thẻ ra"},
                // Dòng 4: Quên chấm công lúc đến
                {"13/05/2026", "Ca sáng", "Phạm Minh Đức",  "",      "17:00", "Thiếu check-in/out", "Quên quẹt thẻ vào"},
                // Dòng 5: Nghỉ làm / Vắng mặt
                {"14/05/2026", "Ca sáng", "Hoàng Thị Em",   "",      "",      "Vắng mặt",          "Nghỉ không phép"},
        };

        DefaultTableModel attendanceModel = new DefaultTableModel(data, cols) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable attendanceTable = createStyledTable(attendanceModel);

        // --- Row Sorter cho bộ lọc trạng thái ---
        TableRowSorter<DefaultTableModel> attendanceSorter = new TableRowSorter<>(attendanceModel);
        attendanceTable.setRowSorter(attendanceSorter);

        // --- Event nút "Lọc dữ liệu" ---
        btnFilter.addActionListener(e -> {
            String selectedStatus = (String) cboStatus.getSelectedItem();
            if (selectedStatus == null || "Tất cả".equals(selectedStatus)) {
                attendanceSorter.setRowFilter(null);
            } else {
                // Lọc chính xác theo cột index 5 (Trạng Thái)
                attendanceSorter.setRowFilter(
                        RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(selectedStatus) + "$", 5));
            }
        });

        // ========== RENDERER: Tô màu cột Trạng Thái (index 5) ==========
        attendanceTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                setHorizontalAlignment(CENTER);
                setFont(FONT_TABLE.deriveFont(Font.BOLD));
                if (!sel && val != null) {
                    String status = val.toString();
                    if ("Đúng giờ".equals(status)) {
                        setForeground(GREEN);
                    } else {
                        // "Đi trễ / Về sớm", "Thiếu check-in/out", "Vắng mặt" → Đỏ + Bold
                        setForeground(RED);
                    }
                }
                setBackground(sel ? new Color(219, 234, 254) : (r % 2 == 0 ? BG_CARD : HEADER_BG));
                return this;
            }
        });

        // Cột widths
        attendanceTable.getColumnModel().getColumn(0).setPreferredWidth(100);  // Ngày
        attendanceTable.getColumnModel().getColumn(1).setPreferredWidth(90);   // Mã Ca
        attendanceTable.getColumnModel().getColumn(2).setPreferredWidth(150);  // Mã NV
        attendanceTable.getColumnModel().getColumn(3).setPreferredWidth(80);   // Giờ Vào
        attendanceTable.getColumnModel().getColumn(4).setPreferredWidth(80);   // Giờ Ra
        attendanceTable.getColumnModel().getColumn(5).setPreferredWidth(150);  // Trạng Thái
        attendanceTable.getColumnModel().getColumn(6).setPreferredWidth(180);  // Ghi Chú

        // ========== POPUP MENU: Cập nhật công thủ công (Click chuột phải) ==========
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuManualUpdate = new JMenuItem("Cập nhật công thủ công");
        menuManualUpdate.setFont(FONT_LABEL);
        popupMenu.add(menuManualUpdate);

        attendanceTable.setComponentPopupMenu(popupMenu);

        // Đảm bảo click chuột phải sẽ select đúng dòng
        attendanceTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = attendanceTable.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < attendanceTable.getRowCount()) {
                        attendanceTable.setRowSelectionInterval(row, row);
                    }
                }
            }
        });

        // --- Action: Mở dialog nhập Giờ Vào, Giờ Ra, Ghi chú ---
        menuManualUpdate.addActionListener(e -> {
            int viewRow = attendanceTable.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(panel, "Vui lòng chọn một dòng để cập nhật.",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Chuyển từ view index sang model index (vì có thể đang lọc)
            int modelRow = attendanceTable.convertRowIndexToModel(viewRow);

            // Lấy giá trị hiện tại để điền sẵn vào dialog
            String currentIn  = attendanceModel.getValueAt(modelRow, 3).toString();
            String currentOut = attendanceModel.getValueAt(modelRow, 4).toString();
            String currentNote = attendanceModel.getValueAt(modelRow, 6).toString();

            // Tạo panel nhập liệu
            JPanel inputPanel = new JPanel(new GridBagLayout());
            inputPanel.setBackground(BG_CARD);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 6, 6, 6);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            JTextField txtGioVao = new JTextField(currentIn, 12);
            JTextField txtGioRa  = new JTextField(currentOut, 12);
            JTextField txtGhiChu = new JTextField(currentNote, 20);
            txtGioVao.setFont(FONT_LABEL);
            txtGioRa.setFont(FONT_LABEL);
            txtGhiChu.setFont(FONT_LABEL);

            gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
            inputPanel.add(createLabel("Giờ Vào (HH:mm):"), gbc);
            gbc.gridx = 1; gbc.weightx = 1.0;
            inputPanel.add(txtGioVao, gbc);

            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
            inputPanel.add(createLabel("Giờ Ra (HH:mm):"), gbc);
            gbc.gridx = 1; gbc.weightx = 1.0;
            inputPanel.add(txtGioRa, gbc);

            gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
            inputPanel.add(createLabel("Ghi chú:"), gbc);
            gbc.gridx = 1; gbc.weightx = 1.0;
            inputPanel.add(txtGhiChu, gbc);

            int result = JOptionPane.showConfirmDialog(
                    SwingUtilities.getWindowAncestor(panel),
                    inputPanel,
                    "Cập nhật công thủ công - " + attendanceModel.getValueAt(modelRow, 2),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String newIn   = txtGioVao.getText().trim();
                String newOut  = txtGioRa.getText().trim();
                String newNote = txtGhiChu.getText().trim();

                // Cập nhật ngược lại vào Model
                attendanceModel.setValueAt(newIn,   modelRow, 3); // Giờ Vào
                attendanceModel.setValueAt(newOut,   modelRow, 4); // Giờ Ra

                // Tự động thêm ghi chú "[Thủ công]" nếu user chưa ghi
                if (newNote.isEmpty()) {
                    newNote = "[Cập nhật thủ công]";
                }
                attendanceModel.setValueAt(newNote, modelRow, 6); // Ghi Chú

                // Cập nhật trạng thái dựa trên giờ vào/ra mới
                String newStatus;
                if (newIn.isEmpty() && newOut.isEmpty()) {
                    newStatus = "Vắng mặt";
                } else if (newIn.isEmpty() || newOut.isEmpty()) {
                    newStatus = "Thiếu check-in/out";
                } else {
                    newStatus = "Đúng giờ";
                }
                attendanceModel.setValueAt(newStatus, modelRow, 5); // Trạng Thái

                attendanceModel.fireTableRowsUpdated(modelRow, modelRow);

                JOptionPane.showMessageDialog(
                        SwingUtilities.getWindowAncestor(panel),
                        "Đã cập nhật công thủ công thành công!",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);

                // In ra console để debug
                System.out.println("===== CẬP NHẬT CÔNG THỦ CÔNG =====");
                System.out.printf("  NV       : %s%n", attendanceModel.getValueAt(modelRow, 2));
                System.out.printf("  Giờ Vào  : %s%n", newIn);
                System.out.printf("  Giờ Ra   : %s%n", newOut);
                System.out.printf("  Trạng thái: %s%n", newStatus);
                System.out.printf("  Ghi chú  : %s%n", newNote);
                System.out.println("====================================");
            }
        });

        panel.add(wrapInScrollPane(attendanceTable), BorderLayout.CENTER);

        return panel;
    }

    // =====================================================
    //  TAB 3: CẤU HÌNH CA (Shift Config) - JSplitPane
    // =====================================================
    private JPanel createShiftConfigTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 8, 8, 8));

        // ========== LEFT: Bảng danh sách ca (Master) ==========
        String[] cols = {"Mã Ca", "Tên Ca", "Giờ BĐ", "Giờ KT"};
        Object[][] data = {
                {"CA01", "Ca Sáng",       "07:00", "12:00"},
                {"CA02", "Ca Chiều",      "12:00", "17:00"},
                {"CA03", "Ca Tối",        "17:00", "22:00"},
                {"CA04", "Ca Hành chính", "08:00", "17:00"},
        };

        DefaultTableModel shiftModel = new DefaultTableModel(data, cols) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable shiftTable = createStyledTable(shiftModel);
        JScrollPane leftScroll = wrapInScrollPane(shiftTable);

        JPanel leftPanel = new JPanel(new BorderLayout(0, 8));
        leftPanel.setBackground(BG_MAIN);
        JLabel leftTitle = new JLabel("  Danh sách Ca làm việc");
        leftTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        leftTitle.setForeground(TEXT_DARK);
        leftTitle.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 0));
        leftPanel.add(leftTitle, BorderLayout.NORTH);
        leftPanel.add(leftScroll, BorderLayout.CENTER);

        // ========== RIGHT: Form thêm/sửa ca (Detail) ==========
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(BG_CARD);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));

        JLabel rightTitle = new JLabel("Thông tin Ca làm việc");
        rightTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        rightTitle.setForeground(PRIMARY);
        rightPanel.add(rightTitle, BorderLayout.NORTH);

        // --- Form fields dùng GridBagLayout ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_CARD);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 4, 8, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Mã Ca & Tên Ca: JTextField thường
        JTextField txtMaCa = new JTextField(15);
        JTextField txtTenCa = new JTextField(15);
        txtMaCa.setFont(FONT_LABEL);
        txtTenCa.setFont(FONT_LABEL);

        // Giờ BĐ & Giờ KT: JFormattedTextField + MaskFormatter("##:##")
        JFormattedTextField txtGioBD = createTimeMaskField();
        JFormattedTextField txtGioKT = createTimeMaskField();

        addFormRow(formPanel, gbc, 0, "Mã Ca:", txtMaCa);
        addFormRow(formPanel, gbc, 1, "Tên Ca:", txtTenCa);
        addFormRow(formPanel, gbc, 2, "Giờ Bắt Đầu:", txtGioBD);
        addFormRow(formPanel, gbc, 3, "Giờ Kết Thúc:", txtGioKT);

        // --- Buttons ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        btnPanel.setBackground(BG_CARD);
        JButton btnSave   = createStyledButton("Thêm / Lưu");
        JButton btnDelete = createStyledButton("Xóa");
        btnDelete.setBackground(RED);
        JButton btnClear  = createStyledButton("Làm mới");
        btnClear.setBackground(TEXT_SECONDARY);
        btnPanel.add(btnSave);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);

        rightPanel.add(formPanel, BorderLayout.CENTER);

        // ========== SỰ KIỆN: Click bảng -> Điền form + Khóa Mã Ca ==========
        shiftTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && shiftTable.getSelectedRow() >= 0) {
                int r = shiftTable.getSelectedRow();
                txtMaCa.setText(shiftModel.getValueAt(r, 0).toString());
                txtTenCa.setText(shiftModel.getValueAt(r, 1).toString());
                txtGioBD.setText(shiftModel.getValueAt(r, 2).toString());
                txtGioKT.setText(shiftModel.getValueAt(r, 3).toString());
                // Khóa Mã Ca khi đang sửa (bảo vệ Khóa chính)
                txtMaCa.setEditable(false);
                txtMaCa.setBackground(new Color(235, 235, 235));
            }
        });

        // ========== SỰ KIỆN: Nút "Làm mới" -> Clear form + Mở khóa Mã Ca ==========
        btnClear.addActionListener(e -> {
            txtMaCa.setText("");
            txtTenCa.setText("");
            txtGioBD.setValue(null);
            txtGioKT.setValue(null);
            // Mở khóa Mã Ca để chuẩn bị Insert mới
            txtMaCa.setEditable(true);
            txtMaCa.setBackground(Color.WHITE);
            // Bỏ chọn dòng trên bảng
            shiftTable.clearSelection();
            txtMaCa.requestFocusInWindow();
        });

        // ========== SỰ KIỆN: Nút "Thêm / Lưu" -> Validate trống + Giả lập lưu ==========
        btnSave.addActionListener(e -> {
            String maCa  = txtMaCa.getText().trim();
            String tenCa = txtTenCa.getText().trim();
            String gioBD = txtGioBD.getText().trim();
            String gioKT = txtGioKT.getText().trim();

            // Kiểm tra không được để trống
            if (maCa.isEmpty() || tenCa.isEmpty()
                    || gioBD.isEmpty() || gioBD.contains(" ")
                    || gioKT.isEmpty() || gioKT.contains(" ")) {
                JOptionPane.showMessageDialog(
                        SwingUtilities.getWindowAncestor(panel),
                        "Vui lòng điền đầy đủ tất cả các trường!",
                        "Lỗi nhập liệu",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Kiểm tra xem đang Insert hay Update
            boolean isUpdate = !txtMaCa.isEditable();

            if (isUpdate) {
                // --- UPDATE: Tìm dòng có Mã Ca trùng và cập nhật ---
                for (int r = 0; r < shiftModel.getRowCount(); r++) {
                    if (maCa.equals(shiftModel.getValueAt(r, 0).toString())) {
                        shiftModel.setValueAt(tenCa, r, 1);
                        shiftModel.setValueAt(gioBD, r, 2);
                        shiftModel.setValueAt(gioKT, r, 3);
                        break;
                    }
                }
            } else {
                // --- INSERT: Thêm dòng mới vào bảng ---
                shiftModel.addRow(new Object[]{maCa, tenCa, gioBD, gioKT});
            }

            JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(panel),
                    "Đã lưu thông tin ca thành công!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);

            // In console để debug
            System.out.println("===== " + (isUpdate ? "CẬP NHẬT" : "THÊM MỚI") + " CA =====");
            System.out.printf("  Mã Ca   : %s%n", maCa);
            System.out.printf("  Tên Ca  : %s%n", tenCa);
            System.out.printf("  Giờ BĐ  : %s%n", gioBD);
            System.out.printf("  Giờ KT  : %s%n", gioKT);
            System.out.println("===============================");
        });

        // ========== SỰ KIỆN: Nút "Xóa" -> Confirm + Xóa dòng khỏi bảng ==========
        btnDelete.addActionListener(e -> {
            int selectedRow = shiftTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(
                        SwingUtilities.getWindowAncestor(panel),
                        "Vui lòng chọn một ca trên bảng để xóa.",
                        "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String maCaXoa = shiftModel.getValueAt(selectedRow, 0).toString();
            int confirm = JOptionPane.showConfirmDialog(
                    SwingUtilities.getWindowAncestor(panel),
                    "Bạn có chắc chắn muốn xóa ca \"" + maCaXoa + "\" không?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                shiftModel.removeRow(selectedRow);
                // Reset form sau khi xóa
                txtMaCa.setText("");
                txtTenCa.setText("");
                txtGioBD.setValue(null);
                txtGioKT.setValue(null);
                txtMaCa.setEditable(true);
                txtMaCa.setBackground(Color.WHITE);
                shiftTable.clearSelection();

                JOptionPane.showMessageDialog(
                        SwingUtilities.getWindowAncestor(panel),
                        "Đã xóa ca \"" + maCaXoa + "\" thành công!",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // ========== JSplitPane: Ghép Master + Detail ==========
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(480);
        splitPane.setResizeWeight(0.55);
        splitPane.setBorder(null);
        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    /** Tạo JFormattedTextField với MaskFormatter ##:## cho nhập giờ */
    private JFormattedTextField createTimeMaskField() {
        try {
            MaskFormatter timeMask = new MaskFormatter("##:##");
            timeMask.setPlaceholderCharacter(' ');
            JFormattedTextField field = new JFormattedTextField(timeMask);
            field.setFont(FONT_LABEL);
            field.setColumns(15);
            return field;
        } catch (ParseException ex) {
            logger.log(Level.SEVERE, "Lỗi tạo MaskFormatter cho giờ", ex);
            // Fallback: trả về field không có mask
            JFormattedTextField fallback = new JFormattedTextField();
            fallback.setFont(FONT_LABEL);
            fallback.setColumns(15);
            return fallback;
        }
    }

    // =====================================================
    //  HELPER METHODS
    // =====================================================

    /** Tạo JTable với style chung */
    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(FONT_TABLE);
        table.setRowHeight(34);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(TEXT_DARK);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFillsViewportHeight(true);

        // Header style
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_TABLE_HEADER);
        header.setBackground(PRIMARY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));
        header.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        // Alternating row color (base)
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                if (!sel) {
                    setBackground(r % 2 == 0 ? BG_CARD : HEADER_BG);
                    setForeground(TEXT_DARK);
                }
                setHorizontalAlignment(c == 0 ? LEFT : CENTER);
                return comp;
            }
        });

        return table;
    }

    /** Wrap bảng trong JScrollPane có viền */
    private JScrollPane wrapInScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        sp.getViewport().setBackground(BG_CARD);
        return sp;
    }

    /** Tạo label nhỏ */
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_DARK);
        return lbl;
    }

    /** Tạo stat label có màu */
    private JLabel createStatLabel(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(color);
        return lbl;
    }

    /** Tạo button với style chung */
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            Color originalBg;
            @Override
            public void mouseEntered(MouseEvent e) {
                originalBg = btn.getBackground();
                btn.setBackground(originalBg.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(originalBg);
            }
        });
        return btn;
    }

    /** Thêm 1 row (label + field) vào form GridBagLayout */
    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel lbl = createLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        form.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(field, gbc);
    }

    // =====================================================
    //  MAIN - Chạy độc lập để test giao diện
    // =====================================================
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
            logger.log(Level.SEVERE, "Không thể thiết lập Look and Feel", ex);
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Quản lý Lịch làm việc & Chấm công");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 700);
            frame.setMinimumSize(new Dimension(800, 500));
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new ScheduleManagementPanel());
            frame.setVisible(true);
        });
    }
}
