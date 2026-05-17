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

    private JRadioButton rbNam;
    private JRadioButton rbThang;
    private JRadioButton rbNgay;
    private ButtonGroup bgFilter;
    private JSpinner spinnerFrom;
    private JSpinner spinnerTo;
    private JLabel lblTongLoiNhuanValue;
    private JLabel lblLoiNhuanBinhQuanValue;
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
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JLabel lbl = new JLabel("Thống kê theo:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(51, 65, 85));

        rbNam = makeRb("Năm");
        rbThang = makeRb("Tháng");
        rbNgay = makeRb("Ngày");
        rbNam.setSelected(true);
        bgFilter = new ButtonGroup();
        bgFilter.add(rbNam);
        bgFilter.add(rbThang);
        bgFilter.add(rbNgay);

        JLabel sep = new JLabel("|");
        sep.setForeground(new Color(203, 213, 225));
        sep.setBorder(new EmptyBorder(0, 4, 0, 4));

        JLabel lblFrom = new JLabel("Từ ngày:");
        lblFrom.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFrom.setForeground(new Color(71, 85, 105));

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -11);
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
        btnLoc.setBackground(new Color(37, 99, 235));
        btnLoc.setForeground(Color.WHITE);
        btnLoc.setBorder(new EmptyBorder(5, 14, 5, 14));
        btnLoc.setFocusPainted(false);
        btnLoc.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLoc.addActionListener(e -> loadData());

        panel.add(lbl);
        panel.add(rbNam);
        panel.add(rbThang);
        panel.add(rbNgay);
        panel.add(sep);
        panel.add(lblFrom);
        panel.add(spinnerFrom);
        panel.add(lblTo);
        panel.add(spinnerTo);
        panel.add(btnLoc);
        return panel;
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

        JLabel chartTitle = new JLabel("Biểu đồ doanh thu theo tháng");
        chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chartTitle.setForeground(new Color(30, 41, 59));
        chartTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        barChart = new BarChartPanel(new String[0], new double[0]);

        chartCard.add(chartTitle, BorderLayout.NORTH);
        chartCard.add(barChart, BorderLayout.CENTER);

        JPanel cardsCol = new JPanel();
        cardsCol.setLayout(new BoxLayout(cardsCol, BoxLayout.Y_AXIS));
        cardsCol.setOpaque(false);
        cardsCol.setPreferredSize(new Dimension(258, 0));

        JPanel card1 = buildCard("Tổng doanh thu (VND)", "—",
                new Color(37, 99, 235), new Color(219, 234, 254));
        JPanel card2 = buildCard("Tổng lợi nhuận (VND)", "—",
                new Color(5, 150, 105), new Color(209, 250, 229));

        lblTongLoiNhuanValue = extractValueLabel(card1);
        lblLoiNhuanBinhQuanValue = extractValueLabel(card2);

        cardsCol.add(card1);
        cardsCol.add(Box.createVerticalStrut(14));
        cardsCol.add(card2);
        cardsCol.add(Box.createVerticalGlue());

        body.add(chartCard, BorderLayout.CENTER);
        body.add(cardsCol, BorderLayout.EAST);
        return body;
    }

    private JPanel buildCard(String title, String val, Color accent, Color tint) {
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
        lblVal.putClientProperty("valueLabel", Boolean.TRUE);

        card.add(stripe, BorderLayout.WEST);
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        return card;
    }

    private JLabel extractValueLabel(JPanel card) {
        for (Component c : card.getComponents()) {
            if (c instanceof JLabel && Boolean.TRUE.equals(((JLabel) c).getClientProperty("valueLabel")))
                return (JLabel) c;
        }
        return null;
    }

    public void loadData() {
        Date from = (Date) spinnerFrom.getValue();
        Date to = nextDay((Date) spinnerTo.getValue());

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            double doanhThu, loiNhuan;
            Map<String, Double> chartData = new LinkedHashMap<>();

            @Override
            protected Void doInBackground() {
                doanhThu = DashboardDAO.getTongDoanhThu(from, to);
                loiNhuan = DashboardDAO.getTongLoiNhuan(from, to);
                if (rbNam.isSelected()) {
                    int year = Calendar.getInstance().get(Calendar.YEAR);
                    Map<String, Double> raw = DashboardDAO.getDoanhThuTheoThang(year);
                    for (int m = 1; m <= 12; m++) {
                        String key = "T" + m;
                        chartData.put(key, raw.getOrDefault(key, 0.0));
                    }
                } else {
                    Map<String, Double> raw = DashboardDAO.getDoanhThuTheoNgay(from, to);
                    chartData.putAll(raw);
                }
                return null;
            }

            @Override
            protected void done() {
                if (lblTongLoiNhuanValue != null)
                    lblTongLoiNhuanValue.setText(DashboardDAO.formatVND(doanhThu));
                if (lblLoiNhuanBinhQuanValue != null)
                    lblLoiNhuanBinhQuanValue.setText(DashboardDAO.formatVND(loiNhuan));

                String[] labels = chartData.keySet().toArray(new String[0]);
                double[] values = chartData.values().stream().mapToDouble(Double::doubleValue).toArray();
                barChart.setData(labels, values);
                barChart.repaint();
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

    public JRadioButton getRbNam() { return rbNam; }
    public JRadioButton getRbThang() { return rbThang; }
    public JRadioButton getRbNgay() { return rbNgay; }
    public JSpinner getSpinnerFrom() { return spinnerFrom; }
    public JSpinner getSpinnerTo() { return spinnerTo; }

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
            int pL = 58, pR = 16, pT = 16, pB = 36;
            int cW = w - pL - pR, cH = h - pT - pB;

            double maxVal = 0;
            for (double v : data) if (v > maxVal) maxVal = v;
            if (maxVal == 0) maxVal = 1;
            double scale = Math.pow(10, Math.floor(Math.log10(maxVal)));
            maxVal = Math.ceil(maxVal / scale) * scale;

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            FontMetrics fm = g2.getFontMetrics();

            int gridLines = 5;
            for (int i = 0; i <= gridLines; i++) {
                int y = pT + cH - (int)((double) cH * i / gridLines);
                g2.setColor(new Color(241, 245, 249));
                g2.drawLine(pL, y, pL + cW, y);
                g2.setColor(new Color(148, 163, 184));
                String yLbl = DashboardDAO.formatVND(maxVal * i / gridLines);
                g2.drawString(yLbl, pL - fm.stringWidth(yLbl) - 4, y + 4);
            }

            int grpW = cW / data.length;
            int bW = Math.max(4, (int)(grpW * 0.55));
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
