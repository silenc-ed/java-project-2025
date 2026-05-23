package View.Admin.Attendance;

import Controller.Admin.ChamCong.ChamCongDAO;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * AttendancePanel — Quản lý ca làm việc & chấm công.
 * Layout: CardLayout gồm 2 card:
 *   Card "calendar"  → lịch tháng
 *   Card "detail"    → panel chi tiết ngày (3 tabs)
 */
public class AttendancePanel extends JPanel {

    // ── Constants ────────────────────────────────────────────────────
    private static final Color BG          = new Color(245, 246, 250);
    private static final Color WHITE       = Color.WHITE;
    private static final Color BORDER_CLR  = new Color(226, 232, 240);
    private static final Color HDR_FG      = new Color(30, 41, 59);
    private static final Color SUB_FG      = new Color(71, 85, 105);
    private static final Color TODAY_BORDER= new Color(59, 130, 246);
    private static final Color FULL_BG     = new Color(220, 252, 231);
    private static final Color FULL_FG     = new Color(22, 163, 74);
    private static final Color PART_BG     = new Color(254, 249, 195);
    private static final Color PART_FG     = new Color(217, 119, 6);
    private static final Color NONE_BG     = new Color(241, 245, 249);
    private static final Color NONE_FG     = new Color(148, 163, 184);
    private static final Color BTN_GREEN   = new Color(40, 167, 69);
    private static final Color BTN_BLUE    = new Color(0, 123, 255);
    private static final Color BTN_RED     = new Color(220, 53, 69);
    private static final Color BTN_PURPLE  = new Color(147, 51, 234);
    private static final Color OFF_BG      = new Color(254, 226, 226);
    private static final Color OFF_FG      = new Color(220, 38, 38);

    // ── State ─────────────────────────────────────────────────────────
    private CardLayout cardLayout;
    private JPanel cardContainer;
    private YearMonth currentYearMonth;
    private java.sql.Date selectedDate;
    private Map<Integer, int[]> monthSummary = new HashMap<>();
    private Map<Integer, String> monthOffDays = new HashMap<>();
    private JPanel storeOffPanel;

    // Calendar widgets
    private JPanel calendarGrid;
    private JComboBox<String> cbMonth;
    private JComboBox<String> cbYear;

    // Detail panel tabs
    private JTabbedPane tabbedPane;
    private JPanel tab1Panel, tab2Panel, tab3Panel;

    // Tab 1 state
    private DefaultTableModel tab1Model;
    private JTable tab1Table;
    private JComboBox<String> cbAddNV, cbAddCa;
    private JComboBox<String> cbFilterCa; // Bộ lọc ca
    private JLabel lblGioLamViec, lblChuCa; // Nhãn hiển thị ca & chủ ca
    private List<Object[]> currentSchedule = new ArrayList<>(); // Lịch hiện tại
    private List<Object[]> nvList = new ArrayList<>();
    private List<Object[]> caList = new ArrayList<>();
    private long defaultMaCN = 1L;

    // Tab 2 state
    private JComboBox<String> cbNV2;
    private JLabel lblNVInfo;
    private JTextField tfGioVao, tfGioRa;
    private JComboBox<String> cbTrangThaiCC;
    private JTextField tfGhiChu2;
    private DefaultTableModel tab2Model;
    private long foundMaLLV2 = -1;

    // Tab 3 state
    private DefaultTableModel tab3Model;
    private JTextField tfTenCa, tfGioBD, tfGioKT;

    // ── Constructor ───────────────────────────────────────────────────
    public AttendancePanel() {
        setLayout(new BorderLayout());
        setBackground(BG);

        currentYearMonth = YearMonth.now();
        defaultMaCN = ChamCongDAO.getFirstMaCN();

        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setBackground(BG);

        cardContainer.add(buildCalendarCard(), "calendar");
        cardContainer.add(buildDetailCard(), "detail");

        add(cardContainer, BorderLayout.CENTER);
        loadMonthData();
    }

    // ════════════════════════════════════════════════════════════════
    //  CARD 1 — CALENDAR
    // ════════════════════════════════════════════════════════════════

    private JPanel buildCalendarCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG);

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(BG);
        wrapper.setBorder(new EmptyBorder(20, 24, 20, 24));

        wrapper.add(buildCalendarHeader());
        wrapper.add(Box.createVerticalStrut(16));
        wrapper.add(buildWeekdayRow());
        wrapper.add(Box.createVerticalStrut(4));
        wrapper.add(buildCalendarGrid());

        card.add(wrapper, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildCalendarHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JLabel title = new JLabel("Quản lý chấm công");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(HDR_FG);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        // Month combo
        String[] months = {"Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6",
                           "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"};
        cbMonth = new JComboBox<>(months);
        cbMonth.setSelectedIndex(currentYearMonth.getMonthValue() - 1);
        cbMonth.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbMonth.setPreferredSize(new Dimension(100, 34));

        // Year combo (±5 years)
        int curYear = currentYearMonth.getYear();
        String[] years = new String[11];
        for (int i = 0; i < 11; i++) years[i] = String.valueOf(curYear - 5 + i);
        cbYear = new JComboBox<>(years);
        cbYear.setSelectedItem(String.valueOf(curYear));
        cbYear.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbYear.setPreferredSize(new Dimension(80, 34));

        JButton btnLoad = makeBtn("Xem lịch", BTN_BLUE);
        btnLoad.addActionListener(e -> {
            int m = cbMonth.getSelectedIndex() + 1;
            int y = Integer.parseInt((String) cbYear.getSelectedItem());
            currentYearMonth = YearMonth.of(y, m);
            loadMonthData();
        });

        right.add(cbMonth);
        right.add(cbYear);
        right.add(btnLoad);

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel buildWeekdayRow() {
        JPanel row = new JPanel(new GridLayout(1, 7, 4, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        String[] days = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "CN"};
        for (String d : days) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(d.equals("CN") ? BTN_RED : SUB_FG);
            lbl.setOpaque(true);
            lbl.setBackground(WHITE);
            lbl.setBorder(new CompoundBorder(
                new LineBorder(BORDER_CLR, 1, true), new EmptyBorder(6, 0, 6, 0)));
            row.add(lbl);
        }
        return row;
    }

    private JPanel buildCalendarGrid() {
        calendarGrid = new JPanel(new GridLayout(6, 7, 4, 4));
        calendarGrid.setOpaque(false);
        // Ô trống placeholder
        for (int i = 0; i < 42; i++) calendarGrid.add(new JPanel());
        return calendarGrid;
    }

    private void loadMonthData() {
        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() {
                Map<Integer, int[]> summary = ChamCongDAO.getMonthSummary(
                        currentYearMonth.getYear(), currentYearMonth.getMonthValue());
                Map<Integer, String> offDays = ChamCongDAO.getMonthOffDays(
                        currentYearMonth.getYear(), currentYearMonth.getMonthValue(), defaultMaCN);
                return new Object[]{summary, offDays};
            }
            @SuppressWarnings("unchecked")
            @Override protected void done() {
                try {
                    Object[] result = get();
                    monthSummary = (Map<Integer, int[]>) result[0];
                    monthOffDays = (Map<Integer, String>) result[1];
                    renderCalendar();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void renderCalendar() {
        calendarGrid.removeAll();

        LocalDate firstDay = currentYearMonth.atDay(1);
        int startOffset = firstDay.getDayOfWeek().getValue() - 1;
        int daysInMonth = currentYearMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 42; i++) {
            int dayNum = i - startOffset + 1;
            if (dayNum < 1 || dayNum > daysInMonth) {
                JPanel empty = new JPanel();
                empty.setBackground(new Color(240, 242, 245));
                empty.setBorder(new LineBorder(BORDER_CLR, 1, true));
                calendarGrid.add(empty);
            } else {
                calendarGrid.add(buildDayCell(dayNum, today));
            }
        }
        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    private JPanel buildDayCell(int dayNum, LocalDate today) {
        int[] stats = monthSummary.getOrDefault(dayNum, new int[]{0, 0});
        int total = stats[0], done = stats[1];
        LocalDate cellDate = currentYearMonth.atDay(dayNum);
        boolean isToday = cellDate.equals(today);
        boolean isFuture = cellDate.isAfter(today);

        Color bg, fg;
        String statusTxt;
        boolean isOff = monthOffDays.containsKey(dayNum);

        if (isOff) {
            bg = OFF_BG;
            fg = OFF_FG;
            String reason = monthOffDays.get(dayNum);
            statusTxt = "🚫 Nghỉ" + (reason != null && !reason.trim().isEmpty() ? ": " + reason : "");
        } else if (isFuture || total == 0) {
            bg = NONE_BG; fg = NONE_FG;
            statusTxt = total == 0 ? "" : total + " lịch";
        } else if (done == total) {
            bg = FULL_BG; fg = FULL_FG;
            statusTxt = "✓ " + done + "/" + total;
        } else if (done > 0) {
            bg = PART_BG; fg = PART_FG;
            statusTxt = "… " + done + "/" + total;
        } else {
            bg = NONE_BG; fg = NONE_FG;
            statusTxt = "0/" + total;
        }

        JPanel cell = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                if (isToday) {
                    g2.setColor(TODAY_BORDER);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                }
                g2.dispose();
            }
        };
        cell.setLayout(new BorderLayout(2, 2));
        cell.setOpaque(false);
        cell.setBorder(new EmptyBorder(6, 8, 6, 8));
        cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (isOff) {
            cell.setToolTipText("Cửa hàng nghỉ: " + monthOffDays.get(dayNum));
        }

        JLabel lblDay = new JLabel(String.valueOf(dayNum));
        lblDay.setFont(new Font("Segoe UI", isToday ? Font.BOLD : Font.PLAIN, 13));
        lblDay.setForeground(isToday ? TODAY_BORDER : HDR_FG);

        JLabel lblStats = new JLabel(statusTxt, SwingConstants.RIGHT);
        lblStats.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStats.setForeground(fg);

        cell.add(lblDay, BorderLayout.NORTH);
        cell.add(lblStats, BorderLayout.SOUTH);

        cell.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                cell.setBorder(new CompoundBorder(
                    new LineBorder(TODAY_BORDER, 1, true), new EmptyBorder(5, 7, 5, 7)));
                cell.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                cell.setBorder(new EmptyBorder(6, 8, 6, 8));
                cell.repaint();
            }
            @Override public void mouseClicked(MouseEvent e) {
                selectedDate = java.sql.Date.valueOf(currentYearMonth.atDay(dayNum));
                showDetailPanel();
            }
        });

        return cell;
    }

    // ════════════════════════════════════════════════════════════════
    //  CARD 2 — DAY DETAIL (3 TABS)
    // ════════════════════════════════════════════════════════════════

    private JPanel buildDetailCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG);
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton btnBack = makeBtn("← Quay lại lịch", new Color(100, 116, 139));
        btnBack.addActionListener(e -> {
            cardLayout.show(cardContainer, "calendar");
            loadMonthData();
        });

        JLabel lblDetailDate = new JLabel("Chi tiết ngày");
        lblDetailDate.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblDetailDate.setForeground(HDR_FG);
        lblDetailDate.setName("lblDetailDate");

        topBar.add(btnBack, BorderLayout.WEST);
        topBar.add(lblDetailDate, BorderLayout.CENTER);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        tab1Panel = buildTab1();
        tab2Panel = buildTab2();
        tab3Panel = buildTab3();

        tabbedPane.addTab("📋  Chi tiết ngày", tab1Panel);
        tabbedPane.addTab("⏱  Chấm công", tab2Panel);
        tabbedPane.addTab("⚙  Cấu hình ca", tab3Panel);

        storeOffPanel = new JPanel();
        storeOffPanel.setOpaque(true);

        card.add(topBar, BorderLayout.NORTH);
        
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG);
        
        JPanel storeOffWrapper = new JPanel(new BorderLayout());
        storeOffWrapper.setOpaque(false);
        storeOffWrapper.add(storeOffPanel, BorderLayout.CENTER);
        storeOffWrapper.add(Box.createVerticalStrut(12), BorderLayout.SOUTH);

        center.add(storeOffWrapper, BorderLayout.NORTH);
        center.add(tabbedPane, BorderLayout.CENTER);
        card.add(center, BorderLayout.CENTER);

        return card;
    }

    private void showDetailPanel() {
        updateDetailDateLabel();

        loadTab1Data();
        loadTab2History();
        loadTab3Data();

        cardLayout.show(cardContainer, "detail");
        tabbedPane.setSelectedIndex(0);
    }

    private void updateDetailDateLabel() {
        if (selectedDate == null) return;
        LocalDate ld = selectedDate.toLocalDate();
        String thu = ld.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("vi"));
        String dateStr = ld.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
        JPanel detailCard = (JPanel) cardContainer.getComponent(1);
        JPanel topBar = (JPanel) detailCard.getComponent(0);
        JLabel lbl = (JLabel) topBar.getComponent(1);
        lbl.setText("📅 Ngày " + dateStr + "  (" + capitalize(thu) + ")");
    }

    private void updateStoreOffPanel(String offReason) {
        storeOffPanel.removeAll();
        storeOffPanel.setLayout(new BorderLayout(15, 0));
        
        boolean isOff = (offReason != null);
        if (!isOff) {
            storeOffPanel.setBackground(WHITE);
            storeOffPanel.setBorder(new CompoundBorder(
                new LineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(10, 16, 10, 16)
            ));
            
            JLabel lblStatus = new JLabel("💡 Cửa hàng đang hoạt động bình thường ngày này.");
            lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblStatus.setForeground(SUB_FG);
            
            JButton btnOff = makeBtn("🚫 Báo nghỉ cửa hàng", BTN_RED);
            btnOff.addActionListener(e -> {
                JTextArea txtReason = new JTextArea(4, 30);
                txtReason.setLineWrap(true);
                txtReason.setWrapStyleWord(true);
                txtReason.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                
                // DocumentFilter to strictly limit the text area to 100 characters
                ((javax.swing.text.AbstractDocument) txtReason.getDocument()).setDocumentFilter(new javax.swing.text.DocumentFilter() {
                    @Override
                    public void insertString(FilterBypass fb, int offset, String string, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
                        if (fb.getDocument().getLength() + string.length() <= 100) {
                            super.insertString(fb, offset, string, attr);
                        } else {
                            Toolkit.getDefaultToolkit().beep();
                        }
                    }
                    @Override
                    public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
                        if (fb.getDocument().getLength() - length + text.length() <= 100) {
                            super.replace(fb, offset, length, text, attr);
                        } else {
                            Toolkit.getDefaultToolkit().beep();
                        }
                    }
                });

                JScrollPane scroll = new JScrollPane(txtReason);
                scroll.setBorder(new LineBorder(BORDER_CLR, 1, true));

                JLabel lblCount = new JLabel("0/100 ký tự");
                lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                lblCount.setForeground(SUB_FG);
                lblCount.setHorizontalAlignment(SwingConstants.RIGHT);

                txtReason.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                    private void updateCount() {
                        lblCount.setText(txtReason.getDocument().getLength() + "/100 ký tự");
                    }
                    @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updateCount(); }
                    @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updateCount(); }
                    @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateCount(); }
                });

                JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
                inputPanel.setOpaque(false);
                
                JLabel lblPrompt = new JLabel("Nhập lý do đóng cửa cửa hàng ngày này (tối đa 100 ký tự):");
                lblPrompt.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lblPrompt.setForeground(HDR_FG);
                
                inputPanel.add(lblPrompt, BorderLayout.NORTH);
                inputPanel.add(scroll, BorderLayout.CENTER);
                inputPanel.add(lblCount, BorderLayout.SOUTH);

                int opt = JOptionPane.showConfirmDialog(this, inputPanel, "Báo nghỉ cửa hàng", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (opt == JOptionPane.OK_OPTION) {
                    String reason = txtReason.getText().trim();
                    if (!reason.isEmpty()) {
                        new SwingWorker<Boolean, Void>() {
                            @Override protected Boolean doInBackground() {
                                return ChamCongDAO.baoNghiCuaHang(selectedDate, defaultMaCN, reason);
                            }
                            @Override protected void done() {
                                try {
                                    if (get()) {
                                        loadTab1Data();
                                    } else {
                                        JOptionPane.showMessageDialog(AttendancePanel.this, 
                                                "Báo nghỉ thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                                    }
                                } catch (Exception ex) { ex.printStackTrace(); }
                            }
                        }.execute();
                    }
                }
            });
            
            // Ẩn nút Báo nghỉ nếu không có quyền Sửa
            btnOff.setVisible(Controller.Admin.PermissionService.canEdit("Cham cong"));
            storeOffPanel.add(lblStatus, BorderLayout.CENTER);
            storeOffPanel.add(btnOff, BorderLayout.EAST);
        } else {
            storeOffPanel.setBackground(OFF_BG);
            storeOffPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(252, 165, 165), 1, true),
                new EmptyBorder(10, 16, 10, 16)
            ));
            
            JLabel lblStatus = new JLabel("⚠️ CỬA HÀNG ĐANG NGHỈ ĐÓNG CỬA: " + offReason);
            lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblStatus.setForeground(OFF_FG);
            
            JButton btnOn = makeBtn("🔓 Mở cửa hoạt động lại", new Color(71, 85, 105));
            btnOn.addActionListener(e -> {
                int opt = JOptionPane.showConfirmDialog(this,
                        "Bạn có chắc chắn muốn hủy báo nghỉ và mở cửa hoạt động lại trong ngày này?",
                        "Xác nhận mở cửa lại", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    new SwingWorker<Boolean, Void>() {
                        @Override protected Boolean doInBackground() {
                            return ChamCongDAO.huyNghiCuaHang(selectedDate, defaultMaCN);
                        }
                        @Override protected void done() {
                            try {
                                if (get()) {
                                    loadTab1Data();
                                } else {
                                    JOptionPane.showMessageDialog(AttendancePanel.this,
                                            "Hủy báo nghỉ thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                                }
                            } catch (Exception ex) { ex.printStackTrace(); }
                        }
                    }.execute();
                }
            });
            
            // Ẩn nút Mở cửa nếu không có quyền Sửa
            btnOn.setVisible(Controller.Admin.PermissionService.canEdit("Cham cong"));
            storeOffPanel.add(lblStatus, BorderLayout.CENTER);
            storeOffPanel.add(btnOn, BorderLayout.EAST);
        }
        
        storeOffPanel.revalidate();
        storeOffPanel.repaint();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ──────────────────────────────────────────────────────────────
    //  TAB 1: CHI TIẾT NGÀY
    // ──────────────────────────────────────────────────────────────

    private JPanel buildTab1() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(16, 0, 0, 0));

        // Filter Panel
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.setBackground(WHITE);
        filterPanel.setBorder(new CompoundBorder(
            new LineBorder(BORDER_CLR, 1, true), new EmptyBorder(10, 16, 10, 16)));

        JPanel leftFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 6));
        leftFilter.setOpaque(false);

        cbFilterCa = new JComboBox<>();
        cbFilterCa.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbFilterCa.setPreferredSize(new Dimension(160, 32));
        cbFilterCa.addActionListener(e -> filterScheduleBySelectedCa());

        lblGioLamViec = new JLabel("⏱ Giờ làm việc: —");
        lblGioLamViec.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGioLamViec.setForeground(SUB_FG);

        lblChuCa = new JLabel("👑 Chủ ca: —");
        lblChuCa.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblChuCa.setForeground(new Color(124, 58, 237)); // BTN_PURPLE

        leftFilter.add(new JLabel("Lọc theo Ca:"));
        leftFilter.add(cbFilterCa);
        leftFilter.add(Box.createHorizontalStrut(15));
        leftFilter.add(lblGioLamViec);
        leftFilter.add(Box.createHorizontalStrut(20));
        leftFilter.add(lblChuCa);

        filterPanel.add(leftFilter, BorderLayout.WEST);
        
        JPanel rightFilter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        rightFilter.setOpaque(false);

        // Bảng lịch làm việc
        String[] cols = {"Mã LLV", "Họ tên", "Ca làm việc", "Giờ BĐ", "Giờ KT", "Vai trò", "Trạng thái"};
        tab1Model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tab1Table = new JTable(tab1Model);
        styleTable(tab1Table);
        tab1Table.getColumnModel().getColumn(0).setPreferredWidth(60);
        tab1Table.getColumnModel().getColumn(0).setMaxWidth(70);

        JScrollPane scroll1 = new JScrollPane(tab1Table);
        scroll1.setBorder(new LineBorder(BORDER_CLR, 1, true));
        scroll1.getViewport().setBackground(WHITE);

        // Panel thêm NV vào lịch
        JPanel addPanel = buildTab1AddForm();

        // Nút đổi ca
        JButton btnEdit = makeBtn("✏️  Đổi ca làm", BTN_BLUE);
        btnEdit.addActionListener(e -> {
            int row = tab1Table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần đổi ca làm.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            long maLLV = (Long) tab1Model.getValueAt(row, 0);
            
            String empName = "";
            long curMaCa = -1;
            for (Object[] r : currentSchedule) {
                if ((Long)r[0] == maLLV) {
                    empName = (String) r[2];
                    curMaCa = (Long) r[12];
                    break;
                }
            }

            JComboBox<String> comboCa = new JComboBox<>();
            for (Object[] ca : caList) {
                comboCa.addItem(ca[1].toString() + " (" + ca[2] + " - " + ca[3] + ")");
            }
            
            for (int i = 0; i < caList.size(); i++) {
                if ((Long)caList.get(i)[0] == curMaCa) {
                    comboCa.setSelectedIndex(i);
                    break;
                }
            }

            Object[] message = {
                "Nhân viên: " + empName,
                "Chọn ca làm mới:", comboCa
            };

            int option = JOptionPane.showConfirmDialog(this, message, "Đổi ca làm việc", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (option == JOptionPane.OK_OPTION) {
                long newMaCa = (Long) caList.get(comboCa.getSelectedIndex())[0];
                new SwingWorker<Boolean, Void>() {
                    @Override protected Boolean doInBackground() {
                        return ChamCongDAO.suaLichLamViec(maLLV, newMaCa);
                    }
                    @Override protected void done() {
                        try {
                            if (get()) {
                                JOptionPane.showMessageDialog(AttendancePanel.this, "Đổi ca thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                                loadTab1Data();
                            } else {
                                JOptionPane.showMessageDialog(AttendancePanel.this, "Đổi ca thất bại (có thể trùng lịch).", "Lỗi", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) { ex.printStackTrace(); }
                    }
                }.execute();
            }
        });

        // Nút Chỉ định chủ ca
        JButton btnSetLeader = makeBtn("⭐  Chỉ định chủ ca", BTN_PURPLE);
        btnSetLeader.addActionListener(e -> {
            int row = tab1Table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên muốn chỉ định làm chủ ca.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            long maLLV = (Long) tab1Model.getValueAt(row, 0);
            
            long maCa = -1;
            String empName = "";
            for (Object[] r : currentSchedule) {
                if ((Long)r[0] == maLLV) {
                    maCa = (Long) r[12];
                    empName = (String) r[2];
                    break;
                }
            }

            int opt = JOptionPane.showConfirmDialog(this,
                    "Bạn muốn chỉ định " + empName + " làm chủ ca cho ca này?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (opt != JOptionPane.YES_OPTION) return;

            final long finalMaCa = maCa;
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() {
                    return ChamCongDAO.chiDinhChuCa(maLLV, finalMaCa, selectedDate);
                }
                @Override protected void done() {
                    try {
                        if (get()) {
                            loadTab1Data();
                        } else {
                            JOptionPane.showMessageDialog(AttendancePanel.this, "Chỉ định chủ ca thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }.execute();
        });

        // Nút xóa
        JButton btnDel = makeBtn("🗑  Xóa khỏi lịch", BTN_RED);
        btnDel.addActionListener(e -> {
            int row = tab1Table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            long maLLV = (Long) tab1Model.getValueAt(row, 0);
            int opt = JOptionPane.showConfirmDialog(this,
                    "Xóa lịch này sẽ xóa cả chấm công liên quan. Tiếp tục?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (opt != JOptionPane.YES_OPTION) return;
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() { return ChamCongDAO.xoaLichLamViec(maLLV); }
                @Override protected void done() {
                    try {
                        if (get()) loadTab1Data();
                        else JOptionPane.showMessageDialog(AttendancePanel.this, "Xóa thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }.execute();
        });

        // Ẩn các nút theo quyền
        boolean canEditCC = Controller.Admin.PermissionService.canEdit("Cham cong");
        boolean canDeleteCC = Controller.Admin.PermissionService.canDelete("Cham cong");
        btnEdit.setVisible(canEditCC);
        btnSetLeader.setVisible(canEditCC);
        btnDel.setVisible(canDeleteCC);

        rightFilter.add(btnEdit);
        rightFilter.add(btnSetLeader);
        rightFilter.add(btnDel);
        filterPanel.add(rightFilter, BorderLayout.EAST);

        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(scroll1, BorderLayout.CENTER);
        
        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);
        bottom.add(addPanel, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildTab1AddForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(WHITE);
        form.setBorder(new CompoundBorder(
            new LineBorder(BORDER_CLR, 1, true), new EmptyBorder(12, 16, 12, 16)));

        JLabel lblTitle = new JLabel("Thêm nhân viên vào lịch ngày này");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(HDR_FG);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbAddNV = new JComboBox<>();
        cbAddNV.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbAddNV.setPreferredSize(new Dimension(200, 32));

        cbAddCa = new JComboBox<>();
        cbAddCa.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbAddCa.setPreferredSize(new Dimension(150, 32));

        JButton btnAdd1 = makeBtn("+ Thêm vào lịch", BTN_GREEN);
        btnAdd1.addActionListener(e -> {
            if (cbAddNV.getSelectedIndex() < 0 || cbAddCa.getSelectedIndex() < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn NV và ca.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            long maNV = (Long) nvList.get(cbAddNV.getSelectedIndex())[0];
            long maCa = (Long) caList.get(cbAddCa.getSelectedIndex())[0];
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() {
                    return ChamCongDAO.themLichLamViec(maNV, maCa, defaultMaCN, selectedDate);
                }
                @Override protected void done() {
                    try {
                        if (get()) loadTab1Data();
                        else JOptionPane.showMessageDialog(AttendancePanel.this, "Thêm thất bại (có thể NV đã có lịch ca này ngày này).", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }.execute();
        });

        // Ẩn các thành phần Thêm nếu không có quyền Thêm
        boolean canAddCC = Controller.Admin.PermissionService.canAdd("Cham cong");
        row.add(new JLabel("Nhân viên:"));
        row.add(cbAddNV);
        row.add(new JLabel("Ca:"));
        row.add(cbAddCa);
        if (canAddCC) row.add(btnAdd1);

        form.add(lblTitle);
        form.add(Box.createVerticalStrut(8));
        form.add(row);
        return form;
    }

    private void filterScheduleBySelectedCa() {
        if (cbFilterCa == null || tab1Model == null) return;
        tab1Model.setRowCount(0);
        int idx = cbFilterCa.getSelectedIndex();
        if (idx <= 0) { // Tất cả các ca
            lblGioLamViec.setText("⏱ Giờ làm việc: —");
            lblChuCa.setText("👑 Chủ ca: —");
            for (Object[] r : currentSchedule) {
                String vaiTro = ((Integer)r[10] == 1) ? "⭐ Chủ ca" : "Nhân viên";
                tab1Model.addRow(new Object[]{r[0], r[2], r[3], r[4], r[5], vaiTro, r[8]});
            }
        } else {
            Object[] selectedCa = caList.get(idx - 1);
            long maCa = (Long) selectedCa[0];
            String gioBD = (String) selectedCa[2];
            String gioKT = (String) selectedCa[3];

            lblGioLamViec.setText("⏱ Giờ làm việc: " + gioBD + " - " + gioKT);

            String chuCaName = "Chưa chỉ định";
            for (Object[] r : currentSchedule) {
                long rMaCa = (Long) r[12];
                if (rMaCa == maCa) {
                    if ((Integer)r[10] == 1) {
                        chuCaName = (String) r[2];
                    }
                }
            }
            lblChuCa.setText("👑 Chủ ca: " + chuCaName);

            for (Object[] r : currentSchedule) {
                long rMaCa = (Long) r[12];
                if (rMaCa == maCa) {
                    String vaiTro = ((Integer)r[10] == 1) ? "⭐ Chủ ca" : "Nhân viên";
                    tab1Model.addRow(new Object[]{r[0], r[2], r[3], r[4], r[5], vaiTro, r[8]});
                }
            }
        }
    }

    private void loadTab1Data() {
        new SwingWorker<Object[], Void>() {
            List<Object[]> schedule;
            String offReason;
            @Override protected Object[] doInBackground() {
                schedule = ChamCongDAO.getDaySchedule(selectedDate);
                nvList = ChamCongDAO.getAllNhanVien();
                caList = ChamCongDAO.getAllCaLamViec();
                offReason = ChamCongDAO.getNgayNghiLyDo(selectedDate, defaultMaCN);
                return new Object[]{schedule, offReason};
            }
            @Override protected void done() {
                try {
                    Object[] res = get();
                    currentSchedule = (List<Object[]>) res[0];
                    String reason = (String) res[1];

                    int prevIdx = cbFilterCa.getSelectedIndex();

                    cbFilterCa.removeAllItems();
                    cbFilterCa.addItem("-- Tất cả các ca --");
                    for (Object[] ca : caList) {
                        cbFilterCa.addItem(ca[1].toString());
                    }

                    if (prevIdx >= 0 && prevIdx < cbFilterCa.getItemCount()) {
                        cbFilterCa.setSelectedIndex(prevIdx);
                    } else {
                        cbFilterCa.setSelectedIndex(0);
                    }

                    filterScheduleBySelectedCa();

                    cbAddNV.removeAllItems();
                    for (Object[] nv : nvList) cbAddNV.addItem(nv[1].toString());

                    cbNV2.removeAllItems();
                    cbNV2.addItem("-- Chọn nhân viên --");
                    for (Object[] nv : nvList) {
                        cbNV2.addItem(nv[0] + " - " + nv[1]);
                    }

                    cbAddCa.removeAllItems();
                    for (Object[] ca : caList) cbAddCa.addItem(ca[1].toString());

                    updateStoreOffPanel(reason);
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    // ──────────────────────────────────────────────────────────────
    //  TAB 2: CHẤM CÔNG
    // ──────────────────────────────────────────────────────────────

    private JPanel buildTab2() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(16, 0, 0, 0));

        JPanel form = new JPanel();
        form.setBackground(WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new CompoundBorder(
            new LineBorder(BORDER_CLR, 1, true), new EmptyBorder(16, 20, 16, 20)));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.setOpaque(false);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbNV2 = new JComboBox<>();
        cbNV2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbNV2.setPreferredSize(new Dimension(220, 32));

        JButton btnFind = makeBtn("🔍 Tìm", BTN_BLUE);

        lblNVInfo = new JLabel("—  Chưa tìm kiếm");
        lblNVInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNVInfo.setForeground(SUB_FG);

        btnFind.addActionListener(e -> findNVForTab2());
        cbNV2.addActionListener(e -> findNVForTab2());

        row1.add(new JLabel("Nhân viên:"));
        row1.add(cbNV2);
        row1.add(btnFind);
        row1.add(lblNVInfo);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.setOpaque(false);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);

        tfGioVao = new JTextField(7);
        tfGioVao.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "HH:mm");
        tfGioVao.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        tfGioRa = new JTextField(7);
        tfGioRa.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "HH:mm");
        tfGioRa.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        cbTrangThaiCC = new JComboBox<>(new String[]{"Đã duyệt", "Chưa duyệt", "Vắng mặt", "Đi muộn"});
        cbTrangThaiCC.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbTrangThaiCC.setPreferredSize(new Dimension(130, 32));

        tfGhiChu2 = new JTextField(15);
        tfGhiChu2.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ghi chú...");
        tfGhiChu2.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        row2.add(new JLabel("Giờ vào:"));
        row2.add(tfGioVao);
        row2.add(new JLabel("Giờ ra:"));
        row2.add(tfGioRa);
        row2.add(new JLabel("Trạng thái:"));
        row2.add(cbTrangThaiCC);
        row2.add(new JLabel("Ghi chú:"));
        row2.add(tfGhiChu2);

        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        row3.setOpaque(false);
        row3.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton btnSave2 = makeBtn("💾  Lưu chấm công", BTN_GREEN);
        btnSave2.addActionListener(e -> saveChamCong());
        row3.add(btnSave2);

        form.add(row1);
        form.add(Box.createVerticalStrut(4));
        form.add(row2);
        form.add(Box.createVerticalStrut(8));
        form.add(row3);

        JLabel lblHist = new JLabel("Lịch sử chấm công trong ngày");
        lblHist.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHist.setForeground(HDR_FG);

        String[] cols2 = {"Mã LLV", "Họ tên", "Ca", "Giờ vào", "Giờ ra", "Trạng thái", "Ghi chú"};
        tab2Model = new DefaultTableModel(cols2, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tab2Table = new JTable(tab2Model);
        styleTable(tab2Table);
        tab2Table.getColumnModel().getColumn(0).setMaxWidth(65);

        JScrollPane scroll2 = new JScrollPane(tab2Table);
        scroll2.setBorder(new LineBorder(BORDER_CLR, 1, true));
        scroll2.getViewport().setBackground(WHITE);

        JPanel histPanel = new JPanel(new BorderLayout(0, 8));
        histPanel.setOpaque(false);
        histPanel.add(lblHist, BorderLayout.NORTH);
        histPanel.add(scroll2, BorderLayout.CENTER);

        panel.add(form, BorderLayout.NORTH);
        panel.add(histPanel, BorderLayout.CENTER);
        return panel;
    }

    private void findNVForTab2() {
        String selected = (String) cbNV2.getSelectedItem();
        if (selected == null || !selected.contains(" - ")) {
            lblNVInfo.setText("—  Chưa tìm kiếm");
            lblNVInfo.setForeground(SUB_FG);
            foundMaLLV2 = -1;
            return;
        }
        long maNV;
        try {
            String[] parts = selected.split(" - ");
            maNV = Long.parseLong(parts[0].trim());
        } catch (Exception ex) {
            return;
        }
        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() {
                return ChamCongDAO.findLichByNVAndDate(maNV, selectedDate);
            }
            @Override protected void done() {
                try {
                    Object[] data = get();
                    if (data == null) {
                        lblNVInfo.setForeground(BTN_RED);
                        lblNVInfo.setText("✗ Không có lịch làm việc ngày này");
                        foundMaLLV2 = -1;
                        tfGioVao.setText("");
                        tfGioRa.setText("");
                        cbTrangThaiCC.setSelectedIndex(1); // Mặc định 'Chưa duyệt'
                        tfGhiChu2.setText("");
                    } else {
                        foundMaLLV2 = (Long) data[0];
                        String info = "✓  " + data[2] + "   |   Ca: " + data[3]
                                + "  (" + data[4] + " - " + data[5] + ")";
                        lblNVInfo.setForeground(FULL_FG);
                        lblNVInfo.setText(info);
                        Timestamp vao = (Timestamp) data[7];
                        Timestamp ra  = (Timestamp) data[8];
                        tfGioVao.setText(vao != null ? vao.toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "");
                        tfGioRa.setText(ra != null ? ra.toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "");
                        cbTrangThaiCC.setSelectedItem(data[9]);
                        tfGhiChu2.setText(data[10] != null ? data[10].toString() : "");
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void saveChamCong() {
        if (foundMaLLV2 == -1) {
            JOptionPane.showMessageDialog(this, "Chưa tìm thấy lịch làm việc hợp lệ.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String vaoStr = tfGioVao.getText().trim();
        String raStr  = tfGioRa.getText().trim();
        if (vaoStr.isEmpty() || raStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập giờ vào và giờ ra.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Timestamp tsVao, tsRa;
        try {
            LocalDate date = selectedDate.toLocalDate();
            tsVao = Timestamp.valueOf(date.atTime(LocalTime.parse(vaoStr, DateTimeFormatter.ofPattern("HH:mm"))));
            tsRa  = Timestamp.valueOf(date.atTime(LocalTime.parse(raStr,  DateTimeFormatter.ofPattern("HH:mm"))));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Giờ không hợp lệ (định dạng HH:mm).", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String tt     = cbTrangThaiCC.getSelectedItem().toString();
        String ghiChu = tfGhiChu2.getText().trim();
        final long maLLV = foundMaLLV2;

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                return ChamCongDAO.chamCong(maLLV, tsVao, tsRa, tt, ghiChu);
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(AttendancePanel.this,
                                "Chấm công thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        loadTab2History();
                    } else {
                        JOptionPane.showMessageDialog(AttendancePanel.this, "Lưu thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void loadTab2History() {
        new SwingWorker<List<Object[]>, Void>() {
            @Override protected List<Object[]> doInBackground() {
                return ChamCongDAO.getDaySchedule(selectedDate);
            }
            @Override protected void done() {
                try {
                    tab2Model.setRowCount(0);
                    for (Object[] r : get()) {
                        String vao = r[6] != null ? ((Timestamp)r[6]).toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "—";
                        String ra  = r[7] != null ? ((Timestamp)r[7]).toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "—";
                        tab2Model.addRow(new Object[]{r[0], r[2], r[3], vao, ra, r[8], r[9]});
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    // ──────────────────────────────────────────────────────────────
    //  TAB 3: CẤU HÌNH CA LÀM VIỆC
    // ──────────────────────────────────────────────────────────────

    private JPanel buildTab3() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(16, 0, 0, 0));

        String[] cols3 = {"Mã Ca", "Tên ca", "Giờ bắt đầu", "Giờ kết thúc"};
        tab3Model = new DefaultTableModel(cols3, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tab3Table = new JTable(tab3Model);
        styleTable(tab3Table);
        tab3Table.getColumnModel().getColumn(0).setMaxWidth(70);

        JScrollPane scroll3 = new JScrollPane(tab3Table);
        scroll3.setBorder(new LineBorder(BORDER_CLR, 1, true));
        scroll3.getViewport().setBackground(WHITE);
        scroll3.setPreferredSize(new Dimension(0, 200));

        JButton btnDelCa = makeBtn("🗑  Xóa ca", BTN_RED);
        btnDelCa.addActionListener(e -> {
            int row = tab3Table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn ca cần xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            long maCa = (Long) tab3Model.getValueAt(row, 0);
            int opt = JOptionPane.showConfirmDialog(this,
                    "Xóa ca này? Lưu ý: Không xóa được nếu còn lịch làm việc dùng ca này.",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (opt != JOptionPane.YES_OPTION) return;
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() { return ChamCongDAO.xoaCaLamViec(maCa); }
                @Override protected void done() {
                    try {
                        if (get()) loadTab3Data();
                        else JOptionPane.showMessageDialog(AttendancePanel.this,
                                "Không thể xóa (ca đang được sử dụng trong lịch làm việc).", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }.execute();
        });

        JPanel header3 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        header3.setOpaque(false);
        header3.add(btnDelCa);
        panel.add(header3, BorderLayout.NORTH);

        JPanel addCaForm = new JPanel();
        addCaForm.setBackground(WHITE);
        addCaForm.setLayout(new BoxLayout(addCaForm, BoxLayout.Y_AXIS));
        addCaForm.setBorder(new CompoundBorder(
            new LineBorder(BORDER_CLR, 1, true), new EmptyBorder(14, 18, 14, 18)));

        JLabel lblAddCaTitle = new JLabel("Thêm ca mới  (Mã ca tự động sinh)");
        lblAddCaTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblAddCaTitle.setForeground(HDR_FG);
        lblAddCaTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel rowCa = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        rowCa.setOpaque(false);
        rowCa.setAlignmentX(Component.LEFT_ALIGNMENT);

        tfTenCa = new JTextField(14);
        tfTenCa.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "VD: Ca Sáng");
        tfTenCa.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        tfGioBD = new JTextField(6);
        tfGioBD.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "HH:mm");
        tfGioBD.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        tfGioKT = new JTextField(6);
        tfGioKT.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "HH:mm");
        tfGioKT.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton btnAddCa = makeBtn("+ Thêm ca", BTN_GREEN);
        btnAddCa.addActionListener(e -> {
            String ten = tfTenCa.getText().trim();
            String bd  = tfGioBD.getText().trim();
            String kt  = tfGioKT.getText().trim();
            if (ten.isEmpty() || bd.isEmpty() || kt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tên ca và giờ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() {
                    return ChamCongDAO.themCaLamViec(ten, bd, kt);
                }
                @Override protected void done() {
                    try {
                        if (get()) {
                            tfTenCa.setText(""); tfGioBD.setText(""); tfGioKT.setText("");
                            loadTab3Data();
                            caList = ChamCongDAO.getAllCaLamViec();
                            cbAddCa.removeAllItems();
                            for (Object[] ca : caList) cbAddCa.addItem(ca[1].toString());
                        } else {
                            JOptionPane.showMessageDialog(AttendancePanel.this, "Thêm ca thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            }.execute();
        });

        rowCa.add(new JLabel("Tên ca:"));
        rowCa.add(tfTenCa);
        rowCa.add(new JLabel("Giờ BĐ:"));
        rowCa.add(tfGioBD);
        rowCa.add(new JLabel("Giờ KT:"));
        rowCa.add(tfGioKT);
        rowCa.add(btnAddCa);

        addCaForm.add(lblAddCaTitle);
        addCaForm.add(Box.createVerticalStrut(8));
        addCaForm.add(rowCa);

        JPanel tableArea = new JPanel(new BorderLayout(0, 6));
        tableArea.setOpaque(false);
        tableArea.add(scroll3, BorderLayout.CENTER);

        panel.add(tableArea, BorderLayout.CENTER);
        panel.add(addCaForm, BorderLayout.SOUTH);
        return panel;
    }

    private void loadTab3Data() {
        new SwingWorker<List<Object[]>, Void>() {
            @Override protected List<Object[]> doInBackground() {
                return ChamCongDAO.getAllCaLamViec();
            }
            @Override protected void done() {
                try {
                    tab3Model.setRowCount(0);
                    for (Object[] r : get()) {
                        tab3Model.addRow(new Object[]{r[0], r[1], r[2], r[3]});
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    // ════════════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════════════

    private void styleTable(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(36);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(241, 245, 249));
        t.setSelectionBackground(new Color(239, 246, 255));
        t.setSelectionForeground(HDR_FG);
        JTableHeader h = t.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 12));
        h.setBackground(new Color(248, 250, 252));
        h.setForeground(SUB_FG);
        h.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_CLR));
        h.setPreferredSize(new Dimension(0, 38));
    }

    private JButton makeBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        View.Admin.UIUtils.styleButton(btn);
        return btn;
    }
}
