package com.mycompany.demo;

import com.mycompany.demo.Common.alertUtil;
import com.mycompany.demo.Common.hashUtil;
import com.mycompany.demo.Controller.CreateAccountProcess;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class CreateAccountController {
    alertUtil al = new alertUtil();
    CreateAccountProcess process = new CreateAccountProcess();

    // Dữ liệu từ Bước 1 gửi sang
    private String step1Fullname, step1Email, step1Number, step1Address;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField repeatPasswordField;

    // Hàm nhận dữ liệu từ Step 1
    public void setUserData(String name, String email, String num, String addr) {
        this.step1Fullname = name;
        this.step1Email = email;
        this.step1Number = num;
        this.step1Address = addr;
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        String username = usernameField.getText().trim();
        String pass = passwordField.getText().trim();
        String repeatPass = repeatPasswordField.getText().trim();

        // 1. Kiểm tra rỗng
        if (username.isEmpty() || pass.isEmpty() || repeatPass.isEmpty()) {
            al.showAlert(Alert.AlertType.WARNING, "Lỗi", "Vui lòng không để trống thông tin tài khoản!");
            return;
        }

        // 2. Kiểm tra mật khẩu khớp nhau
        if (!pass.equals(repeatPass)) {
            al.showAlert(Alert.AlertType.WARNING, "Lỗi", "Mật khẩu xác nhận không khớp!");
            return;
        }

        // 3. Kiểm tra Username đã tồn tại chưa
        if (process.isUsernameTaken(username)) {
            al.showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên đăng nhập này đã có người sử dụng!");
            return;
        }

        // 4. Thực hiện Đăng ký
        // Băm mật khẩu trước khi lưu
        String hashedPassword = hashUtil.hashPassword(pass);
        
        boolean success = process.createAccount(
                step1Fullname, step1Email, step1Number, step1Address, username, hashedPassword
        );

        if (success) {
            al.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Tài khoản của bạn đã được tạo thành công!");
            // TODO: Chuyển về trang Đăng nhập
            try {
                App.setRoot("login"); 
            } catch (Exception e) { e.printStackTrace(); }
        } else {
            al.showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra trong quá trình lưu dữ liệu!");
        }
    }
    
    @FXML
    private void goSignIn(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/demo/SignInView/SignInView.fxml"));
            Parent root = loader.load();

            App.getScene().setRoot(root); 

        } catch (IOException e) {
            al.showAlert(Alert.AlertType.ERROR, "Lỗi chuyển trang", "Không thể mở trang đăng nhập: " + e.getMessage());
            e.printStackTrace();
        }
    }
}