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

    static {
        try (Connection con = ConnectionUtils.getMyConnection();
             Statement st = con.createStatement()) {
            st.execute("CREATE OR REPLACE TRIGGER TRG_KM_TRANG_THAI\n" +
                       "BEFORE UPDATE ON KHUYEN_MAI\n" +
                       "FOR EACH ROW\n" +
                       "BEGIN\n" +
                       "    IF :NEW.NGAY_KET_THUC < SYSTIMESTAMP THEN\n" +
                       "        :NEW.TRANG_THAI := 'Hết hạn';\n" +
                       "    ELSIF :NEW.SO_LUONG_CL <= 0 THEN\n" +
                       "        :NEW.TRANG_THAI := 'Hết lượt';\n" +
                       "    END IF;\n" +
                       "END;");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Lấy tất cả khuyến mãi (JOIN LOAI_KHUYENMAI)
     */
    public List<Map<String, Object>> getAllPromotions(String keyword) throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT KM.MA_KM, KM.TEN_KM, KM.GIA_TRI, KM.DIEM_DOI, KM.SO_LUONG_CL, ");
        sql.append("KM.NGAY_BAT_DAU, KM.NGAY_KET_THUC, KM.TRANG_THAI, ");
        sql.append("LKM.MA_LOAI_KM, LKM.TEN_LOAI_KM ");
        sql.append("FROM KHUYEN_MAI KM ");
        sql.append("JOIN LOAI_KHUYEN_MAI LKM ON KM.MA_LOAI_KM = LKM.MA_LOAI_KM ");
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
                    row.put("DIEM_DOI", rs.getInt("DIEM_DOI"));
                    row.put("SO_LUONG_CL", rs.getInt("SO_LUONG_CL"));
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
        String sql = "SELECT MA_LOAI_KM, TEN_LOAI_KM, MO_TA FROM LOAI_KHUYEN_MAI ORDER BY MA_LOAI_KM";
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
    public int addPromotion(int maLoaiKM, String tenKM, long giaTri, int diemDoi, int soLuongCL,
                            Timestamp ngayBD, Timestamp ngayKT, String trangThai) throws Exception {
        String sql = "INSERT INTO KHUYEN_MAI (MA_LOAI_KM, TEN_KM, GIA_TRI, DIEM_DOI, SO_LUONG_CL, " +
                     "NGAY_BAT_DAU, NGAY_KET_THUC, TRANG_THAI) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"MA_KM"})) {
            ps.setInt(1, maLoaiKM);
            ps.setString(2, tenKM);
            ps.setLong(3, giaTri);
            ps.setInt(4, diemDoi);
            ps.setInt(5, soLuongCL);
            ps.setTimestamp(6, ngayBD);
            ps.setTimestamp(7, ngayKT);
            ps.setString(8, trangThai);
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
                                int diemDoi, int soLuongCL, Timestamp ngayBD, Timestamp ngayKT, String trangThai) throws Exception {
        String sql = "UPDATE KHUYEN_MAI SET MA_LOAI_KM=?, TEN_KM=?, GIA_TRI=?, DIEM_DOI=?, SO_LUONG_CL=?, " +
                     "NGAY_BAT_DAU=?, NGAY_KET_THUC=?, TRANG_THAI=? WHERE MA_KM=?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maLoaiKM);
            ps.setString(2, tenKM);
            ps.setLong(3, giaTri);
            ps.setInt(4, diemDoi);
            ps.setInt(5, soLuongCL);
            ps.setTimestamp(6, ngayBD);
            ps.setTimestamp(7, ngayKT);
            ps.setString(8, trangThai);
            ps.setInt(9, maKM);
            ps.executeUpdate();
        }
    }

    /**
     * Xóa khuyến mãi
     */
    public void deletePromotion(int maKM) throws Exception {
        String sql = "DELETE FROM KHUYEN_MAI WHERE MA_KM = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maKM);
            ps.executeUpdate();
        }
    }

    /**
     * Kiểm tra trùng tên khuyến mãi
     */
    public boolean checkDuplicatePromotionName(int maKM, String tenKM) throws Exception {
        String sql = "SELECT 1 FROM KHUYEN_MAI WHERE UPPER(TEN_KM) = UPPER(?) AND MA_KM != ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tenKM.trim());
            ps.setInt(2, maKM);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // true nếu trùng
            }
        }
    }

    /**
     * Thêm loại khuyến mãi
     */
    public boolean addPromoType(String tenLoai, String moTa) throws Exception {
        String sql = "INSERT INTO LOAI_KHUYEN_MAI (TEN_LOAI_KM, MO_TA) VALUES (?, ?)";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tenLoai);
            ps.setString(2, moTa);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật loại khuyến mãi
     */
    public boolean updatePromoType(int maLoai, String tenLoai, String moTa) throws Exception {
        String sql = "UPDATE LOAI_KHUYEN_MAI SET TEN_LOAI_KM=?, MO_TA=? WHERE MA_LOAI_KM=?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tenLoai);
            ps.setString(2, moTa);
            ps.setInt(3, maLoai);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa loại khuyến mãi
     */
    public boolean deletePromoType(int maLoai) throws Exception {
        String sql = "DELETE FROM LOAI_KHUYEN_MAI WHERE MA_LOAI_KM=?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maLoai);
            return ps.executeUpdate() > 0;
        }
    }
}
