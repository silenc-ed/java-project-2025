package com.mycompany.demo;

import com.mycompany.demo.Controller.LoginProcess;
import com.mycompany.demo.Common.alertUtil;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    alertUtil al = new alertUtil();

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            al.showAlert(Alert.AlertType.WARNING, "Thông báo", "Vui lòng nhập đầy đủ Tên đăng nhập và Mật khẩu!");
            return;
        }

        LoginProcess lp = new LoginProcess();
        if (lp.loginProcess(username, password)) {
            al.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đăng nhập hệ thống thành công!");
        } else {
            al.showAlert(Alert.AlertType.ERROR, "Thất bại", "Tên đăng nhập hoặc mật khẩu không chính xác.");
        }
    }
    
    @FXML
    private void goSignUp(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/demo/SignUpView/CreateUserView.fxml"));            Parent root = loader.load();
            App.getScene().setRoot(root); 
        } catch (IOException e) {
            al.showAlert(Alert.AlertType.ERROR, "Lỗi chuyển trang", "Không thể mở trang đăng ký: " + e.getMessage());
        }
    }
}  