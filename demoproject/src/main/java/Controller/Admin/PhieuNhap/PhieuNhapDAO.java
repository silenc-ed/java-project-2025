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
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT p.MA_PN, n.TEN_NCC, p.MA_NV, p.MA_CN, p.NGAY_NHAP, c.SO_LUONG, c.DON_GIA_NHAP, p.TONG_TIEN, p.TRANG_THAI, p.GHI_CHU " +
                     "FROM PHIEU_NHAP p " +
                     "LEFT JOIN NHA_CUNG_CAP n ON p.MA_NCC = n.MA_NCC " +
                     "LEFT JOIN CHI_TIET_PHIEU_NHAP c ON p.MA_PN = c.MA_PN " +
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
                try (PreparedStatement psInsNcc = con.prepareStatement(insNccSql, Statement.RETURN_GENERATED_KEYS)) {
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
                int newMaPn = -1;
                try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, maNcc);
                    ps.setInt(2, maNv);
                    ps.setInt(3, maCn);
                    ps.setDouble(4, total);
                    ps.setInt(5, status);
                    ps.setString(6, note);
                    ps.executeUpdate();
                    
                    try (ResultSet rsPn = ps.getGeneratedKeys()) {
                        if (rsPn.next()) {
                            newMaPn = rsPn.getInt(1);
                        }
                    }
                }

                if (newMaPn == -1) {
                    throw new Exception("Không thể chèn phiếu nhập!");
                }

                // Get default variant MA_BIENTHE
                int maBienthe = 1;
                try (PreparedStatement psBt = con.prepareStatement("SELECT NVL(MIN(MA_BIENTHE), 1) FROM BIEN_THE_SAN_PHAM")) {
                    try (ResultSet rsBt = psBt.executeQuery()) {
                        if (rsBt.next()) {
                            maBienthe = rsBt.getInt(1);
                        }
                    }
                }

                // Insert details
                String sqlCt = "INSERT INTO CHI_TIET_PHIEU_NHAP (MA_PN, MA_BIENTHE, SO_LUONG, DON_GIA_NHAP) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psCt = con.prepareStatement(sqlCt)) {
                    psCt.setInt(1, newMaPn);
                    psCt.setInt(2, maBienthe);
                    psCt.setInt(3, qty);
                    psCt.setDouble(4, price);
                    psCt.executeUpdate();
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
                
                String sqlCtCheck = "SELECT COUNT(*) FROM CHI_TIET_PHIEU_NHAP WHERE MA_PN = ?";
                boolean hasDetail = false;
                try (PreparedStatement psCtCheck = con.prepareStatement(sqlCtCheck)) {
                    psCtCheck.setInt(1, maPn);
                    try (ResultSet rsCt = psCtCheck.executeQuery()) {
                        if (rsCt.next() && rsCt.getInt(1) > 0) {
                            hasDetail = true;
                        }
                    }
                }
                
                if (hasDetail) {
                    String sqlCtUpd = "UPDATE CHI_TIET_PHIEU_NHAP SET SO_LUONG=?, DON_GIA_NHAP=? WHERE MA_PN=?";
                    try (PreparedStatement psCtUpd = con.prepareStatement(sqlCtUpd)) {
                        psCtUpd.setInt(1, qty);
                        psCtUpd.setDouble(2, price);
                        psCtUpd.setInt(3, maPn);
                        psCtUpd.executeUpdate();
                    }
                } else {
                    int maBienthe = 1;
                    try (PreparedStatement psBt = con.prepareStatement("SELECT NVL(MIN(MA_BIENTHE), 1) FROM BIEN_THE_SAN_PHAM")) {
                        try (ResultSet rsBt = psBt.executeQuery()) {
                            if (rsBt.next()) {
                                maBienthe = rsBt.getInt(1);
                            }
                        }
                    }
                    String sqlCtIns = "INSERT INTO CHI_TIET_PHIEU_NHAP (MA_PN, MA_BIENTHE, SO_LUONG, DON_GIA_NHAP) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement psCtIns = con.prepareStatement(sqlCtIns)) {
                        psCtIns.setInt(1, maPn);
                        psCtIns.setInt(2, maBienthe);
                        psCtIns.setInt(3, qty);
                        psCtIns.setDouble(4, price);
                        psCtIns.executeUpdate();
                    }
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
