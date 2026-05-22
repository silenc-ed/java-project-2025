/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View.Admin;

/**
 *
 * @author DELL
 */
public class Main extends javax.swing.JFrame {

    // private static final java.util.logging.Logger logger =
    // java.util.logging.Logger.getLogger(Main.class.getName());

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
            fullname = "Quản trị viên";
        }
        menu1.setFullName(fullname);

        // --- Cập nhật danh sách mục menu Admin ---
        menu1.setMenu(new String[] {
                "Tổng quan",
                "Mua hàng",
                "Quản lý SP",
                "Đơn nhập hàng",
                "Khách hàng",
                "Nhân viên",
                "Tồn kho CN",
                "Khuyến mãi",
                "Đơn bán hàng",
                "Lịch làm việc"
        });

        // --- Thiết lập khu vực nội dung chính ---
        mainBody = new javax.swing.JPanel();
        mainBody.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.BorderLayout());
        jPanel1.add(menu1, java.awt.BorderLayout.WEST);
        jPanel1.add(mainBody, java.awt.BorderLayout.CENTER);

        // --- Xóa layout tĩnh của NetBeans để cho phép menu giãn toàn màn hình ---
        getContentPane().setLayout(new java.awt.BorderLayout());
        getContentPane().removeAll();
        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        // --- Xử lý sự kiện chọn mục menu Admin ---
        menu1.addEventMenuSelected(new View.Customers.EventMenuSelected() {
            @Override
            public void selected(int index) {
                switch (index) {
                    case 0: // Tổng quan
                        showForm(new View.Admin.DashboardPanel());
                        break;
                    case 1: // Mua hàng
                        showForm(new View.Admin.CreateInvoicePanel());
                        break;
                    case 2: // Quản lý SP
                        showForm(new View.Admin.ProductPanel());
                        break;
                    case 3: // Đơn nhập hàng
                        showForm(new View.Admin.BillPanel());
                        break;
                    case 4: // Khách hàng
                        showForm(new View.Admin.CustomerPanel());
                        break;
                    case 5: // Nhân viên
                        showForm(new View.Admin.EmployeePanel());
                        break;
                    case 6: // Tồn kho CN
                        showForm(new View.Admin.WarehousePanel());
                        break;
                    case 7: // Khuyến mãi
                        showForm(new View.Admin.VoucherPanel());
                        break;
                    case 8: // Đơn bán hàng
                        showForm(new View.Admin.ProcurementPanel());
                        break;
                    case 9: // Lịch làm việc
                        showForm(new View.Schedule.ScheduleManagementPanel());
                        break;
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
                        if (selected == null || selected.trim().isEmpty())
                            return;

                        popupMenu.setVisible(false);
                        optionPanel.getListOptionUser().clearSelection();

                        if ("Đăng xuất".equals(selected)) {
                            int confirm = javax.swing.JOptionPane.showConfirmDialog(Main.this,
                                    "Bạn có chắc chắn muốn đăng xuất không?", "Xác nhận đăng xuất",
                                    javax.swing.JOptionPane.YES_NO_OPTION);
                            if (confirm == javax.swing.JOptionPane.YES_OPTION) {
                                Controller.SignIn.AuthProcess.revokeToken();
                                new View.SignIn.SignInView().setVisible(true);
                                dispose();
                            }
                        } else if ("Hồ sơ".equals(selected)) {
                            View.Admin.UserProfileDialog profileDialog = new View.Admin.UserProfileDialog(Main.this);
                            profileDialog.setVisible(true);
                        } else {
                            javax.swing.JOptionPane.showMessageDialog(Main.this,
                                    "Chức năng " + selected + " đang phát triển.");
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
                // Admin profile panel can be implemented here later
                javax.swing.JOptionPane.showMessageDialog(Main.this, "Chức năng xem hồ sơ Admin đang phát triển.",
                        "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Mặc định hiển thị Dashboard khi mở
        menu1.setSelectedIndex(0);
        showForm(new View.Admin.DashboardPanel());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        menu1 = new View.Admin.Menu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE));
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(menu1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 683,
                                        Short.MAX_VALUE)
                                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap()));
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(76, 76, 76)
                                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
                        .addComponent(menu1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)));

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
    private javax.swing.JPanel jPanel2;
    private View.Admin.Menu menu1;
    // End of variables declaration//GEN-END:variables
}
