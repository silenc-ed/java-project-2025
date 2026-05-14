package View.Customers.ProductPanel;

import Controller.LoaiSanPhamDAO;
import Controller.SanPhamDAO;
import Model.LoaiSanPham;
import Model.SanPham;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryPopupDialog extends JDialog {
    private ProductFilterListener listener;
    private static final Color BG_PRIMARY = new Color(255, 255, 255);
    private static final Color ACCENT_PRIMARY = new Color(79, 70, 229);
    private static final Color TEXT_PRIMARY = new Color(30, 30, 46);
    private static final Color BORDER_LIGHT = new Color(229, 231, 240);

    private List<JCheckBox> catCheckboxes = new ArrayList<>();
    private List<Integer> catIds = new ArrayList<>();
    private List<String> catNamesFallback = new ArrayList<>();

    public CategoryPopupDialog(Frame parent, Component invoker, ProductFilterListener listener) {
        super(parent, false);
        this.listener = listener;
        setUndecorated(true);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_PRIMARY);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        mainPanel.setPreferredSize(new Dimension(1000, 400));

        // Left Panel: Categories (Checkboxes)
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(BG_PRIMARY);
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_LIGHT));
        leftPanel.setPreferredSize(new Dimension(220, 400));
        
        // Push the first checkbox down to align with the title on the right
        leftPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        List<LoaiSanPham> categories = LoaiSanPhamDAO.getAllLoaiSanPham();
        if (categories == null || categories.isEmpty()) {
            // Fallback hardcoded categories
            String[] defaultCats = {"Điện thoại", "Laptop", "Màn hình", "Tai nghe"};
            for (String catName : defaultCats) {
                JCheckBox chk = createCategoryCheckbox(catName);
                leftPanel.add(chk);
                leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                catCheckboxes.add(chk);
                catNamesFallback.add(catName);
                catIds.add(-1); // No ID
            }
        } else {
            for (LoaiSanPham lsp : categories) {
                JCheckBox chk = createCategoryCheckbox(lsp.getTenLsp());
                leftPanel.add(chk);
                leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                catCheckboxes.add(chk);
                catNamesFallback.add(lsp.getTenLsp());
                catIds.add(lsp.getMaLsp());
            }
        }

        // Right Panel: Budget Input
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(null);
        rightPanel.setBackground(BG_PRIMARY);

        JLabel lblBudgetTitle = new JLabel("Mức giá (Budget)");
        lblBudgetTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblBudgetTitle.setForeground(TEXT_PRIMARY);
        lblBudgetTitle.setBounds(40, 20, 200, 30);
        rightPanel.add(lblBudgetTitle);

        JLabel lblSub = new JLabel("Nhập khoảng giá (Từ - Đến) để tìm sản phẩm phù hợp:");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(new Color(100, 100, 100));
        lblSub.setBounds(40, 60, 400, 25);
        rightPanel.add(lblSub);

        JTextField txtMinPrice = new JTextField();
        txtMinPrice.putClientProperty("JTextField.placeholderText", "Giá thấp nhất...");
        txtMinPrice.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMinPrice.setBounds(40, 95, 180, 40);
        rightPanel.add(txtMinPrice);

        JLabel lblDash = new JLabel("-");
        lblDash.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblDash.setBounds(230, 95, 20, 40);
        rightPanel.add(lblDash);

        JTextField txtMaxPrice = new JTextField();
        txtMaxPrice.putClientProperty("JTextField.placeholderText", "Giá cao nhất...");
        txtMaxPrice.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMaxPrice.setBounds(250, 95, 180, 40);
        rightPanel.add(txtMaxPrice);

        // Nút Close "X" làm rõ ràng hơn
        JLabel lblClose = new JLabel("X", SwingConstants.CENTER);
        lblClose.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblClose.setForeground(new Color(100, 100, 100));
        lblClose.setOpaque(true);
        lblClose.setBackground(new Color(240, 240, 240));
        lblClose.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        lblClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblClose.setBounds(700, 10, 30, 30);
        lblClose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                lblClose.setBackground(new Color(255, 100, 100));
                lblClose.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                lblClose.setBackground(new Color(240, 240, 240));
                lblClose.setForeground(new Color(100, 100, 100));
            }
        });
        rightPanel.add(lblClose);

        JButton btnSearchBudget = new JButton("TÌM SẢN PHẨM");
        btnSearchBudget.putClientProperty("JButton.buttonType", "roundRect");
        btnSearchBudget.setBackground(ACCENT_PRIMARY);
        btnSearchBudget.setForeground(Color.WHITE);
        btnSearchBudget.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSearchBudget.setFocusPainted(false);
        btnSearchBudget.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearchBudget.setBounds(580, 280, 150, 40);

        btnSearchBudget.addActionListener(e -> {
            try {
                Double minPrice = null;
                Double maxPrice = null;
                
                if (!txtMinPrice.getText().trim().isEmpty()) {
                    minPrice = Double.parseDouble(txtMinPrice.getText().replace(",", ""));
                }
                if (!txtMaxPrice.getText().trim().isEmpty()) {
                    maxPrice = Double.parseDouble(txtMaxPrice.getText().replace(",", ""));
                }
                
                if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
                    JOptionPane.showMessageDialog(this, "Giá trị 'Từ' không được lớn hơn 'Đến'!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                List<Integer> selectedIds = new ArrayList<>();
                List<String> selectedNames = new ArrayList<>();
                for (int i = 0; i < catCheckboxes.size(); i++) {
                    if (catCheckboxes.get(i).isSelected()) {
                        if (catIds.get(i) != -1) {
                            selectedIds.add(catIds.get(i));
                        } else {
                            selectedNames.add(catNamesFallback.get(i));
                        }
                    }
                }

                List<SanPham> results = SanPhamDAO.searchAdvanced(selectedIds, selectedNames, minPrice, maxPrice);
                
                String title = "KẾT QUẢ LỌC SẢN PHẨM";
                listener.onProductsFiltered(results, title);
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số tiền hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        rightPanel.add(btnSearchBudget);

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        add(mainPanel);
        pack();

        Point p = invoker.getLocationOnScreen();
        setLocation(p.x, p.y + invoker.getHeight() + 10);

        addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {}

            @Override
            public void windowLostFocus(WindowEvent e) {
                dispose();
            }
        });
    }

    private JCheckBox createCategoryCheckbox(String text) {
        JCheckBox chk = new JCheckBox(text);
        chk.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        chk.setForeground(TEXT_PRIMARY);
        chk.setBackground(BG_PRIMARY);
        chk.setFocusPainted(false);
        chk.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chk.setMaximumSize(new Dimension(200, 30));
        return chk;
    }
}
