package com.mycompany.demo.Controller;

import com.mycompany.demo.ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CreateAccountProcess {
    
    // 1. Kiểm tra Username đã tồn tại chưa
    public boolean isUsernameTaken(String username) {
        String SQL = "SELECT 1 FROM TAIKHOAN WHERE TRIM(USERNAME) = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(SQL)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); 
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    // 2. Lưu thông tin người dùng mới
    public boolean createAccount(String hoten, String email, String sdt, String address, String username, String password) {
        boolean isSuccess = false;
        
        try (Connection con = ConnectionUtils.getMyConnection()) {
            con.setAutoCommit(false); 

            try {
                String sqlKH = "INSERT INTO KHACHHANG (HO_TEN, EMAIL, SDT, DIA_CHI) VALUES (?, ?, ?, ?)";
                
                String[] returnId = {"MA_KH"}; 
                PreparedStatement psKH = con.prepareStatement(sqlKH, returnId);
                
                psKH.setString(1, hoten);
                psKH.setString(2, email);
                psKH.setString(3, sdt);
                psKH.setString(4, address);
                psKH.executeUpdate();

                ResultSet rs = psKH.getGeneratedKeys();
                if (rs.next()) {
                    long maKH = rs.getLong(1);

                    String sqlTK = "INSERT INTO TAIKHOAN (MA_KH, USERNAME, PASSWORD_HASH) VALUES (?, ?, ?)";
                    PreparedStatement psTK = con.prepareStatement(sqlTK);
                    
                    psTK.setLong(1, maKH); 
                    psTK.setString(2, username.trim());
                    psTK.setString(3, password.trim());
                    
                    psTK.executeUpdate();

                    con.commit(); 
                    isSuccess = true;
                    
                } else {
                    con.rollback(); 
                }

            } catch (Exception e) {
                con.rollback(); 
                System.out.println("Lỗi quá trình Đăng Ký (Đã Rollback): ");
                e.printStackTrace();
            }
            
        } catch (Exception ex) {
            System.out.println("Lỗi kết nối CSDL: ");
            ex.printStackTrace();
        }
        
        return isSuccess;
    }
}