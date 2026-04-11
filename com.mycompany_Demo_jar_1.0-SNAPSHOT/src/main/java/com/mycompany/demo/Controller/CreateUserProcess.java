/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.demo.Controller;

import com.mycompany.demo.ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author DELL
 */
public class CreateUserProcess {
    public boolean checkInfoProcess(String email, String number) {
        // Chỉ lấy PASSWORD_HASH dựa trên USERNAME
        String SQL = "SELECT 1 FROM KHACHHANG WHERE TRIM(EMAIL) = ? OR TRIM(SDT) = ?";

        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(SQL)) {

            ps.setString(1, email);
            ps.setString(2, number);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return false;
                }
            }
        } catch (Exception ex) {
            System.err.println("Lỗi truy vấn thông tin: " + ex.getMessage());
            ex.printStackTrace();
        }
        return true;
    }
    
}
