package Controller;

import ConnectDB.ConnectionUtils;
import Model.DichVu;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DichVuDAO {

    /**
     * Tự động nhận diện tên bảng (DICH_VU hoặc DICHVU) để tránh lỗi.
     */
    private static String resolveTableName(Connection con) {
        String[] candidates = {"DICHVU", "DICH_VU"};
        for (String tbl : candidates) {
            try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM " + tbl + " WHERE ROWNUM = 1");
                 ResultSet rs = ps.executeQuery()) {
                return tbl;
            } catch (SQLException e) {
                if (e.getErrorCode() == 942) continue; // Bảng không tồn tại, thử tên tiếp theo
                return tbl;
            }
        }
        return "DICHVU"; // Mặc định nếu không tìm thấy
    }

    public static List<DichVu> getAllDichVu() {
        List<DichVu> list = new ArrayList<>();
        try (Connection con = ConnectionUtils.getMyConnection()) {
            String tbl = resolveTableName(con);
            String sql = "SELECT * FROM " + tbl;
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DichVu dv = new DichVu();
                    dv.setMaDv(rs.getInt("MA_DV"));
                    dv.setTenDv(rs.getString("TEN_DV"));
                    try { dv.setMoTa(rs.getString("MO_TA")); } catch (Exception ignored) {}
                    dv.setGiaCuoc(rs.getDouble("GIA_CUOC"));
                    try { dv.setTrangThai(rs.getInt("TRANG_THAI")); } catch (Exception ignored) { dv.setTrangThai(1); }
                    list.add(dv);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean addDichVu(DichVu dv) {
        try (Connection con = ConnectionUtils.getMyConnection()) {
            String tbl = resolveTableName(con);
            String sql;
            try {
                // Try with TRANG_THAI column
                sql = "INSERT INTO " + tbl + " (TEN_DV, MO_TA, GIA_CUOC, TRANG_THAI) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, dv.getTenDv());
                    ps.setString(2, dv.getMoTa());
                    ps.setDouble(3, dv.getGiaCuoc());
                    ps.setInt(4, dv.getTrangThai());
                    return ps.executeUpdate() > 0;
                }
            } catch (SQLException e) {
                if (e.getErrorCode() == 904) {
                    // ORA-00904: TRANG_THAI column doesn't exist — insert without it
                    sql = "INSERT INTO " + tbl + " (TEN_DV, MO_TA, GIA_CUOC) VALUES (?, ?, ?)";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, dv.getTenDv());
                        ps.setString(2, dv.getMoTa());
                        ps.setDouble(3, dv.getGiaCuoc());
                        return ps.executeUpdate() > 0;
                    }
                }
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateDichVu(DichVu dv) {
        try (Connection con = ConnectionUtils.getMyConnection()) {
            String tbl = resolveTableName(con);
            String sql;
            try {
                sql = "UPDATE " + tbl + " SET TEN_DV = ?, MO_TA = ?, GIA_CUOC = ?, TRANG_THAI = ? WHERE MA_DV = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, dv.getTenDv());
                    ps.setString(2, dv.getMoTa());
                    ps.setDouble(3, dv.getGiaCuoc());
                    ps.setInt(4, dv.getTrangThai());
                    ps.setInt(5, dv.getMaDv());
                    return ps.executeUpdate() > 0;
                }
            } catch (SQLException e) {
                if (e.getErrorCode() == 904) {
                    sql = "UPDATE " + tbl + " SET TEN_DV = ?, MO_TA = ?, GIA_CUOC = ? WHERE MA_DV = ?";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, dv.getTenDv());
                        ps.setString(2, dv.getMoTa());
                        ps.setDouble(3, dv.getGiaCuoc());
                        ps.setInt(4, dv.getMaDv());
                        return ps.executeUpdate() > 0;
                    }
                }
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteDichVu(int maDv) throws Exception {
        try (Connection con = ConnectionUtils.getMyConnection()) {
            String tbl = resolveTableName(con);
            String sql = "DELETE FROM " + tbl + " WHERE MA_DV = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, maDv);
                return ps.executeUpdate() > 0;
            }
        }
    }
}
