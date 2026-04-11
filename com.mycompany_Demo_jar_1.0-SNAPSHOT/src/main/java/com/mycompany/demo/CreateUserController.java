package com.mycompany.demo;

import com.mycompany.demo.Common.alertUtil;
import com.mycompany.demo.Controller.CreateUserProcess;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class CreateUserController implements Initializable {
    alertUtil al = new alertUtil();

    @FXML private TextField fullnameField;
    @FXML private TextField emailField;
    @FXML private TextField numberField;
    @FXML private TextField addressField;
    @FXML private Button nextButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Không code gì ở đây để tránh xung đột với On Action trong FXML
    }

    @FXML
    private void handleNext(ActionEvent event) {
        String fullname = fullnameField.getText().trim();
        String email = emailField.getText().trim();
        String number = numberField.getText().trim();
        String address = addressField.getText().trim();

        if (fullname.isEmpty() || email.isEmpty() || number.isEmpty() || address.isEmpty()) {
            al.showAlert(Alert.AlertType.WARNING, "Thông báo", "Vui lòng nhập đầy đủ thông tin cá nhân!");
            return;
        }

        CreateUserProcess cup = new CreateUserProcess();
        if (!cup.checkInfoProcess(email, number)) {
            al.showAlert(Alert.AlertType.WARNING, "Thông báo", "Số điện thoại hoặc Email này đã được sử dụng!");
            return;
        }

        try {
            // Dùng đường dẫn tuyệt đối để Maven không bị lạc đường
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/demo/SignUpView/CreateAccountView.fxml"));
            Parent root = loader.load();

            // Lấy controller của trang 2
            CreateAccountController step2Controller = loader.getController();
            
            // Kiểm tra tránh Null nếu trang 2 chưa cài Controller
            if (step2Controller != null) {
                step2Controller.setUserData(fullname, email, number, address);
            }

            App.getScene().setRoot(root); 

        } catch (IOException e) {
            al.showAlert(Alert.AlertType.ERROR, "Lỗi Load FXML", "Chi tiết lỗi: " + e.getMessage());
            e.printStackTrace();
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