/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View.Admin;

import View.Admin.DashBoard.DashboardPanel;
import Common.LoadingOverlay;

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
    private final LoadingOverlay loadingOverlay = new LoadingOverlay();

    private void showForm(javax.swing.JComponent com) {
        loadingOverlay.showLoading(mainBody);

        javax.swing.SwingWorker<Void, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                Thread.sleep(350); // Thời gian tối thiểu hiển thị spinner
                return null;
            }

            @Override
            protected void done() {
                mainBody.removeAll();
                mainBody.add(com);
                mainBody.repaint();
                mainBody.revalidate();
                loadingOverlay.hideLoading();
            }
        };
        worker.execute();
    }

    public void setFullName(String hoTen) {
        if (menu1 != null) {
            menu1.setFullName(hoTen);
        }
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

        // --- Load quyền từ DB ---
        Controller.Admin.PermissionService.loadPermissions();

        // --- Mapping: key chức năng (DB) → tên hiển thị menu ---
        // Thứ tự phải nhất quán với switch-case bên dưới
        final java.util.LinkedHashMap<String, String> featureToLabel = new java.util.LinkedHashMap<>();
        featureToLabel.put("DASHBOARD",   "Tổng quan");   // luôn hiển thị
        featureToLabel.put("Mua hang",    "Mua hàng");
        featureToLabel.put("Quan ly SP",  "Quản lý SP");
        featureToLabel.put("Dich vu",     "Dịch vụ");
        featureToLabel.put("Don hang",    "Đơn hàng");
        featureToLabel.put("Khach hang",  "Khách hàng");
        featureToLabel.put("Nhan vien",   "Nhân viên");
        featureToLabel.put("Ton kho CN",  "Tồn kho CN");
        featureToLabel.put("Khuyen mai",  "Khuyến mãi");
        featureToLabel.put("Nhap kho",    "Nhập kho");
        featureToLabel.put("Cham cong",   "Chấm công");

        // --- Build menu động: chỉ gồm tab user được xem ---
        final java.util.List<String> menuLabels = new java.util.ArrayList<>();
        final java.util.List<String> menuKeys   = new java.util.ArrayList<>();

        for (java.util.Map.Entry<String, String> entry : featureToLabel.entrySet()) {
            String key   = entry.getKey();
            String label = entry.getValue();
            // Dashboard luôn hiển thị; các tab khác kiểm tra quyền xem
            if ("DASHBOARD".equals(key) || Controller.Admin.PermissionService.canView(key)) {
                menuLabels.add(label);
                menuKeys.add(key);
            }
        }

        menu1.setMenu(menuLabels.toArray(new String[0]));

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

        // --- Xử lý sự kiện chọn mục menu Admin (dùng menuKeys thay vì index cứng) ---
        menu1.addEventMenuSelected(new View.Customers.EventMenuSelected() {
            private View.Admin.Customer.CustomerPanel cachedCustomerPanel;
            private View.Admin.Employee.EmployeePanel cachedEmployeePanel;

            @Override
            public void selected(int index) {
                if (index < 0 || index >= menuKeys.size()) return;
                String key = menuKeys.get(index);

                switch (key) {
                    case "DASHBOARD":
                        showForm(new View.Admin.DashBoard.DashboardPanel());
                        break;
                    case "Mua hang":
                        showForm(new View.Admin.CreateInvoice.CreateInvoicePanel());
                        break;
                    case "Quan ly SP":
                        showForm(new View.Admin.Product.ProductManagementController());
                        break;
                    case "Dich vu":
                        showForm(new View.Admin.Service.ServicePanel());
                        break;
                    case "Don hang":
                        showForm(new View.Admin.Procurement.ProcurementPanel());
                        break;
                    case "Khach hang":
                        if (cachedCustomerPanel == null) {
                            cachedCustomerPanel = new View.Admin.Customer.CustomerPanel();
                        }
                        showForm(cachedCustomerPanel);
                        break;
                    case "Nhan vien":
                        if (cachedEmployeePanel == null) {
                            cachedEmployeePanel = new View.Admin.Employee.EmployeePanel();
                        }
                        showForm(cachedEmployeePanel);
                        break;
                    case "Ton kho CN":
                        showForm(new View.Admin.Warehouse.WarehousePanel());
                        break;
                    case "Khuyen mai":
                        showForm(new View.Admin.Voucher.VoucherPanel());
                        break;
                    case "Nhap kho":
                        showForm(new View.Admin.Bill.BillPanel());
                        break;
                    case "Cham cong":
                        showForm(new View.Admin.Attendance.AttendancePanel());
                        break;
                }
            }
        });

        // --- Xử lý sự kiện click vào icon (logo user) ---
        menu1.addEventProfileClicked(new Runnable() {
            @Override
            public void run() {
                javax.swing.JPopupMenu popupMenu = new javax.swing.JPopupMenu();
                popupMenu.setBorder(javax.swing.BorderFactory.createEmptyBorder());

                View.Customers.UserOption.UserOptionPanel optionPanel = new View.Customers.UserOption.UserOptionPanel();

                optionPanel.getListOptionUser().addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        String selected = optionPanel.getListOptionUser().getSelectedValue();
                        if (selected == null || selected.trim().isEmpty())
                            return;

                        popupMenu.setVisible(false);
                        optionPanel.getListOptionUser().clearSelection();

                        if ("Hồ sơ".equals(selected)) {
                            menu1.clearSelection();
                            showForm(new View.Customers.UserAccount.UserAccountPanel());
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
                            javax.swing.JOptionPane.showMessageDialog(Main.this,
                                    "Chức năng " + selected + " đang phát triển.");
                        }
                    }
                });

                popupMenu.add(optionPanel);
                popupMenu.show(menu1, 60, menu1.getHeight() - optionPanel.getPreferredSize().height - 50);
            }
        });

        // --- Xử lý sự kiện click vào tên người dùng ---
        menu1.addEventUserNameClicked(new Runnable() {
            @Override
            public void run() {
                menu1.clearSelection();
                showForm(new View.Customers.UserAccount.UserAccountPanel());
            }
        });

        // Mặc định hiển thị Dashboard khi mở
        menu1.setSelectedIndex(0);
        showForm(new View.Admin.DashBoard.DashboardPanel());
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
