package Controller;

import ConnectDB.ConnectionUtils;
import Model.BienTheSanPham;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class BienTheSanPhamDAO {
    
    public static boolean addBienTheSanPham(BienTheSanPham bt) throws Exception {
        String sql = "INSERT INTO BIEN_THE_SAN_PHAM (MA_SP, TEN_BIENTHE, GIA_BAN, TRANG_THAI) VALUES (?, ?, ?, ?)";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bt.getMaSp());
            ps.setString(2, bt.getTenBienThe());
            ps.setDouble(3, bt.getGiaBan());
            ps.setString(4, bt.getTrangThai());
            return ps.executeUpdate() > 0;
        }
    }

    public static boolean updateBienTheSanPham(BienTheSanPham bt) throws Exception {
        String sql = "UPDATE BIEN_THE_SAN_PHAM SET TEN_BIENTHE = ?, GIA_BAN = ?, TRANG_THAI = ? WHERE MA_BIENTHE = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, bt.getTenBienThe());
            ps.setDouble(2, bt.getGiaBan());
            ps.setString(3, bt.getTrangThai());
            ps.setInt(4, bt.getMaBienThe());
            return ps.executeUpdate() > 0;
        }
    }

    public static boolean deleteBienTheSanPham(int maBienThe) throws Exception {
        String sqlKs = "DELETE FROM KHO_SERIAL WHERE MA_BIENTHE = ?";
        String sqlTk = "DELETE FROM TON_KHO WHERE MA_BIENTHE = ?";
        String sqlBt = "DELETE FROM BIEN_THE_SAN_PHAM WHERE MA_BIENTHE = ?";
        
        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);
            
            try (PreparedStatement psKs = con.prepareStatement(sqlKs)) {
                psKs.setInt(1, maBienThe);
                psKs.executeUpdate();
            }
            
            try (PreparedStatement psTk = con.prepareStatement(sqlTk)) {
                psTk.setInt(1, maBienThe);
                psTk.executeUpdate();
            }
            
            try (PreparedStatement psBt = con.prepareStatement(sqlBt)) {
                psBt.setInt(1, maBienThe);
                int count = psBt.executeUpdate();
                con.commit();
                return count > 0;
            }
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

    public static int getTonKhoByMaBienThe(int maBienThe) throws Exception {
        String sql = "SELECT SUM(SO_LUONG_TON) AS TONG_TON FROM TON_KHO WHERE MA_BIENTHE = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maBienThe);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("TONG_TON");
                }
            }
        }
        return 0;
    }
}
