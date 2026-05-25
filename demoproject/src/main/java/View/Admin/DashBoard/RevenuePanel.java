package View.Admin.DashBoard;

import Controller.Admin.DashboardDAO;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import java.awt.*;
import java.awt.event.ItemListener;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Professional BI-style Revenue Dashboard panel.
 * Replaces the old single-bar-chart with:
 *  - 4 KPI Cards (Doanh thu, Lợi nhuận, Tổng đơn, TB đơn hàng)
 *  - Combo chart: Bar (revenue) + Line (profit)
 *  - Pie chart: breakdown by product category / branch
 *  - Trend line/area chart (detail view)
 */
public class RevenuePanel extends JPanel {

    // --- Filter controls ---
    private JRadioButton rbNam, rbThang, rbNgay;
    private ButtonGroup bgFilter;
    private JSpinner spinnerYear;
    private JSpinner spinnerMonth, spinnerMonthYear;
    private JSpinner spinnerFrom, spinnerTo;
    private JPanel pickerPanel;
    private JPanel yearPicker, monthPicker, dateRangePicker;
    private JPanel activePicker;

    // --- KPI Cards ---
    private KPICard cardDoanhThu;
    private KPICard cardLoiNhuan;
    private KPICard cardTongDon;
    private KPICard cardTrungBinh;

    // --- Chart containers (swapped in/out) ---
    private JPanel comboChartContainer;
    private JPanel pieChartContainer;
    private JPanel trendChartContainer;

    // --- Section labels ---
    private JLabel lblComboTitle;
    private JLabel lblPieTitle;
    private JLabel lblTrendTitle;

    public RevenuePanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250));

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(new Color(245, 246, 250));
        wrapper.setBorder(new EmptyBorder(18, 22, 18, 22));

        // 1. Filter bar
        wrapper.add(buildFilterBar());
        wrapper.add(Box.createVerticalStrut(14));

        // 2. KPI Cards row
        wrapper.add(buildKPIRow());
        wrapper.add(Box.createVerticalStrut(14));

        // 3. Main charts row (combo + pie side by side)
        wrapper.add(buildMainChartsRow());
        wrapper.add(Box.createVerticalStrut(14));

        // 4. Trend chart (full width)
        wrapper.add(buildTrendSection());

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(245, 246, 250));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        add(scroll, BorderLayout.CENTER);

        loadData();
    }

    // =========================================================
    // FILTER BAR
    // =========================================================
    private JPanel buildFilterBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel("Thống kê theo:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(51, 65, 85));

        rbNam   = makeRb("Năm");
        rbThang = makeRb("Tháng");
        rbNgay  = makeRb("Tùy chọn");
        rbNam.setSelected(true);

        bgFilter = new ButtonGroup();
        bgFilter.add(rbNam);
        bgFilter.add(rbThang);
        bgFilter.add(rbNgay);

        ItemListener rbListener = ev -> {
            for (JRadioButton rb : new JRadioButton[]{rbNam, rbThang, rbNgay}) {
                if (rb.isSelected()) {
                    rb.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    rb.setForeground(new Color(37, 99, 235));
                } else {
                    rb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    rb.setForeground(new Color(51, 65, 85));
                }
            }
        };
        rbNam.addItemListener(rbListener);
        rbThang.addItemListener(rbListener);
        rbNgay.addItemListener(rbListener);
        rbListener.itemStateChanged(null);

        JButton btnLoc = new JButton("Lọc");
        View.Admin.UIUtils.styleButton(btnLoc);
        btnLoc.addActionListener(e -> loadData());

        yearPicker      = buildYearPicker();
        monthPicker     = buildMonthPicker();
        dateRangePicker = buildDateRangePicker();

        pickerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pickerPanel.setOpaque(false);
        activePicker = yearPicker;
        pickerPanel.add(activePicker);

        rbNam.addActionListener(e   -> switchPicker(yearPicker));
        rbThang.addActionListener(e -> switchPicker(monthPicker));
        rbNgay.addActionListener(e  -> switchPicker(dateRangePicker));

        panel.add(lbl);
        panel.add(rbNam);
        panel.add(rbThang);
        panel.add(rbNgay);
        panel.add(makeSep());
        panel.add(pickerPanel);
        panel.add(btnLoc);
        return panel;
    }

    private void switchPicker(JPanel newPicker) {
        pickerPanel.remove(activePicker);
        activePicker = newPicker;
        pickerPanel.add(activePicker);
        pickerPanel.revalidate();
        pickerPanel.repaint();
        pickerPanel.getParent().revalidate();
        pickerPanel.getParent().repaint();
    }

    private JPanel buildYearPicker() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        int yr = Calendar.getInstance().get(Calendar.YEAR);
        spinnerYear = new JSpinner(new SpinnerNumberModel(yr, 2000, 2100, 1));
        spinnerYear.setEditor(new JSpinner.NumberEditor(spinnerYear, "####"));
        spinnerYear.setPreferredSize(new Dimension(80, 28));
        p.add(makePickerLabel("Năm:"));
        p.add(spinnerYear);
        return p;
    }

    private JPanel buildMonthPicker() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        int curM = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int curY = Calendar.getInstance().get(Calendar.YEAR);
        spinnerMonth = new JSpinner(new SpinnerNumberModel(curM, 1, 12, 1));
        spinnerMonth.setPreferredSize(new Dimension(52, 28));
        spinnerMonthYear = new JSpinner(new SpinnerNumberModel(curY, 2000, 2100, 1));
        spinnerMonthYear.setEditor(new JSpinner.NumberEditor(spinnerMonthYear, "####"));
        spinnerMonthYear.setPreferredSize(new Dimension(80, 28));
        p.add(makePickerLabel("Tháng:"));
        p.add(spinnerMonth);
        p.add(makePickerLabel("Năm:"));
        p.add(spinnerMonthYear);
        return p;
    }

    private JPanel buildDateRangePicker() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        spinnerFrom = new JSpinner(new SpinnerDateModel(cal.getTime(), null, new Date(), Calendar.DAY_OF_MONTH));
        spinnerFrom.setEditor(new JSpinner.DateEditor(spinnerFrom, "dd/MM/yyyy"));
        spinnerFrom.setPreferredSize(new Dimension(110, 28));
        spinnerTo = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        spinnerTo.setEditor(new JSpinner.DateEditor(spinnerTo, "dd/MM/yyyy"));
        spinnerTo.setPreferredSize(new Dimension(110, 28));
        p.add(makePickerLabel("Từ:"));
        p.add(spinnerFrom);
        p.add(makePickerLabel("Đến:"));
        p.add(spinnerTo);
        return p;
    }

    // =========================================================
    // KPI CARDS ROW
    // =========================================================
    private JPanel buildKPIRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        cardDoanhThu = new KPICard("Tổng doanh thu", "",
                DashboardChartFactory.PRIMARY, new Color(219, 234, 254));
        cardLoiNhuan = new KPICard("Tổng lợi nhuận", "",
                DashboardChartFactory.SUCCESS, new Color(209, 250, 229));
        cardTongDon = new KPICard("Tổng đơn hàng", "",
                DashboardChartFactory.PURPLE, new Color(237, 233, 254));
        cardTrungBinh = new KPICard("TB giá trị đơn", "",
                DashboardChartFactory.WARNING, new Color(254, 243, 199));

        row.add(cardDoanhThu);
        row.add(cardLoiNhuan);
        row.add(cardTongDon);
        row.add(cardTrungBinh);
        return row;
    }

    // =========================================================
    // MAIN CHARTS ROW: Combo (65%) + Pie (35%)
    // =========================================================
    private JPanel buildMainChartsRow() {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- Combo chart card ---
        JPanel comboCard = new JPanel(new BorderLayout(0, 8));
        comboCard.setBackground(Color.WHITE);
        comboCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(14, 14, 14, 14)));

        lblComboTitle = sectionTitle("Doanh thu & Lợi nhuận");
        comboChartContainer = new JPanel(new BorderLayout());
        comboChartContainer.setBackground(Color.WHITE);
        comboChartContainer.setPreferredSize(new Dimension(0, 360));
        comboChartContainer.add(makePlaceholder("Đang tải dữ liệu..."), BorderLayout.CENTER);

        comboCard.add(lblComboTitle, BorderLayout.NORTH);
        comboCard.add(comboChartContainer, BorderLayout.CENTER);

        // --- Pie chart card ---
        JPanel pieCard = new JPanel(new BorderLayout(0, 8));
        pieCard.setBackground(Color.WHITE);
        pieCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(14, 14, 14, 14)));
        pieCard.setPreferredSize(new Dimension(300, 0));

        lblPieTitle = sectionTitle("Phân bổ theo loại sản phẩm");
        pieChartContainer = new JPanel(new BorderLayout());
        pieChartContainer.setBackground(Color.WHITE);
        pieChartContainer.setPreferredSize(new Dimension(0, 360));
        pieChartContainer.add(makePlaceholder("Đang tải dữ liệu..."), BorderLayout.CENTER);

        pieCard.add(lblPieTitle, BorderLayout.NORTH);
        pieCard.add(pieChartContainer, BorderLayout.CENTER);

        row.add(comboCard, BorderLayout.CENTER);
        row.add(pieCard, BorderLayout.EAST);
        return row;
    }

    // =========================================================
    // TREND CHART (Full width, line/area)
    // =========================================================
    private JPanel buildTrendSection() {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(14, 14, 14, 14)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblTrendTitle = sectionTitle("Biểu đồ xu hướng doanh thu");
        trendChartContainer = new JPanel(new BorderLayout());
        trendChartContainer.setBackground(Color.WHITE);
        trendChartContainer.setPreferredSize(new Dimension(0, 260));
        trendChartContainer.add(makePlaceholder("Đang tải dữ liệu..."), BorderLayout.CENTER);

        card.add(lblTrendTitle, BorderLayout.NORTH);
        card.add(trendChartContainer, BorderLayout.CENTER);
        return card;
    }

    // =========================================================
    // DATA LOADING
    // =========================================================
    public void loadData() {
        if (rbNam.isSelected()) loadNam();
        else if (rbThang.isSelected()) loadThang();
        else loadTuyChon();
    }

    private void loadNam() {
        int year = (int) spinnerYear.getValue();
        Calendar s = Calendar.getInstance();
        s.set(year, 0, 1, 0, 0, 0); s.set(Calendar.MILLISECOND, 0);
        Calendar e = Calendar.getInstance();
        e.set(year + 1, 0, 1, 0, 0, 0); e.set(Calendar.MILLISECOND, 0);
        Date from = s.getTime(), to = e.getTime();

        // Previous year for trend
        Calendar ps = Calendar.getInstance();
        ps.set(year - 1, 0, 1, 0, 0, 0); ps.set(Calendar.MILLISECOND, 0);
        Calendar pe = Calendar.getInstance();
        pe.set(year, 0, 1, 0, 0, 0); pe.set(Calendar.MILLISECOND, 0);
        Date prevFrom = ps.getTime(), prevTo = pe.getTime();

        new SwingWorker<Void, Void>() {
            double dt, ln, prevDt, prevLn, tbDon;
            long tongDon;
            Map<String, double[]> comboData;
            Map<String, Double> pieData;
            Map<String, Double> trendData;

            @Override
            protected Void doInBackground() {
                dt       = DashboardDAO.getTongDoanhThu(from, to);
                ln       = DashboardDAO.getTongLoiNhuan(from, to);
                prevDt   = DashboardDAO.getTongDoanhThu(prevFrom, prevTo);
                prevLn   = DashboardDAO.getTongLoiNhuan(prevFrom, prevTo);
                tongDon  = DashboardDAO.getTongDonHang(from, to);
                tbDon    = DashboardDAO.getTrungBinhGiaTriDonHang(from, to);
                comboData = DashboardDAO.getDoanhThuVaLoiNhuanTheoThang(year);
                pieData   = DashboardDAO.getDoanhThuTheoLoaiSP(from, to);
                trendData = DashboardDAO.getDoanhThuTheoThangTrongNam(year);
                return null;
            }

            @Override
            protected void done() {
                // KPI cards
                cardDoanhThu.setValue(DashboardDAO.formatVND(dt));
                cardDoanhThu.setTrend(calcGrowth(dt, prevDt), "so với năm trước");
                cardLoiNhuan.setValue(DashboardDAO.formatVND(ln));
                cardLoiNhuan.setTrend(calcGrowth(ln, prevLn), "so với năm trước");
                cardTongDon.setValue(String.format("%,d", tongDon));
                cardTongDon.clearTrend();
                cardTrungBinh.setValue(DashboardDAO.formatVND(tbDon));
                cardTrungBinh.clearTrend();

                // Charts
                lblComboTitle.setText("Doanh thu & Lợi nhuận năm " + year + " theo tháng");
                lblPieTitle.setText("Phân bổ doanh thu theo loại sản phẩm");
                lblTrendTitle.setText("Xu hướng doanh thu năm " + year);

                updateComboChart(comboData);
                updatePieChart(pieData, "Phân bổ theo loại sản phẩm");
                updateTrendChart(trendData, "Doanh thu theo tháng " + year);
            }
        }.execute();
    }

    private void loadThang() {
        int month = (int) spinnerMonth.getValue();
        int year  = (int) spinnerMonthYear.getValue();
        Calendar s = Calendar.getInstance();
        s.set(year, month - 1, 1, 0, 0, 0); s.set(Calendar.MILLISECOND, 0);
        Calendar e = (Calendar) s.clone();
        e.add(Calendar.MONTH, 1);
        Date from = s.getTime(), to = e.getTime();

        // Previous month
        Calendar ps = (Calendar) s.clone();
        ps.add(Calendar.MONTH, -1);
        Date prevFrom = ps.getTime(), prevTo = s.getTime();

        new SwingWorker<Void, Void>() {
            double dt, ln, prevDt, prevLn, tbDon;
            long tongDon;
            Map<String, double[]> comboDual;
            Map<String, Double> pieData, trendData;

            @Override
            protected Void doInBackground() {
                dt      = DashboardDAO.getTongDoanhThu(from, to);
                ln      = DashboardDAO.getTongLoiNhuan(from, to);
                prevDt  = DashboardDAO.getTongDoanhThu(prevFrom, prevTo);
                prevLn  = DashboardDAO.getTongLoiNhuan(prevFrom, prevTo);
                tongDon = DashboardDAO.getTongDonHang(from, to);
                tbDon   = DashboardDAO.getTrungBinhGiaTriDonHang(from, to);
                comboDual   = DashboardDAO.getDoanhThuVaLoiNhuanTheoNgay(from, to);
                pieData     = DashboardDAO.getDoanhThuTheoLoaiSP(from, to);
                trendData   = DashboardDAO.getDoanhThuTheoNgay(from, to);
                return null;
            }

            @Override
            protected void done() {
                cardDoanhThu.setValue(DashboardDAO.formatVND(dt));
                cardDoanhThu.setTrend(calcGrowth(dt, prevDt), "so với tháng trước");
                cardLoiNhuan.setValue(DashboardDAO.formatVND(ln));
                cardLoiNhuan.setTrend(calcGrowth(ln, prevLn), "so với tháng trước");
                cardTongDon.setValue(String.format("%,d", tongDon));
                cardTongDon.clearTrend();
                cardTrungBinh.setValue(DashboardDAO.formatVND(tbDon));
                cardTrungBinh.clearTrend();

                String monthLabel = String.format("Tháng %d/%d", month, year);
                lblComboTitle.setText("Doanh thu & Lợi nhuận — " + monthLabel);
                lblPieTitle.setText("Phân bổ loại sản phẩm — " + monthLabel);
                lblTrendTitle.setText("Xu hướng doanh thu — " + monthLabel);

                JFreeChart dualLineChart = DashboardChartFactory.createDualLineChart(
                        comboDual, "Doanh thu & Lợi nhuận");
                replaceChart(comboChartContainer, dualLineChart);
                updatePieChart(pieData, "Phân bổ theo loại sản phẩm");
                updateTrendChart(trendData, "Xu hướng " + monthLabel);
            }
        }.execute();
    }

    private void loadTuyChon() {
        Date from = (Date) spinnerFrom.getValue();
        Calendar toC = Calendar.getInstance();
        toC.setTime((Date) spinnerTo.getValue());
        toC.add(Calendar.DAY_OF_MONTH, 1);
        Date to = toC.getTime();
        long diffDays = (to.getTime() - from.getTime()) / 86400000L;

        new SwingWorker<Void, Void>() {
            double dt, ln, tbDon;
            long tongDon;
            Map<String, Double> mainData, pieData, trendData;
            String comboTitle, trendTitle;

            @Override
            protected Void doInBackground() {
                dt      = DashboardDAO.getTongDoanhThu(from, to);
                ln      = DashboardDAO.getTongLoiNhuan(from, to);
                tongDon = DashboardDAO.getTongDonHang(from, to);
                tbDon   = DashboardDAO.getTrungBinhGiaTriDonHang(from, to);
                pieData = DashboardDAO.getDoanhThuTheoLoaiSP(from, to);

                if (diffDays > 730) {
                    mainData    = DashboardDAO.getDoanhThuTheoNam(from, to);
                    trendData   = mainData;
                    comboTitle  = "Doanh thu theo năm";
                    trendTitle  = "Xu hướng theo năm";
                } else if (diffDays > 60) {
                    mainData    = DashboardDAO.getDoanhThuTheoThang(from, to);
                    trendData   = mainData;
                    comboTitle  = "Doanh thu theo tháng";
                    trendTitle  = "Xu hướng theo tháng";
                } else if (diffDays > 1) {
                    mainData    = DashboardDAO.getDoanhThuTheoNgay(from, to);
                    trendData   = mainData;
                    comboTitle  = "Doanh thu theo ngày";
                    trendTitle  = "Xu hướng theo ngày";
                } else {
                    mainData    = DashboardDAO.getDoanhThuTheoGio(from);
                    trendData   = mainData;
                    comboTitle  = "Doanh thu theo giờ";
                    trendTitle  = "Xu hướng theo giờ";
                }
                return null;
            }

            @Override
            protected void done() {
                cardDoanhThu.setValue(DashboardDAO.formatVND(dt));
                cardDoanhThu.clearTrend();
                cardLoiNhuan.setValue(DashboardDAO.formatVND(ln));
                cardLoiNhuan.clearTrend();
                cardTongDon.setValue(String.format("%,d", tongDon));
                cardTongDon.clearTrend();
                cardTrungBinh.setValue(DashboardDAO.formatVND(tbDon));
                cardTrungBinh.clearTrend();

                lblComboTitle.setText(comboTitle);
                lblPieTitle.setText("Phân bổ theo loại sản phẩm");
                lblTrendTitle.setText(trendTitle);

                JFreeChart barChart = DashboardChartFactory.createBarChart(mainData, comboTitle);
                replaceChart(comboChartContainer, barChart);
                updatePieChart(pieData, "Phân bổ theo loại sản phẩm");
                updateTrendChart(trendData, trendTitle);
            }
        }.execute();
    }

    // =========================================================
    // CHART UPDATE HELPERS
    // =========================================================
    private void updateComboChart(Map<String, double[]> data) {
        JFreeChart chart = DashboardChartFactory.createDualLineChart(data, lblComboTitle.getText());
        replaceChart(comboChartContainer, chart);
    }

    private void updatePieChart(Map<String, Double> data, String title) {
        if (data == null || data.isEmpty()) {
            replacePlaceholder(pieChartContainer, "Không có dữ liệu");
            return;
        }
        JFreeChart chart = DashboardChartFactory.createPieChart(data, title);
        replaceChart(pieChartContainer, chart);
    }

    private void updateTrendChart(Map<String, Double> data, String title) {
        if (data == null || data.isEmpty()) {
            replacePlaceholder(trendChartContainer, "Không có dữ liệu");
            return;
        }
        JFreeChart chart = DashboardChartFactory.createTrendChart(data, title);
        replaceChart(trendChartContainer, chart);
    }

    private void replaceChart(JPanel container, JFreeChart chart) {
        ChartPanel cp = DashboardChartFactory.wrapChart(chart);
        container.removeAll();
        container.add(cp, BorderLayout.CENTER);
        container.revalidate();
        container.repaint();
    }

    private void replacePlaceholder(JPanel container, String msg) {
        container.removeAll();
        container.add(makePlaceholder(msg), BorderLayout.CENTER);
        container.revalidate();
        container.repaint();
    }

    // =========================================================
    // UTILITY
    // =========================================================

    /** Calculate growth % relative to previous period. */
    private double calcGrowth(double current, double previous) {
        if (previous == 0) return 0;
        return (current - previous) / previous * 100.0;
    }

    private JLabel makePlaceholder(String msg) {
        JLabel lbl = new JLabel(msg, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(new Color(148, 163, 184));
        return lbl;
    }

    private JLabel sectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(30, 41, 59));
        lbl.setBorder(new EmptyBorder(0, 0, 6, 0));
        return lbl;
    }

    private JRadioButton makeRb(String text) {
        JRadioButton rb = new JRadioButton(text);
        rb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rb.setForeground(new Color(51, 65, 85));
        rb.setOpaque(false);
        rb.setFocusPainted(false);
        rb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return rb;
    }

    private JLabel makePickerLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(new Color(71, 85, 105));
        return l;
    }

    private JLabel makeSep() {
        JLabel sep = new JLabel("|");
        sep.setForeground(new Color(203, 213, 225));
        sep.setBorder(new EmptyBorder(0, 4, 0, 4));
        return sep;
    }
}
