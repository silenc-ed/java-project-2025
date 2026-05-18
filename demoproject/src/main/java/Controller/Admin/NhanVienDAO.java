package Controller.Admin;

import Common.HashUtil;
import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {

    public static List<Object[]> getAllRoleGroups() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT MA_ROLEGRP, TEN_NHOM FROM ROLE_GROUP ORDER BY MA_ROLEGRP";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{ rs.getLong(1), rs.getString(2) });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public static List<Object[]> getAllNhanVien() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT nv.MA_NV, nv.HO_TEN, nv.SDT, nv.EMAIL, nv.CCCD, nv.LUONG_CO_BAN, " +
                     "NVL(tk.TRANG_THAI, 'Chưa có TK') AS TRANG_THAI_TK, " +
                     "tk.USERNAME, rg.TEN_NHOM AS VAI_TRO " +
                     "FROM NHANVIEN nv " +
                     "LEFT JOIN TAIKHOAN tk ON nv.MA_NV = tk.MA_NV " +
                     "LEFT JOIN ACCOUNT_ASSIGN_ROLEGROUP aarg ON tk.MA_TK = aarg.MA_TK " +
                     "LEFT JOIN ROLE_GROUP rg ON aarg.MA_ROLEGRP = rg.MA_ROLEGRP " +
                     "ORDER BY nv.MA_NV DESC";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getLong("MA_NV"),
                    rs.getString("HO_TEN"),
                    rs.getString("SDT"),
                    rs.getString("EMAIL"),
                    rs.getString("CCCD"),
                    rs.getLong("LUONG_CO_BAN"),
                    rs.getString("TRANG_THAI_TK"),
                    rs.getString("USERNAME"),
                    rs.getString("VAI_TRO") == null ? "Chưa cấp vai trò" : rs.getString("VAI_TRO")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public static Object[] getNhanVienById(long maNv) {
        String sql = "SELECT nv.MA_NV, nv.HO_TEN, nv.SDT, nv.EMAIL, nv.CCCD, nv.LUONG_CO_BAN, " +
                     "NVL(tk.TRANG_THAI, 'Chưa có TK') AS TRANG_THAI_TK, " +
                     "tk.USERNAME, rg.MA_ROLEGRP " +
                     "FROM NHANVIEN nv " +
                     "LEFT JOIN TAIKHOAN tk ON nv.MA_NV = tk.MA_NV " +
                     "LEFT JOIN ACCOUNT_ASSIGN_ROLEGROUP aarg ON tk.MA_TK = aarg.MA_TK " +
                     "LEFT JOIN ROLE_GROUP rg ON aarg.MA_ROLEGRP = rg.MA_ROLEGRP " +
                     "WHERE nv.MA_NV = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, maNv);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getLong("MA_NV"),
                        rs.getString("HO_TEN"),
                        rs.getString("SDT"),
                        rs.getString("EMAIL"),
                        rs.getString("CCCD"),
                        rs.getLong("LUONG_CO_BAN"),
                        rs.getString("TRANG_THAI_TK"),
                        rs.getString("USERNAME"),
                        rs.getLong("MA_ROLEGRP")
                    };
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public static boolean themNhanVien(String hoTen, String sdt, String email, String cccd, long luongCB,
                                       String username, String password, Long maRoleGrp) {
        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);

            // Tạm thời MA_CN fix cứng = 1 vì database yêu cầu MA_CN NOT NULL
            String sqlNV = "INSERT INTO NHANVIEN (MA_CN, HO_TEN, SDT, EMAIL, CCCD, LUONG_CO_BAN, TRANG_THAI) VALUES (1, ?, ?, ?, ?, ?, 'Đang làm việc')";
            long maNv;
            try (PreparedStatement ps = con.prepareStatement(sqlNV, new String[]{"MA_NV"})) {
                ps.setString(1, hoTen);
                ps.setString(2, sdt);
                ps.setString(3, email);
                ps.setString(4, cccd);
                ps.setLong(5, luongCB);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    maNv = rs.getLong(1);
                }
            }

            if (username != null && !username.trim().isEmpty()) {
                String sqlTK = "INSERT INTO TAIKHOAN (MA_NV, USERNAME, PASSWORD_HASH, TRANG_THAI) VALUES (?, ?, ?, 'Hoạt động')";
                long maTk;
                try (PreparedStatement ps2 = con.prepareStatement(sqlTK, new String[]{"MA_TK"})) {
                    ps2.setLong(1, maNv);
                    ps2.setString(2, username.trim());
                    ps2.setString(3, HashUtil.hashPassword(password));
                    ps2.executeUpdate();
                    try (ResultSet rs = ps2.getGeneratedKeys()) {
                        rs.next();
                        maTk = rs.getLong(1);
                    }
                }
                if (maRoleGrp != null) {
                    String sqlRole = "INSERT INTO ACCOUNT_ASSIGN_ROLEGROUP (MA_TK, MA_ROLEGRP) VALUES (?, ?)";
                    try (PreparedStatement psRole = con.prepareStatement(sqlRole)) {
                        psRole.setLong(1, maTk);
                        psRole.setLong(2, maRoleGrp);
                        psRole.executeUpdate();
                    }
                }
            }

            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try { if (con != null) con.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (Exception ex) { ex.printStackTrace(); }
        }
        return false;
    }

    public static boolean capNhatNhanVien(long maNv, String hoTen, String sdt, String email, String cccd, long luongCB) {
        String sql = "UPDATE NHANVIEN SET HO_TEN = ?, SDT = ?, EMAIL = ?, CCCD = ?, LUONG_CO_BAN = ? WHERE MA_NV = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hoTen);
            ps.setString(2, sdt);
            ps.setString(3, email);
            ps.setString(4, cccd);
            ps.setLong(5, luongCB);
            ps.setLong(6, maNv);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public static String toggleTrangThaiTK(long maNv) {
        String sqlGet = "SELECT TRANG_THAI FROM TAIKHOAN WHERE MA_NV = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sqlGet)) {
            ps.setLong(1, maNv);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String current = rs.getString("TRANG_THAI");
                String next = "Hoạt động".equals(current) ? "Bị khóa" : "Hoạt động";
                String sqlUpd = "UPDATE TAIKHOAN SET TRANG_THAI = ? WHERE MA_NV = ?";
                try (PreparedStatement ps2 = con.prepareStatement(sqlUpd)) {
                    ps2.setString(1, next);
                    ps2.setLong(2, maNv);
                    ps2.executeUpdate();
                }
                return next;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public static boolean capNhatTaiKhoan(long maNv, String username, String newPassword, Long maRoleGrp) {
        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);
            
            // Lấy MA_TK của NV
            long maTk = -1;
            String checkTK = "SELECT MA_TK FROM TAIKHOAN WHERE MA_NV = ?";
            try (PreparedStatement ps = con.prepareStatement(checkTK)) {
                ps.setLong(1, maNv);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) maTk = rs.getLong(1);
                }
            }

            if (maTk != -1) {
                // Đã có TK -> UPDATE
                boolean hasPassword = (newPassword != null && !newPassword.trim().isEmpty());
                String updateSql = hasPassword 
                    ? "UPDATE TAIKHOAN SET USERNAME = ?, PASSWORD_HASH = ? WHERE MA_NV = ?"
                    : "UPDATE TAIKHOAN SET USERNAME = ? WHERE MA_NV = ?";
                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setString(1, username);
                    if (hasPassword) {
                        ps.setString(2, HashUtil.hashPassword(newPassword));
                        ps.setLong(3, maNv);
                    } else {
                        ps.setLong(2, maNv);
                    }
                    ps.executeUpdate();
                }
            } else {
                // Chưa có TK -> INSERT
                String insertSql = "INSERT INTO TAIKHOAN (MA_NV, USERNAME, PASSWORD_HASH, TRANG_THAI) VALUES (?, ?, ?, 'Hoạt động')";
                try (PreparedStatement ps2 = con.prepareStatement(insertSql, new String[]{"MA_TK"})) {
                    ps2.setLong(1, maNv);
                    ps2.setString(2, username);
                    ps2.setString(3, HashUtil.hashPassword(newPassword));
                    ps2.executeUpdate();
                    try (ResultSet rs = ps2.getGeneratedKeys()) {
                        if (rs.next()) maTk = rs.getLong(1);
                    }
                }
            }

            // Cập nhật ROLE_GROUP cho MA_TK
            if (maTk != -1 && maRoleGrp != null) {
                // Xóa role group cũ nếu có
                String delRole = "DELETE FROM ACCOUNT_ASSIGN_ROLEGROUP WHERE MA_TK = ?";
                try (PreparedStatement psDel = con.prepareStatement(delRole)) {
                    psDel.setLong(1, maTk);
                    psDel.executeUpdate();
                }
                // Thêm role group mới
                String insRole = "INSERT INTO ACCOUNT_ASSIGN_ROLEGROUP (MA_TK, MA_ROLEGRP) VALUES (?, ?)";
                try (PreparedStatement psIns = con.prepareStatement(insRole)) {
                    psIns.setLong(1, maTk);
                    psIns.setLong(2, maRoleGrp);
                    psIns.executeUpdate();
                }
            }

            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try { if (con != null) con.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (Exception ex) { ex.printStackTrace(); }
        }
        return false;
    }

    public static List<Object[]> timKiemNhanVien(String keyword) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT nv.MA_NV, nv.HO_TEN, nv.SDT, nv.EMAIL, nv.CCCD, nv.LUONG_CO_BAN, " +
                     "NVL(tk.TRANG_THAI, 'Chưa có TK') AS TRANG_THAI_TK, " +
                     "tk.USERNAME, rg.TEN_NHOM AS VAI_TRO " +
                     "FROM NHANVIEN nv " +
                     "LEFT JOIN TAIKHOAN tk ON nv.MA_NV = tk.MA_NV " +
                     "LEFT JOIN ACCOUNT_ASSIGN_ROLEGROUP aarg ON tk.MA_TK = aarg.MA_TK " +
                     "LEFT JOIN ROLE_GROUP rg ON aarg.MA_ROLEGRP = rg.MA_ROLEGRP " +
                     "WHERE LOWER(nv.HO_TEN) LIKE ? OR nv.SDT LIKE ? OR nv.CCCD LIKE ? " +
                     "OR LOWER(nv.EMAIL) LIKE ? OR TO_CHAR(nv.MA_NV) LIKE ? " +
                     "ORDER BY nv.MA_NV DESC";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String kw = "%" + keyword.toLowerCase() + "%";
            for(int i = 1; i <= 5; i++) ps.setString(i, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getLong("MA_NV"),
                        rs.getString("HO_TEN"),
                        rs.getString("SDT"),
                        rs.getString("EMAIL"),
                        rs.getString("CCCD"),
                        rs.getLong("LUONG_CO_BAN"),
                        rs.getString("TRANG_THAI_TK"),
                        rs.getString("USERNAME"),
                        rs.getString("VAI_TRO") == null ? "Chưa cấp vai trò" : rs.getString("VAI_TRO")
                    });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}
