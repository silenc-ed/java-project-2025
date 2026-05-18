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

    private JSpinner spinnerFrom, spinnerTo;
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton btnToggleMode;
    private JButton btnLoc;

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
        wrapper.add(buildTableCard());
        add(wrapper, BorderLayout.CENTER);
        loadData();
    }

    private JPanel buildFilterBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JLabel lbl = new JLabel("Top 10 Bán chạy");
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
        btnLoc.setBackground(new Color(148, 163, 184)); // xám ban đầu
        btnLoc.setForeground(Color.WHITE);
        btnLoc.setBorder(new EmptyBorder(5, 14, 5, 14));
        btnLoc.setFocusPainted(false);
        btnLoc.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLoc.addActionListener(e -> loadData());

        btnToggleMode = new JButton("Loại sản phẩm");
        styleToggleBtn(btnToggleMode, false);
        btnToggleMode.addActionListener(e -> toggleMode());

        panel.add(lbl);
        panel.add(lblFrom);
        panel.add(spinnerFrom);
        panel.add(lblTo);
        panel.add(spinnerTo);
        panel.add(btnLoc);
        panel.add(Box.createHorizontalStrut(10));
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
            btn.setBackground(new Color(241, 245, 249));
            btn.setForeground(new Color(71, 85, 105));
            btn.setBorder(new CompoundBorder(
                    new LineBorder(new Color(203, 213, 225), 1, true),
                    new EmptyBorder(5, 14, 5, 14)));
        }
    }

    private void markFilterChanged() {
        if (btnLoc != null) {
            btnLoc.setBackground(new Color(37, 99, 235));
        }
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(1, 1, 1, 1)));

        tableModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        setupTableColumns(isByCategory);

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

    private void setupTableColumns(boolean categoryMode) {
        if (categoryMode) {
            tableModel.setColumnIdentifiers(new String[]{"STT", "Loại sản phẩm", "Đã bán (sp)", "Doanh thu (VND)", "Lợi nhuận (VND)"});
        } else {
            tableModel.setColumnIdentifiers(new String[]{"STT", "Mã SP", "Tên sản phẩm", "Đã bán (sp)", "Doanh thu (VND)"});
        }
    }

    private void toggleMode() {
        isByCategory = !isByCategory;
        styleToggleBtn(btnToggleMode, isByCategory);
        setupTableColumns(isByCategory);
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        loadData();
    }

    public void loadData() {
        if (btnLoc != null) {
            btnLoc.setBackground(new Color(148, 163, 184)); // về màu xám
        }
        Date from = (Date) spinnerFrom.getValue();
        Calendar calTo = Calendar.getInstance();
        calTo.setTime((Date) spinnerTo.getValue());
        calTo.add(Calendar.DAY_OF_MONTH, 1);
        Date to = calTo.getTime();
        boolean byCategory = isByCategory;

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            List<Object[]> rows;
            @Override protected Void doInBackground() {
                if (byCategory) {
                    rows = DashboardDAO.getTopLoaiSanPhamThinhHanh(from, to, 10);
                } else {
                    rows = DashboardDAO.getTopSanPhamThinhHanh(from, to, 10);
                }
                return null;
            }
            @Override protected void done() {
                tableModel.setRowCount(0);
                if (rows != null) {
                    for (int i = 0; i < rows.size(); i++) {
                        Object[] r = rows.get(i);
                        if (byCategory) {
                            tableModel.addRow(new Object[]{ i + 1, r[0], r[1], DashboardDAO.formatVND((Double)r[2]), DashboardDAO.formatVND((Double)r[3]) });
                        } else {
                            tableModel.addRow(new Object[]{ i + 1, r[0], r[1], r[2], DashboardDAO.formatVND((Double)r[3]) });
                        }
                    }
                }
            }
        };
        worker.execute();
    }
}
