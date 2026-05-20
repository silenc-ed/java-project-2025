package Controller.Admin.KhuyenMai;

import ConnectDB.ConnectionUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO quản lý khuyến mãi
 * Bảng: KHUYENMAI, LOAI_KHUYENMAI
 */
public class KhuyenMaiDAO {

    /**
     * Lấy tất cả khuyến mãi (JOIN LOAI_KHUYENMAI)
     */
    public List<Map<String, Object>> getAllPromotions(String keyword) throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT KM.MA_KM, KM.TEN_KM, KM.GIA_TRI, KM.RANG_BUOC_GIA_TRI, ");
        sql.append("KM.NGAY_BAT_DAU, KM.NGAY_KET_THUC, KM.TRANG_THAI, ");
        sql.append("LKM.MA_LOAI_KM, LKM.TEN_LOAI_KM ");
        sql.append("FROM KHUYENMAI KM ");
        sql.append("JOIN LOAI_KHUYENMAI LKM ON KM.MA_LOAI_KM = LKM.MA_LOAI_KM ");
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("WHERE UPPER(KM.TEN_KM) LIKE UPPER(?) OR UPPER(LKM.TEN_LOAI_KM) LIKE UPPER(?) ");
        }
        sql.append("ORDER BY KM.MA_KM DESC");

        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim() + "%";
                ps.setString(1, kw);
                ps.setString(2, kw);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("MA_KM", rs.getInt("MA_KM"));
                    row.put("TEN_KM", rs.getString("TEN_KM"));
                    row.put("GIA_TRI", rs.getLong("GIA_TRI"));
                    row.put("RANG_BUOC", rs.getString("RANG_BUOC_GIA_TRI"));
                    row.put("NGAY_BAT_DAU", rs.getTimestamp("NGAY_BAT_DAU"));
                    row.put("NGAY_KET_THUC", rs.getTimestamp("NGAY_KET_THUC"));
                    row.put("TRANG_THAI", rs.getString("TRANG_THAI"));
                    row.put("MA_LOAI_KM", rs.getInt("MA_LOAI_KM"));
                    row.put("TEN_LOAI_KM", rs.getString("TEN_LOAI_KM"));
                    results.add(row);
                }
            }
        }
        return results;
    }

    /**
     * Lấy tất cả loại khuyến mãi
     */
    public List<Map<String, Object>> getAllPromoTypes() throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT MA_LOAI_KM, TEN_LOAI_KM, MO_TA FROM LOAI_KHUYENMAI ORDER BY MA_LOAI_KM";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("MA_LOAI_KM", rs.getInt("MA_LOAI_KM"));
                row.put("TEN_LOAI_KM", rs.getString("TEN_LOAI_KM"));
                row.put("MO_TA", rs.getString("MO_TA"));
                results.add(row);
            }
        }
        return results;
    }

    /**
     * Thêm khuyến mãi mới
     */
    public int addPromotion(int maLoaiKM, String tenKM, long giaTri, String rangBuoc,
                            Timestamp ngayBD, Timestamp ngayKT, String trangThai) throws Exception {
        String sql = "INSERT INTO KHUYENMAI (MA_LOAI_KM, TEN_KM, GIA_TRI, RANG_BUOC_GIA_TRI, " +
                     "NGAY_BAT_DAU, NGAY_KET_THUC, TRANG_THAI) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"MA_KM"})) {
            ps.setInt(1, maLoaiKM);
            ps.setString(2, tenKM);
            ps.setLong(3, giaTri);
            ps.setString(4, rangBuoc);
            ps.setTimestamp(5, ngayBD);
            ps.setTimestamp(6, ngayKT);
            ps.setString(7, trangThai);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    /**
     * Cập nhật khuyến mãi
     */
    public void updatePromotion(int maKM, int maLoaiKM, String tenKM, long giaTri,
                                String rangBuoc, Timestamp ngayBD, Timestamp ngayKT, String trangThai) throws Exception {
        String sql = "UPDATE KHUYENMAI SET MA_LOAI_KM=?, TEN_KM=?, GIA_TRI=?, RANG_BUOC_GIA_TRI=?, " +
                     "NGAY_BAT_DAU=?, NGAY_KET_THUC=?, TRANG_THAI=? WHERE MA_KM=?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maLoaiKM);
            ps.setString(2, tenKM);
            ps.setLong(3, giaTri);
            ps.setString(4, rangBuoc);
            ps.setTimestamp(5, ngayBD);
            ps.setTimestamp(6, ngayKT);
            ps.setString(7, trangThai);
            ps.setInt(8, maKM);
            ps.executeUpdate();
        }
    }

    /**
     * Xóa khuyến mãi
     */
    public void deletePromotion(int maKM) throws Exception {
        String sql = "DELETE FROM KHUYENMAI WHERE MA_KM = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maKM);
            ps.executeUpdate();
        }
    }
}
