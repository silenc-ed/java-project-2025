package View.Admin.DashBoard;

import Controller.Admin.DashboardDAO;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;

public class RevenuePanel extends JPanel {

    private JRadioButton rbNam, rbThang, rbNgay;
    private ButtonGroup bgFilter;

    private JSpinner spinnerYear;
    private JSpinner spinnerMonth, spinnerMonthYear;
    private JSpinner spinnerFrom, spinnerTo;

    private CardLayout pickerCardLayout;
    private JPanel pickerPanel;

    private JLabel lblTongDoanhThuValue;
    private JLabel lblLoiNhuanValue;
    private JLabel lblChartTitle;
    private BarChartPanel barChart;

    public RevenuePanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250));
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(new Color(245, 246, 250));
        wrapper.setBorder(new EmptyBorder(20, 24, 20, 24));
        wrapper.add(buildFilterBar());
        wrapper.add(Box.createVerticalStrut(16));
        wrapper.add(buildBody());
        add(wrapper, BorderLayout.CENTER);
        loadData();
    }

    private JPanel buildFilterBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JLabel lbl = new JLabel("Thống kê theo:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(51, 65, 85));

        rbNam = makeRb("Năm");
        rbThang = makeRb("Tháng");
        rbNgay = makeRb("Tùy chọn");
        rbNam.setSelected(true);
        bgFilter = new ButtonGroup();
        bgFilter.add(rbNam);
        bgFilter.add(rbThang);
        bgFilter.add(rbNgay);

        rbNam.addActionListener(e -> { pickerCardLayout.show(pickerPanel, "NAM"); loadData(); });
        rbThang.addActionListener(e -> { pickerCardLayout.show(pickerPanel, "THANG"); loadData(); });
        rbNgay.addActionListener(e -> { pickerCardLayout.show(pickerPanel, "NGAY"); loadData(); });

        pickerCardLayout = new CardLayout();
        pickerPanel = new JPanel(pickerCardLayout);
        pickerPanel.setOpaque(false);
        pickerPanel.add(buildYearPicker(), "NAM");
        pickerPanel.add(buildMonthPicker(), "THANG");
        pickerPanel.add(buildDateRangePicker(), "NGAY");

        JButton btnLoc = new JButton("Lọc");
        btnLoc.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLoc.setBackground(new Color(37, 99, 235));
        btnLoc.setForeground(Color.WHITE);
        btnLoc.setBorder(new EmptyBorder(5, 16, 5, 16));
        btnLoc.setFocusPainted(false);
        btnLoc.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLoc.addActionListener(e -> loadData());

        panel.add(lbl);
        panel.add(rbNam);
        panel.add(rbThang);
        panel.add(rbNgay);
        panel.add(makeSep());
        panel.add(pickerPanel);
        panel.add(btnLoc);
        return panel;
    }

    private JPanel buildYearPicker() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JLabel lbl = makePickerLabel("Năm:");
        int yr = Calendar.getInstance().get(Calendar.YEAR);
        spinnerYear = new JSpinner(new SpinnerNumberModel(yr, 2000, 2100, 1));
        spinnerYear.setEditor(new JSpinner.NumberEditor(spinnerYear, "####"));
        spinnerYear.setPreferredSize(new Dimension(80, 28));
        p.add(lbl);
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

    private JRadioButton makeRb(String text) {
        JRadioButton rb = new JRadioButton(text);
        rb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rb.setForeground(new Color(51, 65, 85));
        rb.setOpaque(false);
        rb.setFocusPainted(false);
        rb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return rb;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);

        JPanel chartCard = new JPanel(new BorderLayout());
        chartCard.setBackground(Color.WHITE);
        chartCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 16, 16, 16)));

        lblChartTitle = new JLabel("Biểu đồ doanh thu theo tháng");
        lblChartTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblChartTitle.setForeground(new Color(30, 41, 59));
        lblChartTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        barChart = new BarChartPanel(new String[0], new double[0]);
        chartCard.add(lblChartTitle, BorderLayout.NORTH);
        chartCard.add(barChart, BorderLayout.CENTER);

        JPanel cardsCol = new JPanel();
        cardsCol.setLayout(new BoxLayout(cardsCol, BoxLayout.Y_AXIS));
        cardsCol.setOpaque(false);
        cardsCol.setPreferredSize(new Dimension(258, 0));

        JPanel card1 = buildSummaryCard("Tổng doanh thu (VND)", "—",
                new Color(37, 99, 235), new Color(219, 234, 254));
        JPanel card2 = buildSummaryCard("Tổng lợi nhuận (VND)", "—",
                new Color(5, 150, 105), new Color(209, 250, 229));

        lblTongDoanhThuValue = extractLabel(card1);
        lblLoiNhuanValue = extractLabel(card2);

        cardsCol.add(card1);
        cardsCol.add(Box.createVerticalStrut(14));
        cardsCol.add(card2);
        cardsCol.add(Box.createVerticalGlue());

        body.add(chartCard, BorderLayout.CENTER);
        body.add(cardsCol, BorderLayout.EAST);
        return body;
    }

    private JPanel buildSummaryCard(String title, String val, Color accent, Color tint) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(18, 16, 18, 16)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        JPanel stripe = new JPanel();
        stripe.setBackground(tint);
        stripe.setPreferredSize(new Dimension(5, 0));
        JLabel lblTitle = new JLabel("<html><body style='width:170px'>" + title + "</body></html>");
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(new Color(100, 116, 139));
        JLabel lblVal = new JLabel(val);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblVal.setForeground(accent);
        lblVal.putClientProperty("v", Boolean.TRUE);
        card.add(stripe, BorderLayout.WEST);
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        return card;
    }

    private JLabel extractLabel(JPanel card) {
        for (Component c : card.getComponents())
            if (c instanceof JLabel && Boolean.TRUE.equals(((JLabel) c).getClientProperty("v")))
                return (JLabel) c;
        return null;
    }

    public void loadData() {
        if (rbNam.isSelected()) {
            loadNam();
        } else if (rbThang.isSelected()) {
            loadThang();
        } else {
            loadTuyChon();
        }
    }

    private void loadNam() {
        int year = (int) spinnerYear.getValue();
        Calendar s = Calendar.getInstance();
        s.set(year, 0, 1, 0, 0, 0); s.set(Calendar.MILLISECOND, 0);
        Calendar e = Calendar.getInstance();
        e.set(year + 1, 0, 1, 0, 0, 0); e.set(Calendar.MILLISECOND, 0);
        Date from = s.getTime(), to = e.getTime();

        new SwingWorker<Void, Void>() {
            double dt, ln; Map<String, Double> cd;
            @Override protected Void doInBackground() {
                dt = DashboardDAO.getTongDoanhThu(from, to);
                ln = DashboardDAO.getTongLoiNhuan(from, to);
                cd = DashboardDAO.getDoanhThuTheoThangTrongNam(year);
                return null;
            }
            @Override protected void done() {
                updateCards(dt, ln);
                updateChart("Biểu đồ doanh thu năm " + year + " theo tháng", cd);
            }
        }.execute();
    }

    private void loadThang() {
        int month = (int) spinnerMonth.getValue();
        int year = (int) spinnerMonthYear.getValue();
        Calendar s = Calendar.getInstance();
        s.set(year, month - 1, 1, 0, 0, 0); s.set(Calendar.MILLISECOND, 0);
        Calendar e = (Calendar) s.clone();
        e.add(Calendar.MONTH, 1);
        Date from = s.getTime(), to = e.getTime();

        new SwingWorker<Void, Void>() {
            double dt, ln; Map<String, Double> cd;
            @Override protected Void doInBackground() {
                dt = DashboardDAO.getTongDoanhThu(from, to);
                ln = DashboardDAO.getTongLoiNhuan(from, to);
                cd = DashboardDAO.getDoanhThuTheoNgay(from, to);
                return null;
            }
            @Override protected void done() {
                updateCards(dt, ln);
                updateChart(String.format("Biểu đồ doanh thu tháng %d/%d theo ngày", month, year), cd);
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
            double dt, ln; Map<String, Double> cd; String title;
            @Override protected Void doInBackground() {
                dt = DashboardDAO.getTongDoanhThu(from, to);
                ln = DashboardDAO.getTongLoiNhuan(from, to);
                if (diffDays > 730) {
                    cd = DashboardDAO.getDoanhThuTheoNam(from, to);
                    title = "Biểu đồ doanh thu theo năm";
                } else if (diffDays > 60) {
                    cd = DashboardDAO.getDoanhThuTheoThang(from, to);
                    title = "Biểu đồ doanh thu theo tháng";
                } else if (diffDays > 1) {
                    cd = DashboardDAO.getDoanhThuTheoNgay(from, to);
                    title = "Biểu đồ doanh thu theo ngày";
                } else {
                    cd = DashboardDAO.getDoanhThuTheoGio(from);
                    title = "Biểu đồ doanh thu theo giờ";
                }
                return null;
            }
            @Override protected void done() {
                updateCards(dt, ln);
                updateChart(title, cd);
            }
        }.execute();
    }

    private void updateCards(double doanhThu, double loiNhuan) {
        if (lblTongDoanhThuValue != null) lblTongDoanhThuValue.setText(DashboardDAO.formatVND(doanhThu));
        if (lblLoiNhuanValue != null) lblLoiNhuanValue.setText(DashboardDAO.formatVND(loiNhuan));
    }

    private void updateChart(String title, Map<String, Double> data) {
        if (lblChartTitle != null) lblChartTitle.setText(title);
        String[] labels = data.keySet().toArray(new String[0]);
        double[] values = data.values().stream().mapToDouble(Double::doubleValue).toArray();
        barChart.setData(labels, values);
        barChart.repaint();
    }

    static class BarChartPanel extends JPanel {
        private String[] labels;
        private double[] data;

        BarChartPanel(String[] labels, double[] data) {
            this.labels = labels;
            this.data = data;
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(0, 260));
        }

        void setData(String[] labels, double[] data) {
            this.labels = labels;
            this.data = data;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (labels == null || labels.length == 0) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int pL = 60, pR = 16, pT = 16, pB = 36;
            int cW = w - pL - pR, cH = h - pT - pB;

            double maxVal = 0;
            for (double v : data) if (v > maxVal) maxVal = v;
            if (maxVal == 0) maxVal = 1;
            double scale = Math.pow(10, Math.floor(Math.log10(maxVal)));
            maxVal = Math.ceil(maxVal / scale) * scale;

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            FontMetrics fm = g2.getFontMetrics();

            for (int i = 0; i <= 5; i++) {
                int y = pT + cH - (int)((double) cH * i / 5);
                g2.setColor(new Color(241, 245, 249));
                g2.drawLine(pL, y, pL + cW, y);
                g2.setColor(new Color(148, 163, 184));
                String yLbl = DashboardDAO.formatVND(maxVal * i / 5);
                g2.drawString(yLbl, pL - fm.stringWidth(yLbl) - 4, y + 4);
            }

            int grpW = cW / data.length;
            int bW = Math.max(4, (int)(grpW * 0.6));
            int bOff = (grpW - bW) / 2;

            for (int i = 0; i < data.length; i++) {
                int bH = (int)(cH * data[i] / maxVal);
                int x = pL + i * grpW + bOff;
                int y = pT + cH - bH;
                GradientPaint gp = new GradientPaint(x, y, new Color(99, 149, 255), x, y + bH, new Color(37, 99, 235));
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, bW, bH, 6, 6);
                g2.setColor(new Color(100, 116, 139));
                int lx = x + (bW - fm.stringWidth(labels[i])) / 2;
                g2.drawString(labels[i], lx, pT + cH + 16);
            }

            g2.setColor(new Color(203, 213, 225));
            g2.drawLine(pL, pT, pL, pT + cH);
            g2.drawLine(pL, pT + cH, pL + cW, pT + cH);
            g2.dispose();
        }
    }
}
