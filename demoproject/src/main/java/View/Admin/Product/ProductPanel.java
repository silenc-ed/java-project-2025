package View.Admin.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import ConnectDB.ConnectionUtils;

class DBItem {
    private int id;
    private String name;
    public DBItem(int id, String name) { this.id = id; this.name = name; }
    public int getId() { return id; }
    public String getName() { return name; }
    @Override public String toString() { return name; }
}

class ProductIconLabel extends JLabel {
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

class ProductCellRenderer extends DefaultTableCellRenderer {
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

class StatusBadgeRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String status = value != null ? value.toString() : "Ngừng kinh doanh";
        JLabel label = new JLabel(status) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(getWidth() / 2 - 55, getHeight() / 2 - 12, 110, 24, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setOpaque(false);
        
        if ("Đang kinh doanh".equalsIgnoreCase(status) || "Còn bán".equalsIgnoreCase(status) || "Còn hàng".equalsIgnoreCase(status)) {
            label.setBackground(new Color(220, 252, 231));
            label.setForeground(new Color(21, 128, 61));
        } else {
            label.setBackground(new Color(254, 226, 226));
            label.setForeground(new Color(185, 28, 28));
        }
        return label;
    }
}

class EditIcon implements Icon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(59, 130, 246));
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawRoundRect(x + 2, y + 2, 12, 12, 2, 2);
        g2.drawLine(x + 6, y + 6, x + 10, y + 6);
        g2.drawLine(x + 6, y + 10, x + 10, y + 10);
        g2.dispose();
    }
    @Override public int getIconWidth() { return 18; }
    @Override public int getIconHeight() { return 18; }
}

class DeleteIcon implements Icon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(239, 68, 68));
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawRect(x + 3, y + 5, 10, 10);
        g2.drawLine(x + 1, y + 3, x + 15, y + 3);
        g2.drawRect(x + 6, y + 1, 4, 2);
        g2.drawLine(x + 8, y + 7, x + 8, y + 12);
        g2.dispose();
    }
    @Override public int getIconWidth() { return 18; }
    @Override public int getIconHeight() { return 18; }
}

class ActionPanel extends JPanel {
    public JButton btnEdit = new JButton();
    public JButton btnDelete = new JButton();
    
    public ActionPanel() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        setOpaque(true);
        setBackground(Color.WHITE);
        
        btnEdit.setIcon(new EditIcon());
        btnEdit.setPreferredSize(new Dimension(28, 28));
        btnEdit.setContentAreaFilled(false);
        btnEdit.setBorderPainted(false);
        btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEdit.setFocusPainted(false);
        
        btnDelete.setIcon(new DeleteIcon());
        btnDelete.setPreferredSize(new Dimension(28, 28));
        btnDelete.setContentAreaFilled(false);
        btnDelete.setBorderPainted(false);
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.setFocusPainted(false);
        
        add(btnEdit);
        add(btnDelete);
    }
}

class ActionCellRenderer extends DefaultTableCellRenderer {
    private ActionPanel panel = new ActionPanel();
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

class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
    private ActionPanel panel = new ActionPanel();
    private JTable table;
    
    public ActionCellEditor(JTable table, Runnable onEdit, Runnable onDelete) {
        this.table = table;
        panel.btnEdit.addActionListener(e -> {
            stopCellEditing();
            onEdit.run();
        });
        panel.btnDelete.addActionListener(e -> {
            stopCellEditing();
            onDelete.run();
        });
    }
    
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        panel.setBackground(table.getSelectionBackground());
        return panel;
    }
    
    @Override
    public Object getCellEditorValue() {
        return null;
    }
}

public class ProductPanel extends javax.swing.JPanel {

    private DefaultTableModel tableModel;
    private JTable dataTable;
    private List<DBItem> categoryList = new ArrayList<>();

    public ProductPanel() {
        initComponents();
        setupCustomUI();
    }

    private void setupCustomUI() {
        this.removeAll();
        this.setLayout(new BorderLayout(15, 15));
        this.setBackground(new Color(248, 250, 252));
        this.setBorder(new EmptyBorder(20, 20, 20, 20));

        // ================= HEADER =================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản lý sản phẩm");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(15, 23, 42));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        JButton btnAdd = new JButton("+ Thêm sản phẩm") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(175, 122, 197), 0, getHeight(), new Color(210, 160, 205));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setContentAreaFilled(false);
        btnAdd.setBorderPainted(false);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.setPreferredSize(new Dimension(160, 40));
        headerPanel.add(btnAdd, BorderLayout.EAST);

        this.add(headerPanel, BorderLayout.NORTH);

        // ================= CENTER =================
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);

        // Search Bar
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        JTextField txtSearch = new JTextField("Tìm kiếm sản phẩm...");
        txtSearch.setBorder(null);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtSearch.setForeground(Color.GRAY);
        txtSearch.setSelectionColor(new Color(210, 160, 205));
        txtSearch.setSelectedTextColor(Color.WHITE);
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals("Tìm kiếm sản phẩm...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(new Color(15, 23, 42));
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText("Tìm kiếm sản phẩm...");
                    txtSearch.setForeground(Color.GRAY);
                }
            }
        });
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        centerPanel.add(searchPanel, BorderLayout.NORTH);

        // Table: Mã sản phẩm, Tên sản phẩm, Danh mục, Mô tả, Số lượng đã bán, Giá bán, Đơn vị tính, Trạng thái, Thao tác
        String[] columns = {"Mã sản phẩm", "Tên sản phẩm", "Danh mục", "Mô tả", "Số lượng đã bán", "Giá bán", "Đơn vị tính", "Trạng thái", "Thao tác"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 8; }
        };
        dataTable = new JTable(tableModel);
        dataTable.setRowHeight(60);
        dataTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dataTable.setBackground(Color.WHITE);
        dataTable.setShowVerticalLines(false);
        dataTable.setShowHorizontalLines(true);
        dataTable.setGridColor(new Color(241, 245, 249));
        dataTable.setSelectionBackground(new Color(245, 235, 250));
        dataTable.setSelectionForeground(new Color(142, 68, 173));
        
        // Table Header styling
        dataTable.getTableHeader().setPreferredSize(new Dimension(0, 45));
        dataTable.getTableHeader().setBackground(Color.WHITE);
        dataTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        dataTable.getTableHeader().setForeground(new Color(100, 116, 139));
        dataTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        // Custom Renderers
        dataTable.getColumnModel().getColumn(1).setCellRenderer(new ProductCellRenderer());
        dataTable.getColumnModel().getColumn(7).setCellRenderer(new StatusBadgeRenderer());
        
        // Column Alignment
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        dataTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        dataTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        dataTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        dataTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        dataTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        // Action Renderer & Editor
        ActionCellEditor actionEditor = new ActionCellEditor(dataTable, 
            () -> handleEditProduct(), 
            () -> handleDeleteProduct()
        );
        dataTable.getColumnModel().getColumn(8).setCellRenderer(new ActionCellRenderer());
        dataTable.getColumnModel().getColumn(8).setCellEditor(actionEditor);

        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        this.add(centerPanel, BorderLayout.CENTER);

        // Search filtering
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = txtSearch.getText().trim();
                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
                dataTable.setRowSorter(sorter);
                if (text.isEmpty() || text.equals("Tìm kiếm sản phẩm...")) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0, 1));
                }
            }
        });

        // Add Product Event
        btnAdd.addActionListener(e -> handleAddProduct());

        // Load data from DB in background
        loadCategories();
        loadDataToTable();
    }

    private void loadCategories() {
        categoryList.clear();
        String sql = "SELECT MA_LSP, TEN_LSP FROM LOAISANPHAM";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categoryList.add(new DBItem(rs.getInt("MA_LSP"), rs.getString("TEN_LSP")));
            }
        } catch (Exception e) {
            // Fallback sample categories
            categoryList.add(new DBItem(1, "Laptop"));
            categoryList.add(new DBItem(2, "Điện thoại"));
            categoryList.add(new DBItem(3, "Tai nghe"));
            categoryList.add(new DBItem(4, "Máy tính bảng"));
        }
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);
        String sql = "SELECT SP.*, LSP.TEN_LSP FROM SANPHAM SP LEFT JOIN LOAISANPHAM LSP ON SP.MA_LSP = LSP.MA_LSP ORDER BY SP.MA_SP DESC";
        DecimalFormat df = new DecimalFormat("#,###đ");
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                int id = rs.getInt("MA_SP");
                String name = rs.getString("TEN_SP");
                String cat = rs.getString("TEN_LSP");
                String desc = rs.getString("MO_TA");
                int qty = rs.getInt("SO_LUONG_DA_BAN");
                double price = rs.getDouble("GIA_BAN");
                String donVi = rs.getString("DON_VI_TINH");
                String status = rs.getString("TRANG_THAI");

                tableModel.addRow(new Object[]{
                    "SP" + String.format("%03d", id),
                    formatProductHtml(name),
                    cat != null ? cat : "Danh mục khác",
                    desc != null ? desc : "",
                    qty,
                    df.format(price),
                    donVi != null ? donVi : "Cái",
                    status != null ? status : "Đang kinh doanh",
                    id
                });
            }
            
            if (!hasData) {
                loadSampleData();
            }
        } catch (Exception e) {
            loadSampleData();
        }
    }

    private void loadSampleData() {
        DecimalFormat df = new DecimalFormat("#,###đ");
        Object[][] samples = {
            {"Laptop Dell XPS 13", "Laptop", "Laptop cao cấp siêu mỏng nhẹ", 15, 25000000.0, "Cái", "Đang kinh doanh"},
            {"iPhone 15 Pro Max", "Điện thoại", "Màn hình OLED, chip A17 Pro", 8, 35000000.0, "Cái", "Đang kinh doanh"},
            {"Samsung Galaxy S24", "Điện thoại", "Camera AI zoom 100x", 12, 22000000.0, "Cái", "Đang kinh doanh"},
            {"AirPods Pro 2", "Tai nghe", "Tai nghe chống ồn chủ động", 30, 6500000.0, "Cái", "Đang kinh doanh"},
            {"MacBook Pro 14\"", "Laptop", "Chip M3 Pro, màn hình Liquid Retina", 6, 45000000.0, "Cái", "Đang kinh doanh"},
            {"Sony WH-1000XM5", "Tai nghe", "Chống ồn đỉnh cao, pin 30h", 20, 8500000.0, "Cái", "Đang kinh doanh"},
            {"iPad Air M2", "Máy tính bảng", "Màn hình Liquid Retina 11 inch", 10, 18000000.0, "Cái", "Ngừng kinh doanh"}
        };
        int simId = 101;
        for (Object[] row : samples) {
            tableModel.addRow(new Object[]{
                "SP" + String.format("%03d", simId),
                formatProductHtml((String) row[0]),
                row[1],
                row[2],
                row[3],
                df.format((Double) row[4]),
                row[5],
                row[6],
                -1
            });
            simId++;
        }
    }

    private String formatProductHtml(String rawName) {
        if (rawName == null) return "";
        String[] parts = rawName.split(" ", 3);
        if (parts.length >= 2) {
            String boldText = parts[0] + " " + parts[1];
            String restText = parts.length > 2 ? parts[2] : "";
            return "<html><body style='font-family: Segoe UI; font-size: 11px;'>" +
                   "<font color='#0F172A'><b>" + boldText + "</b></font><br>" +
                   "<font color='#64748B'>" + restText + "</font>" +
                   "</body></html>";
        }
        return "<html><body style='font-family: Segoe UI; font-size: 11px;'>" +
               "<font color='#0F172A'><b>" + rawName + "</b></font>" +
               "</body></html>";
    }

    private String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", "").trim();
    }

    private void handleAddProduct() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        Frame frame = parent instanceof Frame ? (Frame) parent : null;
        ProductDialog dialog = new ProductDialog(frame, "Thêm sản phẩm mới", categoryList);
        dialog.setVisible(true);

        if (dialog.isSaveClicked()) {
            String name = dialog.getTenSp();
            DBItem cat = dialog.getSelectedCategory();
            String desc = dialog.getMoTa();
            int qty = dialog.getSoLuongDaBan();
            double price = dialog.getGiaBan();
            String unit = dialog.getDonViTinh();
            String status = dialog.getTrangThai();

            if (name.isEmpty() || cat == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String sql = "INSERT INTO SANPHAM (MA_LSP, TEN_SP, GIA_BAN, TRANG_THAI, SO_LUONG_DA_BAN, DON_VI_TINH, MO_TA) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection con = ConnectionUtils.getMyConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, cat.getId());
                ps.setString(2, name);
                ps.setDouble(3, price);
                ps.setString(4, status);
                ps.setInt(5, qty);
                ps.setString(6, unit);
                ps.setString(7, desc);
                ps.executeUpdate();
                loadDataToTable();
                JOptionPane.showMessageDialog(this, "Thêm sản phẩm thành công!");
            } catch (Exception ex) {
                // Fallback simulation
                tableModel.insertRow(0, new Object[]{
                    "SP" + String.format("%03d", (int)(Math.random() * 900) + 100),
                    formatProductHtml(name),
                    cat.getName(),
                    desc,
                    qty,
                    new DecimalFormat("#,###đ").format(price),
                    unit,
                    status,
                    -1
                });
                JOptionPane.showMessageDialog(this, "Đã lưu mô phỏng sản phẩm!");
            }
        }
    }

    private void handleEditProduct() {
        int row = dataTable.getSelectedRow();
        if (row == -1) return;
        int modelRow = dataTable.convertRowIndexToModel(row);
        
        String currentHtml = tableModel.getValueAt(modelRow, 1).toString();
        String currentName = stripHtml(currentHtml);
        String currentCat = tableModel.getValueAt(modelRow, 2).toString();
        String currentDesc = tableModel.getValueAt(modelRow, 3).toString();
        int currentQty = (int) tableModel.getValueAt(modelRow, 4);
        String currentPriceStr = tableModel.getValueAt(modelRow, 5).toString().replace(",", "").replace(".", "").replace("đ", "");
        double currentPrice = 0;
        try { currentPrice = Double.parseDouble(currentPriceStr); } catch (Exception ignored) {}
        String currentUnit = tableModel.getValueAt(modelRow, 6).toString();
        String currentStatus = tableModel.getValueAt(modelRow, 7).toString();
        Object idObj = tableModel.getValueAt(modelRow, 8);
        int id = idObj instanceof Integer ? (int) idObj : -1;

        Window parent = SwingUtilities.getWindowAncestor(this);
        Frame frame = parent instanceof Frame ? (Frame) parent : null;
        ProductDialog dialog = new ProductDialog(frame, "Cập nhật sản phẩm", categoryList);
        dialog.setTenSp(currentName);
        dialog.setMoTa(currentDesc);
        dialog.setSoLuongDaBan(currentQty);
        dialog.setGiaBan(currentPrice);
        dialog.setDonViTinh(currentUnit);
        dialog.setTrangThai(currentStatus);
        
        // Match category
        for (DBItem item : categoryList) {
            if (item.toString().equalsIgnoreCase(currentCat)) {
                dialog.setSelectedCategory(item.getId());
                break;
            }
        }

        dialog.setVisible(true);

        if (dialog.isSaveClicked()) {
            String name = dialog.getTenSp();
            DBItem cat = dialog.getSelectedCategory();
            String desc = dialog.getMoTa();
            int qty = dialog.getSoLuongDaBan();
            double price = dialog.getGiaBan();
            String unit = dialog.getDonViTinh();
            String status = dialog.getTrangThai();

            if (name.isEmpty() || cat == null) return;

            if (id != -1) {
                String sql = "UPDATE SANPHAM SET MA_LSP = ?, TEN_SP = ?, GIA_BAN = ?, TRANG_THAI = ?, SO_LUONG_DA_BAN = ?, DON_VI_TINH = ?, MO_TA = ? WHERE MA_SP = ?";
                try (Connection con = ConnectionUtils.getMyConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, cat.getId());
                    ps.setString(2, name);
                    ps.setDouble(3, price);
                    ps.setString(4, status);
                    ps.setInt(5, qty);
                    ps.setString(6, unit);
                    ps.setString(7, desc);
                    ps.setInt(8, id);
                    ps.executeUpdate();
                    loadDataToTable();
                    JOptionPane.showMessageDialog(this, "Cập nhật sản phẩm thành công!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                // Edit simulation row
                tableModel.setValueAt(formatProductHtml(name), modelRow, 1);
                tableModel.setValueAt(cat.getName(), modelRow, 2);
                tableModel.setValueAt(desc, modelRow, 3);
                tableModel.setValueAt(qty, modelRow, 4);
                tableModel.setValueAt(new DecimalFormat("#,###đ").format(price), modelRow, 5);
                tableModel.setValueAt(unit, modelRow, 6);
                tableModel.setValueAt(status, modelRow, 7);
                JOptionPane.showMessageDialog(this, "Đã cập nhật mô phỏng sản phẩm!");
            }
        }
    }

    private void handleDeleteProduct() {
        int row = dataTable.getSelectedRow();
        if (row == -1) return;
        int modelRow = dataTable.convertRowIndexToModel(row);
        Object idObj = tableModel.getValueAt(modelRow, 8);
        int id = idObj instanceof Integer ? (int) idObj : -1;

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa sản phẩm này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (id != -1) {
                // Delete children first due to constraints
                try (Connection con = ConnectionUtils.getMyConnection()) {
                    con.setAutoCommit(false);
                    try (PreparedStatement psBt = con.prepareStatement("DELETE FROM BIENTHE_SANPHAM WHERE MA_SP = ?");
                         PreparedStatement psSp = con.prepareStatement("DELETE FROM SANPHAM WHERE MA_SP = ?")) {
                        psBt.setInt(1, id);
                        psBt.executeUpdate();
                        psSp.setInt(1, id);
                        psSp.executeUpdate();
                        con.commit();
                        loadDataToTable();
                        JOptionPane.showMessageDialog(this, "Xóa sản phẩm thành công!");
                    } catch (Exception ex) {
                        con.rollback();
                        throw ex;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Lỗi khi xóa từ cơ sở dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Delete simulation row
                tableModel.removeRow(modelRow);
                JOptionPane.showMessageDialog(this, "Đã xóa sản phẩm mô phỏng!");
            }
        }
    }

    class ProductDialog extends JDialog {
        private JTextField txtTen = new JTextField();
        private JComboBox<DBItem> cbCategory = new JComboBox<>();
        private JTextField txtMoTa = new JTextField();
        private JTextField txtSoLuongDaBan = new JTextField("0");
        private JTextField txtGia = new JTextField("0");
        private JTextField txtDonViTinh = new JTextField("Cái");
        private JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"Đang kinh doanh", "Ngừng kinh doanh"});
        private JButton btnSave = new JButton("Lưu sản phẩm");
        private JButton btnCancel = new JButton("Hủy");
        private boolean isSaveClicked = false;

        public ProductDialog(Frame owner, String title, List<DBItem> categories) {
            super(owner, title, true);
            setSize(400, 520);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());
            
            JPanel content = new JPanel(new GridBagLayout());
            content.setBackground(Color.WHITE);
            content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(8, 0, 8, 0);
            gbc.weightx = 1.0;
            
            for (DBItem cat : categories) {
                cbCategory.addItem(cat);
            }
            
            Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
            Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);
            
            gbc.gridy = 0;
            JLabel lblTen = new JLabel("Tên sản phẩm"); lblTen.setFont(labelFont); content.add(lblTen, gbc);
            gbc.gridy = 1;
            txtTen.setFont(fieldFont); txtTen.setPreferredSize(new Dimension(340, 35)); content.add(txtTen, gbc);
            
            gbc.gridy = 2;
            JLabel lblCat = new JLabel("Danh mục"); lblCat.setFont(labelFont); content.add(lblCat, gbc);
            gbc.gridy = 3;
            cbCategory.setFont(fieldFont); cbCategory.setPreferredSize(new Dimension(340, 35)); content.add(cbCategory, gbc);
            
            gbc.gridy = 4;
            JLabel lblMoTa = new JLabel("Mô tả"); lblMoTa.setFont(labelFont); content.add(lblMoTa, gbc);
            gbc.gridy = 5;
            txtMoTa.setFont(fieldFont); txtMoTa.setPreferredSize(new Dimension(340, 35)); content.add(txtMoTa, gbc);

            gbc.gridy = 6;
            JLabel lblSold = new JLabel("Số lượng đã bán"); lblSold.setFont(labelFont); content.add(lblSold, gbc);
            gbc.gridy = 7;
            txtSoLuongDaBan.setFont(fieldFont); txtSoLuongDaBan.setPreferredSize(new Dimension(340, 35)); content.add(txtSoLuongDaBan, gbc);
            
            gbc.gridy = 8;
            JLabel lblGia = new JLabel("Giá bán (đ)"); lblGia.setFont(labelFont); content.add(lblGia, gbc);
            gbc.gridy = 9;
            txtGia.setFont(fieldFont); txtGia.setPreferredSize(new Dimension(340, 35)); content.add(txtGia, gbc);

            gbc.gridy = 10;
            JLabel lblUnit = new JLabel("Đơn vị tính"); lblUnit.setFont(labelFont); content.add(lblUnit, gbc);
            gbc.gridy = 11;
            txtDonViTinh.setFont(fieldFont); txtDonViTinh.setPreferredSize(new Dimension(340, 35)); content.add(txtDonViTinh, gbc);
            
            gbc.gridy = 12;
            JLabel lblStatus = new JLabel("Trạng thái"); lblStatus.setFont(labelFont); content.add(lblStatus, gbc);
            gbc.gridy = 13;
            cbTrangThai.setFont(fieldFont); cbTrangThai.setPreferredSize(new Dimension(340, 35)); content.add(cbTrangThai, gbc);
            
            JScrollPane scrollPane = new JScrollPane(content);
            scrollPane.setBorder(null);
            add(scrollPane, BorderLayout.CENTER);
            
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
            footer.setBackground(new Color(248, 250, 252));
            
            btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnSave.setBackground(new Color(37, 99, 235));
            btnSave.setForeground(Color.WHITE);
            btnSave.setFocusPainted(false);
            btnSave.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
            
            btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnCancel.setBackground(new Color(226, 232, 240));
            btnCancel.setForeground(new Color(71, 85, 105));
            btnCancel.setFocusPainted(false);
            btnCancel.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
            
            footer.add(btnCancel);
            footer.add(btnSave);
            add(footer, BorderLayout.SOUTH);
            
            btnSave.addActionListener(e -> {
                isSaveClicked = true;
                setVisible(false);
            });
            btnCancel.addActionListener(e -> setVisible(false));
        }
        
        public String getTenSp() { return txtTen.getText().trim(); }
        public void setTenSp(String name) { txtTen.setText(name); }
        public DBItem getSelectedCategory() { return (DBItem) cbCategory.getSelectedItem(); }
        public void setSelectedCategory(int maLsp) {
            for (int i = 0; i < cbCategory.getItemCount(); i++) {
                if (cbCategory.getItemAt(i).getId() == maLsp) {
                    cbCategory.setSelectedIndex(i);
                    break;
                }
            }
        }
        public String getMoTa() { return txtMoTa.getText().trim(); }
        public void setMoTa(String desc) { txtMoTa.setText(desc); }
        public int getSoLuongDaBan() {
            try { return Integer.parseInt(txtSoLuongDaBan.getText().trim()); } catch (Exception e) { return 0; }
        }
        public void setSoLuongDaBan(int qty) { txtSoLuongDaBan.setText(String.valueOf(qty)); }
        public double getGiaBan() {
            try { return Double.parseDouble(txtGia.getText().trim()); } catch (Exception e) { return 0.0; }
        }
        public void setGiaBan(double price) { txtGia.setText(String.valueOf((long)price)); }
        public String getDonViTinh() { return txtDonViTinh.getText().trim(); }
        public void setDonViTinh(String unit) { txtDonViTinh.setText(unit); }
        public String getTrangThai() { return cbTrangThai.getSelectedItem().toString(); }
        public void setTrangThai(String status) { cbTrangThai.setSelectedItem(status); }
        public boolean isSaveClicked() { return isSaveClicked; }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
}
