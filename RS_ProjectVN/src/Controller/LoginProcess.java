/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author DELL
 */
public class LoginProcess {    
    public boolean loginProcess(String username, String password) {
        boolean isSucess = false;
        try (Connection con = ConnectionUtils.getMyConnection()) {

            String SQL = "SELECT * "
                    + " FROM TAIKHOAN "
                    + " WHERE TRIM(USERNAME) = ? " 
                    + " AND TRIM(PASSWORD_HASH) = ? ";
            
            PreparedStatement ps = con.prepareStatement(SQL);
            ps.setString(1, username.trim());  
            ps.setString(2, password.trim());
            ResultSet rs = ps.executeQuery();
            System.out.println(rs);
            
            if (rs.next()) {
                isSucess = true;
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        
        System.out.println(isSucess);
        return isSucess;
    }
}
