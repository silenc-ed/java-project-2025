package Controller.Admin;

import Common.HashUtil;
import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class KhachHangDAO {

    public static List<Object[]> getAllKhachHang() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT kh.MA_KH, kh.HO_TEN, kh.SDT, kh.DIA_CHI, kh.EMAIL, " +
                     "kh.DIEM_TICH_LUY, " +
                     "NVL(tk.TRANG_THAI, 'Chưa có TK') AS TRANG_THAI_TK, " +
                     "tk.USERNAME " +
                     "FROM KHACH_HANG kh " +
                     "LEFT JOIN TAI_KHOAN tk ON kh.MA_KH = tk.MA_KH " +
                     "ORDER BY kh.MA_KH DESC";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getLong("MA_KH"),
                    rs.getString("HO_TEN"),
                    rs.getString("SDT"),
                    rs.getString("DIA_CHI"),
                    rs.getString("EMAIL"),
                    rs.getLong("DIEM_TICH_LUY"),
                    rs.getString("TRANG_THAI_TK"),
                    rs.getString("USERNAME")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Object[] getKhachHangById(long maKh) {
        String sql = "SELECT kh.MA_KH, kh.HO_TEN, kh.SDT, kh.DIA_CHI, kh.EMAIL, " +
                     "kh.DIEM_TICH_LUY, " +
                     "NVL(tk.TRANG_THAI, 'Chưa có TK') AS TRANG_THAI_TK, " +
                     "tk.USERNAME " +
                     "FROM KHACH_HANG kh " +
                     "LEFT JOIN TAI_KHOAN tk ON kh.MA_KH = tk.MA_KH " +
                     "WHERE kh.MA_KH = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, maKh);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getLong("MA_KH"),
                        rs.getString("HO_TEN"),
                        rs.getString("SDT"),
                        rs.getString("DIA_CHI"),
                        rs.getString("EMAIL"),
                        rs.getLong("DIEM_TICH_LUY"),
                        rs.getString("TRANG_THAI_TK"),
                        rs.getString("USERNAME")
                    };
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean themKhachHang(String hoTen, String sdt, String diaChi, String email,
                                         String username, String password) {
        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);

            String sqlKH = "INSERT INTO KHACH_HANG (HO_TEN, SDT, DIA_CHI, EMAIL) VALUES (?, ?, ?, ?)";
            long maKh;
            try (PreparedStatement ps = con.prepareStatement(sqlKH, new String[]{"MA_KH"})) {
                ps.setString(1, hoTen);
                ps.setString(2, sdt);
                ps.setString(3, diaChi);
                ps.setString(4, email);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    maKh = rs.getLong(1);
                }
            }

            if (username != null && !username.trim().isEmpty()) {
                String sqlTK = "INSERT INTO TAI_KHOAN (MA_KH, USERNAME, PASSWORD_HASH, TRANG_THAI) " +
                               "VALUES (?, ?, ?, 'Hoạt động')";
                try (PreparedStatement ps2 = con.prepareStatement(sqlTK)) {
                    ps2.setLong(1, maKh);
                    ps2.setString(2, username.trim());
                    ps2.setString(3, HashUtil.hashPassword(password));
                    ps2.executeUpdate();
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

    public static boolean capNhatKhachHang(long maKh, String hoTen, String sdt, String diaChi, String email) {
        String sql = "UPDATE KHACH_HANG SET HO_TEN = ?, SDT = ?, DIA_CHI = ?, EMAIL = ? WHERE MA_KH = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hoTen);
            ps.setString(2, sdt);
            ps.setString(3, diaChi);
            ps.setString(4, email);
            ps.setLong(5, maKh);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String toggleTrangThaiTK(long maKh) {
        String sqlGet = "SELECT TRANG_THAI FROM TAI_KHOAN WHERE MA_KH = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sqlGet)) {
            ps.setLong(1, maKh);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String current = rs.getString("TRANG_THAI");
                String next = "Hoạt động".equals(current) ? "Bị khóa" : "Hoạt động";
                String sqlUpd = "UPDATE TAI_KHOAN SET TRANG_THAI = ? WHERE MA_KH = ?";
                try (PreparedStatement ps2 = con.prepareStatement(sqlUpd)) {
                    ps2.setString(1, next);
                    ps2.setLong(2, maKh);
                    ps2.executeUpdate();
                }
                return next;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean capNhatTaiKhoan(long maKh, String username, String newPassword) {
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            String sql = "UPDATE TAI_KHOAN SET USERNAME = ?, PASSWORD_HASH = ? WHERE MA_KH = ?";
            try (Connection con = ConnectionUtils.getMyConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, HashUtil.hashPassword(newPassword));
                ps.setLong(3, maKh);
                return ps.executeUpdate() > 0;
            } catch (Exception e) { e.printStackTrace(); }
        } else {
            String sql = "UPDATE TAI_KHOAN SET USERNAME = ? WHERE MA_KH = ?";
            try (Connection con = ConnectionUtils.getMyConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setLong(2, maKh);
                return ps.executeUpdate() > 0;
            } catch (Exception e) { e.printStackTrace(); }
        }
        return false;
    }

    public static List<Object[]> timKiemKhachHang(String keyword) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT kh.MA_KH, kh.HO_TEN, kh.SDT, kh.DIA_CHI, kh.EMAIL, " +
                     "kh.DIEM_TICH_LUY, " +
                     "NVL(tk.TRANG_THAI, 'Chưa có TK') AS TRANG_THAI_TK, " +
                     "tk.USERNAME " +
                     "FROM KHACH_HANG kh " +
                     "LEFT JOIN TAI_KHOAN tk ON kh.MA_KH = tk.MA_KH " +
                     "WHERE LOWER(kh.HO_TEN) LIKE ? OR kh.SDT LIKE ? " +
                     "OR LOWER(kh.EMAIL) LIKE ? OR TO_CHAR(kh.MA_KH) LIKE ? " +
                     "ORDER BY kh.MA_KH DESC";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String kw = "%" + keyword.toLowerCase() + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, kw);
            ps.setString(4, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getLong("MA_KH"),
                        rs.getString("HO_TEN"),
                        rs.getString("SDT"),
                        rs.getString("DIA_CHI"),
                        rs.getString("EMAIL"),
                        rs.getLong("DIEM_TICH_LUY"),
                        rs.getString("TRANG_THAI_TK"),
                        rs.getString("USERNAME")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
