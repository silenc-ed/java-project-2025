package View.Admin.DashBoard;

import Controller.Admin.DashboardDAO;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class PopularProductPanel extends JPanel {

    private JSpinner spinnerFrom;
    private JSpinner spinnerTo;
    private JLabel lblTongSpValue;
    private JLabel lblDoanhThuSpValue;
    private JLabel lblSoLuongBanValue;
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton btnToggleMode;

    private boolean isByCategory = false;

    public PopularProductPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250));

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(new Color(245, 246, 250));
        wrapper.setBorder(new EmptyBorder(20, 24, 20, 24));

        wrapper.add(buildFilterBar());
        wrapper.add(Box.createVerticalStrut(16));
        wrapper.add(buildSummaryCards());
        wrapper.add(Box.createVerticalStrut(16));
        wrapper.add(buildTablePanel());
        add(wrapper, BorderLayout.CENTER);

        loadData();
    }

    private JPanel buildFilterBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel("Lọc theo thời gian:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(51, 65, 85));

        JLabel lblFrom = new JLabel("Từ ngày:");
        lblFrom.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFrom.setForeground(new Color(71, 85, 105));

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -3);
        spinnerFrom = new JSpinner(new SpinnerDateModel(cal.getTime(), null, new Date(), Calendar.DAY_OF_MONTH));
        spinnerFrom.setEditor(new JSpinner.DateEditor(spinnerFrom, "dd/MM/yyyy"));
        spinnerFrom.setPreferredSize(new Dimension(110, 28));

        JLabel lblTo = new JLabel("Đến ngày:");
        lblTo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTo.setForeground(new Color(71, 85, 105));

        spinnerTo = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        spinnerTo.setEditor(new JSpinner.DateEditor(spinnerTo, "dd/MM/yyyy"));
        spinnerTo.setPreferredSize(new Dimension(110, 28));

        JButton btnLoc = new JButton("Lọc");
        btnLoc.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLoc.setBackground(new Color(148, 163, 184)); // default gray
        btnLoc.setForeground(Color.WHITE);
        btnLoc.setBorder(new EmptyBorder(5, 14, 5, 14));
        btnLoc.setFocusPainted(false);
        btnLoc.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLoc.addActionListener(e -> {
            btnLoc.setBackground(new Color(148, 163, 184)); // reset to gray
            loadData();
        });

        spinnerFrom.addChangeListener(e -> btnLoc.setBackground(new Color(37, 99, 235))); // blue on change
        spinnerTo.addChangeListener(e -> btnLoc.setBackground(new Color(37, 99, 235))); // blue on change

        btnToggleMode = new JButton("Loại sản phẩm");
        styleToggleBtn(btnToggleMode, false);
        btnToggleMode.addActionListener(e -> toggleMode());

        panel.add(lbl);
        panel.add(lblFrom);
        panel.add(spinnerFrom);
        panel.add(lblTo);
        panel.add(spinnerTo);
        panel.add(btnLoc);
        panel.add(btnToggleMode);
        return panel;
    }

    private void styleToggleBtn(JButton btn, boolean active) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (active) {
            btn.setBackground(new Color(124, 58, 237));
            btn.setForeground(Color.WHITE);
            btn.setBorder(new CompoundBorder(
                    new LineBorder(new Color(109, 40, 217), 1, true),
                    new EmptyBorder(5, 14, 5, 14)));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(71, 85, 105));
            btn.setBorder(new CompoundBorder(
                    new LineBorder(new Color(203, 213, 225), 1, true),
                    new EmptyBorder(5, 14, 5, 14)));
        }
    }

    private void toggleMode() {
        isByCategory = !isByCategory;
        if (isByCategory) {
            btnToggleMode.setText("Sản phẩm");
            styleToggleBtn(btnToggleMode, true);
            updateTableColumns(new String[]{"#", "Loại sản phẩm", "Số lượng bán", "Doanh thu (VND)"});
        } else {
            btnToggleMode.setText("Loại sản phẩm");
            styleToggleBtn(btnToggleMode, false);
            updateTableColumns(new String[]{"#", "Tên sản phẩm", "Danh mục", "Số lượng bán", "Doanh thu (VND)"});
        }
        loadData();
    }

    private void updateTableColumns(String[] cols) {
        tableModel.setColumnCount(0);
        for (String col : cols) tableModel.addColumn(col);
        applyColumnRenderers();
    }

    private void applyColumnRenderers() {
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);

        int colCount = tableModel.getColumnCount();
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(colCount - 2).setCellRenderer(center);
        table.getColumnModel().getColumn(colCount - 1).setCellRenderer(right);
    }

    private JPanel buildSummaryCards() {
        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel c1 = buildCard("Số mặt hàng bán ra", "—", new Color(37, 99, 235), new Color(219, 234, 254));
        JPanel c2 = buildCard("Tổng doanh thu (VND)", "—", new Color(5, 150, 105), new Color(209, 250, 229));
        JPanel c3 = buildCard("Tổng lượt bán", "—", new Color(245, 158, 11), new Color(254, 243, 199));

        lblTongSpValue = findLabel(c1);
        lblDoanhThuSpValue = findLabel(c2);
        lblSoLuongBanValue = findLabel(c3);

        row.add(c1);
        row.add(c2);
        row.add(c3);
        return row;
    }

    private JPanel buildCard(String title, String val, Color accent, Color tint) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(14, 14, 14, 14)));

        JPanel stripe = new JPanel();
        stripe.setBackground(tint);
        stripe.setPreferredSize(new Dimension(5, 0));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitle.setForeground(new Color(100, 116, 139));

        JLabel lblVal = new JLabel(val);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblVal.setForeground(accent);
        lblVal.putClientProperty("valueLabel", Boolean.TRUE);

        card.add(stripe, BorderLayout.WEST);
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        return card;
    }

    private JLabel findLabel(JPanel card) {
        for (Component c : card.getComponents()) {
            if (c instanceof JLabel && Boolean.TRUE.equals(((JLabel) c).getClientProperty("valueLabel")))
                return (JLabel) c;
        }
        return null;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 16, 16, 16)));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Top sản phẩm thịnh hành");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(new Color(30, 41, 59));
        title.setBorder(new EmptyBorder(0, 0, 12, 0));

        String[] cols = {"#", "Tên sản phẩm", "Danh mục", "Số lượng bán", "Doanh thu (VND)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(36);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(241, 245, 249));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setForeground(new Color(71, 85, 105));
        table.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(new Color(30, 41, 59));

        applyColumnRenderers();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    public void loadData() {
        Date from = (Date) spinnerFrom.getValue();
        Date to = nextDay((Date) spinnerTo.getValue());
        boolean byCategory = isByCategory;

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            List<Object[]> rows;
            long tongLuotBan;
            double tongDoanhThu;

            @Override
            protected Void doInBackground() {
                tongLuotBan = DashboardDAO.getTongSanPhamBan(from, to);
                tongDoanhThu = DashboardDAO.getTongDoanhThu(from, to);
                if (byCategory) {
                    rows = DashboardDAO.getTopLoaiSanPhamThinhHanh(from, to, 10);
                } else {
                    rows = DashboardDAO.getTopSanPhamThinhHanh(from, to, 10);
                }
                return null;
            }

            @Override
            protected void done() {
                if (lblTongSpValue != null) lblTongSpValue.setText(String.valueOf(rows.size()));
                if (lblDoanhThuSpValue != null) lblDoanhThuSpValue.setText(DashboardDAO.formatVND(tongDoanhThu));
                if (lblSoLuongBanValue != null) lblSoLuongBanValue.setText(String.format("%,d", tongLuotBan));

                tableModel.setRowCount(0);

                if (byCategory) {
                    for (int i = 0; i < rows.size(); i++) {
                        Object[] r = rows.get(i);
                        long soLuong = ((Number) r[1]).longValue();
                        long dthu = ((Number) r[2]).longValue();
                        tableModel.addRow(new Object[]{
                            i + 1,
                            r[0],
                            String.format("%,d", soLuong),
                            DashboardDAO.formatVND((double) dthu)
                        });
                    }
                } else {
                    for (int i = 0; i < rows.size(); i++) {
                        Object[] r = rows.get(i);
                        long soLuong = ((Number) r[2]).longValue();
                        long dthu = ((Number) r[3]).longValue();
                        tableModel.addRow(new Object[]{
                            i + 1,
                            r[0],
                            r[1],
                            String.format("%,d", soLuong),
                            DashboardDAO.formatVND((double) dthu)
                        });
                    }
                }
            }
        };
        worker.execute();
    }

    private Date nextDay(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(Calendar.DAY_OF_MONTH, 1);
        return c.getTime();
    }

    public JSpinner getSpinnerFrom() { return spinnerFrom; }
    public JSpinner getSpinnerTo() { return spinnerTo; }
}
