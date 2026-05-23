package Controller.Admin.TonKho;

import ConnectDB.ConnectionUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO quản lý tồn kho chi nhánh
 * Bảng: CHINHANH, TONKHO, BIENTHE_SANPHAM, SANPHAM, KHO_SERIAL
 */
public class TonKhoDAO {

    /**
     * Lấy tất cả chi nhánh + tổng tồn kho
     */
    public List<Map<String, Object>> getAllBranches(String keyword) throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT CN.MA_CN, CN.TEN_CN, CN.DIA_CHI, CN.SDT_HOTLINE, CN.TRANG_THAI, ");
        sql.append("NVL((SELECT SUM(TK.SO_LUONG_TON) FROM TON_KHO TK WHERE TK.MA_CN = CN.MA_CN), 0) AS TONG_TON ");
        sql.append("FROM CHI_NHANH CN ");
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("WHERE UPPER(CN.TEN_CN) LIKE UPPER(?) OR UPPER(CN.DIA_CHI) LIKE UPPER(?) ");
        }
        sql.append("ORDER BY CN.MA_CN");

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
                    row.put("MA_CN", rs.getInt("MA_CN"));
                    row.put("TEN_CN", rs.getString("TEN_CN"));
                    row.put("DIA_CHI", rs.getString("DIA_CHI"));
                    row.put("SDT_HOTLINE", rs.getString("SDT_HOTLINE"));
                    row.put("TRANG_THAI", rs.getString("TRANG_THAI"));
                    row.put("TONG_TON", rs.getLong("TONG_TON"));
                    results.add(row);
                }
            }
        }
        return results;
    }

    /**
     * Lấy sản phẩm tồn kho tại chi nhánh (JOIN TONKHO + BIENTHE + SANPHAM)
     */
    public List<Map<String, Object>> getProductsByBranch(int maCN, String keyword) throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT SP.MA_SP, SP.TEN_SP, BT.MA_BIENTHE, BT.TEN_BIENTHE, BT.GIA_BAN, ");
        sql.append("TK.SO_LUONG_TON, TK.NGAY_CAP_NHAT_CUOI ");
        sql.append("FROM TON_KHO TK ");
        sql.append("JOIN BIEN_THE_SAN_PHAM BT ON TK.MA_BIENTHE = BT.MA_BIENTHE ");
        sql.append("JOIN SAN_PHAM SP ON BT.MA_SP = SP.MA_SP ");
        sql.append("WHERE TK.MA_CN = ? ");
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (UPPER(SP.TEN_SP) LIKE UPPER(?) OR UPPER(BT.TEN_BIENTHE) LIKE UPPER(?)) ");
        }
        sql.append("ORDER BY SP.TEN_SP, BT.TEN_BIENTHE");

        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setInt(idx++, maCN);
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim() + "%";
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("MA_SP", rs.getInt("MA_SP"));
                    row.put("TEN_SP", rs.getString("TEN_SP"));
                    row.put("MA_BIENTHE", rs.getInt("MA_BIENTHE"));
                    row.put("TEN_BIENTHE", rs.getString("TEN_BIENTHE"));
                    row.put("GIA_BAN", rs.getLong("GIA_BAN"));
                    row.put("SO_LUONG_TON", rs.getLong("SO_LUONG_TON"));
                    row.put("NGAY_CAP_NHAT", rs.getTimestamp("NGAY_CAP_NHAT_CUOI"));
                    results.add(row);
                }
            }
        }
        return results;
    }

    /**
     * Lấy danh sách serial theo biến thể + chi nhánh
     */
    public List<Map<String, Object>> getSerialsByVariantAndBranch(int maBienthe, int maCN, String keyword) throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT MA_SN, SERIAL_NUMBER, MA_PN, TRANG_THAI ");
        sql.append("FROM KHO_SERIAL ");
        sql.append("WHERE MA_BIENTHE = ? AND MA_CN = ? ");
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND UPPER(SERIAL_NUMBER) LIKE UPPER(?) ");
        }
        sql.append("ORDER BY TRANG_THAI, SERIAL_NUMBER");

        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setInt(idx++, maBienthe);
            ps.setInt(idx++, maCN);
            if (keyword != null && !keyword.trim().isEmpty()) {
                ps.setString(idx++, "%" + keyword.trim() + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("MA_SN", rs.getInt("MA_SN"));
                    row.put("SERIAL_NUMBER", rs.getString("SERIAL_NUMBER"));
                    row.put("MA_PN", rs.getObject("MA_PN"));
                    row.put("TRANG_THAI", rs.getString("TRANG_THAI"));
                    results.add(row);
                }
            }
        }
        return results;
    }

    /**
     * Đếm serial theo trạng thái cho 1 biến thể tại 1 chi nhánh
     */
    public Map<String, Integer> countSerialsByStatus(int maBienthe, int maCN) throws Exception {
        Map<String, Integer> counts = new HashMap<>();
        String sql = "SELECT TRANG_THAI, COUNT(*) AS CNT FROM KHO_SERIAL WHERE MA_BIENTHE = ? AND MA_CN = ? GROUP BY TRANG_THAI";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maBienthe);
            ps.setInt(2, maCN);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getString("TRANG_THAI"), rs.getInt("CNT"));
                }
            }
        }
        return counts;
    }

    /**
     * Lấy tên chi nhánh theo mã
     */
    public String getBranchName(int maCN) throws Exception {
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement("SELECT TEN_CN FROM CHI_NHANH WHERE MA_CN = ?")) {
            ps.setInt(1, maCN);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("TEN_CN");
            }
        }
        return "Chi nhánh #" + maCN;
    }

    /**
     * Cập nhật thông tin chi nhánh
     */
    public boolean updateBranch(int maCN, String tenCN, String diaChi, String sdtHotline, String trangThai) throws Exception {
        String sql = "UPDATE CHI_NHANH SET TEN_CN = ?, DIA_CHI = ?, SDT_HOTLINE = ?, TRANG_THAI = ? WHERE MA_CN = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tenCN);
            ps.setString(2, diaChi);
            ps.setString(3, sdtHotline);
            ps.setString(4, trangThai);
            ps.setInt(5, maCN);
            return ps.executeUpdate() > 0;
        }
    }
}
