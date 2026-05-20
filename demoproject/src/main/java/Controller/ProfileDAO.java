package Controller;

import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ProfileDAO {
    
    public static class Profile {
        public String role; // "ADMIN" or "CUSTOMER"
        public long id;
        public String hoTen;
        public String sdt;
        public String email;
        public String diaChi;      // Added for Customer
        public long diemTichLuy;   // Added for Customer
        public String username;    // Username from TAIKHOAN
        
        public Profile(String role, long id, String hoTen, String sdt, String email, String diaChi, long diemTichLuy, String username) {
            this.role = role;
            this.id = id;
            this.hoTen = hoTen;
            this.sdt = sdt;
            this.email = email;
            this.diaChi = diaChi;
            this.diemTichLuy = diemTichLuy;
            this.username = username;
        }
    }

    public static Profile getProfileByToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        String sql = "SELECT tk.MA_NV, tk.MA_KH, tk.USERNAME "
                   + "FROM ACCOUNT_TOKEN atok "
                   + "JOIN TAIKHOAN tk ON atok.MA_TK = tk.MA_TK "
                   + "WHERE atok.TOKEN_VALUE = ? "
                   + "  AND atok.THOI_GIAN_HET_HAN > CURRENT_TIMESTAMP "
                   + "  AND atok.TRANG_THAI = 'Y'";
                   
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
             ps.setString(1, token);
             try (ResultSet rs = ps.executeQuery()) {
                 if (rs.next()) {
                     Object maNVObj = rs.getObject("MA_NV");
                     Object maKHObj = rs.getObject("MA_KH");
                     String username = rs.getString("USERNAME");
                     if (maNVObj != null) {
                         long maNV = ((Number) maNVObj).longValue();
                         String sqlNV = "SELECT HO_TEN, SDT, EMAIL FROM NHANVIEN WHERE MA_NV = ?";
                         try (PreparedStatement psNV = con.prepareStatement(sqlNV)) {
                             psNV.setLong(1, maNV);
                             try (ResultSet rsNV = psNV.executeQuery()) {
                                 if (rsNV.next()) {
                                     return new Profile("ADMIN", maNV, rsNV.getString("HO_TEN"), rsNV.getString("SDT"), rsNV.getString("EMAIL"), null, 0, username);
                                 }
                             }
                         }
                     } else if (maKHObj != null) {
                         long maKH = ((Number) maKHObj).longValue();
                         String sqlKH = "SELECT HO_TEN, SDT, EMAIL, DIA_CHI, DIEM_TICH_LUY FROM KHACHHANG WHERE MA_KH = ?";
                         try (PreparedStatement psKH = con.prepareStatement(sqlKH)) {
                             psKH.setLong(1, maKH);
                             try (ResultSet rsKH = psKH.executeQuery()) {
                                 if (rsKH.next()) {
                                     return new Profile("CUSTOMER", maKH, rsKH.getString("HO_TEN"), rsKH.getString("SDT"), rsKH.getString("EMAIL"), rsKH.getString("DIA_CHI"), rsKH.getLong("DIEM_TICH_LUY"), username);
                                 }
                             }
                         }
                     }
                 }
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return null;
    }

    public static boolean updateProfileByToken(String token, String hoTen, String sdt, String email) {
        return updateProfileByToken(token, hoTen, sdt, email, null);
    }

    public static boolean updateProfileByToken(String token, String hoTen, String sdt, String email, String diaChi) {
        Profile profile = getProfileByToken(token);
        if (profile == null) return false;
        
        try (Connection con = ConnectionUtils.getMyConnection()) {
            if ("ADMIN".equals(profile.role)) {
                String sql = "UPDATE NHANVIEN SET HO_TEN = ?, SDT = ?, EMAIL = ? WHERE MA_NV = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, hoTen);
                    ps.setString(2, sdt);
                    ps.setString(3, email);
                    ps.setLong(4, profile.id);
                    return ps.executeUpdate() > 0;
                }
            } else if ("CUSTOMER".equals(profile.role)) {
                String sql = "UPDATE KHACHHANG SET HO_TEN = ?, SDT = ?, EMAIL = ?, DIA_CHI = ? WHERE MA_KH = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, hoTen);
                    ps.setString(2, sdt);
                    ps.setString(3, email);
                    ps.setString(4, diaChi != null ? diaChi : profile.diaChi);
                    ps.setLong(5, profile.id);
                    return ps.executeUpdate() > 0;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean changePasswordByToken(String token, String currentPassword, String newPassword) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        String sql = "SELECT tk.MA_TK, tk.PASSWORD_HASH "
                   + "FROM ACCOUNT_TOKEN atok "
                   + "JOIN TAIKHOAN tk ON atok.MA_TK = tk.MA_TK "
                   + "WHERE atok.TOKEN_VALUE = ? "
                   + "  AND atok.THOI_GIAN_HET_HAN > CURRENT_TIMESTAMP "
                   + "  AND atok.TRANG_THAI = 'Y'";
        
        try (Connection con = ConnectionUtils.getMyConnection()) {
            long maTk = -1;
            String oldHash = null;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, token);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        maTk = rs.getLong("MA_TK");
                        oldHash = rs.getString("PASSWORD_HASH");
                    }
                }
            }
            
            if (maTk == -1 || oldHash == null) {
                return false;
            }
            
            // Verify current password
            if (!Common.HashUtil.checkPassword(currentPassword.trim(), oldHash)) {
                return false; // Old password doesn't match
            }
            
            // Hash and update new password
            String newHash = Common.HashUtil.hashPassword(newPassword.trim());
            String sqlUpdate = "UPDATE TAIKHOAN SET PASSWORD_HASH = ? WHERE MA_TK = ?";
            try (PreparedStatement psUp = con.prepareStatement(sqlUpdate)) {
                psUp.setString(1, newHash);
                psUp.setLong(2, maTk);
                return psUp.executeUpdate() > 0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
