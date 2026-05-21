package Controller.ForgottedPassword;

import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ForgottedPasswordProcess {

    /**
     * Tìm kiếm tài khoản thông qua USERNAME hoặc EMAIL
     * @param input Có thể là Username hoặc Email
     * @return Mảng chứa [Email, Username] nếu tìm thấy, ngược lại trả về null
     */
    public String[] findAccountAndGetEmail(String input) {
        String[] result = null;
        try (Connection con = ConnectionUtils.getMyConnection()) {
            // Join giữa TAIKHOAN và KHACHHANG
            // Hiện tại chỉ hỗ trợ lấy email của khách hàng.
            String sql = "SELECT KH.EMAIL, TK.USERNAME "
                       + "FROM TAI_KHOAN TK "
                       + "JOIN KHACH_HANG KH ON TK.MA_KH = KH.MA_KH "
                       + "WHERE TK.USERNAME = ? OR KH.EMAIL = ?";
            
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, input.trim());
            ps.setString(2, input.trim());
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = new String[2];
                result[0] = rs.getString("EMAIL");
                result[1] = rs.getString("USERNAME");
            }
        } catch (Exception e) {
            System.out.println("Lỗi quá trình tìm kiếm tài khoản (Quên mật khẩu):");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Cập nhật mật khẩu mới cho tài khoản (đã mã hóa bằng BCrypt)
     * @param username Tên đăng nhập
     * @param newPassword Mật khẩu mới chưa mã hóa
     * @return true nếu thành công
     */
    public boolean resetPassword(String username, String newPassword) {
        boolean isSuccess = false;
        try (Connection con = ConnectionUtils.getMyConnection()) {
            String sql = "UPDATE TAI_KHOAN SET PASSWORD_HASH = ? WHERE USERNAME = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            
            String hashedPassword = Common.HashUtil.hashPassword(newPassword.trim());
            ps.setString(1, hashedPassword);
            ps.setString(2, username.trim());
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                isSuccess = true;
            }
        } catch (Exception e) {
            System.out.println("Lỗi cập nhật mật khẩu mới:");
            e.printStackTrace();
        }
        return isSuccess;
    }
}
