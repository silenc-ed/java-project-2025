package View.Admin.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class InventoryBranchViewPanel extends JPanel {

    private ProductManagementController controller;
    private int currentVariantId = -1;
    private DefaultTableModel tableModel;
    private JTable dataTable;
    private JTextField txtSearch;

    public InventoryBranchViewPanel(ProductManagementController controller) {
        this.controller = controller;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("Tồn kho theo Chi nhánh");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(15, 23, 42));
        leftHeader.add(lblTitle);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Panel
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);

        // Search Bar
        txtSearch = new JTextField("Tìm kiếm Chi nhánh...");
        txtSearch.setPreferredSize(new Dimension(300, 35));
        txtSearch.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = txtSearch.getText().trim();
                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
                dataTable.setRowSorter(sorter);
                if (text.isEmpty() || text.equals("Tìm kiếm Chi nhánh...")) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1));
                }
            }
        });
        centerPanel.add(txtSearch, BorderLayout.NORTH);

        // Table
        String[] columns = {"Mã CN", "Tên Chi nhánh", "Số lượng tồn", "Thao tác"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return c == 3;
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

        ProductSharedUtils.ActionCellEditor actionEditor = new ProductSharedUtils.ActionCellEditor(
            dataTable, 
            this::handleEditStock, 
            null,
            false // No "Chi tiết" button at leaf node
        );
        dataTable.getColumnModel().getColumn(3).setCellRenderer(new ProductSharedUtils.ActionCellRenderer(false));
        dataTable.getColumnModel().getColumn(3).setCellEditor(actionEditor);

        JScrollPane scrollPane = new JScrollPane(dataTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void loadInventoryForVariant(int variantId) {
        this.currentVariantId = variantId;
        tableModel.setRowCount(0);
        try {
            List<Map<String, Object>> list = Controller.TonKhoDAO.getTonKhoByBienThe(variantId);
            for (Map<String, Object> row : list) {
                tableModel.addRow(new Object[]{
                    row.get("MA_CN"),
                    row.get("TEN_CN"),
                    row.get("SO_LUONG_TON"),
                    row.get("MA_CN")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleEditStock() {
        try {
            int row = dataTable.getSelectedRow();
            if (row == -1) return;
            int modelRow = dataTable.convertRowIndexToModel(row);
            
            int maCn = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());
            String tenCn = tableModel.getValueAt(modelRow, 1).toString();
            int currentStock = Integer.parseInt(tableModel.getValueAt(modelRow, 2).toString());
    
            String input = JOptionPane.showInputDialog(this, 
                "Nhập số lượng tồn kho mới cho chi nhánh:\n" + tenCn, 
                currentStock);
            
            if (input != null && !input.trim().isEmpty()) {
                int newStock = Integer.parseInt(input.trim());
                if (newStock < 0) {
                    JOptionPane.showMessageDialog(this, "Số lượng không được âm!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                boolean success = Controller.TonKhoDAO.updateTonKho(currentVariantId, maCn, newStock);
                if (success) {
                    loadInventoryForVariant(currentVariantId);
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra: " + e.getMessage());
        }
    }
}
