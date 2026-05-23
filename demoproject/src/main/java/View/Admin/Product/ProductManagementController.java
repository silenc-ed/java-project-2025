package View.Admin.Product;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ProductManagementController extends JPanel {

    private JPanel breadcrumbPanel;
    private JPanel cardsPanel;
    private CardLayout cardLayout;

    private CategoryViewPanel categoryPanel;
    private ProductViewPanel productPanel;
    private VariantViewPanel variantPanel;
    private SerialNumberViewPanel serialNumberPanel;

    private List<BreadcrumbItem> breadcrumbs;

    private static class BreadcrumbItem {
        String label;
        String cardName;
        Object id; // ID of the item if needed

        public BreadcrumbItem(String label, String cardName, Object id) {
            this.label = label;
            this.cardName = cardName;
            this.id = id;
        }
    }

    public ProductManagementController() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Breadcrumb Top Panel
        breadcrumbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        breadcrumbPanel.setOpaque(false);
        add(breadcrumbPanel, BorderLayout.NORTH);

        // Cards Panel
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setOpaque(false);
        
        // Initialize sub-panels
        categoryPanel = new CategoryViewPanel(this);
        productPanel = new ProductViewPanel(this);
        variantPanel = new VariantViewPanel(this);
        serialNumberPanel = new SerialNumberViewPanel(this);

        cardsPanel.add(categoryPanel, "CATEGORY");
        cardsPanel.add(productPanel, "PRODUCT");
        cardsPanel.add(variantPanel, "VARIANT");
        cardsPanel.add(serialNumberPanel, "SERIAL");

        add(cardsPanel, BorderLayout.CENTER);

        breadcrumbs = new ArrayList<>();
        showCategory();
    }

    private void renderBreadcrumbs() {
        breadcrumbPanel.removeAll();
        
        for (int i = 0; i < breadcrumbs.size(); i++) {
            BreadcrumbItem item = breadcrumbs.get(i);
            JLabel lbl = new JLabel(item.label);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
            
            if (i < breadcrumbs.size() - 1) {
                lbl.setForeground(new Color(59, 130, 246)); // Blue for clickable links
                lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
                
                int index = i;
                lbl.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        navigateTo(index);
                    }
                });
                
                breadcrumbPanel.add(lbl);
                
                JLabel separator = new JLabel(" > ");
                separator.setFont(new Font("Segoe UI", Font.BOLD, 16));
                separator.setForeground(new Color(148, 163, 184));
                breadcrumbPanel.add(separator);
            } else {
                lbl.setForeground(new Color(15, 23, 42)); // Dark for current page
                breadcrumbPanel.add(lbl);
            }
        }
        
        breadcrumbPanel.revalidate();
        breadcrumbPanel.repaint();
    }

    private void navigateTo(int index) {
        // Truncate the breadcrumbs list after the index
        List<BreadcrumbItem> newBreadcrumbs = new ArrayList<>(breadcrumbs.subList(0, index + 1));
        breadcrumbs = newBreadcrumbs;
        
        BreadcrumbItem target = breadcrumbs.get(index);
        
        if (target.cardName.equals("CATEGORY")) {
            categoryPanel.refreshData();
        } else if (target.cardName.equals("PRODUCT")) {
            productPanel.loadProductsForCategory((Integer) target.id);
        } else if (target.cardName.equals("VARIANT")) {
            variantPanel.loadVariantsForProduct((Integer) target.id);
        }
        
        cardLayout.show(cardsPanel, target.cardName);
        renderBreadcrumbs();
    }

    public void showCategory() {
        breadcrumbs.clear();
        breadcrumbs.add(new BreadcrumbItem("Loại sản phẩm", "CATEGORY", null));
        categoryPanel.refreshData();
        cardLayout.show(cardsPanel, "CATEGORY");
        renderBreadcrumbs();
    }

    public void showProduct(int categoryId, String categoryName) {
        breadcrumbs.add(new BreadcrumbItem(categoryName, "PRODUCT", categoryId));
        productPanel.loadProductsForCategory(categoryId);
        cardLayout.show(cardsPanel, "PRODUCT");
        renderBreadcrumbs();
    }

    public void showVariant(int productId, String productName) {
        breadcrumbs.add(new BreadcrumbItem(productName, "VARIANT", productId));
        variantPanel.loadVariantsForProduct(productId);
        cardLayout.show(cardsPanel, "VARIANT");
        renderBreadcrumbs();
    }

    public void showSerialNumber(int variantId, String variantName) {
        breadcrumbs.add(new BreadcrumbItem(variantName, "SERIAL", variantId));
        serialNumberPanel.loadSerialsForVariant(variantId);
        cardLayout.show(cardsPanel, "SERIAL");
        renderBreadcrumbs();
    }
}
