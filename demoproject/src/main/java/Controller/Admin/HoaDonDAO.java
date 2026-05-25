package Controller.Admin;

import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class HoaDonDAO {

    public static List<Map<String, Object>> getAllHoaDon(boolean canEdit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT H.MA_HD, K.HO_TEN as TEN_KH, H.MA_NV, H.THOI_GIAN_LAP, H.THANH_TIEN, H.PHUONG_THUC_TT, H.TRANG_THAI, H.IS_DELETED " +
                     "FROM HOA_DON H LEFT JOIN KHACH_HANG K ON H.MA_KH = K.MA_KH ";
        if (!canEdit) {
            sql += "WHERE H.IS_DELETED = 0 ";
        }
        sql += "ORDER BY H.MA_HD DESC";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("MA_HD", rs.getInt("MA_HD"));
                row.put("TEN_KH", rs.getString("TEN_KH"));
                row.put("MA_NV", rs.getInt("MA_NV"));
                row.put("THOI_GIAN_LAP", rs.getTimestamp("THOI_GIAN_LAP"));
                row.put("THANH_TIEN", rs.getDouble("THANH_TIEN"));
                row.put("PHUONG_THUC_TT", rs.getString("PHUONG_THUC_TT"));
                row.put("TRANG_THAI", rs.getString("TRANG_THAI"));
                row.put("IS_DELETED", rs.getInt("IS_DELETED"));
                list.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Map<String, Object> getHoaDonById(int maHd) {
        String sql = "SELECT TONG_TIEN, GIAM_GIA, THANH_TIEN, PHUONG_THUC_TT, TRANG_THAI, MA_KH, MA_NV, MA_CN, MA_KM FROM HOA_DON WHERE MA_HD = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maHd);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("TONG_TIEN", rs.getDouble("TONG_TIEN"));
                    row.put("GIAM_GIA", rs.getDouble("GIAM_GIA"));
                    row.put("THANH_TIEN", rs.getDouble("THANH_TIEN"));
                    row.put("PHUONG_THUC_TT", rs.getString("PHUONG_THUC_TT"));
                    row.put("TRANG_THAI", rs.getString("TRANG_THAI"));
                    row.put("MA_KH", rs.getInt("MA_KH"));
                    row.put("MA_NV", rs.getInt("MA_NV"));
                    row.put("MA_CN", rs.getInt("MA_CN"));
                    row.put("MA_KM", rs.getInt("MA_KM"));
                    return row;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean saveHoaDon(int maHd, Integer maKh, int maNv, int maCn, Integer maKm, 
                                     double tongTien, double giamGia, double thanhTien, 
                                     String phuongThuc, String trangThai, boolean isEdit) throws Exception {
        String sql;
        if (!isEdit) {
            sql = "INSERT INTO HOA_DON (MA_KH, MA_NV, MA_CN, MA_KM, TONG_TIEN, GIAM_GIA, THANH_TIEN, PHUONG_THUC_TT, TRANG_THAI) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE HOA_DON SET MA_KH=?, MA_NV=?, MA_CN=?, MA_KM=?, TONG_TIEN=?, GIAM_GIA=?, THANH_TIEN=?, PHUONG_THUC_TT=?, TRANG_THAI=? WHERE MA_HD=?";
        }

        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (maKh != null && maKh > 0) {
                ps.setInt(1, maKh);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, maNv);
            ps.setInt(3, maCn);
            if (maKm != null && maKm > 0) {
                ps.setInt(4, maKm);
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setDouble(5, tongTien);
            ps.setDouble(6, giamGia);
            ps.setDouble(7, thanhTien);
            ps.setString(8, phuongThuc);
            ps.setString(9, trangThai);
            
            if (isEdit) {
                ps.setInt(10, maHd);
            }

            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    public static boolean deleteHoaDons(List<Integer> ids) throws Exception {
        if (ids == null || ids.isEmpty()) return false;
        
        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);
            
            // Xóa mềm hóa đơn (set IS_DELETED = 1) thay vì xóa thật khỏi CSDL
            String sqlHd = "UPDATE HOA_DON SET IS_DELETED = 1 WHERE MA_HD = ?";
            
            try (PreparedStatement psHd = con.prepareStatement(sqlHd)) {
                for (int maHd : ids) {
                    psHd.setInt(1, maHd);
                    psHd.executeUpdate();
                }
            }
            
            con.commit();
            return true;
        } catch (Exception ex) {
            if (con != null) {
                try { con.rollback(); } catch (Exception ignored) {}
            }
            throw ex;
        } finally {
            if (con != null) {
                try { con.close(); } catch (Exception ignored) {}
            }
        }
    }

    public static boolean restoreHoaDon(int maHd) throws Exception {
        String sql = "UPDATE HOA_DON SET IS_DELETED = 0 WHERE MA_HD = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maHd);
            return ps.executeUpdate() > 0;
        }
    }

    public static List<Map<String, Object>> getComboData(String table, String idCol, String nameCol) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT " + idCol + ", " + nameCol + " FROM " + table;
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("ID", rs.getInt(1));
                map.put("NAME", rs.getString(2));
                list.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
