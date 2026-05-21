/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller.SignIn;

import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author DELL
 */
public class LoginProcess {
    public String loginProcess(String username, String password) {
        String role = "INVALID";
        try (Connection con = ConnectionUtils.getMyConnection()) {

            String SQL = "SELECT MA_TK, PASSWORD_HASH, MA_NV "
                    + " FROM TAI_KHOAN "
                    + " WHERE TRIM(USERNAME) = ? ";

            PreparedStatement ps = con.prepareStatement(SQL);
            ps.setString(1, username.trim());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String hashedPasswordFromDB = rs.getString("PASSWORD_HASH");
                if (Common.HashUtil.checkPassword(password.trim(), hashedPasswordFromDB)) {
                    long maTk = rs.getLong("MA_TK");
                    
                    // Changed to use AuthProcess instead of TokenManager
                    AuthProcess.generateAndSaveToken(maTk);
                    
                    if (rs.getObject("MA_NV") != null) {
                        role = "ADMIN";
                    } else {
                        role = "CUSTOMER";
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }

        System.out.println("Login Role: " + role);
        return role;
    }
}
