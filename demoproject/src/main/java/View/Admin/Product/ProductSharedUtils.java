package View.Admin.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class ProductSharedUtils {

    public static class DBItem {
        private int id;
        private String name;
        public DBItem(int id, String name) { this.id = id; this.name = name; }
        public int getId() { return id; }
        public String getName() { return name; }
        @Override public String toString() { return name; }
    }

    public static class ProductIconLabel extends JLabel {
        public ProductIconLabel() {
            setPreferredSize(new Dimension(36, 36));
            setHorizontalAlignment(SwingConstants.CENTER);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(241, 245, 249));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            
            g2.setColor(new Color(71, 85, 105));
            g2.setStroke(new BasicStroke(2.0f));
            int xc = getWidth() / 2;
            int yc = getHeight() / 2;
            int r = 9;
            int[] xs = new int[6];
            int[] ys = new int[6];
            for (int i = 0; i < 6; i++) {
                double angle = Math.PI / 180 * (60 * i - 30);
                xs[i] = (int)(xc + r * Math.cos(angle));
                ys[i] = (int)(yc + r * Math.sin(angle));
            }
            g2.drawPolygon(xs, ys, 6);
            g2.drawLine(xc, yc, xc, yc + r);
            g2.drawLine(xc, yc, (int)(xc + r * Math.cos(Math.PI / 180 * 30)), (int)(yc + r * Math.sin(Math.PI / 180 * 30)));
            g2.drawLine(xc, yc, (int)(xc + r * Math.cos(Math.PI / 180 * 150)), (int)(yc + r * Math.sin(Math.PI / 180 * 150)));
            g2.dispose();
        }
    }

    public static class ProductCellRenderer extends DefaultTableCellRenderer {
        private JPanel panel = new JPanel(new BorderLayout(10, 0));
        private ProductIconLabel iconLabel = new ProductIconLabel();
        private JLabel textLabel = new JLabel();

        public ProductCellRenderer() {
            panel.setOpaque(true);
            panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            panel.add(iconLabel, BorderLayout.WEST);
            panel.add(textLabel, BorderLayout.CENTER);
            textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
                textLabel.setForeground(table.getSelectionForeground());
            } else {
                panel.setBackground(Color.WHITE);
                textLabel.setForeground(new Color(15, 23, 42));
            }
            textLabel.setText(value != null ? value.toString() : "");
            return panel;
        }
    }

    public static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String status = value != null ? value.toString() : "Ngừng kinh doanh";
            JLabel label = new JLabel(status);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 13));
            label.setOpaque(false);
            
            if ("Đang kinh doanh".equalsIgnoreCase(status) || "Còn bán".equalsIgnoreCase(status) || "Còn hàng".equalsIgnoreCase(status)) {
                label.setForeground(new Color(21, 128, 61));
            } else {
                label.setForeground(new Color(185, 28, 28));
            }
            return label;
        }
    }

    public static class ActionPanel extends JPanel {
        public JButton btnEdit = new JButton();
        public JButton btnView = new JButton();
        
        public ActionPanel(boolean showViewButton) {
            setLayout(new GridBagLayout());
            setOpaque(true);
            setBackground(Color.WHITE);
            
            btnEdit.setText("Sửa");
            View.Admin.UIUtils.styleButton(btnEdit);
            
            JPanel btnContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            btnContainer.setOpaque(false);
            
            if (showViewButton) {
                btnView.setText("Chi tiết");
                View.Admin.UIUtils.styleButton(btnView);
                btnContainer.add(btnView);
            }
            btnContainer.add(btnEdit);
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.anchor = GridBagConstraints.CENTER;
            add(btnContainer, gbc);
        }
    }

    public static class ActionCellRenderer extends DefaultTableCellRenderer {
        private ActionPanel panel;
        public ActionCellRenderer(boolean showViewButton) {
            this.panel = new ActionPanel(showViewButton);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(Color.WHITE);
            }
            return panel;
        }
    }

    public static class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private ActionPanel panel;
        private JTable table;
        private int editingRow = -1;
        
        public ActionCellEditor(JTable table, Runnable onEdit, Runnable onView, boolean showViewButton) {
            this.table = table;
            this.panel = new ActionPanel(showViewButton);
            this.panel.btnEdit.addActionListener(e -> {
                int row = editingRow;
                stopCellEditing();
                if (row >= 0) {
                    table.setRowSelectionInterval(row, row);
                }
                if (onEdit != null) onEdit.run();
            });
            if (showViewButton) {
                this.panel.btnView.addActionListener(e -> {
                    int row = editingRow;
                    stopCellEditing();
                    if (row >= 0) {
                        table.setRowSelectionInterval(row, row);
                    }
                    if (onView != null) onView.run();
                });
            }
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.editingRow = row;
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return null;
        }
    }
}
