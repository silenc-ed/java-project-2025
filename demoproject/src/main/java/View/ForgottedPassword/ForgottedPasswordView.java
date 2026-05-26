/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View.ForgottedPassword;

import View.CreateAccount.*;

/**
 *
 * @author DELL
 */
public class ForgottedPasswordView extends javax.swing.JFrame {
    
   /**
     * Creates new form SignInView
     */
    public ForgottedPasswordView() {
        initComponents();
        setLocationRelativeTo(null);
    }

    public ForgottedPasswordView(String fullname, String gmail, String number, String address, String expectedOtp) {
        initComponents();
        setLocationRelativeTo(null);
        
        accountField.putClientProperty("JTextField.placeholderText", "Tên tài khoản, số điện thoại, hoặc email...");
        accountField.putClientProperty("JTextField.showRevealButton", true);

        signInLink.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        // Listener already added in initComponents()
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        nextButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jTextArea1 = new javax.swing.JTextArea();
        accountField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        signInLink = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        signUpLink = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(204, 153, 255));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jPanel1.setBackground(new java.awt.Color(204, 153, 255));

        jPanel2.setPreferredSize(new java.awt.Dimension(937, 524));
        jPanel2.setRequestFocusEnabled(false);
        jPanel2.setVerifyInputWhenFocusTarget(false);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setPreferredSize(new java.awt.Dimension(307, 500));

        nextButton.setText("Tiếp Theo");
        nextButton.addActionListener(this::nextButtonActionPerformed);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setText("Quên mật khẩu");

        jTextArea1.setEditable(false);
        jTextArea1.setBackground(new java.awt.Color(255, 255, 255));
        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(3);
        jTextArea1.setText("Hãy nhập tên đăng nhập, số điện thoại hoặc email liên kết với tài khoản cá nhân của bạn");
        jTextArea1.setToolTipText("");
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setBorder(null);
        jTextArea1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jTextArea1.setDisabledTextColor(new java.awt.Color(153, 153, 153));
        jTextArea1.setFocusable(false);
        jTextArea1.setHighlighter(null);

        accountField.addActionListener(this::accountFieldActionPerformed);

        jLabel3.setText("Trở lại đăng nhập?");

        signInLink.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        signInLink.setForeground(new java.awt.Color(204, 51, 255));
        signInLink.setText("Đăng nhập");
        signInLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                signInLinkMouseClicked(evt);
            }
        });

        jLabel4.setText("Bạn chưa có tài khoản?");

        signUpLink.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        signUpLink.setForeground(new java.awt.Color(204, 51, 255));
        signUpLink.setText("Đăng ký");
        signUpLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                signUpLinkMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(signUpLink))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(signInLink))))
                    .addComponent(nextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextArea1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(accountField, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(39, 39, 39))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {accountField, jLabel2, jTextArea1, nextButton});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(121, 121, 121)
                .addComponent(jLabel2)
                .addGap(12, 12, 12)
                .addComponent(jTextArea1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(accountField, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addComponent(nextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(signInLink)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(signUpLink))
                .addContainerGap(140, Short.MAX_VALUE))
        );

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Login/Theme/theme.jpg"))); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 614, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 521, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 929, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(43, 43, 43))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void signUpLinkMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_signUpLinkMouseClicked
        this.dispose();
        new View.CreateAccount.CreateUserView().setVisible(true);
    }//GEN-LAST:event_signUpLinkMouseClicked

    private void signInLinkMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_signInLinkMouseClicked
        this.dispose();
        new View.SignIn.SignInView().setVisible(true);
    }//GEN-LAST:event_signInLinkMouseClicked

    private void accountFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_accountFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_accountFieldActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        String input = accountField.getText().trim();
        if (input.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng nhập tên đăng nhập hoặc email!", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Chặn tìm bằng sđt theo yêu cầu
        String phoneRegex = "^[0-9]{10,11}$";
        if (input.matches(phoneRegex)) {
            javax.swing.JOptionPane.showMessageDialog(this, "Tính năng xác thực bằng số điện thoại đang được phát triển.\nVui lòng nhập Username hoặc Email của bạn.", "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Controller.ForgottedPassword.ForgottedPasswordProcess process = new Controller.ForgottedPassword.ForgottedPasswordProcess();
        String[] accountData = process.findAccountAndGetEmail(input);

        if (accountData == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Không tìm thấy tài khoản với thông tin này!", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        String email = accountData[0];
        String username = accountData[1];

        if (email == null || email.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Tài khoản này chưa được liên kết với Email nào!", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        nextButton.setEnabled(false);
        nextButton.setText("Đang gửi OTP...");

        new Thread(() -> {
            String otp = Common.EmailService.generateOTP();
            boolean sent = Common.EmailService.sendOTP(email, otp);

            javax.swing.SwingUtilities.invokeLater(() -> {
                nextButton.setEnabled(true);
                nextButton.setText("Tiếp Theo");

                if (!sent) {
                    javax.swing.JOptionPane.showMessageDialog(ForgottedPasswordView.this, "Không thể gửi email OTP. Vui lòng kiểm tra kết nối mạng!", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
                    return;
                }

                GetRequestAccount requestView = new GetRequestAccount(username, email, otp);
                requestView.setVisible(true);
                ForgottedPasswordView.this.dispose();
            });
        }).start();
    }//GEN-LAST:event_nextButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {        
        try {
            com.formdev.flatlaf.FlatLightLaf.setup(); 
        } catch (Exception ex) {
            System.err.println("Không thể khởi tạo FlatLaf");
        }

        java.awt.EventQueue.invokeLater(() -> new ForgottedPasswordView().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField accountField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JButton nextButton;
    private javax.swing.JLabel signInLink;
    private javax.swing.JLabel signUpLink;
    // End of variables declaration//GEN-END:variables
}
