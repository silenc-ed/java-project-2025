/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package View.Customers.Warranty;
import Controller.Customers.Warranty.WarrantyCheckingProcess;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JFrame;
import java.sql.Date;

public class WarrantyPanel extends javax.swing.JPanel {

    public WarrantyPanel() {
        initComponents();
        
//        jLabel2.setLocation(293, 270);
//        numberField.setLocation(293, 290);
//        numberField1.setLocation(293, 310);
        sdtField.putClientProperty("JTextField.placeholderText", "Số điện thoại hoặc Email");
        imeiField.putClientProperty("JTextField.placeholderText", "Số sê-ri cúa máy");
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jRadioButton1 = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        sdtField = new javax.swing.JTextField();
        imeiField = new javax.swing.JTextField();
        confirmButton = new javax.swing.JButton();

        jRadioButton1.setText("jRadioButton1");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        jLabel1.setText("Tra cứu bảo hành");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel2.setFont(new java.awt.Font("Segoe UI", 3, 24)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Nhập thông tin tra cứu");

        sdtField.addActionListener(this::sdtFieldActionPerformed);

        imeiField.addActionListener(this::imeiFieldActionPerformed);

        confirmButton.setText("Xác nhận");
        confirmButton.addActionListener(this::confirmButtonActionPerformed);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(211, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sdtField, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(imeiField, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(confirmButton))
                .addContainerGap(211, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(117, 117, 117)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addComponent(sdtField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(imeiField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(confirmButton)
                .addContainerGap(204, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jSeparator1)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(12, 12, 12))
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void sdtFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sdtFieldActionPerformed
        
    }//GEN-LAST:event_sdtFieldActionPerformed

    private void imeiFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imeiFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_imeiFieldActionPerformed

    private void confirmButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmButtonActionPerformed
        String sdt = sdtField.getText(); 
        String imei = imeiField.getText();
        
        if(sdt.isEmpty() || imei.isEmpty()){
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tài khoản và mật khẩu!", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        java.sql.Date[] thoiGian = new java.sql.Date[2];
        Controller.Customers.Warranty.WarrantyCheckingProcess checkingWarranty = new Controller.Customers.Warranty.WarrantyCheckingProcess();
        
        try {
            String rs = checkingWarranty.checking(sdt, imei, thoiGian);
            if("Còn hiệu lực".equals(rs)){
                javax.swing.JOptionPane.showMessageDialog(this, "Còn hiệu lực!", "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                String msg = "Máy hợp lệ!\nNgày bắt đầu: " + thoiGian[0] + "\nNgày kết thúc: "
                        + thoiGian[1];
                javax.swing.JOptionPane.showMessageDialog(this, msg, "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
            } else if( "Hết hiệu lực".equals(rs)){
                String msg = "Từ chối bảo hành! Máy đã hết hiệu lực!\nNgày bắt đầu: "+ thoiGian[0] + "!\nNgày kết thúc: " + thoiGian[1];
                javax.swing.JOptionPane.showMessageDialog(this, msg, "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);            
            } else {
                String msg = "Máy chưa được kích hoạt bảo hành!";
                javax.swing.JOptionPane.showMessageDialog(this, msg, "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);            
            }
        } catch (Exception ex){
            javax.swing.JOptionPane.showMessageDialog(this, "Lỗi kết nối cơ sở dữ liệu: " + ex.getMessage(), "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);        
            ex.printStackTrace();
        }    
    }//GEN-LAST:event_confirmButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton confirmButton;
    private javax.swing.JTextField imeiField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField sdtField;
    // End of variables declaration//GEN-END:variables
    
    

}
