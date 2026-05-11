/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package View.Customers.ProductPanel;

/**
 *
 * @author DELL
 */
public class ProductCard extends javax.swing.JPanel {

    private java.awt.Image currentImage;
    private javax.swing.Timer zoomTimer;
    private int currentImgSize = 180;
    private int targetImgSize = 180;

    /**
     * Creates new form ProductPanel2
     */
    public ProductCard() {
        initComponents();
        applyModernStyling();
    }

    private void applyModernStyling() {
        // Áp dụng Look and Feel hiện đại
        this.setBackground(java.awt.Color.WHITE);
        jPanel1.setBackground(java.awt.Color.WHITE);
        
        // Bo góc viền nhẹ nhàng
        this.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 230, 230), 1, true),
            javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Format tên sản phẩm
        jLabel1.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        jLabel1.setForeground(new java.awt.Color(40, 40, 40));
        
        // Format giá bán
        productCost.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        productCost.setForeground(new java.awt.Color(220, 53, 69)); // Đỏ nổi bật
        
        // Format lượt bán
        soldCounted.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        soldCounted.setForeground(new java.awt.Color(120, 120, 120)); // Xám nhạt
        
        // Căn giữa hình ảnh
        productImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        
        // Hiệu ứng hover mượt mà với Timer
        zoomTimer = new javax.swing.Timer(10, e -> {
            if (currentImage != null && currentImgSize != targetImgSize) {
                if (currentImgSize < targetImgSize) {
                    currentImgSize += 2;
                    if (currentImgSize > targetImgSize) currentImgSize = targetImgSize;
                } else {
                    currentImgSize -= 2;
                    if (currentImgSize < targetImgSize) currentImgSize = targetImgSize;
                }
                
                // Scale ảnh bằng Graphics2D để đảm bảo mượt và nhẹ nhất
                java.awt.image.BufferedImage resizedImg = new java.awt.image.BufferedImage(
                    currentImgSize, currentImgSize, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                java.awt.Graphics2D g2 = resizedImg.createGraphics();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                // Canh giữa ảnh khi zoom
                int offset = (195 - currentImgSize) / 2;
                g2.drawImage(currentImage, offset, offset, currentImgSize, currentImgSize, null);
                g2.dispose();
                
                productImage.setIcon(new javax.swing.ImageIcon(resizedImg));
            } else {
                zoomTimer.stop();
            }
        });

        this.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(new java.awt.Color(160, 160, 160), 1, true), // Viền xám khi hover
                    javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
                targetImgSize = 195; // Phóng to lên 195px
                zoomTimer.start();
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 230, 230), 1, true),
                    javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
                targetImgSize = 180; // Thu về 180px
                zoomTimer.start();
            }
        });
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        // Đặt kích thước cứng để WrapLayout tự động tính toán và xuống dòng
        this.setPreferredSize(new java.awt.Dimension(220, 280));
    }

    public void setData(Model.SanPham sp) {
        jLabel1.setText(sp.getTenSp());
        
        java.text.DecimalFormat formatter = new java.text.DecimalFormat("###,###,###");
        productCost.setText(formatter.format(sp.getGiaBan()) + "₫");
        
        soldCounted.setText("Đã bán " + sp.getSoLuongDaBan());
        
        String imagePath = sp.getHinhAnh();
        if (imagePath == null || imagePath.trim().isEmpty()) {
            imagePath = "/Default/Product/laptop.png";
        }
        
        java.net.URL imgUrl = getClass().getResource(imagePath);
        if (imgUrl != null) {
            javax.swing.ImageIcon icon = new javax.swing.ImageIcon(imgUrl);
            currentImage = icon.getImage();
            
            // Vẽ ảnh gốc ngay lần đầu (180x180) nhưng đặt trong khung 195x195 để không bị giật layout khi zoom
            java.awt.image.BufferedImage initialImg = new java.awt.image.BufferedImage(
                195, 195, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g2 = initialImg.createGraphics();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int offset = (195 - 180) / 2;
            g2.drawImage(currentImage, offset, offset, 180, 180, null);
            g2.dispose();
            
            productImage.setIcon(new javax.swing.ImageIcon(initialImg));
        } else {
            productImage.setIcon(null);
            productImage.setText("No Image");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        productImage = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        productCost = new javax.swing.JLabel();
        soldCounted = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        productCost.setText("productCost");

        soldCounted.setText("soldCounted");

        jLabel1.setText("productName");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(productCost)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 76, Short.MAX_VALUE)
                        .addComponent(soldCounted)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(productCost, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(soldCounted, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(productImage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(productImage, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel productCost;
    private javax.swing.JLabel productImage;
    private javax.swing.JLabel soldCounted;
    // End of variables declaration//GEN-END:variables
}
