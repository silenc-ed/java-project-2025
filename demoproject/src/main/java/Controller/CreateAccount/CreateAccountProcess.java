package Controller.CreateAccount;

import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CreateAccountProcess {
    
    public boolean createAccount(String hoTen, String email, String sdt, String address, String username, String password) {
        boolean isSuccess = false;
        
        try (Connection con = ConnectionUtils.getMyConnection()) {
            con.setAutoCommit(false); 

            try {
                String sqlKH = "INSERT INTO KHACHHANG (HO_TEN, EMAIL, SDT, DIA_CHI) VALUES (?, ?, ?, ?)";
                
                String[] returnId = {"MA_KH"}; 
                PreparedStatement psKH = con.prepareStatement(sqlKH, returnId);
                
                psKH.setString(1, hoTen);
                psKH.setString(2, email);
                psKH.setString(3, sdt);
                psKH.setString(4, address);
                psKH.executeUpdate();

                ResultSet rs = psKH.getGeneratedKeys();
                if (rs.next()) {
                    long maKH = rs.getLong(1);

                    String sqlTK = "INSERT INTO TAIKHOAN (MA_KH, USERNAME, PASSWORD_HASH, TRANG_THAI) VALUES (?, ?, ?, 'Đã kích hoạt')";
                    PreparedStatement psTK = con.prepareStatement(sqlTK);
                    
                    psTK.setLong(1, maKH); 
                    psTK.setString(2, username.trim());
                    String hashedPassword = Common.HashUtil.hashPassword(password.trim());
                    psTK.setString(3, hashedPassword);
                    
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

    public boolean checkUsernameExists(String username) {
        boolean exists = false;
        try (Connection con = ConnectionUtils.getMyConnection()) {
            String sql = "SELECT USERNAME FROM TAIKHOAN WHERE USERNAME = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                exists = true;
            }
        } catch (Exception e) {
            System.out.println("Lỗi kiểm tra Username: ");
            e.printStackTrace();
        }
        return exists;
    }
}
