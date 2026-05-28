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

    private JSpinner spinnerFrom;
    private JSpinner spinnerTo;
    private JLabel lblTongDonValue;
    private JLabel lblDoanhThuValue;
    private DefaultTableModel tableModel;
    private JComboBox<String> cbSort;

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
        wrapper.add(buildOrderTable());
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
        cal.add(Calendar.MONTH, -1);
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
        View.Admin.UIUtils.styleButton(btnLoc);
        btnLoc.addActionListener(e -> loadData());

        cbSort = new JComboBox<>(new String[]{
            "Mới nhất", "Cũ nhất", "Thành tiền giảm dần", "Thành tiền tăng dần"
        });
        cbSort.setPreferredSize(new Dimension(160, 28));
        cbSort.addActionListener(e -> loadData());

        panel.add(lbl);
        panel.add(lblFrom);
        panel.add(spinnerFrom);
        panel.add(lblTo);
        panel.add(spinnerTo);
        panel.add(cbSort);
        panel.add(btnLoc);
        return panel;
    }

    private JPanel buildStatCards() {
        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel c1 = card("Tổng hóa đơn", "—", new Color(37, 99, 235), new Color(219, 234, 254));
        JPanel c2 = card("Tổng doanh thu (VND)", "—", new Color(5, 150, 105), new Color(209, 250, 229));

        lblTongDonValue = findLabel(c1);
        lblDoanhThuValue = findLabel(c2);

        row.add(c1);
        row.add(c2);
        return row;
    }

    private JPanel card(String title, String val, Color accent, Color tint) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 12, 12, 12)));

        JPanel stripe = new JPanel();
        stripe.setBackground(tint);
        stripe.setPreferredSize(new Dimension(4, 0));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitle.setForeground(new Color(100, 116, 139));

        JLabel lblVal = new JLabel(val);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 26));
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

    private JPanel buildOrderTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 16, 16, 16)));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Hóa đơn gần đây");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(new Color(30, 41, 59));
        title.setBorder(new EmptyBorder(0, 0, 12, 0));

        String[] cols = {"Mã HD", "Khách hàng", "Sản phẩm", "Thanh tiền (VND)", "Thời gian"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(36);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(new Color(30, 41, 59));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setBorder(new MatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(right);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);

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

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            long tongDon;
            double doanhThu;
            List<Object[]> rows;

            @Override
            protected Void doInBackground() {
                tongDon = DashboardDAO.getTongDonHang(from, to);
                doanhThu = DashboardDAO.getTongDoanhThu(from, to);
                
                String sortBy = "THOI_GIAN_DESC";
                if (cbSort.getSelectedIndex() == 1) sortBy = "THOI_GIAN_ASC";
                else if (cbSort.getSelectedIndex() == 2) sortBy = "THANH_TIEN_DESC";
                else if (cbSort.getSelectedIndex() == 3) sortBy = "THANH_TIEN_ASC";
                
                rows = DashboardDAO.getDonHangGanDay(from, to, 20, sortBy);
                return null;
            }

            @Override
            protected void done() {
                if (lblTongDonValue != null) lblTongDonValue.setText(String.format("%,d", tongDon));
                if (lblDoanhThuValue != null) lblDoanhThuValue.setText(DashboardDAO.formatVND(doanhThu));

                tableModel.setRowCount(0);
                for (Object[] r : rows) {
                    long thanhTien = ((Number) r[3]).longValue();
                    tableModel.addRow(new Object[]{
                        r[0],
                        r[1],
                        r[2],
                        DashboardDAO.formatVND((double) thanhTien),
                        r[4]
                    });
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
