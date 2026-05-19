package Controller.Admin;

import Common.HashUtil;
import ConnectDB.ConnectionUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {

    // ─── Lấy danh sách chi nhánh cho ComboBox ────────────────────────
    public static List<Object[]> getAllChiNhanh() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT MA_CN, TEN_CN FROM CHINHANH WHERE TRANG_THAI = N'Đang hoạt động' ORDER BY MA_CN";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{rs.getLong("MA_CN"), rs.getString("TEN_CN")});
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ─── Lấy tất cả nhân viên ────────────────────────────────────────
    // Trả về: {MA_NV, HO_TEN, NGAY_SINH, CCCD, SDT, EMAIL, LUONG_CO_BAN,
    //          NGAY_VAO_LAM, TRANG_THAI, MA_CN, TEN_CN,
    //          TRANG_THAI_TK, USERNAME, TEN_NHOM}
    public static List<Object[]> getAllNhanVien() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT nv.MA_NV, nv.HO_TEN, nv.NGAY_SINH, nv.CCCD, nv.SDT, nv.EMAIL, " +
                     "nv.LUONG_CO_BAN, nv.NGAY_VAO_LAM, nv.TRANG_THAI, " +
                     "nv.MA_CN, cn.TEN_CN, " +
                     "NVL(tk.TRANG_THAI, N'Chưa có TK') AS TRANG_THAI_TK, " +
                     "tk.USERNAME, " +
                     "(SELECT LISTAGG(rg.TEN_NHOM, ', ') WITHIN GROUP (ORDER BY rg.TEN_NHOM) " +
                     " FROM ACCOUNT_ASSIGN_ROLEGROUP aarg " +
                     " JOIN ROLE_GROUP rg ON aarg.MA_ROLEGRP = rg.MA_ROLEGRP " +
                     " WHERE aarg.MA_TK = tk.MA_TK) AS TEN_NHOM " +
                     "FROM NHANVIEN nv " +
                     "JOIN CHINHANH cn ON nv.MA_CN = cn.MA_CN " +
                     "LEFT JOIN TAIKHOAN tk ON nv.MA_NV = tk.MA_NV " +
                     "ORDER BY nv.MA_NV DESC";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getLong("MA_NV"),       // 0
                    rs.getString("HO_TEN"),    // 1
                    rs.getDate("NGAY_SINH"),   // 2
                    rs.getString("CCCD"),       // 3
                    rs.getString("SDT"),        // 4
                    rs.getString("EMAIL"),      // 5
                    rs.getLong("LUONG_CO_BAN"), // 6
                    rs.getDate("NGAY_VAO_LAM"),// 7
                    rs.getString("TRANG_THAI"), // 8
                    rs.getLong("MA_CN"),        // 9
                    rs.getString("TEN_CN"),     // 10
                    rs.getString("TRANG_THAI_TK"), // 11
                    rs.getString("USERNAME"),   // 12
                    rs.getString("TEN_NHOM")   // 13
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ─── Tìm kiếm nhân viên ──────────────────────────────────────────
    public static List<Object[]> timKiemNhanVien(String kw) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT nv.MA_NV, nv.HO_TEN, nv.NGAY_SINH, nv.CCCD, nv.SDT, nv.EMAIL, " +
                     "nv.LUONG_CO_BAN, nv.NGAY_VAO_LAM, nv.TRANG_THAI, " +
                     "nv.MA_CN, cn.TEN_CN, " +
                     "NVL(tk.TRANG_THAI, N'Chưa có TK') AS TRANG_THAI_TK, " +
                     "tk.USERNAME, " +
                     "(SELECT LISTAGG(rg.TEN_NHOM, ', ') WITHIN GROUP (ORDER BY rg.TEN_NHOM) " +
                     " FROM ACCOUNT_ASSIGN_ROLEGROUP aarg " +
                     " JOIN ROLE_GROUP rg ON aarg.MA_ROLEGRP = rg.MA_ROLEGRP " +
                     " WHERE aarg.MA_TK = tk.MA_TK) AS TEN_NHOM " +
                     "FROM NHANVIEN nv " +
                     "JOIN CHINHANH cn ON nv.MA_CN = cn.MA_CN " +
                     "LEFT JOIN TAIKHOAN tk ON nv.MA_NV = tk.MA_NV " +
                     "WHERE CAST(nv.MA_NV AS VARCHAR2(20)) LIKE ? " +
                     "OR LOWER(nv.HO_TEN) LIKE ? OR nv.SDT LIKE ? " +
                     "OR LOWER(nv.EMAIL) LIKE ? OR nv.CCCD LIKE ? " +
                     "ORDER BY nv.MA_NV DESC";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String pKw = "%" + kw.toLowerCase() + "%";
            ps.setString(1, pKw); ps.setString(2, pKw);
            ps.setString(3, pKw); ps.setString(4, pKw);
            ps.setString(5, pKw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getLong("MA_NV"),       // 0
                        rs.getString("HO_TEN"),    // 1
                        rs.getDate("NGAY_SINH"),   // 2
                        rs.getString("CCCD"),       // 3
                        rs.getString("SDT"),        // 4
                        rs.getString("EMAIL"),      // 5
                        rs.getLong("LUONG_CO_BAN"), // 6
                        rs.getDate("NGAY_VAO_LAM"),// 7
                        rs.getString("TRANG_THAI"), // 8
                        rs.getLong("MA_CN"),        // 9
                        rs.getString("TEN_CN"),     // 10
                        rs.getString("TRANG_THAI_TK"), // 11
                        rs.getString("USERNAME"),   // 12
                        rs.getString("TEN_NHOM")   // 13
                    });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ─── Lấy nhân viên theo ID ───────────────────────────────────────
    public static Object[] getNhanVienById(long maNV) {
        String sql = "SELECT nv.MA_NV, nv.HO_TEN, nv.NGAY_SINH, nv.CCCD, nv.SDT, nv.EMAIL, " +
                     "nv.LUONG_CO_BAN, nv.NGAY_VAO_LAM, nv.TRANG_THAI, " +
                     "nv.MA_CN, cn.TEN_CN, " +
                     "NVL(tk.TRANG_THAI, N'Chưa có TK') AS TRANG_THAI_TK, " +
                     "tk.USERNAME, " +
                     "(SELECT LISTAGG(rg.TEN_NHOM, ', ') WITHIN GROUP (ORDER BY rg.TEN_NHOM) " +
                     " FROM ACCOUNT_ASSIGN_ROLEGROUP aarg " +
                     " JOIN ROLE_GROUP rg ON aarg.MA_ROLEGRP = rg.MA_ROLEGRP " +
                     " WHERE aarg.MA_TK = tk.MA_TK) AS TEN_NHOM " +
                     "FROM NHANVIEN nv " +
                     "JOIN CHINHANH cn ON nv.MA_CN = cn.MA_CN " +
                     "LEFT JOIN TAIKHOAN tk ON nv.MA_NV = tk.MA_NV " +
                     "WHERE nv.MA_NV = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getLong("MA_NV"),       // 0
                        rs.getString("HO_TEN"),    // 1
                        rs.getDate("NGAY_SINH"),   // 2
                        rs.getString("CCCD"),       // 3
                        rs.getString("SDT"),        // 4
                        rs.getString("EMAIL"),      // 5
                        rs.getLong("LUONG_CO_BAN"), // 6
                        rs.getDate("NGAY_VAO_LAM"),// 7
                        rs.getString("TRANG_THAI"), // 8
                        rs.getLong("MA_CN"),        // 9
                        rs.getString("TEN_CN"),     // 10
                        rs.getString("TRANG_THAI_TK"), // 11
                        rs.getString("USERNAME"),   // 12
                        rs.getString("TEN_NHOM")   // 13
                    };
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ─── Thêm nhân viên ──────────────────────────────────────────────
    public static boolean themNhanVien(long maCN, String hoTen, java.sql.Date ngaySinh,
                                        String cccd, String sdt, String email,
                                        long luongCoBan, java.sql.Date ngayVaoLam,
                                        String trangThai,
                                        String username, String pass) {
        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);

            String sqlNV = "INSERT INTO NHANVIEN (MA_CN, HO_TEN, NGAY_SINH, CCCD, SDT, EMAIL, LUONG_CO_BAN, NGAY_VAO_LAM, TRANG_THAI) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            long maNV = -1;
            try (PreparedStatement ps = con.prepareStatement(sqlNV, new String[]{"MA_NV"})) {
                ps.setLong(1, maCN);
                ps.setString(2, hoTen);
                if (ngaySinh != null) ps.setDate(3, ngaySinh); else ps.setNull(3, Types.DATE);
                ps.setString(4, cccd);
                ps.setString(5, sdt);
                ps.setString(6, email);
                ps.setLong(7, luongCoBan);
                if (ngayVaoLam != null) ps.setDate(8, ngayVaoLam); else ps.setNull(8, Types.DATE);
                ps.setString(9, trangThai);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) maNV = rs.getLong(1);
                }
            }

            if (maNV != -1 && username != null && !username.trim().isEmpty()
                    && pass != null && !pass.trim().isEmpty()) {
                String sqlTK = "INSERT INTO TAIKHOAN (MA_NV, USERNAME, PASSWORD_HASH, TRANG_THAI) " +
                               "VALUES (?, ?, ?, N'Hoạt động')";
                try (PreparedStatement ps = con.prepareStatement(sqlTK)) {
                    ps.setLong(1, maNV);
                    ps.setString(2, username);
                    ps.setString(3, HashUtil.hashPassword(pass));
                    ps.executeUpdate();
                }
            }
            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (con != null) try { con.rollback(); } catch (Exception ex) {}
            return false;
        } finally {
            if (con != null) try { con.setAutoCommit(true); con.close(); } catch (Exception ex) {}
        }
    }

    // ─── Cập nhật nhân viên ──────────────────────────────────────────
    public static boolean capNhatNhanVien(long maNV, long maCN, String hoTen,
                                           java.sql.Date ngaySinh, String cccd,
                                           String sdt, String email,
                                           long luongCoBan, java.sql.Date ngayVaoLam,
                                           String trangThai) {
        String sql = "UPDATE NHANVIEN SET MA_CN=?, HO_TEN=?, NGAY_SINH=?, CCCD=?, SDT=?, EMAIL=?, " +
                     "LUONG_CO_BAN=?, NGAY_VAO_LAM=?, TRANG_THAI=? WHERE MA_NV=?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, maCN);
            ps.setString(2, hoTen);
            if (ngaySinh != null) ps.setDate(3, ngaySinh); else ps.setNull(3, Types.DATE);
            ps.setString(4, cccd);
            ps.setString(5, sdt);
            ps.setString(6, email);
            ps.setLong(7, luongCoBan);
            if (ngayVaoLam != null) ps.setDate(8, ngayVaoLam); else ps.setNull(8, Types.DATE);
            ps.setString(9, trangThai);
            ps.setLong(10, maNV);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ─── Xóa nhân viên ──────────────────────────────────────────────
    public static boolean xoaNhanVien(long maNV) {
        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);

            // Xóa role group assignments nếu có tài khoản
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM ACCOUNT_ASSIGN_ROLEGROUP WHERE MA_TK IN (SELECT MA_TK FROM TAIKHOAN WHERE MA_NV = ?)")) {
                ps.setLong(1, maNV);
                ps.executeUpdate();
            }
            // Xóa role assignments nếu có
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM ACCOUNT_ASSIGN_ROLE WHERE MA_TK IN (SELECT MA_TK FROM TAIKHOAN WHERE MA_NV = ?)")) {
                ps.setLong(1, maNV);
                ps.executeUpdate();
            }
            // Xóa tài khoản
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM TAIKHOAN WHERE MA_NV = ?")) {
                ps.setLong(1, maNV);
                ps.executeUpdate();
            }
            // Xóa nhân viên
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM NHANVIEN WHERE MA_NV = ?")) {
                ps.setLong(1, maNV);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (con != null) try { con.rollback(); } catch (Exception ex) {}
            return false;
        } finally {
            if (con != null) try { con.setAutoCommit(true); con.close(); } catch (Exception ex) {}
        }
    }

    // ─── Toggle trạng thái tài khoản ─────────────────────────────────
    public static String toggleTrangThaiTK(long maNV) {
        String sqlGet = "SELECT TRANG_THAI FROM TAIKHOAN WHERE MA_NV = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps1 = con.prepareStatement(sqlGet)) {
            ps1.setLong(1, maNV);
            try (ResultSet rs = ps1.executeQuery()) {
                if (rs.next()) {
                    String current = rs.getString("TRANG_THAI");
                    String next = "Hoạt động".equals(current) ? "Bị khóa" : "Hoạt động";
                    try (PreparedStatement ps2 = con.prepareStatement("UPDATE TAIKHOAN SET TRANG_THAI = ? WHERE MA_NV = ?")) {
                        ps2.setString(1, next);
                        ps2.setLong(2, maNV);
                        ps2.executeUpdate();
                    }
                    return next;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ─── Cập nhật tài khoản ──────────────────────────────────────────
    public static boolean capNhatTaiKhoan(long maNV, String username, String newPass) {
        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);

            String checkSql = "SELECT MA_TK FROM TAIKHOAN WHERE MA_NV = ?";
            boolean hasTk = false;
            try (PreparedStatement ps = con.prepareStatement(checkSql)) {
                ps.setLong(1, maNV);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) hasTk = true; }
            }

            if (!hasTk) {
                if (username != null && !username.trim().isEmpty()
                        && newPass != null && !newPass.trim().isEmpty()) {
                    String sqlTK = "INSERT INTO TAIKHOAN (MA_NV, USERNAME, PASSWORD_HASH, TRANG_THAI) " +
                                   "VALUES (?, ?, ?, N'Hoạt động')";
                    try (PreparedStatement ps = con.prepareStatement(sqlTK)) {
                        ps.setLong(1, maNV);
                        ps.setString(2, username);
                        ps.setString(3, HashUtil.hashPassword(newPass));
                        ps.executeUpdate();
                    }
                }
            } else {
                if (newPass != null && !newPass.trim().isEmpty()) {
                    String sql = "UPDATE TAIKHOAN SET USERNAME = ?, PASSWORD_HASH = ? WHERE MA_NV = ?";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, username);
                        ps.setString(2, HashUtil.hashPassword(newPass));
                        ps.setLong(3, maNV);
                        ps.executeUpdate();
                    }
                } else if (username != null && !username.trim().isEmpty()) {
                    String sql = "UPDATE TAIKHOAN SET USERNAME = ? WHERE MA_NV = ?";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, username);
                        ps.setLong(2, maNV);
                        ps.executeUpdate();
                    }
                }
            }
            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (con != null) try { con.rollback(); } catch (Exception ex) {}
            return false;
        } finally {
            if (con != null) try { con.setAutoCommit(true); con.close(); } catch (Exception ex) {}
        }
    }
}
