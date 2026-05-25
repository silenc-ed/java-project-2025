package View.Admin.DashBoard;

import Controller.Admin.DashboardDAO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

/**
 * Utility class to create professional BI-style JFreeChart instances
 * with consistent theming for the Dashboard.
 */
public class DashboardChartFactory {

    // BI Color Palette
    public static final Color PRIMARY     = new Color(37, 99, 235);   // Blue
    public static final Color PRIMARY_LT  = new Color(99, 149, 255);  // Light Blue
    public static final Color SUCCESS     = new Color(5, 150, 105);   // Green
    public static final Color WARNING     = new Color(245, 158, 11);  // Amber
    public static final Color PURPLE      = new Color(124, 58, 237);  // Purple
    public static final Color PINK        = new Color(236, 72, 153);  // Pink
    public static final Color TEAL        = new Color(20, 184, 166);  // Teal
    public static final Color INDIGO      = new Color(99, 102, 241);  // Indigo

    private static final Color BG_WHITE   = Color.WHITE;
    private static final Color GRID_COLOR = new Color(241, 245, 249);
    private static final Color AXIS_COLOR = new Color(148, 163, 184);
    private static final Color LABEL_COLOR= new Color(71, 85, 105);

    private static final Font TITLE_FONT  = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font AXIS_FONT   = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font TICK_FONT   = new Font("Segoe UI", Font.PLAIN, 10);
    private static final Font LEGEND_FONT = new Font("Segoe UI", Font.PLAIN, 11);

    private static final Color[] PIE_COLORS = {
        PRIMARY, SUCCESS, WARNING, PURPLE, PINK, TEAL, INDIGO,
        new Color(251, 146, 60), new Color(168, 85, 247), new Color(34, 211, 238)
    };

    /**
     * Creates a Dual Line chart for Doanh thu and Lợi nhuận.
     */
    public static JFreeChart createDualLineChart(Map<String, double[]> data, String title) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, double[]> entry : data.entrySet()) {
            String key = entry.getKey();
            double[] vals = entry.getValue();
            dataset.addValue(vals[0], "Doanh thu", key);
            dataset.addValue(vals[1], "Lợi nhuận", key);
        }

        // Line renderer for both
        LineAndShapeRenderer renderer = new LineAndShapeRenderer(true, true);
        
        // Series 0: Doanh thu
        renderer.setSeriesPaint(0, PRIMARY);
        renderer.setSeriesStroke(0, new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-3, -3, 6, 6));
        renderer.setSeriesFillPaint(0, Color.WHITE);
        renderer.setUseFillPaint(true);
        
        // Series 1: Lợi nhuận
        renderer.setSeriesPaint(1, SUCCESS);
        renderer.setSeriesStroke(1, new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        renderer.setSeriesShape(1, new java.awt.geom.Ellipse2D.Double(-3, -3, 6, 6));

        renderer.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator(
                "{0}: {1} - {2}", NumberFormat.getInstance(new Locale("vi", "VN"))));

        // Category axis (X)
        CategoryAxis categoryAxis = new CategoryAxis();
        categoryAxis.setTickLabelFont(TICK_FONT);
        categoryAxis.setTickLabelPaint(LABEL_COLOR);
        categoryAxis.setAxisLinePaint(GRID_COLOR);
        if (data.size() > 15) {
            categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }

        // Value axis (Y)
        NumberAxis valueAxis = new NumberAxis("Số tiền (VND)");
        valueAxis.setTickLabelFont(TICK_FONT);
        valueAxis.setTickLabelPaint(LABEL_COLOR);
        valueAxis.setLabelFont(AXIS_FONT);
        valueAxis.setLabelPaint(LABEL_COLOR);
        valueAxis.setAxisLinePaint(GRID_COLOR);
        valueAxis.setNumberFormatOverride(new java.text.DecimalFormat("#,##0"));

        // Build the plot
        CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis, renderer);
        plot.setBackgroundPaint(BG_WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(GRID_COLOR);
        plot.setRangeGridlineStroke(new BasicStroke(1f));
        plot.setDomainGridlinesVisible(false);
        plot.setInsets(new RectangleInsets(8, 8, 8, 8));

        JFreeChart chart = new JFreeChart(title, TITLE_FONT, plot, true);
        applyTheme(chart);
        return chart;
    }

    /**
     * Creates a simple bar chart (single series).
     */
    public static JFreeChart createBarChart(Map<String, Double> data, String title) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> e : data.entrySet()) {
            dataset.addValue(e.getValue(), "Doanh thu", e.getKey());
        }

        CategoryAxis categoryAxis = new CategoryAxis();
        categoryAxis.setTickLabelFont(TICK_FONT);
        categoryAxis.setTickLabelPaint(LABEL_COLOR);
        categoryAxis.setAxisLinePaint(GRID_COLOR);
        if (data.size() > 15) {
            categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }

        NumberAxis valueAxis = new NumberAxis();
        valueAxis.setTickLabelFont(TICK_FONT);
        valueAxis.setTickLabelPaint(LABEL_COLOR);
        valueAxis.setAxisLinePaint(GRID_COLOR);
        valueAxis.setNumberFormatOverride(new java.text.DecimalFormat("#,##0"));

        BarRenderer renderer = new BarRenderer();
        renderer.setSeriesPaint(0, new GradientPaint(0, 0, PRIMARY_LT, 0, 200, PRIMARY));
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.08);
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        renderer.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator(
                "{1}: {2}", NumberFormat.getInstance(new Locale("vi", "VN"))));

        CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis, renderer);
        plot.setBackgroundPaint(BG_WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(GRID_COLOR);
        plot.setRangeGridlineStroke(new BasicStroke(1f));
        plot.setDomainGridlinesVisible(false);
        plot.setInsets(new RectangleInsets(8, 8, 8, 8));

        JFreeChart chart = new JFreeChart(title, TITLE_FONT, plot, false);
        applyTheme(chart);
        return chart;
    }

    /**
     * Creates a Pie/Donut chart for category breakdown.
     */
    @SuppressWarnings("unchecked")
    public static JFreeChart createPieChart(Map<String, Double> data, String title) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Map.Entry<String, Double> e : data.entrySet()) {
            dataset.setValue(e.getKey(), e.getValue());
        }

        PiePlot plot = new PiePlot(dataset);
        plot.setBackgroundPaint(BG_WHITE);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        plot.setLabelPaint(LABEL_COLOR);
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200));
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}\n{2}", NumberFormat.getInstance(), new java.text.DecimalFormat("0.0%")));
        plot.setInteriorGap(0.04);
        plot.setCircular(true);
        plot.setInsets(new RectangleInsets(4, 4, 4, 4));

        // Apply colors from palette
        int i = 0;
        for (Object key : dataset.getKeys()) {
            plot.setSectionPaint((Comparable) key, PIE_COLORS[i % PIE_COLORS.length]);
            i++;
        }

        JFreeChart chart = new JFreeChart(title, TITLE_FONT, plot, true);
        applyTheme(chart);
        return chart;
    }

    /**
     * Creates a smooth Line/Area chart for trend visualization.
     */
    public static JFreeChart createTrendChart(Map<String, Double> data, String title) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> e : data.entrySet()) {
            dataset.addValue(e.getValue(), "Doanh thu", e.getKey());
        }

        CategoryAxis categoryAxis = new CategoryAxis();
        categoryAxis.setTickLabelFont(TICK_FONT);
        categoryAxis.setTickLabelPaint(LABEL_COLOR);
        categoryAxis.setAxisLinePaint(GRID_COLOR);
        if (data.size() > 15) {
            categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }

        NumberAxis valueAxis = new NumberAxis();
        valueAxis.setTickLabelFont(TICK_FONT);
        valueAxis.setTickLabelPaint(LABEL_COLOR);
        valueAxis.setAxisLinePaint(GRID_COLOR);
        valueAxis.setNumberFormatOverride(new java.text.DecimalFormat("#,##0"));

        // Area renderer with gradient fill
        org.jfree.chart.renderer.category.AreaRenderer areaRenderer =
                new org.jfree.chart.renderer.category.AreaRenderer();
        areaRenderer.setSeriesPaint(0, new Color(37, 99, 235, 40));
        areaRenderer.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator(
                "{1}: {2}", NumberFormat.getInstance(new Locale("vi", "VN"))));

        // Line renderer on top
        LineAndShapeRenderer lineRenderer = new LineAndShapeRenderer(true, true);
        lineRenderer.setSeriesPaint(0, PRIMARY);
        lineRenderer.setSeriesStroke(0, new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        lineRenderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-3, -3, 6, 6));
        lineRenderer.setSeriesFillPaint(0, Color.WHITE);
        lineRenderer.setUseFillPaint(true);
        lineRenderer.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator(
                "{1}: {2}", NumberFormat.getInstance(new Locale("vi", "VN"))));

        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(0, dataset);
        plot.setRenderer(0, areaRenderer);
        plot.setDataset(1, dataset);
        plot.setRenderer(1, lineRenderer);
        plot.setDomainAxis(categoryAxis);
        plot.setRangeAxis(valueAxis);
        plot.setOrientation(PlotOrientation.VERTICAL);
        plot.setBackgroundPaint(BG_WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(GRID_COLOR);
        plot.setRangeGridlineStroke(new BasicStroke(1f));
        plot.setDomainGridlinesVisible(false);
        plot.setInsets(new RectangleInsets(8, 8, 8, 8));

        JFreeChart chart = new JFreeChart(title, TITLE_FONT, plot, false);
        applyTheme(chart);
        return chart;
    }

    /**
     * Wraps a JFreeChart into a ChartPanel with standard settings.
     */
    public static ChartPanel wrapChart(JFreeChart chart) {
        ChartPanel panel = new ChartPanel(chart);
        panel.setBackground(BG_WHITE);
        panel.setDomainZoomable(false);
        panel.setRangeZoomable(false);
        panel.setPopupMenu(null);
        panel.setMouseWheelEnabled(false);
        return panel;
    }

    /**
     * Apply consistent BI theme to all charts.
     */
    private static void applyTheme(JFreeChart chart) {
        chart.setBackgroundPaint(BG_WHITE);
        chart.setBorderVisible(false);
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);

        if (chart.getTitle() != null) {
            chart.getTitle().setFont(TITLE_FONT);
            chart.getTitle().setPaint(new Color(30, 41, 59));
        }

        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setItemFont(LEGEND_FONT);
            legend.setItemPaint(LABEL_COLOR);
            legend.setBackgroundPaint(BG_WHITE);
            legend.setFrame(org.jfree.chart.block.BlockBorder.NONE);
            legend.setPosition(RectangleEdge.BOTTOM);
        }
    }
}
