package View.Admin.Warehouse;

import Controller.Admin.TonKho.TonKhoDAO;

import View.Admin.UIUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Panel tồn kho chi nhánh — 3 cấp drill-down:
 * Chi nhánh -> Sản phẩm (số lượng) -> Serial (trạng thái)
 */
public class WarehousePanel extends javax.swing.JPanel {

    private TonKhoDAO dao;
    private CardLayout cardLayout;
    private JPanel cardContainer;

    // State
    private int currentMaCN = -1;
    private String currentTenCN = "";
    private int currentMaBienthe = -1;
    private String currentTenSP = "";
    private String currentTenBienthe = "";

    // Tables
    private DefaultTableModel branchModel, productModel, serialModel;
    private JTable branchTable, productTable, serialTable;
    private JTextField txtSearchBranch, txtSearchProduct, txtSearchSerial;
    private JLabel lblLastUpdate;

    private static final DecimalFormat DF = new DecimalFormat("#,###");
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final Color PURPLE = new Color(142, 68, 173);
    private static final Color PURPLE_LIGHT = new Color(175, 122, 197);
    private static final Color BG = new Color(248, 250, 252);

    public WarehousePanel() {
        dao = new TonKhoDAO();
        initUI();
        loadBranches(null);
    }

    private void initUI() {
        this.setLayout(new BorderLayout());
        this.setBackground(BG);

        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setBackground(BG);

        cardContainer.add(buildBranchCard(), "branches");
        cardContainer.add(buildProductCard(), "products");
        cardContainer.add(buildSerialCard(), "serials");

        this.add(cardContainer, BorderLayout.CENTER);
        cardLayout.show(cardContainer, "branches");
    }

    // =====================================================================
    //  CARD 1: DANH SÁCH CHI NHÁNH
    // =====================================================================

    private JPanel buildBranchCard() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG);

        // Custom Header for branch card
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, PURPLE_LIGHT),
            new EmptyBorder(14, 20, 14, 20)
        ));

        // Left Header: Title + Timestamp
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftHeader.setOpaque(false);
        
        JLabel lblTitle = new JLabel("Tồn kho chi nhánh");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(30, 41, 59));
        leftHeader.add(lblTitle);

        lblLastUpdate = new JLabel("Chưa cập nhật");
        lblLastUpdate.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblLastUpdate.setForeground(new Color(148, 163, 184));
        leftHeader.add(lblLastUpdate);
        
        header.add(leftHeader, BorderLayout.WEST);

        // Right Header: Search Panel + Cập nhật Button + Sửa chi nhánh Button
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightHeader.setOpaque(false);

        // Search Panel
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setOpaque(false);
        searchPanel.setPreferredSize(new Dimension(200, 36));

        txtSearchBranch = new JTextField("Tìm chi nhánh...");
        txtSearchBranch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearchBranch.setForeground(Color.GRAY);
        txtSearchBranch.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (txtSearchBranch.getForeground() == Color.GRAY) { txtSearchBranch.setText(""); txtSearchBranch.setForeground(new Color(30, 41, 59)); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (txtSearchBranch.getText().isEmpty()) { txtSearchBranch.setText("Tìm chi nhánh..."); txtSearchBranch.setForeground(Color.GRAY); }
            }
        });
        txtSearchBranch.addActionListener(e -> loadBranches(getSearchText(txtSearchBranch)));

        JButton btnSearch = new JButton("Tìm");
        UIUtils.styleButton(btnSearch);
        btnSearch.addActionListener(e -> loadBranches(getSearchText(txtSearchBranch)));

        searchPanel.add(txtSearchBranch, BorderLayout.CENTER);
        searchPanel.add(btnSearch, BorderLayout.EAST);
        rightHeader.add(searchPanel);

        // Button Cập nhật
        JButton btnRefresh = new JButton("Cập nhật");
        UIUtils.styleButton(btnRefresh);
        btnRefresh.setPreferredSize(new Dimension(130, 36));
        btnRefresh.addActionListener(e -> loadBranches(getSearchText(txtSearchBranch)));
        rightHeader.add(btnRefresh);

        // Button Sửa chi nhánh
        JButton btnEditBranch = new JButton("Sửa chi nhánh");
        UIUtils.styleButton(btnEditBranch);
        btnEditBranch.setPreferredSize(new Dimension(150, 36));
        btnEditBranch.addActionListener(e -> showEditBranchDialog());
        // Ẩn nếu không có quyền Sửa
        btnEditBranch.setVisible(Controller.Admin.PermissionService.canEdit("Ton kho CN"));
        rightHeader.add(btnEditBranch);

        header.add(rightHeader, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"Mã CN", "Tên chi nhánh", "Địa chỉ", "SĐT Hotline", "Trạng thái", "Tổng tồn kho"};
        branchModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        branchTable = buildStyledTable(branchModel);
        branchTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        branchTable.getColumnModel().getColumn(0).setMaxWidth(80);
        branchTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        branchTable.getColumnModel().getColumn(2).setPreferredWidth(250);
        branchTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        branchTable.getColumnModel().getColumn(4).setPreferredWidth(130);
        branchTable.getColumnModel().getColumn(5).setPreferredWidth(120);

        // Status renderer
        branchTable.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        // Right-align stock
        DefaultTableCellRenderer rightBold = new DefaultTableCellRenderer();
        rightBold.setHorizontalAlignment(SwingConstants.CENTER);
        rightBold.setFont(new Font("Segoe UI", Font.BOLD, 14));
        branchTable.getColumnModel().getColumn(5).setCellRenderer(rightBold);
        // Center MA_CN
        DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
        centerR.setHorizontalAlignment(SwingConstants.CENTER);
        branchTable.getColumnModel().getColumn(0).setCellRenderer(centerR);
        branchTable.getColumnModel().getColumn(3).setCellRenderer(centerR);

        // Double-click -> drill into branch
        branchTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = branchTable.getSelectedRow();
                    if (row >= 0) {
                        int modelRow = branchTable.convertRowIndexToModel(row);
                        currentMaCN = (int) branchModel.getValueAt(modelRow, 0);
                        currentTenCN = (String) branchModel.getValueAt(modelRow, 1);
                        showProducts();
                    }
                }
            }
        });

        JScrollPane sp = new JScrollPane(branchTable);
        sp.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        sp.getViewport().setBackground(Color.WHITE);
        panel.add(sp, BorderLayout.CENTER);

        return panel;
    }

    private void loadBranches(String keyword) {
        branchModel.setRowCount(0);
        try {
            List<Map<String, Object>> list = dao.getAllBranches(keyword);
            for (Map<String, Object> r : list) {
                branchModel.addRow(new Object[]{
                    r.get("MA_CN"),
                    r.get("TEN_CN"),
                    r.get("DIA_CHI") != null ? r.get("DIA_CHI") : "",
                    r.get("SDT_HOTLINE") != null ? r.get("SDT_HOTLINE") : "",
                    r.get("TRANG_THAI") != null ? r.get("TRANG_THAI") : "",
                    r.get("TONG_TON")
                });
            }
            if (lblLastUpdate != null) {
                String time = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));
                lblLastUpdate.setText("Cập nhật lúc: " + time);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // =====================================================================
    //  CARD 2: SẢN PHẨM TỒN KHO TẠI CHI NHÁNH
    // =====================================================================

    private JLabel lblProductTitle;

    private JPanel buildProductCard() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG);

        // Header with back button
        lblProductTitle = new JLabel();
        JPanel header = createHeader("", "<- Quay lại", e -> {
            cardLayout.show(cardContainer, "branches");
        });
        txtSearchProduct = addSearchToHeader(header, "Tìm sản phẩm...", e -> loadProducts(getSearchText(txtSearchProduct)));

        // Replace title label
        for (Component c : ((JPanel) header.getComponent(0)).getComponents()) {
            if (c instanceof JLabel && !(c instanceof JButton)) {
                lblProductTitle = (JLabel) c;
                break;
            }
        }
        panel.add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"Mã SP", "Tên sản phẩm", "Biến thể", "Giá bán", "Số lượng tồn", "Cập nhật"};
        productModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        productTable = buildStyledTable(productModel);
        productTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        productTable.getColumnModel().getColumn(0).setMaxWidth(80);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        productTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        productTable.getColumnModel().getColumn(5).setPreferredWidth(130);

        // Center columns
        DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
        centerR.setHorizontalAlignment(SwingConstants.CENTER);
        productTable.getColumnModel().getColumn(0).setCellRenderer(centerR);
        productTable.getColumnModel().getColumn(5).setCellRenderer(centerR);

        // Right-align price
        DefaultTableCellRenderer priceR = new DefaultTableCellRenderer();
        priceR.setHorizontalAlignment(SwingConstants.RIGHT);
        productTable.getColumnModel().getColumn(3).setCellRenderer(priceR);

        // Bold stock count
        DefaultTableCellRenderer stockR = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                long qty = 0;
                try { qty = Long.parseLong(v.toString()); } catch (Exception ignored) {}
                if (!s) {
                    if (qty == 0) lbl.setForeground(new Color(185, 28, 28));
                    else if (qty < 5) lbl.setForeground(new Color(180, 130, 0));
                    else lbl.setForeground(new Color(5, 122, 85));
                }
                return lbl;
            }
        };
        productTable.getColumnModel().getColumn(4).setCellRenderer(stockR);

        // Store MA_BIENTHE as hidden data
        // Double-click -> drill into serials
        productTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = productTable.getSelectedRow();
                    if (row >= 0) {
                        int modelRow = productTable.convertRowIndexToModel(row);
                        currentMaBienthe = (int) productTable.getClientProperty("bt_" + modelRow);
                        currentTenSP = (String) productModel.getValueAt(modelRow, 1);
                        currentTenBienthe = (String) productModel.getValueAt(modelRow, 2);
                        showSerials();
                    }
                }
            }
        });

        JScrollPane sp = new JScrollPane(productTable);
        sp.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        sp.getViewport().setBackground(Color.WHITE);
        panel.add(sp, BorderLayout.CENTER);

        return panel;
    }

    private void showProducts() {
        lblProductTitle.setText("Tồn kho — " + currentTenCN);
        loadProducts(null);
        if (txtSearchProduct != null) txtSearchProduct.setText("");
        cardLayout.show(cardContainer, "products");
    }

    private void loadProducts(String keyword) {
        productModel.setRowCount(0);
        try {
            List<Map<String, Object>> list = dao.getProductsByBranch(currentMaCN, keyword);
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> r = list.get(i);
                Timestamp ts = (Timestamp) r.get("NGAY_CAP_NHAT");
                productModel.addRow(new Object[]{
                    r.get("MA_SP"),
                    r.get("TEN_SP"),
                    r.get("TEN_BIENTHE") != null ? r.get("TEN_BIENTHE") : "",
                    DF.format((long) r.get("GIA_BAN")) + "đ",
                    r.get("SO_LUONG_TON"),
                    ts != null ? SDF.format(ts) : ""
                });
                productTable.putClientProperty("bt_" + i, r.get("MA_BIENTHE"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // =====================================================================
    //  CARD 3: DANH SÁCH SERIAL
    // =====================================================================

    private JLabel lblSerialTitle;
    private JLabel lblStats;

    private JPanel buildSerialCard() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG);

        // Header
        lblSerialTitle = new JLabel();
        JPanel header = createHeader("", "<- Quay lại", e -> {
            cardLayout.show(cardContainer, "products");
        });
        txtSearchSerial = addSearchToHeader(header, "Tìm serial...", e -> loadSerials(getSearchText(txtSearchSerial)));

        for (Component c : ((JPanel) header.getComponent(0)).getComponents()) {
            if (c instanceof JLabel && !(c instanceof JButton)) {
                lblSerialTitle = (JLabel) c;
                break;
            }
        }
        panel.add(header, BorderLayout.NORTH);

        // Stats bar
        lblStats = new JLabel(" ");
        lblStats.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStats.setBorder(new EmptyBorder(0, 25, 8, 25));
        lblStats.setOpaque(true);
        lblStats.setBackground(BG);
        panel.add(lblStats, BorderLayout.SOUTH);

        // Table
        String[] cols = {"ID", "Serial Number", "Phiếu nhập", "Trạng thái"};
        serialModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        serialTable = buildStyledTable(serialModel);
        serialTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        serialTable.getColumnModel().getColumn(0).setMaxWidth(80);
        serialTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        serialTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        serialTable.getColumnModel().getColumn(3).setPreferredWidth(150);

        DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
        centerR.setHorizontalAlignment(SwingConstants.CENTER);
        serialTable.getColumnModel().getColumn(0).setCellRenderer(centerR);
        serialTable.getColumnModel().getColumn(2).setCellRenderer(centerR);

        // Serial status badge
        serialTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setOpaque(true);
                String status = v != null ? v.toString() : "";
                if (!s) {
                    switch (status) {
                        case "Khả dụng":
                            lbl.setText("● Khả dụng");
                            lbl.setForeground(new Color(5, 122, 85));
                            lbl.setBackground(new Color(220, 252, 231));
                            break;
                        case "Đang đặt":
                            lbl.setText("● Đang đặt");
                            lbl.setForeground(new Color(180, 130, 0));
                            lbl.setBackground(new Color(255, 249, 219));
                            break;
                        case "Đã bán":
                            lbl.setText("● Đã bán");
                            lbl.setForeground(new Color(185, 28, 28));
                            lbl.setBackground(new Color(254, 226, 226));
                            break;
                        default:
                            lbl.setText("● " + status);
                            lbl.setForeground(new Color(100, 116, 139));
                            lbl.setBackground(new Color(241, 245, 249));
                    }
                }
                return lbl;
            }
        });

        JScrollPane sp = new JScrollPane(serialTable);
        sp.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        sp.getViewport().setBackground(Color.WHITE);
        panel.add(sp, BorderLayout.CENTER);

        return panel;
    }

    private void showSerials() {
        String title = currentTenSP;
        if (currentTenBienthe != null && !currentTenBienthe.isEmpty()) {
            title += " (" + currentTenBienthe + ")";
        }
        title += " — " + currentTenCN;
        lblSerialTitle.setText("Serial — " + title);
        loadSerials(null);
        loadStats();
        if (txtSearchSerial != null) txtSearchSerial.setText("");
        cardLayout.show(cardContainer, "serials");
    }

    private void loadSerials(String keyword) {
        serialModel.setRowCount(0);
        try {
            List<Map<String, Object>> list = dao.getSerialsByVariantAndBranch(currentMaBienthe, currentMaCN, keyword);
            for (Map<String, Object> r : list) {
                serialModel.addRow(new Object[]{
                    r.get("MA_SN"),
                    r.get("SERIAL_NUMBER"),
                    r.get("MA_PN") != null ? "PN#" + r.get("MA_PN") : "",
                    r.get("TRANG_THAI")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadStats() {
        try {
            Map<String, Integer> counts = dao.countSerialsByStatus(currentMaBienthe, currentMaCN);
            int total = counts.values().stream().mapToInt(Integer::intValue).sum();
            int khaDung = counts.getOrDefault("Khả dụng", 0);
            int dangDat = counts.getOrDefault("Đang đặt", 0);
            int daBan = counts.getOrDefault("Đã bán", 0);
            int khac = total - khaDung - dangDat - daBan;

            StringBuilder sb = new StringBuilder();
            sb.append("Tổng: ").append(total).append("  |  ");
            sb.append("Khả dụng: ").append(khaDung).append("  |  ");
            sb.append("Đang đặt: ").append(dangDat).append("  |  ");
            sb.append("Đã bán: ").append(daBan);
            if (khac > 0) sb.append("  |  Khác: ").append(khac);
            lblStats.setText(sb.toString());
        } catch (Exception e) {
            lblStats.setText(" ");
        }
    }

    // =====================================================================
    //  UI FACTORY
    // =====================================================================

    /**
     * Header: [BackBtn] [Title]       [SearchField] [SearchBtn]
     */
    private JPanel createHeader(String title, String backText, ActionListener backAction) {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, PURPLE_LIGHT),
            new EmptyBorder(14, 20, 14, 20)
        ));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        if (backText != null && backAction != null) {
            JButton btnBack = new JButton(backText);
            UIUtils.styleButton(btnBack);
            btnBack.addActionListener(backAction);
            leftPanel.add(btnBack);
        }

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(30, 41, 59));
        leftPanel.add(lblTitle);

        header.add(leftPanel, BorderLayout.WEST);
        return header;
    }

    private JTextField addSearchToHeader(JPanel header, String placeholder, ActionListener searchAction) {
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setOpaque(false);
        searchPanel.setPreferredSize(new Dimension(300, 35));

        JTextField txt = new JTextField(placeholder);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txt.setForeground(Color.GRAY);
        txt.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (txt.getForeground() == Color.GRAY) { txt.setText(""); txt.setForeground(new Color(30, 41, 59)); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (txt.getText().isEmpty()) { txt.setText(placeholder); txt.setForeground(Color.GRAY); }
            }
        });
        txt.addActionListener(searchAction);

        JButton btnSearch = new JButton("Tìm");
        UIUtils.styleButton(btnSearch);
        btnSearch.addActionListener(searchAction);

        searchPanel.add(txt, BorderLayout.CENTER);
        searchPanel.add(btnSearch, BorderLayout.EAST);
        header.add(searchPanel, BorderLayout.EAST);

        return txt;
    }

    private String getSearchText(JTextField txt) {
        if (txt == null || txt.getForeground() == Color.GRAY) return null;
        String t = txt.getText().trim();
        return t.isEmpty() ? null : t;
    }

    private JTable buildStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(42);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(245, 235, 250));
        table.setSelectionForeground(PURPLE);
        table.setIntercellSpacing(new Dimension(0, 1));

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(248, 248, 252));
        table.getTableHeader().setForeground(new Color(100, 100, 130));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));

        table.setRowSorter(new TableRowSorter<>(model));

        // Hover effect
        table.addMouseMotionListener(new MouseMotionAdapter() {
            int lastRow = -1;
            @Override public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != lastRow) {
                    lastRow = row;
                    table.setCursor(row >= 0 ? new Cursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
                }
            }
        });

        return table;
    }

    /** Badge renderer for branch status */
    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setOpaque(true);
            String status = v != null ? v.toString() : "";
            if (!s) {
                if (status.contains("Hoạt động") && !status.contains("Ngừng")) {
                    lbl.setForeground(new Color(5, 122, 85));
                    lbl.setBackground(new Color(220, 252, 231));
                } else {
                    lbl.setForeground(new Color(185, 28, 28));
                    lbl.setBackground(new Color(254, 226, 226));
                }
            }
            return lbl;
        }
    }

    private void showEditBranchDialog() {
        int selectedRow = branchTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một chi nhánh để sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = branchTable.convertRowIndexToModel(selectedRow);
        int maCN = (int) branchModel.getValueAt(modelRow, 0);
        String tenCN = (String) branchModel.getValueAt(modelRow, 1);
        String diaChi = (String) branchModel.getValueAt(modelRow, 2);
        String sdtHotline = (String) branchModel.getValueAt(modelRow, 3);
        String trangThai = (String) branchModel.getValueAt(modelRow, 4);

        // Create Dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sửa thông tin chi nhánh", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(450, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Label style
        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 13);

        // MA CN
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblMa = new JLabel("Mã chi nhánh:");
        lblMa.setFont(labelFont);
        mainPanel.add(lblMa, gbc);

        gbc.gridx = 1;
        JLabel lblMaVal = new JLabel(String.valueOf(maCN));
        lblMaVal.setFont(labelFont);
        mainPanel.add(lblMaVal, gbc);

        // TEN CN
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblTen = new JLabel("Tên chi nhánh:");
        lblTen.setFont(labelFont);
        mainPanel.add(lblTen, gbc);

        gbc.gridx = 1;
        JTextField txtTen = new JTextField(tenCN);
        txtTen.setFont(inputFont);
        txtTen.setPreferredSize(new Dimension(200, 30));
        mainPanel.add(txtTen, gbc);

        // DIA CHI
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblDiaChi = new JLabel("Địa chỉ:");
        lblDiaChi.setFont(labelFont);
        mainPanel.add(lblDiaChi, gbc);

        gbc.gridx = 1;
        JTextField txtDiaChi = new JTextField(diaChi);
        txtDiaChi.setFont(inputFont);
        txtDiaChi.setPreferredSize(new Dimension(200, 30));
        mainPanel.add(txtDiaChi, gbc);

        // SDT HOTLINE
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel lblSdt = new JLabel("Hotline:");
        lblSdt.setFont(labelFont);
        mainPanel.add(lblSdt, gbc);

        gbc.gridx = 1;
        JTextField txtSdt = new JTextField(sdtHotline);
        txtSdt.setFont(inputFont);
        txtSdt.setPreferredSize(new Dimension(200, 30));
        mainPanel.add(txtSdt, gbc);

        // TRANG THAI
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel lblTrangThai = new JLabel("Trạng thái:");
        lblTrangThai.setFont(labelFont);
        mainPanel.add(lblTrangThai, gbc);

        gbc.gridx = 1;
        String[] statuses = {"Đang hoạt động", "Ngừng hoạt động"};
        JComboBox<String> cbTrangThai = new JComboBox<>(statuses);
        cbTrangThai.setFont(inputFont);
        cbTrangThai.setPreferredSize(new Dimension(200, 30));
        if (trangThai != null && trangThai.contains("Ngừng")) {
            cbTrangThai.setSelectedItem("Ngừng hoạt động");
        } else {
            cbTrangThai.setSelectedItem("Đang hoạt động");
        }
        mainPanel.add(cbTrangThai, gbc);

        dialog.add(mainPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setBackground(new Color(248, 250, 252));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton btnSave = new JButton("Lưu");
        UIUtils.styleButton(btnSave);
        btnSave.setPreferredSize(new Dimension(100, 35));
        btnSave.addActionListener(e -> {
            String newTen = txtTen.getText().trim();
            String newDiaChi = txtDiaChi.getText().trim();
            String newSdt = txtSdt.getText().trim();
            String newTrangThai = (String) cbTrangThai.getSelectedItem();

            if (newTen.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Tên chi nhánh không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                boolean success = dao.updateBranch(maCN, newTen, newDiaChi, newSdt, newTrangThai);
                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Cập nhật chi nhánh thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadBranches(getSearchText(txtSearchBranch));
                } else {
                    JOptionPane.showMessageDialog(dialog, "Cập nhật chi nhánh thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Lỗi kết nối database: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnCancel = new JButton("Hủy");
        UIUtils.styleButton(btnCancel);
        btnCancel.setPreferredSize(new Dimension(100, 35));
        btnCancel.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
