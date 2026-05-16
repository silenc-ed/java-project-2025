/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package View.Customers.ProductPanel;

import View.Customers.StockPanel.PacketPanel;

/**
 *
 * @author DELL
 */
public class InfoProductPanel extends javax.swing.JPanel {

    private Model.SanPham currentProduct;
    private Model.BienTheSanPham currentVariant;

    /**
     * Creates new form InfoProductPanel
     */
    public InfoProductPanel() {
        initComponents();
        setupBackButton();
        setupCartActions();
    }
    
    private void setupCartActions() {
        Delivery.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        Delivery.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(InfoProductPanel.this);
                if (window instanceof View.Customers.Main) {
                    ((View.Customers.Main) window).toggleCartDrawer();
                }
            }
        });
        
        jLabel2.setOpaque(true);
        jLabel2.setBackground(new java.awt.Color(238, 242, 255));
        jLabel2.setForeground(new java.awt.Color(79, 70, 229));
        jLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 10, 20));
        jLabel2.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        jLabel2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                addToCart();
            }
        });
        
        jLabel3.setOpaque(true);
        jLabel3.setBackground(new java.awt.Color(79, 70, 229));
        jLabel3.setForeground(java.awt.Color.WHITE);
        jLabel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 10, 20));
        jLabel3.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        jLabel3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel3.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                addToCart();
                java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(InfoProductPanel.this);
                if (window instanceof View.Customers.Main) {
                    ((View.Customers.Main) window).getMenu().setSelectedIndex(1);
                    ((View.Customers.Main) window).showForm(new PacketPanel());
                }
            }
        });
    }
    
    private void addToCart() {
        if (currentProduct != null) {
            Model.CartManager.getInstance().addItem(new Model.CartItem(currentProduct, currentVariant, 1));
            javax.swing.JOptionPane.showMessageDialog(this, "Đã thêm vào giỏ hàng!");
            java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (window instanceof View.Customers.Main) {
                View.Customers.CartDrawer drawer = ((View.Customers.Main) window).cartDrawer;
                if (drawer != null && drawer.isShowingDrawer()) {
                    drawer.updateCartContent();
                }
            }
        }
    }

    private void setupBackButton() {
        tabName.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tabName.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(InfoProductPanel.this);
                if (window instanceof View.Customers.Main) {
                    ((View.Customers.Main) window).showForm(new View.Customers.ProductPanel.ProductPanel());
                }
            }
        });
    }

    public void setData(Model.SanPham sp) {
        this.currentProduct = sp;
        this.currentVariant = null; // reset variant
        productName.setText(sp.getTenSp());
        
        java.text.DecimalFormat formatter = new java.text.DecimalFormat("###,###,###");
        priceField.setText(formatter.format(sp.getGiaBan()) + "₫");
        priceField.setForeground(new java.awt.Color(220, 53, 69)); // Màu đỏ
        
        warantyInfo.setText("Bảo hành: " + sp.getThoiGianBh() + " tháng");
        
        String moTa = sp.getMoTa() != null ? sp.getMoTa() : "Không có mô tả.";
        descrpieField.setText("<html>Trạng thái: " + sp.getTrangThai() + "<br>Đơn vị tính: " + sp.getDonViTinh() + "<br>Mô tả: " + moTa + "</html>");
        
        String imagePath = sp.getHinhAnh();
        if (imagePath == null || imagePath.trim().isEmpty()) {
            imagePath = "/Default/Product/laptop.png";
        }
        
        java.net.URL imgUrl = getClass().getResource(imagePath);
        if (imgUrl != null) {
            javax.swing.ImageIcon icon = new javax.swing.ImageIcon(imgUrl);
            java.awt.Image img = icon.getImage();
            int targetWidth = 260;
            int targetHeight = 260;
            int imgW = img.getWidth(null);
            int imgH = img.getHeight(null);
            
            if (imgW > 0 && imgH > 0) {
                double ratio = Math.min((double) targetWidth / imgW, (double) targetHeight / imgH);
                int drawW = (int) (imgW * ratio);
                int drawH = (int) (imgH * ratio);
                java.awt.Image newImg = img.getScaledInstance(drawW, drawH, java.awt.Image.SCALE_SMOOTH);
                jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                jLabel1.setIcon(new javax.swing.ImageIcon(newImg));
            } else {
                jLabel1.setIcon(icon);
            }
        } else {
            jLabel1.setIcon(null);
            jLabel1.setText("No Image");
        }
        
        // Load variants
        variatyPanel.removeAll();
        java.util.List<Model.BienTheSanPham> variants = Controller.SanPhamDAO.getBienTheByMaSp(sp.getMaSp());
        if (variants != null && !variants.isEmpty()) {
            for (Model.BienTheSanPham variant : variants) {
                javax.swing.JButton btnVariant = new javax.swing.JButton(variant.getTenBienThe());
                btnVariant.setFont(new java.awt.Font("Segoe UI", 0, 14));
                btnVariant.setBackground(new java.awt.Color(255, 255, 255));
                btnVariant.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                btnVariant.addActionListener(e -> {
                    currentVariant = variant;
                    priceField.setText(formatter.format(variant.getGiaBan()) + "₫");
                });
                variatyPanel.add(btnVariant);
            }
        } else {
            javax.swing.JLabel noVariantLabel = new javax.swing.JLabel("Không có biến thể");
            noVariantLabel.setFont(new java.awt.Font("Segoe UI", 0, 14));
            variatyPanel.add(noVariantLabel);
        }
        variatyPanel.revalidate();
        variatyPanel.repaint();
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
        Delivery = new javax.swing.JLabel();
        tabName = new javax.swing.JLabel();
        DashboardPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        productName = new javax.swing.JLabel();
        priceField = new javax.swing.JLabel();
        warantyInfo = new javax.swing.JLabel();
        descrpieField = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        variatyPanel = new javax.swing.JPanel();
        variatyPanel.setLayout(new View.Customers.ProductPanel.WrapLayout(java.awt.FlowLayout.LEFT, 5, 5));
        variatyPanel.setBackground(new java.awt.Color(248, 249, 252));

        HeadingPanel.setBackground(new java.awt.Color(248, 249, 252));

        Delivery.setBackground(new java.awt.Color(255, 255, 255));
        Delivery.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        Delivery.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Default/Icon/stock.png"))); // NOI18N
        Delivery.setText("Giỏ hàng");

        tabName.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tabName.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Default/Icon/back-vector.png"))); // NOI18N
        tabName.setText("Trở về");

        javax.swing.GroupLayout HeadingPanelLayout = new javax.swing.GroupLayout(HeadingPanel);
        HeadingPanel.setLayout(HeadingPanelLayout);
        HeadingPanelLayout.setHorizontalGroup(
            HeadingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(HeadingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                    .addComponent(tabName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        DashboardPanel.setBackground(new java.awt.Color(248, 249, 252));

        productName.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        productName.setText("Tên sản phẩm");

        priceField.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        priceField.setText("Giá");

        warantyInfo.setText("Chú thích bảo hành");

        descrpieField.setText("Mô tả");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel2.setText("Thêm vào giỏ hàng");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel3.setText("Đặt ngay");

        javax.swing.GroupLayout DashboardPanelLayout = new javax.swing.GroupLayout(DashboardPanel);
        DashboardPanel.setLayout(DashboardPanelLayout);
        DashboardPanelLayout.setHorizontalGroup(
            DashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DashboardPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(DashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(descrpieField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(DashboardPanelLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(DashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(productName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(DashboardPanelLayout.createSequentialGroup()
                                .addComponent(variatyPanel)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, DashboardPanelLayout.createSequentialGroup()
                                .addGap(0, 157, Short.MAX_VALUE)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel3)
                                .addGap(6, 6, 6))
                            .addComponent(priceField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(warantyInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        DashboardPanelLayout.setVerticalGroup(
            DashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DashboardPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(DashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(DashboardPanelLayout.createSequentialGroup()
                        .addComponent(productName, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(priceField, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(warantyInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(variatyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(DashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)))
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(descrpieField, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
                .addContainerGap())
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel DashboardPanel;
    private javax.swing.JLabel Delivery;
    private javax.swing.JPanel HeadingPanel;
    private javax.swing.JLabel descrpieField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel priceField;
    private javax.swing.JLabel productName;
    private javax.swing.JLabel tabName;
    private javax.swing.JPanel variatyPanel;
    private javax.swing.JLabel warantyInfo;
    // End of variables declaration//GEN-END:variables
}
