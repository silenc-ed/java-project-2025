package com.mycompany.demo.Controller;

import com.mycompany.demo.Common.hashUtil;
import com.mycompany.demo.ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginProcess {
    public boolean loginProcess(String username, String rawPassword) {
        // Chỉ lấy PASSWORD_HASH dựa trên USERNAME
        String SQL = "SELECT PASSWORD_HASH FROM TAIKHOAN WHERE TRIM(USERNAME) = ?";

        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(SQL)) {

            ps.setString(1, username);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("PASSWORD_HASH");
                    
                    // Dùng thư viện BCrypt (thông qua hashUtil) để so sánh
                    return hashUtil.checkPassword(rawPassword, storedHash);
                }
            }
        } catch (Exception ex) {
            System.err.println("Lỗi truy vấn Đăng nhập: " + ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }
}