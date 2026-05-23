package Controller.Admin.PhieuNhap;

import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhieuNhapDAO {

    public static List<Map<String, Object>> getAllPhieuNhap() {
        return getAllPhieuNhap(true);
    }

    public static List<Map<String, Object>> getAllPhieuNhap(boolean hasEditRole) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT p.MA_PN, ncc.TEN_NCC, p.MA_NV, p.MA_CN, p.NGAY_NHAP, " +
                     "NVL(SUM(c.SO_LUONG), 0) AS SO_LUONG, " +
                     "NVL(AVG(c.DON_GIA_NHAP), 0) AS DON_GIA_NHAP, p.TONG_TIEN, p.TRANG_THAI, p.GHI_CHU " +
                     "FROM PHIEU_NHAP p " +
                     "LEFT JOIN NHA_CUNG_CAP ncc ON p.MA_NCC = ncc.MA_NCC " +
                     "LEFT JOIN CHI_TIET_PHIEU_NHAP c ON p.MA_PN = c.MA_PN ";
        if (!hasEditRole) {
            sql += "WHERE p.TRANG_THAI = 1 ";
        }
        sql += "GROUP BY p.MA_PN, ncc.TEN_NCC, p.MA_NV, p.MA_CN, p.NGAY_NHAP, p.TONG_TIEN, p.TRANG_THAI, p.GHI_CHU " +
               "ORDER BY p.MA_PN DESC";
        
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("MA_PN", rs.getInt("MA_PN"));
                row.put("TEN_NCC", rs.getString("TEN_NCC"));
                row.put("MA_NV", rs.getInt("MA_NV"));
                row.put("MA_CN", rs.getInt("MA_CN"));
                row.put("NGAY_NHAP", rs.getTimestamp("NGAY_NHAP"));
                row.put("SO_LUONG", rs.getInt("SO_LUONG"));
                row.put("DON_GIA_NHAP", rs.getDouble("DON_GIA_NHAP"));
                row.put("TONG_TIEN", rs.getDouble("TONG_TIEN"));
                row.put("TRANG_THAI", rs.getInt("TRANG_THAI"));
                row.put("GHI_CHU", rs.getString("GHI_CHU"));
                list.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Map<String, Object>> getChiTietPhieuNhap(int maPn) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT C.MA_PN, C.MA_BIENTHE, SP.TEN_SP, BT.TEN_BIENTHE, C.SO_LUONG, C.DON_GIA_NHAP, (C.SO_LUONG * C.DON_GIA_NHAP) AS THANH_TIEN " +
                     "FROM CHI_TIET_PHIEU_NHAP C " +
                     "LEFT JOIN BIEN_THE_SAN_PHAM BT ON C.MA_BIENTHE = BT.MA_BIENTHE " +
                     "LEFT JOIN SAN_PHAM SP ON BT.MA_SP = SP.MA_SP " +
                     "WHERE C.MA_PN = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, maPn);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("MA_PN", rs.getInt("MA_PN"));
                    row.put("MA_BIENTHE", rs.getInt("MA_BIENTHE"));
                    row.put("TEN_SP", rs.getString("TEN_SP"));
                    row.put("TEN_BIENTHE", rs.getString("TEN_BIENTHE"));
                    row.put("SO_LUONG", rs.getInt("SO_LUONG"));
                    row.put("DON_GIA_NHAP", rs.getDouble("DON_GIA_NHAP"));
                    row.put("THANH_TIEN", rs.getDouble("THANH_TIEN"));
                    list.add(row);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean savePhieuNhap(int maPn, String supplierName, int maNv, int maCn, 
                                       int qty, double price, double total, int status, 
                                       String note, boolean isEdit) throws Exception {
        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);

            // 1. Resolve supplier
            int maNcc = -1;
            String checkSql = "SELECT MA_NCC FROM NHA_CUNG_CAP WHERE UPPER(TEN_NCC) = UPPER(?)";
            try (PreparedStatement psCheck = con.prepareStatement(checkSql)) {
                psCheck.setString(1, supplierName);
                try (ResultSet rsCheck = psCheck.executeQuery()) {
                    if (rsCheck.next()) {
                        maNcc = rsCheck.getInt("MA_NCC");
                    }
                }
            }

            if (maNcc == -1) {
                String insNccSql = "INSERT INTO NHA_CUNG_CAP (TEN_NCC) VALUES (?)";
                try (PreparedStatement psInsNcc = con.prepareStatement(insNccSql, new String[]{"MA_NCC"})) {
                    psInsNcc.setString(1, supplierName);
                    psInsNcc.executeUpdate();
                    try (ResultSet rsKey = psInsNcc.getGeneratedKeys()) {
                        if (rsKey.next()) {
                            maNcc = rsKey.getInt(1);
                        }
                    }
                }
            }

            if (maNcc == -1) {
                throw new Exception("Không thể tạo hoặc tìm thấy Nhà cung cấp!");
            }

            // 2. Insert or update PHIEU_NHAP
            if (!isEdit) {
                String sql = "INSERT INTO PHIEU_NHAP (MA_NCC, MA_NV, MA_CN, TONG_TIEN, TRANG_THAI, GHI_CHU) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql, new String[]{"MA_PN"})) {
                    ps.setInt(1, maNcc);
                    ps.setInt(2, maNv);
                    ps.setInt(3, maCn);
                    ps.setDouble(4, total);
                    ps.setInt(5, status);
                    ps.setString(6, note);
                    ps.executeUpdate();
                    
                    try (ResultSet rsPn = ps.getGeneratedKeys()) {
                        if (rsPn.next()) {
                            int newMaPn = rsPn.getInt(1);
                            if (newMaPn == -1) {
                                throw new Exception("Không thể chèn phiếu nhập!");
                            }
                        }
                    }
                }
            } else {
                String sql = "UPDATE PHIEU_NHAP SET MA_NCC=?, MA_NV=?, MA_CN=?, TONG_TIEN=?, TRANG_THAI=?, GHI_CHU=? WHERE MA_PN=?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, maNcc);
                    ps.setInt(2, maNv);
                    ps.setInt(3, maCn);
                    ps.setDouble(4, total);
                    ps.setInt(5, status);
                    ps.setString(6, note);
                    ps.setInt(7, maPn);
                    ps.executeUpdate();
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

    public static boolean softDeletePhieuNhaps(List<Integer> ids) {
        String sql = "UPDATE PHIEU_NHAP SET TRANG_THAI = 0 WHERE MA_PN = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int id : ids) {
                ps.setInt(1, id);
                ps.addBatch();
            }
            ps.executeBatch();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean saveChiTietPhieuNhap(int maPn, int maBienthe, int qty, double price, boolean isEdit) throws Exception {
        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);

            if (isEdit) {
                String sqlUpdate = "UPDATE CHI_TIET_PHIEU_NHAP SET SO_LUONG = ?, DON_GIA_NHAP = ? WHERE MA_PN = ? AND MA_BIENTHE = ?";
                try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
                    ps.setInt(1, qty);
                    ps.setDouble(2, price);
                    ps.setInt(3, maPn);
                    ps.setInt(4, maBienthe);
                    ps.executeUpdate();
                }
            } else {
                // Nếu mặt hàng đã tồn tại → cộng dồn số lượng (UPSERT)
                String sqlCheck = "SELECT SO_LUONG FROM CHI_TIET_PHIEU_NHAP WHERE MA_PN = ? AND MA_BIENTHE = ?";
                try (PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {
                    psCheck.setInt(1, maPn);
                    psCheck.setInt(2, maBienthe);
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next()) {
                            // Đã tồn tại → cộng dồn số lượng
                            int existingQty = rs.getInt("SO_LUONG");
                            String sqlMerge = "UPDATE CHI_TIET_PHIEU_NHAP SET SO_LUONG = ?, DON_GIA_NHAP = ? WHERE MA_PN = ? AND MA_BIENTHE = ?";
                            try (PreparedStatement psMerge = con.prepareStatement(sqlMerge)) {
                                psMerge.setInt(1, existingQty + qty);
                                psMerge.setDouble(2, price);
                                psMerge.setInt(3, maPn);
                                psMerge.setInt(4, maBienthe);
                                psMerge.executeUpdate();
                            }
                        } else {
                            // Chưa tồn tại → thêm mới
                            String sqlInsert = "INSERT INTO CHI_TIET_PHIEU_NHAP (MA_PN, MA_BIENTHE, SO_LUONG, DON_GIA_NHAP) VALUES (?, ?, ?, ?)";
                            try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                                ps.setInt(1, maPn);
                                ps.setInt(2, maBienthe);
                                ps.setInt(3, qty);
                                ps.setDouble(4, price);
                                ps.executeUpdate();
                            }
                        }
                    }
                }
            }

            String sqlUpdateTotal = "UPDATE PHIEU_NHAP SET TONG_TIEN = (SELECT NVL(SUM(SO_LUONG * DON_GIA_NHAP), 0) FROM CHI_TIET_PHIEU_NHAP WHERE MA_PN = ?) WHERE MA_PN = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlUpdateTotal)) {
                ps.setInt(1, maPn);
                ps.setInt(2, maPn);
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

    public static boolean deleteChiTietPhieuNhaps(int maPn, List<Integer> listMaBienthe) {
        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);

            String sql = "DELETE FROM CHI_TIET_PHIEU_NHAP WHERE MA_PN = ? AND MA_BIENTHE = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (int maBienthe : listMaBienthe) {
                    ps.setInt(1, maPn);
                    ps.setInt(2, maBienthe);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            String sqlUpdateTotal = "UPDATE PHIEU_NHAP SET TONG_TIEN = (SELECT NVL(SUM(SO_LUONG * DON_GIA_NHAP), 0) FROM CHI_TIET_PHIEU_NHAP WHERE MA_PN = ?) WHERE MA_PN = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlUpdateTotal)) {
                ps.setInt(1, maPn);
                ps.setInt(2, maPn);
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

    public static boolean deletePhieuNhaps(List<Integer> ids) throws Exception {
        if (ids == null || ids.isEmpty()) return false;

        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);
            
            try (PreparedStatement psCt = con.prepareStatement("DELETE FROM CHI_TIET_PHIEU_NHAP WHERE MA_PN = ?");
                 PreparedStatement psPn = con.prepareStatement("DELETE FROM PHIEU_NHAP WHERE MA_PN = ?")) {
                for (int maPn : ids) {
                    psCt.setInt(1, maPn);
                    psCt.executeUpdate();
                    
                    psPn.setInt(1, maPn);
                    psPn.executeUpdate();
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
}
