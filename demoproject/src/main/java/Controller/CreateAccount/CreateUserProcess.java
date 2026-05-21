/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller.CreateAccount;

import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author DELL
 */
public class CreateUserProcess {
    public boolean isCreated(String fullname, String gmail ,String sdt, String address) {
        boolean checkCreated = false;
        try (Connection con = ConnectionUtils.getMyConnection()) {
        
        String SQL = "SELECT * "
                + " FROM KHACH_HANG " 
                + " WHERE (SDT = ? "
                + " OR EMAIL = ?) ";
        
        PreparedStatement ps = con.prepareStatement(SQL); 
        ps.setString(1, sdt);
        ps.setString(2, gmail);
        ResultSet rs = ps.executeQuery();
        System.out.println(rs);

        if (rs.next()) {
            checkCreated = true;
        }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return checkCreated;
    }
}
