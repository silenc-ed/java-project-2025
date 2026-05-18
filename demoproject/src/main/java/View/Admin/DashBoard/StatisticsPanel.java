package View.Admin.DashBoard;

import Controller.Admin.DashboardDAO;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class StatisticsPanel extends JPanel {

    private JSpinner spinnerFrom, spinnerTo;
    private JLabel lblTongDonValue, lblDoanhThuValue;
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton btnLoc;

    public StatisticsPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250));

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(new Color(245, 246, 250));
        wrapper.setBorder(new EmptyBorder(20, 24, 20, 24));

        wrapper.add(buildFilterBar());
        wrapper.add(Box.createVerticalStrut(16));
        wrapper.add(buildStatCards());
        wrapper.add(Box.createVerticalStrut(16));
        wrapper.add(buildTableCard());

        add(wrapper, BorderLayout.CENTER);
        loadData();
    }

    private JPanel buildFilterBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JLabel lbl = new JLabel("Hiệu suất nhân viên");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(51, 65, 85));

        JLabel lblFrom = new JLabel("Từ ngày:");
        lblFrom.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFrom.setForeground(new Color(71, 85, 105));

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        spinnerFrom = new JSpinner(new SpinnerDateModel(cal.getTime(), null, new Date(), Calendar.DAY_OF_MONTH));
        spinnerFrom.setEditor(new JSpinner.DateEditor(spinnerFrom, "dd/MM/yyyy"));
        spinnerFrom.setPreferredSize(new Dimension(110, 28));
        spinnerFrom.addChangeListener(e -> markFilterChanged());

        JLabel lblTo = new JLabel("Đến ngày:");
        lblTo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTo.setForeground(new Color(71, 85, 105));

        spinnerTo = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        spinnerTo.setEditor(new JSpinner.DateEditor(spinnerTo, "dd/MM/yyyy"));
        spinnerTo.setPreferredSize(new Dimension(110, 28));
        spinnerTo.addChangeListener(e -> markFilterChanged());

        btnLoc = new JButton("Lọc");
        btnLoc.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLoc.setBackground(new Color(148, 163, 184)); // Default gray
        btnLoc.setForeground(Color.WHITE);
        btnLoc.setBorder(new EmptyBorder(5, 14, 5, 14));
        btnLoc.setFocusPainted(false);
        btnLoc.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLoc.addActionListener(e -> loadData());

        panel.add(lbl);
        panel.add(lblFrom);
        panel.add(spinnerFrom);
        panel.add(lblTo);
        panel.add(spinnerTo);
        panel.add(btnLoc);
        return panel;
    }

    private void markFilterChanged() {
        if (btnLoc != null) {
            btnLoc.setBackground(new Color(37, 99, 235));
        }
    }

    private JPanel buildStatCards() {
        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JPanel c1 = card("Tổng hóa đơn", "—", new Color(37, 99, 235), new Color(219, 234, 254));
        JPanel c2 = card("Tổng doanh thu (VND)", "—", new Color(5, 150, 105), new Color(209, 250, 229));

        lblTongDonValue = findLabel(c1);
        lblDoanhThuValue = findLabel(c2);

        row.add(c1);
        row.add(c2);
        return row;
    }

    private JPanel card(String title, String val, Color accent, Color tint) {
        JPanel c = new JPanel(new BorderLayout(0, 6));
        c.setBackground(Color.WHITE);
        c.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 16, 16, 16)));
        JPanel stripe = new JPanel();
        stripe.setBackground(tint);
        stripe.setPreferredSize(new Dimension(5, 0));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(new Color(100, 116, 139));
        JLabel lblVal = new JLabel(val);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblVal.setForeground(accent);
        lblVal.putClientProperty("v", Boolean.TRUE);

        c.add(stripe, BorderLayout.WEST);
        c.add(lblTitle, BorderLayout.NORTH);
        c.add(lblVal, BorderLayout.CENTER);
        return c;
    }

    private JLabel findLabel(JPanel p) {
        for (Component c : p.getComponents()) {
            if (c instanceof JLabel && Boolean.TRUE.equals(((JLabel) c).getClientProperty("v"))) {
                return (JLabel) c;
            }
        }
        return null;
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(1, 1, 1, 1)));

        String[] cols = {"STT", "Mã NV", "Họ tên NV", "Số hóa đơn đã lập", "Tổng tiền bán (VND)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(36);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(new Color(30, 41, 59));

        JTableHeader th = table.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(new Color(248, 250, 252));
        th.setForeground(new Color(71, 85, 105));
        th.setBorder(new MatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
        th.setPreferredSize(new Dimension(0, 40));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(0).setMaxWidth(50);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    public void loadData() {
        if (btnLoc != null) {
            btnLoc.setBackground(new Color(148, 163, 184));
        }
        Date from = (Date) spinnerFrom.getValue();
        Calendar calTo = Calendar.getInstance();
        calTo.setTime((Date) spinnerTo.getValue());
        calTo.add(Calendar.DAY_OF_MONTH, 1);
        Date to = calTo.getTime();

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            double td, dt;
            List<Object[]> rows;
            @Override protected Void doInBackground() {
                td = DashboardDAO.getTongDonHang(from, to);
                dt = DashboardDAO.getTongDoanhThu(from, to);
                rows = DashboardDAO.getHieuSuatNhanVien(from, to);
                return null;
            }
            @Override protected void done() {
                lblTongDonValue.setText(String.format("%,d", (long) td));
                lblDoanhThuValue.setText(DashboardDAO.formatVND(dt));

                tableModel.setRowCount(0);
                if (rows != null) {
                    for (int i = 0; i < rows.size(); i++) {
                        Object[] r = rows.get(i);
                        tableModel.addRow(new Object[]{
                                i + 1, r[0], r[1], r[2], DashboardDAO.formatVND((Double) r[3])
                        });
                    }
                }
            }
        };
        worker.execute();
    }
}
