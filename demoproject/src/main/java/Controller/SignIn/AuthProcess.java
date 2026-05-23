package Controller.SignIn;

import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public class AuthProcess {

    // 1. Generate and save token to DB, then save locally via TokenManager
    public static void generateAndSaveToken(long maTk) {
        String tokenValue = UUID.randomUUID().toString();

        try (Connection con = ConnectionUtils.getMyConnection()) {
            // Invalidate old tokens for this user first
            String invalidateSQL = "UPDATE ACCOUNT_TOKEN SET TRANG_THAI = 'N' WHERE MA_TK = ?";
            try (PreparedStatement psInv = con.prepareStatement(invalidateSQL)) {
                psInv.setLong(1, maTk);
                psInv.executeUpdate();
            }

            // Insert new token
            String SQL = "INSERT INTO ACCOUNT_TOKEN (MA_TK, TOKEN_VALUE, THOI_GIAN_HET_HAN, TRANG_THAI) "
                       + "VALUES (?, ?, CURRENT_TIMESTAMP + INTERVAL '7' DAY, 'Y')";
            try (PreparedStatement ps = con.prepareStatement(SQL)) {
                ps.setLong(1, maTk);
                ps.setString(2, tokenValue);
                ps.executeUpdate();
            }

            // Save token locally
            Common.TokenManager.saveLocalToken(tokenValue);

        } catch (Exception ex) {
            System.err.println("Error saving token to DB: " + ex.getMessage());
        }
    }

    // 2. Validate token against DB and return Role
    public static String validateToken() {
        String tokenValue = Common.TokenManager.getLocalToken();

        if (tokenValue == null || tokenValue.isEmpty()) {
            return "INVALID";
        }

        try (Connection con = ConnectionUtils.getMyConnection()) {
            // Join with TAIKHOAN to determine Role based on MA_NV
            String SQL = "SELECT tk.MA_NV "
                       + "FROM ACCOUNT_TOKEN atok "
                       + "JOIN TAI_KHOAN tk ON atok.MA_TK = tk.MA_TK "
                       + "WHERE atok.TOKEN_VALUE = ? "
                       + "  AND atok.THOI_GIAN_HET_HAN > CURRENT_TIMESTAMP "
                       + "  AND atok.TRANG_THAI = 'Y'";

            try (PreparedStatement ps = con.prepareStatement(SQL)) {
                ps.setString(1, tokenValue);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // Valid token found! Check role
                        if (rs.getObject("MA_NV") != null) {
                            return "ADMIN";
                        } else {
                            return "CUSTOMER";
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Error validating token in DB: " + ex.getMessage());
        }

        return "INVALID";
    }

    // 3. Revoke Token in DB (Logout)
    public static void revokeToken() {
        String tokenValue = Common.TokenManager.getLocalToken();

        if (tokenValue != null && !tokenValue.isEmpty()) {
            try (Connection con = ConnectionUtils.getMyConnection()) {
                String SQL = "UPDATE ACCOUNT_TOKEN SET TRANG_THAI = 'N' WHERE TOKEN_VALUE = ?";
                try (PreparedStatement ps = con.prepareStatement(SQL)) {
                    ps.setString(1, tokenValue);
                    ps.executeUpdate();
                }
            } catch (Exception ex) {
                System.err.println("Error revoking token in DB: " + ex.getMessage());
            }
        }
        
        // Clear permission cache khi đăng xuất
        Controller.Admin.PermissionService.clearCache();

        // Remove locally
        Common.TokenManager.clearLocalToken();
    }

    // 4. Get Full Name from Token via DB
    public static String getFullNameFromToken() {
        String tokenValue = Common.TokenManager.getLocalToken();

        if (tokenValue == null || tokenValue.isEmpty()) {
            return "User";
        }

        try (Connection con = ConnectionUtils.getMyConnection()) {
            String SQL = "SELECT tk.MA_NV, tk.MA_KH "
                       + "FROM ACCOUNT_TOKEN atok "
                       + "JOIN TAI_KHOAN tk ON atok.MA_TK = tk.MA_TK "
                       + "WHERE atok.TOKEN_VALUE = ? "
                       + "  AND atok.THOI_GIAN_HET_HAN > CURRENT_TIMESTAMP "
                       + "  AND atok.TRANG_THAI = 'Y'";

            try (PreparedStatement ps = con.prepareStatement(SQL)) {
                ps.setString(1, tokenValue);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Object maNV = rs.getObject("MA_NV");
                        Object maKH = rs.getObject("MA_KH");

                        if (maNV != null) {
                            String sqlNV = "SELECT * FROM NHAN_VIEN WHERE MA_NV = ?";
                            try (PreparedStatement psNV = con.prepareStatement(sqlNV)) {
                                psNV.setObject(1, maNV);
                                try (ResultSet rsNV = psNV.executeQuery()) {
                                    if (rsNV.next()) {
                                        java.sql.ResultSetMetaData metaData = rsNV.getMetaData();
                                        int columnCount = metaData.getColumnCount();
                                        for (int i = 1; i <= columnCount; i++) {
                                            String colName = metaData.getColumnName(i).toUpperCase();
                                            if (colName.contains("TEN") || colName.contains("NAME")) {
                                                return rsNV.getString(i);
                                            }
                                        }
                                        return "Admin";
                                    }
                                }
                            }
                        } else if (maKH != null) {
                            String sqlKH = "SELECT * FROM KHACH_HANG WHERE MA_KH = ?";
                            try (PreparedStatement psKH = con.prepareStatement(sqlKH)) {
                                psKH.setObject(1, maKH);
                                try (ResultSet rsKH = psKH.executeQuery()) {
                                    if (rsKH.next()) {
                                        java.sql.ResultSetMetaData metaData = rsKH.getMetaData();
                                        int columnCount = metaData.getColumnCount();
                                        for (int i = 1; i <= columnCount; i++) {
                                            String colName = metaData.getColumnName(i).toUpperCase();
                                            if (colName.contains("TEN") || colName.contains("NAME")) {
                                                return rsKH.getString(i);
                                            }
                                        }
                                        return "Customer";
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Error getting full name from DB: " + ex.getMessage());
        }

        return "User";
    }
}
