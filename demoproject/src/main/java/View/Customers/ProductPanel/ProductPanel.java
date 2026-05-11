/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package View.Customers.ProductPanel;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author DELL
 */
public class ProductPanel extends javax.swing.JPanel {

    /**
     * Creates new form TongQuanPanel
     */
    private View.Customers.ProductPanel.BannerSlideshow bannerSlideshow;

    // Modern color palette
    private static final Color BG_PRIMARY = new Color(248, 249, 252);
    private static final Color BG_WHITE = Color.WHITE;
    private static final Color ACCENT_PRIMARY = new Color(79, 70, 229);    // Indigo
    private static final Color ACCENT_SECONDARY = new Color(99, 102, 241); // Light Indigo
    private static final Color TEXT_PRIMARY = new Color(30, 30, 46);
    private static final Color TEXT_SECONDARY = new Color(108, 117, 139);
    private static final Color BORDER_LIGHT = new Color(229, 231, 240);
    private static final Color SEARCH_BG = new Color(241, 243, 249);
    private static final Color HOVER_BG = new Color(238, 242, 255);

    public ProductPanel() {
        initComponents();
        applyModernStyling();
        setupBanner();
        setupProductGrid();
    }

    private void applyModernStyling() {
        // === Main panel background ===
        setBackground(BG_PRIMARY);

        // === Heading Panel ===
        // Use same background as main panel to avoid color mismatch strip.
        // Do NOT set preferredSize — GroupLayout from .form controls sizing;
        // overriding it breaks internal child positioning.
        HeadingPanel.setBackground(BG_PRIMARY);
        HeadingPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LIGHT));

        // Tab name - bold modern typography (keep size 20 to fit 27px GroupLayout constraint)
        tabName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        tabName.setForeground(TEXT_PRIMARY);

        // Category - interactive style
        Category.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        Category.setForeground(TEXT_SECONDARY);
        Category.setCursor(new Cursor(Cursor.HAND_CURSOR));
        Category.setOpaque(true);
        Category.setBackground(BG_PRIMARY);
        Category.setBorder(new EmptyBorder(5, 10, 5, 10));
        Category.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                Category.setForeground(ACCENT_PRIMARY);
                Category.setBackground(HOVER_BG);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                Category.setForeground(TEXT_SECONDARY);
                Category.setBackground(BG_PRIMARY);
            }
        });

        // Delivery - interactive style with icon
        Delivery.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        Delivery.setForeground(TEXT_SECONDARY);
        Delivery.setCursor(new Cursor(Cursor.HAND_CURSOR));
        Delivery.setOpaque(true);
        Delivery.setBackground(BG_PRIMARY);
        Delivery.setBorder(new EmptyBorder(5, 10, 5, 10));
        Delivery.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                Delivery.setForeground(ACCENT_PRIMARY);
                Delivery.setBackground(HOVER_BG);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                Delivery.setForeground(TEXT_SECONDARY);
                Delivery.setBackground(BG_PRIMARY);
            }
        });

        // === Dashboard Panel ===
        DashboardPanel.setBackground(BG_PRIMARY);

        // === Search Panel - Modern glass-style ===
        // Only bottom border, no internal padding (GroupLayout controls gaps)
        SearchPanel.setBackground(BG_PRIMARY);
        SearchPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LIGHT));

        // Search text field - FlatLaf client properties for modern look
        searchField.putClientProperty("JTextField.placeholderText", "Tìm kiếm sản phẩm...");
        searchField.putClientProperty("JTextField.showClearButton", true);
        searchField.putClientProperty("JComponent.roundRect", true);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBackground(SEARCH_BG);
        searchField.setForeground(TEXT_PRIMARY);
        searchField.setCaretColor(ACCENT_PRIMARY);

        // Search button - modern style
        SearchButton.putClientProperty("JButton.buttonType", "roundRect");
        SearchButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        SearchButton.setBackground(ACCENT_PRIMARY);
        SearchButton.setForeground(Color.WHITE);
        SearchButton.setFocusPainted(false);
        SearchButton.setBorderPainted(false);
        SearchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // === Product Panel ===
        ProductPanel.setBackground(BG_PRIMARY);
        BannerPanel.setBackground(BG_PRIMARY);
    }

    private void setupBanner() {
        // Replace BannerPanel content with BannerSlideshow
        BannerPanel.setLayout(new java.awt.BorderLayout());
        BannerPanel.removeAll();

        bannerSlideshow = new View.Customers.ProductPanel.BannerSlideshow();
        bannerSlideshow.loadBannerImages(new String[]{
            "/Default/Theme/wallpaper/wallpaperflare.com_wallpaper(1).jpg",
            "/Default/Theme/wallpaper/wallpaperflare.com_wallpaper(2).jpg",
            "/Default/Theme/wallpaper/wallpaperflare.com_wallpaper(3).jpg",
            "/Default/Theme/wallpaper/wallpaperflare.com_wallpaper(4).jpg",
            "/Default/Theme/wallpaper/wallpaperflare.com_wallpaper(5).jpg"
        });
        bannerSlideshow.startAutoSlide(5000);

        BannerPanel.setPreferredSize(new java.awt.Dimension(0, 200));
        BannerPanel.add(bannerSlideshow, java.awt.BorderLayout.CENTER);
        BannerPanel.revalidate();
        BannerPanel.repaint();
    }

    private void setupProductGrid() {
        // Lấy dữ liệu từ database thông qua DAO
        java.util.List<Model.SanPham> products = Controller.SanPhamDAO.getAllSanPham();

        // Create grid panel with WrapLayout
        javax.swing.JPanel gridPanel = new javax.swing.JPanel(
            new View.Customers.ProductPanel.WrapLayout(java.awt.FlowLayout.LEFT, 15, 15)
        );
        gridPanel.setBackground(BG_PRIMARY);

        for (Model.SanPham p : products) {
            View.Customers.ProductPanel.ProductCard card = new View.Customers.ProductPanel.ProductCard();
            card.setData(p);
            gridPanel.add(card);
        }

        // Wrap in scroll pane
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(BG_PRIMARY);
        scrollPane.getViewport().setBackground(BG_PRIMARY);

        // Recalculate wrap on resize
        scrollPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    gridPanel.revalidate();
                    gridPanel.repaint();
                });
            }
        });

        // Add section title
        javax.swing.JLabel sectionTitle = new javax.swing.JLabel("  GỢI Ý HÔM NAY") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Draw gradient bottom border
                int w = getWidth();
                int h = getHeight();
                java.awt.GradientPaint gp = new java.awt.GradientPaint(
                    0, h - 3, ACCENT_PRIMARY, w, h - 3, ACCENT_SECONDARY);
                g2.setPaint(gp);
                g2.fillRect(0, h - 3, w, 3);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sectionTitle.setForeground(ACCENT_PRIMARY);
        sectionTitle.setPreferredSize(new java.awt.Dimension(0, 44));
        sectionTitle.setBorder(new EmptyBorder(8, 4, 8, 4));
        sectionTitle.setOpaque(false);

        // Replace ProductPanel (inner) layout
        ProductPanel.setLayout(new java.awt.BorderLayout(0, 10));
        ProductPanel.removeAll();
        ProductPanel.add(BannerPanel, java.awt.BorderLayout.NORTH);

        javax.swing.JPanel bottomSection = new javax.swing.JPanel(new java.awt.BorderLayout());
        bottomSection.setBackground(BG_PRIMARY);
        bottomSection.add(sectionTitle, java.awt.BorderLayout.NORTH);
        bottomSection.add(scrollPane, java.awt.BorderLayout.CENTER);

        ProductPanel.add(bottomSection, java.awt.BorderLayout.CENTER);
        ProductPanel.revalidate();
        ProductPanel.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        HeadingPanel = new javax.swing.JPanel();
        Category = new javax.swing.JLabel();
        Delivery = new javax.swing.JLabel();
        tabName = new javax.swing.JLabel();
        DashboardPanel = new javax.swing.JPanel();
        SearchPanel = new javax.swing.JPanel();
        searchField = new javax.swing.JTextField();
        SearchButton = new javax.swing.JButton();
        ProductPanel = new javax.swing.JPanel();
        BannerPanel = new javax.swing.JPanel();

        setBackground(new java.awt.Color(248, 249, 252));

        HeadingPanel.setBackground(new java.awt.Color(248, 249, 252));

        Category.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        Category.setText("Danh mục");

        Delivery.setBackground(new java.awt.Color(255, 255, 255));
        Delivery.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        Delivery.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Default/Icon/stock.png"))); // NOI18N
        Delivery.setText("Giỏ hàng");

        tabName.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tabName.setText("Sản phẩm");

        javax.swing.GroupLayout HeadingPanelLayout = new javax.swing.GroupLayout(HeadingPanel);
        HeadingPanel.setLayout(HeadingPanelLayout);
        HeadingPanelLayout.setHorizontalGroup(
            HeadingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(HeadingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabName, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(Category)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 377, Short.MAX_VALUE)
                .addComponent(Delivery)
                .addGap(15, 15, 15))
        );
        HeadingPanelLayout.setVerticalGroup(
            HeadingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(HeadingPanelLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(HeadingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(HeadingPanelLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(Delivery, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE))
                    .addGroup(HeadingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(Category, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(tabName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        DashboardPanel.setBackground(new java.awt.Color(248, 249, 252));

        searchField.addActionListener(this::searchFieldActionPerformed);

        SearchButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Default/Icon/search.png"))); // NOI18N
        SearchButton.setText("Tìm kiếm");
        SearchButton.addActionListener(this::SearchButtonActionPerformed);

        javax.swing.GroupLayout SearchPanelLayout = new javax.swing.GroupLayout(SearchPanel);
        SearchPanel.setLayout(SearchPanelLayout);
        SearchPanelLayout.setHorizontalGroup(
            SearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SearchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchField, javax.swing.GroupLayout.DEFAULT_SIZE, 552, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(SearchButton)
                .addGap(17, 17, 17))
        );
        SearchPanelLayout.setVerticalGroup(
            SearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SearchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(SearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SearchButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout BannerPanelLayout = new javax.swing.GroupLayout(BannerPanel);
        BannerPanel.setLayout(BannerPanelLayout);
        BannerPanelLayout.setHorizontalGroup(
            BannerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        BannerPanelLayout.setVerticalGroup(
            BannerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 200, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout ProductPanelLayout = new javax.swing.GroupLayout(ProductPanel);
        ProductPanel.setLayout(ProductPanelLayout);
        ProductPanelLayout.setHorizontalGroup(
            ProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ProductPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(BannerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        ProductPanelLayout.setVerticalGroup(
            ProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ProductPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(BannerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(230, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout DashboardPanelLayout = new javax.swing.GroupLayout(DashboardPanel);
        DashboardPanel.setLayout(DashboardPanelLayout);
        DashboardPanelLayout.setHorizontalGroup(
            DashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(SearchPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(ProductPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        DashboardPanelLayout.setVerticalGroup(
            DashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DashboardPanelLayout.createSequentialGroup()
                .addComponent(SearchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ProductPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(DashboardPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(HeadingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(HeadingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(DashboardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void searchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_searchFieldActionPerformed

    private void SearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SearchButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SearchButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel BannerPanel;
    private javax.swing.JLabel Category;
    private javax.swing.JPanel DashboardPanel;
    private javax.swing.JLabel Delivery;
    private javax.swing.JPanel HeadingPanel;
    private javax.swing.JPanel ProductPanel;
    private javax.swing.JButton SearchButton;
    private javax.swing.JPanel SearchPanel;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel tabName;
    // End of variables declaration//GEN-END:variables
}
