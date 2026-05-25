package View.Admin.Warranty;

import Controller.Admin.BaoHanh.BaoHanhAdminDAO;
import View.Admin.UIUtils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class WarrantyPanel extends javax.swing.JPanel {

    private DefaultTableModel tableModel;
    private JTable dataTable;
    private JTextField txtSearch;
    private JComboBox<String> cbStatusFilter;

    // UI Constants
    private final Color COLOR_BACKGROUND = Color.WHITE;
    private final Color COLOR_TEXT_DARK = new Color(50, 50, 50);
    private final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 14);

    public WarrantyPanel() {
        initComponents();
        setupCustomUI();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BACKGROUND);

        // -- Top Header Panel --
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_BACKGROUND);
        headerPanel.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel lblTitle = new JLabel("Quản lý Bảo hành");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_TEXT_DARK);
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(COLOR_BACKGROUND);

        JButton btnRefresh = new JButton("Làm mới & Quét Hết hạn");
        UIUtils.styleButton(btnRefresh);
        btnRefresh.setBackground(new Color(23, 162, 184)); // Cyan
        btnRefresh.addActionListener(e -> {
            BaoHanhAdminDAO.expireAllOverdue();
            loadData();
            JOptionPane.showMessageDialog(this, "Đã quét trạng thái và làm mới dữ liệu thành công!");
        });

        actionPanel.add(btnRefresh);
        headerPanel.add(actionPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // -- Main Content Panel --
        JPanel contentPanel = new JPanel(new BorderLayout(0, 15));
        contentPanel.setBackground(COLOR_BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        filterPanel.setBackground(COLOR_BACKGROUND);

        txtSearch = new JTextField("Tìm kiếm theo Mã Serial, SĐT, Tên KH...");
        txtSearch.setPreferredSize(new Dimension(300, 35));
        txtSearch.setFont(FONT_NORMAL);
        txtSearch.setForeground(Color.GRAY);
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals("Tìm kiếm theo Mã Serial, SĐT, Tên KH...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(COLOR_TEXT_DARK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText("Tìm kiếm theo Mã Serial, SĐT, Tên KH...");
                    txtSearch.setForeground(Color.GRAY);
                }
            }
        });
        
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                applyFilters();
            }
        });

        cbStatusFilter = new JComboBox<>(new String[]{"Tất cả trạng thái", "Hiệu lực", "Hết hiệu lực", "Vô hiệu lực"});
        cbStatusFilter.setPreferredSize(new Dimension(150, 35));
        cbStatusFilter.setFont(FONT_NORMAL);
        cbStatusFilter.addActionListener(e -> applyFilters());

        filterPanel.add(new JLabel("Tìm kiếm: "));
        filterPanel.add(txtSearch);
        filterPanel.add(new JLabel("Trạng thái: "));
        filterPanel.add(cbStatusFilter);

        contentPanel.add(filterPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"STT", "Mã BH", "Serial Number", "Sản phẩm", "Phiên bản", "Khách hàng", "SĐT", "Ngày BH", "Hết hạn", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        dataTable = new JTable(tableModel);
        dataTable.setRowHeight(35);
        dataTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        dataTable.getTableHeader().setBackground(new Color(240, 240, 240));
        dataTable.getTableHeader().setOpaque(false);
        dataTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dataTable.setShowVerticalLines(false);
        dataTable.setIntercellSpacing(new Dimension(0, 0));

        // Custom Renderer for Status column
        dataTable.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = value != null ? value.toString() : "";
                
                label.setOpaque(true);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                
                if (status.equals("Hiệu lực")) {
                    label.setBackground(new Color(200, 240, 200));
                    label.setForeground(new Color(0, 100, 0));
                } else if (status.equals("Hết hiệu lực")) {
                    label.setBackground(new Color(255, 230, 230));
                    label.setForeground(new Color(200, 0, 0));
                } else {
                    label.setBackground(new Color(240, 240, 240));
                    label.setForeground(Color.GRAY);
                }
                
                if (isSelected) {
                    label.setBackground(table.getSelectionBackground());
                    label.setForeground(table.getSelectionForeground());
                }
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        contentPanel.add(scrollPane, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    private void setupCustomUI() {
        // Ensure initial load expires overdue stuff quietly
        BaoHanhAdminDAO.expireAllOverdue();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Map<String, Object>> warranties = BaoHanhAdminDAO.getAllWarranties();
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        int stt = 1;
        
        for (Map<String, Object> w : warranties) {
            String startDate = w.get("NGAY_BAT_DAU") != null ? sdf.format((java.util.Date) w.get("NGAY_BAT_DAU")) : "";
            String endDate = w.get("NGAY_KET_THUC") != null ? sdf.format((java.util.Date) w.get("NGAY_KET_THUC")) : "";
            
            tableModel.addRow(new Object[]{
                stt++,
                w.get("MA_BH"),
                w.get("SERIAL_NUMBER"),
                w.get("TEN_SP"),
                w.get("TEN_BIENTHE"),
                w.get("TEN_KHACH_HANG"),
                w.get("SDT"),
                startDate,
                endDate,
                w.get("TRANG_THAI")
            });
        }
    }

    private void applyFilters() {
        String searchText = txtSearch.getText().trim();
        String selectedStatus = cbStatusFilter.getSelectedItem().toString();

        RowFilter<DefaultTableModel, Object> searchFilter = null;
        if (!searchText.isEmpty() && !searchText.equals("Tìm kiếm theo Mã Serial, SĐT, Tên KH...")) {
            final String searchLower = searchText.toLowerCase();
            searchFilter = new RowFilter<DefaultTableModel, Object>() {
                @Override
                public boolean include(RowFilter.Entry<? extends DefaultTableModel, ? extends Object> entry) {
                    String serial = entry.getStringValue(2).toLowerCase();
                    String kh = entry.getStringValue(5).toLowerCase();
                    String sdt = entry.getStringValue(6).toLowerCase();
                    return serial.contains(searchLower) || kh.contains(searchLower) || sdt.contains(searchLower);
                }
            };
        }

        RowFilter<DefaultTableModel, Object> statusFilter = null;
        if (!selectedStatus.equals("Tất cả trạng thái")) {
            statusFilter = RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(selectedStatus) + "$", 9);
        }

        java.util.List<RowFilter<DefaultTableModel, Object>> filters = new java.util.ArrayList<>();
        if (searchFilter != null) filters.add(searchFilter);
        if (statusFilter != null) filters.add(statusFilter);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        dataTable.setRowSorter(sorter);
        
        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }
}
