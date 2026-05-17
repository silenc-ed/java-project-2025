package Controller.Admin.CreateInvoicePanel;

import ConnectDB.ConnectionUtils;

import Model.KhachHang;

import java.sql.Connection;

import java.sql.PreparedStatement;

import java.sql.ResultSet;



public class CreateInvoiceProcess {

    

    public KhachHang getCustomerByPhone(String phone) throws Exception {

        KhachHang cus = null;

        

        String SQL = "SELECT * FROM KHACHHANG WHERE SDT = ?";

        

        try (Connection con = ConnectionUtils.getMyConnection()) {

            PreparedStatement ps = con.prepareStatement(SQL);

            ps.setString(1, phone.trim());

            

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    // Đã đổi thành getLong() để khớp hoàn toàn với kiểu biến long

                    long maKH = rs.getLong("MA_KH"); 

                    String tenKH = rs.getString("HO_TEN");

                    String sdt = rs.getString("SDT");

                    String diaChi = rs.getString("DIA_CHI");

                    String mail = rs.getString("EMAIL");

                    int diem = rs.getInt("DIEM_TICH_LUY");

                    

                    cus = new KhachHang(maKH, tenKH, sdt, diaChi, mail, diem);

                }

            }

        }

        

        return cus;

    }

    

}

