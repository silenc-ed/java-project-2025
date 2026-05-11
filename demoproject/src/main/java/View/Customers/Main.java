/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View.Customers;

import View.Customers.ProductPanel.ProductPanel;

/**
 *
 * @author DELL
 */
public class Main extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Main.class.getName());

    /**
     * Creates new form Main
     */
    private javax.swing.JPanel mainBody;

    private void showForm(javax.swing.JComponent com) {
        mainBody.removeAll();
        mainBody.add(com);
        mainBody.repaint();
        mainBody.revalidate();
    }

    public Main() {
        initComponents();
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);

        // --- Cập nhật tên người đăng nhập ---
        String fullname = Controller.SignIn.AuthProcess.getFullNameFromToken();
        if (fullname == null || fullname.isEmpty()) {
            fullname = "Khách hàng mới";
        }
        menu1.setFullName(fullname);

        // --- Cập nhật danh sách mục menu ---
        menu1.setMenu(new String[]{
            "Trang chủ",
            "Giỏ hàng",
            "Khuyến mãi",
            "Bảo hành"
        });

        // --- Thiết lập khu vực nội dung chính ---
        mainBody = new javax.swing.JPanel();
        mainBody.setLayout(new java.awt.BorderLayout());

        // --- Xóa bỏ các panel phụ được NetBeans tự sinh ra để tránh đè giao diện ---
        jPanel1.removeAll();
        jPanel1.setLayout(new java.awt.BorderLayout());
        jPanel1.add(menu1, java.awt.BorderLayout.WEST);
        jPanel1.add(mainBody, java.awt.BorderLayout.CENTER);

        // --- Xóa layout tĩnh của NetBeans để cho phép menu giãn toàn màn hình ---
        getContentPane().setLayout(new java.awt.BorderLayout());
        getContentPane().removeAll();
        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        // --- Xử lý sự kiện chọn mục menu ---
        menu1.addEventMenuSelected(new EventMenuSelected() {
            @Override
            public void selected(int index) {
                if (index == 0) {
                    showForm(new ProductPanel());
                } else if (index == 1) {
                    showForm(new PacketPanel());
                } else if (index == 2) {
                    showForm(new VoucherPanel());
                } else if (index == 3) {
                    showForm(new WarrantyPanel());
                }
            }
        });

        // --- Xử lý sự kiện click vào icon (logo user) ---
        menu1.addEventProfileClicked(new Runnable() {
            @Override
            public void run() {
                javax.swing.JPopupMenu popupMenu = new javax.swing.JPopupMenu();
                popupMenu.setBorder(javax.swing.BorderFactory.createEmptyBorder()); // optional styling
                
                View.Customers.UserOptionPanel optionPanel = new View.Customers.UserOptionPanel();
                
                // Thêm sự kiện cho list các chức năng (Hồ sơ, Cài đặt, ...)
                optionPanel.getListOptionUser().addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        String selected = optionPanel.getListOptionUser().getSelectedValue();
                        if (selected == null || selected.trim().isEmpty()) return;
                        
                        popupMenu.setVisible(false);
                        optionPanel.getListOptionUser().clearSelection();

                        if ("Hồ sơ".equals(selected)) {
                            menu1.clearSelection();
                            showForm(new UserAccountPanel());
                        } else if ("Đăng xuất".equals(selected)) {
                            int confirm = javax.swing.JOptionPane.showConfirmDialog(Main.this, 
                                    "Bạn có chắc chắn muốn đăng xuất không?", "Xác nhận đăng xuất", 
                                    javax.swing.JOptionPane.YES_NO_OPTION);
                            if (confirm == javax.swing.JOptionPane.YES_OPTION) {
                                Controller.SignIn.AuthProcess.revokeToken();
                                new View.SignIn.SignInView().setVisible(true);
                                dispose();
                            }
                        } else {
                            javax.swing.JOptionPane.showMessageDialog(Main.this, "Chức năng " + selected + " đang phát triển.");
                        }
                    }
                });
                
                popupMenu.add(optionPanel);
                
                // Hiển thị ở góc trái dưới cùng
                popupMenu.show(menu1, 60, menu1.getHeight() - optionPanel.getPreferredSize().height - 50);
            }
        });

        // --- Xử lý sự kiện click vào tên người dùng ---
        menu1.addEventUserNameClicked(new Runnable() {
            @Override
            public void run() {
                menu1.clearSelection();
                showForm(new UserAccountPanel());
            }
        });

        //Mặc định hiển thị Trang chủ khi mở
        menu1.setSelectedIndex(0);
        showForm(new ProductPanel());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        menu1 = new View.Customers.Menu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(menu1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(702, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(menu1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Main().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private View.Customers.Menu menu1;
    // End of variables declaration//GEN-END:variables
}
