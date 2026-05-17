/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package View.Admin.CreateInvoice;
import Controller.Admin.CreateInvoicePanel.CreateInvoiceProcess;
import Model.KhachHang;
// import Controller.CustomerController;
// import Utils.SessionManager;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class CreateInvoicePanel extends javax.swing.JPanel {

    private CreateInvoiceProcess process;

    public CreateInvoicePanel() {
        initComponents();
        process = new CreateInvoiceProcess(); 
        startRouting();
    }
    
    public void startRouting(){
        if(showCustomerPopup()){
            showRoutingFork1();
        }
    }
    
    // --- BƯỚC 1: POPUP NHẬP SĐT ---
    private boolean showCustomerPopup() {
        // 1. Tạo một Panel tùy chỉnh để chứa các thành phần giao diện
        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setLayout(new java.awt.GridLayout(2, 1, 5, 10));

        // Hàng 1: Chứa Label SĐT, Ô nhập và Nút Kiểm tra
        javax.swing.JPanel row1 = new javax.swing.JPanel(new java.awt.BorderLayout(5, 0));
        row1.add(new javax.swing.JLabel("Số điện thoại: "), java.awt.BorderLayout.WEST);
        JTextField txtPhone = new JTextField();
        row1.add(txtPhone, java.awt.BorderLayout.CENTER);
        javax.swing.JButton btnCheck = new javax.swing.JButton("Kiểm tra");
        row1.add(btnCheck, java.awt.BorderLayout.EAST);

        // Hàng 2: Chứa Label Tên KH và Ô hiển thị tên
        javax.swing.JPanel row2 = new javax.swing.JPanel(new java.awt.BorderLayout(5, 0));
        row2.add(new javax.swing.JLabel("Tên khách hàng:"), java.awt.BorderLayout.WEST);
        JTextField txtName = new JTextField();
        txtName.setEditable(false); // Luôn khóa, chỉ để hiển thị
        row2.add(txtName, java.awt.BorderLayout.CENTER);

        panel.add(row1);
        panel.add(row2);

        // 2. Biến cờ (flag) để theo dõi trạng thái tìm thấy khách hàng
        final boolean[] isCustomerFound = {false}; 

        // 3. Logic xử lý khi nhấn nút "Kiểm tra"
        btnCheck.addActionListener(e -> {
            String phone = txtPhone.getText().trim();
            if (phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại cần kiểm tra!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                // Gọi Database thông qua đối tượng process
                KhachHang cus = process.getCustomerByPhone(phone);
                
                if (cus != null) {
                    // TÌNH HUỐNG 1: Tìm thấy khách hàng
                    // Giả sử Model KhachHang của bạn có hàm getTenKH()
                    txtName.setText(cus.getHoTen()); 
                    txtName.setForeground(java.awt.Color.BLACK);
                    isCustomerFound[0] = true;
                } else {
                    // TÌNH HUỐNG 2: Không tìm thấy khách hàng
                    txtName.setText("Không tìm thấy khách hàng!");
                    txtName.setForeground(java.awt.Color.RED);
                    isCustomerFound[0] = false;
                }
            } catch (Exception ex) {
                // Bắt buộc phải có try-catch vì hàm DB ném ra Exception
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi tra cứu Database: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Hỗ trợ thêm: Nhấn Enter ở ô nhập SĐT cũng tương đương bấm nút Kiểm tra
        txtPhone.addActionListener(e -> btnCheck.doClick());

        // 4. Hiển thị Popup với Panel tự tạo
        int option = JOptionPane.showConfirmDialog(
                this, panel, "Kiểm Tra Khách Hàng", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
        
        
        
        // 5. Xử lý logic khi nhấn nút "Xác nhận" (OK_OPTION)
        if (option == JOptionPane.OK_OPTION) {
            String phone = txtPhone.getText().trim();
            if(phone.isEmpty()){
                JOptionPane.showMessageDialog(this, 
                    "Số điện thoại không được để trống!", 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return showCustomerPopup();
            }
            
            if (isCustomerFound[0] == false) {
                JOptionPane.showMessageDialog(this, 
                    "Vui lòng bấm 'Kiểm tra' và đảm bảo khách hàng tồn tại trước khi xác nhận!", 
                    "Chưa xác minh", JOptionPane.WARNING_MESSAGE);
                return showCustomerPopup(); // Gọi lại chính hàm này để hiện lại Popup
            }
            
            if (isCustomerFound[0] == true) {
                String finalPhone = txtPhone.getText().trim();
                String finalName = txtName.getText().trim();
                
                showRoutingFork1();
                
                return true; 
            } else {
                return false; 
            }
        }
        
        return false; 
    }

    // --- BƯỚC 2: RẼ NHÁNH 1 (Sản phẩm / Dịch vụ) ---
    private void showRoutingFork1() {
        Object[] options = {"Sản phẩm", "Dịch vụ"};
        int choice = JOptionPane.showOptionDialog(this, 
                "Khách hàng muốn mua gì?", "Chọn Loại Hàng",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 
                null, options, options[0]);
        //YES = 0
        //NO = 1

        if (choice == 0) {
            System.out.println("Đang mở màn hình Sản phẩm...");
            
            
            
            // new View.Admin.CreateInvoice.Product.ProductMainView().setVisible(true);
        } else if (choice == 1) {
            showRoutingFork2(); 
        }
    }

    // --- BƯỚC 3: RẼ NHÁNH 2 (Thông thường / Sửa chữa) ---
    private void showRoutingFork2() {
        Object[] options = {"Thông thường", "Sửa chữa"};
        int choice = JOptionPane.showOptionDialog(this, 
                "Chọn loại dịch vụ:", "Phân Loại Dịch Vụ",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 
                null, options, options[0]);

        if (choice == 0) {
            System.out.println("Đang mở màn hình Dịch vụ...");
            // new View.Admin.CreateInvoice.Service.ServiceMainView().setVisible(true);
        } else if (choice == 1) {
            System.out.println("Đang mở màn hình Phiếu sửa chữa...");
            // new View.Admin.CreateInvoice.RepairTicket.RepairTicketMainView().setVisible(true);
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

        jLabel1 = new javax.swing.JLabel();

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setText("Mua hàng - Sản phẩm");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(212, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(250, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}

