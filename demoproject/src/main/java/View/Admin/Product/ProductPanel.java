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


class ActionPanel extends JPanel {
    public JButton btnEdit = new JButton();
    
    public ActionPanel() {
        setLayout(new GridBagLayout());
        setOpaque(true);
        setBackground(Color.WHITE);
        
        btnEdit.setText("Sửa");
        View.Admin.UIUtils.styleButton(btnEdit);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(btnEdit, gbc);
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
    private int editingRow = -1;
    
    public ActionCellEditor(JTable table, Runnable onEdit) {
        this.table = table;
        panel.btnEdit.addActionListener(e -> {
            int row = editingRow;
            stopCellEditing();
            if (row >= 0) {
                table.setRowSelectionInterval(row, row);
            }
            onEdit.run();
        });
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

public class ProductPanel extends javax.swing.JPanel {

    private DefaultTableModel tableModel;
    private JTable dataTable;
    private List<DBItem> categoryList = new ArrayList<>();
    private JLabel lblLastUpdate;
    private boolean isProductMode = true;
    private JButton btnSwitchMode;
    private JButton btnDeleteSelected;
    private JButton btnAdd;
    private JLabel lblTitle;
    private JTextField txtSearch;

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

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftHeader.setOpaque(false);
        
        lblTitle = new JLabel("Quản lý sản phẩm");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(15, 23, 42));
        
        lblLastUpdate = new JLabel("Chưa cập nhật");
        lblLastUpdate.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblLastUpdate.setForeground(new Color(148, 163, 184));
        
        leftHeader.add(lblTitle);
        leftHeader.add(lblLastUpdate);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightHeader.setOpaque(false);

        btnSwitchMode = new JButton("🗂️ Loại sản phẩm");
        View.Admin.UIUtils.styleButton(btnSwitchMode);
        btnSwitchMode.setPreferredSize(new Dimension(160, 36));
        btnSwitchMode.addActionListener(e -> toggleMode());

        btnDeleteSelected = new JButton("🗑️ Xóa");
        View.Admin.UIUtils.styleButton(btnDeleteSelected);
        btnDeleteSelected.setPreferredSize(new Dimension(140, 36));
        btnDeleteSelected.addActionListener(e -> handleDeleteSelected());

        JButton btnRefresh = new JButton("↻ Cập nhật");
        View.Admin.UIUtils.styleButton(btnRefresh);
        btnRefresh.setPreferredSize(new Dimension(130, 36));
        btnRefresh.addActionListener(e -> {
            if (isProductMode) {
                loadCategories();
                loadDataToTable();
            } else {
                loadCategoriesToTable();
            }
        });

        btnAdd = new JButton("+ Thêm sản phẩm");
        View.Admin.UIUtils.styleButton(btnAdd);
        btnAdd.setPreferredSize(new Dimension(160, 36));
        btnAdd.addActionListener(e -> {
            if (isProductMode) {
                handleAddProduct();
            } else {
                handleAddCategory();
            }
        });

        rightHeader.add(btnSwitchMode);
        rightHeader.add(btnDeleteSelected);
        rightHeader.add(btnRefresh);
        rightHeader.add(btnAdd);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);

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

        txtSearch = new JTextField("Tìm kiếm sản phẩm...");
        txtSearch.setBorder(null);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtSearch.setForeground(Color.GRAY);
        txtSearch.setSelectionColor(new Color(210, 160, 205));
        txtSearch.setSelectedTextColor(Color.WHITE);
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals("Tìm kiếm sản phẩm...") || txtSearch.getText().equals("Tìm kiếm loại sản phẩm...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(new Color(15, 23, 42));
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText(isProductMode ? "Tìm kiếm sản phẩm..." : "Tìm kiếm loại sản phẩm...");
                    txtSearch.setForeground(Color.GRAY);
                }
            }
        });
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        centerPanel.add(searchPanel, BorderLayout.NORTH);

        // Table initialization
        String[] columns = {"", "Mã sản phẩm", "Tên sản phẩm", "Loại sản phẩm", "Mô tả", "Số lượng đã bán", "Giá bán", "Đơn vị tính", "Trạng thái", "Thao tác"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public Class<?> getColumnClass(int c) {
                if (c == 0) return Boolean.class;
                return super.getColumnClass(c);
            }
            @Override public boolean isCellEditable(int r, int c) {
                return c == 0 || c == 9;
            }
        };
        
        dataTable = new JTable(tableModel);
        dataTable.setRowHeight(60);
        dataTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dataTable.setBackground(Color.WHITE);
        dataTable.setShowVerticalLines(false);
        dataTable.setShowHorizontalLines(true);
        dataTable.setGridColor(new Color(241, 245, 249));
        dataTable.setSelectionBackground(new Color(245, 235, 250));
        dataTable.setSelectionForeground(new Color(15, 23, 42));
        
        updateTableStructure();

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
                if (text.isEmpty() || text.equals("Tìm kiếm sản phẩm...") || text.equals("Tìm kiếm loại sản phẩm...")) {
                    sorter.setRowFilter(null);
                } else {
                    final String searchLower = text.toLowerCase();
                    sorter.setRowFilter(new RowFilter<DefaultTableModel, Object>() {
                        @Override
                        public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Object> entry) {
                            if (isProductMode) {
                                String maSp = entry.getStringValue(1).toLowerCase();
                                String tenSp = stripHtml(entry.getStringValue(2)).toLowerCase();
                                String danhMuc = entry.getStringValue(3).toLowerCase();
                                return maSp.contains(searchLower) || tenSp.contains(searchLower) || danhMuc.contains(searchLower);
                            } else {
                                String maLsp = entry.getStringValue(1).toLowerCase();
                                String tenLsp = entry.getStringValue(2).toLowerCase();
                                return maLsp.contains(searchLower) || tenLsp.contains(searchLower);
                            }
                        }
                    });
                }
            }
        });

        // Load data from DB in background
        loadCategories();
        loadDataToTable();
    }

    private void updateTableStructure() {
        if (isProductMode) {
            String[] columns = {"", "Mã sản phẩm", "Tên sản phẩm", "Loại sản phẩm", "Mô tả", "Số lượng đã bán", "Giá bán", "Đơn vị tính", "Trạng thái", "Thao tác"};
            
            tableModel = new DefaultTableModel(columns, 0) {
                @Override public Class<?> getColumnClass(int c) {
                    if (c == 0) return Boolean.class;
                    return super.getColumnClass(c);
                }
                @Override public boolean isCellEditable(int r, int c) {
                    return c == 0 || c == 9;
                }
            };
            dataTable.setModel(tableModel);
            dataTable.setRowSorter(null);
            dataTable.setRowHeight(60);

            // Re-adjust column widths/renderers
            dataTable.getColumnModel().getColumn(0).setPreferredWidth(40);
            dataTable.getColumnModel().getColumn(0).setMaxWidth(40);
            dataTable.getColumnModel().getColumn(2).setCellRenderer(new ProductCellRenderer());
            dataTable.getColumnModel().getColumn(8).setCellRenderer(new StatusBadgeRenderer());

            // Center align column content
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            dataTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
            dataTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
            dataTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
            dataTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
            dataTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);

            // Action Editor & Renderer
            ActionCellEditor actionEditor = new ActionCellEditor(dataTable, () -> handleEditProduct());
            dataTable.getColumnModel().getColumn(9).setCellRenderer(new ActionCellRenderer());
            dataTable.getColumnModel().getColumn(9).setCellEditor(actionEditor);
        } else {
            String[] columns = {"", "Mã loại sản phẩm", "Tên loại sản phẩm", "Mô tả", "Tổng số mặt hàng", "Thao tác"};
            
            tableModel = new DefaultTableModel(columns, 0) {
                @Override public Class<?> getColumnClass(int c) {
                    if (c == 0) return Boolean.class;
                    return super.getColumnClass(c);
                }
                @Override public boolean isCellEditable(int r, int c) {
                    return c == 0 || c == 5;
                }
            };
            dataTable.setModel(tableModel);
            dataTable.setRowSorter(null);
            dataTable.setRowHeight(60);

            // Widths & Renderers
            dataTable.getColumnModel().getColumn(0).setPreferredWidth(40);
            dataTable.getColumnModel().getColumn(0).setMaxWidth(40);

            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            dataTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
            dataTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

            // Action Editor & Renderer
            ActionCellEditor actionEditor = new ActionCellEditor(dataTable, () -> handleEditCategory());
            dataTable.getColumnModel().getColumn(5).setCellRenderer(new ActionCellRenderer());
            dataTable.getColumnModel().getColumn(5).setCellEditor(actionEditor);
        }
        
        // General styling for the header
        dataTable.getTableHeader().setPreferredSize(new Dimension(0, 45));
        dataTable.getTableHeader().setBackground(Color.WHITE);
        dataTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        dataTable.getTableHeader().setForeground(new Color(100, 116, 139));
        dataTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
    }

    private void toggleMode() {
        isProductMode = !isProductMode;
        if (isProductMode) {
            lblTitle.setText("Quản lý sản phẩm");
            btnSwitchMode.setText("🗂️ Loại sản phẩm");
            btnAdd.setText("+ Thêm sản phẩm");
            txtSearch.setText("Tìm kiếm sản phẩm...");
            txtSearch.setForeground(Color.GRAY);
            updateTableStructure();
            loadDataToTable();
        } else {
            lblTitle.setText("Quản lý loại sản phẩm");
            btnSwitchMode.setText("📦 Sản phẩm");
            btnAdd.setText("+ Thêm loại sản phẩm");
            txtSearch.setText("Tìm kiếm loại sản phẩm...");
            txtSearch.setForeground(Color.GRAY);
            updateTableStructure();
            loadCategoriesToTable();
        }
        this.revalidate();
        this.repaint();
    }

    private void loadCategories() {
        categoryList.clear();
        try {
            List<Model.LoaiSanPham> list = Controller.LoaiSanPhamDAO.getAllLoaiSanPham();
            for (Model.LoaiSanPham lsp : list) {
                categoryList.add(new DBItem(lsp.getMaLsp(), lsp.getTenLsp()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);
        String time = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));
        if (lblLastUpdate != null) {
            lblLastUpdate.setText("Cập nhật lúc: " + time);
        }
        DecimalFormat df = new DecimalFormat("#,###đ");
        try {
            List<Model.SanPham> list = Controller.SanPhamDAO.getAllSanPham();
            for (Model.SanPham sp : list) {
                int id = sp.getMaSp();
                String name = sp.getTenSp();
                
                String cat = "Danh mục khác";
                for (DBItem item : categoryList) {
                    if (item.getId() == sp.getMaLsp()) {
                        cat = item.getName();
                        break;
                    }
                }
                
                String desc = sp.getMoTa();
                int qty = sp.getSoLuongDaBan();
                double price = sp.getGiaBan();
                String donVi = sp.getDonViTinh();
                String status = sp.getTrangThai();

                tableModel.addRow(new Object[]{
                    Boolean.FALSE,
                    String.valueOf(id),
                    formatProductHtml(name),
                    cat,
                    desc != null ? desc : "",
                    qty,
                    df.format(price),
                    donVi != null ? donVi : "Cái",
                    status != null ? status : "Đang kinh doanh",
                    id
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCategoriesToTable() {
        tableModel.setRowCount(0);
        String time = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));
        if (lblLastUpdate != null) {
            lblLastUpdate.setText("Cập nhật lúc: " + time);
        }
        try {
            List<Model.LoaiSanPham> list = Controller.LoaiSanPhamDAO.getAllLoaiSanPham();
            for (Model.LoaiSanPham lsp : list) {
                tableModel.addRow(new Object[]{
                    Boolean.FALSE,
                    String.valueOf(lsp.getMaLsp()),
                    lsp.getTenLsp(),
                    lsp.getMoTa() != null ? lsp.getMoTa() : "",
                    lsp.getTongSoMatHang(),
                    lsp.getMaLsp()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatProductHtml(String rawName) {
        return rawName != null ? rawName : "";
    }

    private String stripHtml(String html) {
        if (html == null) return "";
        // Replace <br> tags with a space to prevent words from sticking together
        html = html.replaceAll("(?i)<br[^>]*>", " ");
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

            try {
                Model.SanPham sp = new Model.SanPham();
                sp.setMaLsp(cat.getId());
                sp.setTenSp(name);
                sp.setTrangThai(status);
                sp.setSoLuongDaBan(qty);
                sp.setDonViTinh(unit);
                sp.setMoTa(desc);
                
                boolean success = Controller.SanPhamDAO.addSanPham(sp, price);
                if (success) {
                    loadDataToTable();
                    JOptionPane.showMessageDialog(this, "Thêm sản phẩm thành công!");
                } else {
                    throw new Exception("Không thể thêm sản phẩm");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                // Fallback simulation
                tableModel.insertRow(0, new Object[]{
                    Boolean.FALSE,
                    String.valueOf((int)(Math.random() * 900) + 100),
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

    private void handleAddCategory() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        Frame frame = parent instanceof Frame ? (Frame) parent : null;
        CategoryDialog dialog = new CategoryDialog(frame, "Thêm loại sản phẩm mới");
        dialog.setVisible(true);

        if (dialog.isSaveClicked()) {
            String name = dialog.getTenLsp();
            String desc = dialog.getMoTa();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng điền tên loại sản phẩm!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Model.LoaiSanPham lsp = new Model.LoaiSanPham();
                lsp.setTenLsp(name);
                lsp.setMoTa(desc);

                boolean success = Controller.LoaiSanPhamDAO.addLoaiSanPham(lsp);
                if (success) {
                    loadCategoriesToTable();
                    JOptionPane.showMessageDialog(this, "Thêm loại sản phẩm thành công!");
                } else {
                    throw new Exception("Không thể thêm loại sản phẩm");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                // Fallback simulation
                tableModel.insertRow(0, new Object[]{
                    Boolean.FALSE,
                    String.valueOf((int)(Math.random() * 900) + 100),
                    name,
                    desc,
                    0,
                    -1
                });
                JOptionPane.showMessageDialog(this, "Đã lưu mô phỏng loại sản phẩm!");
            }
        }
    }

    private void handleEditProduct() {
        int row = dataTable.getSelectedRow();
        if (row == -1) return;
        int modelRow = dataTable.convertRowIndexToModel(row);
        
        String currentHtml = tableModel.getValueAt(modelRow, 2).toString();
        String currentName = stripHtml(currentHtml);
        String currentCat = tableModel.getValueAt(modelRow, 3).toString();
        String currentDesc = tableModel.getValueAt(modelRow, 4).toString();
        int currentQty = (int) tableModel.getValueAt(modelRow, 5);
        String currentPriceStr = tableModel.getValueAt(modelRow, 6).toString().replace(",", "").replace(".", "").replace("đ", "");
        double currentPrice = 0;
        try { currentPrice = Double.parseDouble(currentPriceStr); } catch (Exception ignored) {}
        String currentUnit = tableModel.getValueAt(modelRow, 7).toString();
        String currentStatus = tableModel.getValueAt(modelRow, 8).toString();
        Object idObj = tableModel.getValueAt(modelRow, 9);
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
                try {
                    Model.SanPham sp = new Model.SanPham();
                    sp.setMaSp(id);
                    sp.setMaLsp(cat.getId());
                    sp.setTenSp(name);
                    sp.setTrangThai(status);
                    sp.setSoLuongDaBan(qty);
                    sp.setDonViTinh(unit);
                    sp.setMoTa(desc);
                    
                    boolean success = Controller.SanPhamDAO.updateSanPham(sp, price);
                    if (success) {
                        loadDataToTable();
                        JOptionPane.showMessageDialog(this, "Cập nhật sản phẩm thành công!");
                    } else {
                        throw new Exception("Không thể cập nhật sản phẩm");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật cơ sở dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Edit simulation row
                tableModel.setValueAt(formatProductHtml(name), modelRow, 2);
                tableModel.setValueAt(cat.getName(), modelRow, 3);
                tableModel.setValueAt(desc, modelRow, 4);
                tableModel.setValueAt(qty, modelRow, 5);
                tableModel.setValueAt(new DecimalFormat("#,###đ").format(price), modelRow, 6);
                tableModel.setValueAt(unit, modelRow, 7);
                tableModel.setValueAt(status, modelRow, 8);
                JOptionPane.showMessageDialog(this, "Đã cập nhật mô phỏng sản phẩm!");
            }
        }
    }

    private void handleEditCategory() {
        int row = dataTable.getSelectedRow();
        if (row == -1) return;
        int modelRow = dataTable.convertRowIndexToModel(row);

        String currentName = tableModel.getValueAt(modelRow, 2).toString();
        String currentDesc = tableModel.getValueAt(modelRow, 3).toString();
        Object idObj = tableModel.getValueAt(modelRow, 5);
        int id = idObj instanceof Integer ? (int) idObj : -1;

        Window parent = SwingUtilities.getWindowAncestor(this);
        Frame frame = parent instanceof Frame ? (Frame) parent : null;
        CategoryDialog dialog = new CategoryDialog(frame, "Cập nhật loại sản phẩm");
        dialog.setTenLsp(currentName);
        dialog.setMoTa(currentDesc);
        dialog.setVisible(true);

        if (dialog.isSaveClicked()) {
            String name = dialog.getTenLsp();
            String desc = dialog.getMoTa();

            if (name.isEmpty()) return;

            if (id != -1) {
                try {
                    Model.LoaiSanPham lsp = new Model.LoaiSanPham();
                    lsp.setMaLsp(id);
                    lsp.setTenLsp(name);
                    lsp.setMoTa(desc);

                    boolean success = Controller.LoaiSanPhamDAO.updateLoaiSanPham(lsp);
                    if (success) {
                        loadCategoriesToTable();
                        JOptionPane.showMessageDialog(this, "Cập nhật loại sản phẩm thành công!");
                    } else {
                        throw new Exception("Không thể cập nhật loại sản phẩm");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật cơ sở dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Edit simulation row
                tableModel.setValueAt(name, modelRow, 2);
                tableModel.setValueAt(desc, modelRow, 3);
                JOptionPane.showMessageDialog(this, "Đã cập nhật mô phỏng loại sản phẩm!");
            }
        }
    }

    private void handleDeleteSelected() {
        if (dataTable.isEditing()) {
            dataTable.getCellEditor().stopCellEditing();
        }

        List<Integer> selectedIds = new ArrayList<>();
        List<Integer> selectedModelRows = new ArrayList<>();

        int idColumnIndex = isProductMode ? 9 : 5;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean checked = (Boolean) tableModel.getValueAt(i, 0);
            if (checked != null && checked) {
                selectedModelRows.add(i);
                Object idObj = tableModel.getValueAt(i, idColumnIndex);
                if (idObj instanceof Integer) {
                    selectedIds.add((Integer) idObj);
                }
            }
        }

        if (selectedIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một mục để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String itemType = isProductMode ? "sản phẩm" : "loại sản phẩm";
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "⚠️ CẢNH BÁO: Hành động này sẽ XÓA HẾT tất cả dữ liệu liên quan đến " + itemType + " đó\n" +
            "(bao gồm tất cả mã serial, số lượng tồn kho, các phiên bản/biến thể liên quan)!\n\n" +
            "Bạn có chắc chắn muốn xóa không?",
            "Cảnh báo xóa dữ liệu liên quan",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            int successCount = 0;
            int failedCount = 0;
            String failReason = "";

            for (int id : selectedIds) {
                if (id != -1) {
                    try {
                        boolean success;
                        if (isProductMode) {
                            success = Controller.SanPhamDAO.deleteSanPham(id);
                        } else {
                            success = Controller.LoaiSanPhamDAO.deleteLoaiSanPham(id);
                        }
                        if (success) {
                            successCount++;
                        } else {
                            failedCount++;
                        }
                    } catch (Exception ex) {
                        failedCount++;
                        String errorMsg = ex.getMessage();
                        // Bắt lỗi khóa ngoại ORA-02292 và dịch sang tiếng Việt thân thiện
                        if (errorMsg != null && errorMsg.contains("ORA-02292")) {
                            if (!isProductMode) {
                                failReason = "Không thể xóa Loại sản phẩm này vì vẫn còn Sản phẩm bên trong (Vui lòng xóa sản phẩm trước).";
                            } else {
                                failReason = "Không thể xóa Sản phẩm này vì dữ liệu đã nằm trong Hóa đơn hoặc Phiếu nhập.";
                            }
                        } else {
                            failReason = errorMsg;
                        }
                    }
                } else {
                    // Simulation row
                    successCount++;
                }
            }

            if (isProductMode) {
                loadDataToTable();
            } else {
                loadCategoriesToTable();
            }

            if (failedCount > 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "Lỗi bảo vệ dữ liệu:\n\n- Số lượng xóa thành công: " + successCount + " mục.\n- Số lượng thất bại: " + failedCount + " mục.\n\nNguyên nhân thất bại:\n" + failReason,
                    "Từ chối xóa dữ liệu",
                    JOptionPane.WARNING_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(this, "Đã xóa thành công tất cả các mục đã chọn!");
            }
        }
    }

    class CategoryDialog extends JDialog {
        private JTextField txtTen = new JTextField();
        private JTextField txtMoTa = new JTextField();
        private JButton btnSave = new JButton("Lưu loại sản phẩm");
        private JButton btnCancel = new JButton("Hủy");
        private boolean isSaveClicked = false;

        public CategoryDialog(Frame owner, String title) {
            super(owner, title, true);
            setSize(400, 300);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());
            
            JPanel content = new JPanel(new GridBagLayout());
            content.setBackground(Color.WHITE);
            content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(10, 0, 10, 0);
            gbc.weightx = 1.0;
            
            Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
            Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);
            
            gbc.gridy = 0;
            JLabel lblTen = new JLabel("Tên loại sản phẩm"); lblTen.setFont(labelFont); content.add(lblTen, gbc);
            gbc.gridy = 1;
            txtTen.setFont(fieldFont); txtTen.setPreferredSize(new Dimension(340, 35)); content.add(txtTen, gbc);
            
            gbc.gridy = 2;
            JLabel lblMoTa = new JLabel("Mô tả"); lblMoTa.setFont(labelFont); content.add(lblMoTa, gbc);
            gbc.gridy = 3;
            txtMoTa.setFont(fieldFont); txtMoTa.setPreferredSize(new Dimension(340, 35)); content.add(txtMoTa, gbc);
            
            add(content, BorderLayout.CENTER);
            
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
            footer.setBackground(new Color(248, 250, 252));
            
            View.Admin.UIUtils.styleButton(btnSave);
            View.Admin.UIUtils.styleButton(btnCancel);
            
            footer.add(btnCancel);
            footer.add(btnSave);
            add(footer, BorderLayout.SOUTH);
            
            btnSave.addActionListener(e -> {
                isSaveClicked = true;
                setVisible(false);
            });
            btnCancel.addActionListener(e -> setVisible(false));
        }
        
        public String getTenLsp() { return txtTen.getText().trim(); }
        public void setTenLsp(String name) { txtTen.setText(name); }
        public String getMoTa() { return txtMoTa.getText().trim(); }
        public void setMoTa(String desc) { txtMoTa.setText(desc); }
        public boolean isSaveClicked() { return isSaveClicked; }
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
            JLabel lblCat = new JLabel("Loại sản phẩm"); lblCat.setFont(labelFont); content.add(lblCat, gbc);
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
            
            View.Admin.UIUtils.styleButton(btnSave);
            View.Admin.UIUtils.styleButton(btnCancel);
            
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
