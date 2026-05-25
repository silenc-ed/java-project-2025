package Controller;

import ConnectDB.ConnectionUtils;
import Model.LoaiSanPham;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class LoaiSanPhamDAO {
    public static List<LoaiSanPham> getAllLoaiSanPham() {
        List<LoaiSanPham> list = new ArrayList<>();
        String sql = "SELECT * FROM LOAI_SAN_PHAM";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                LoaiSanPham lsp = new LoaiSanPham();
                lsp.setMaLsp(rs.getInt("MA_LSP"));
                lsp.setTenLsp(rs.getString("TEN_LSP"));
                try {
                    lsp.setMoTa(rs.getString("MO_TA"));
                } catch (Exception ignored) {}
                try {
                    lsp.setTongSoMatHang(rs.getInt("TONG_SO_MAT_HANG"));
                } catch (Exception ignored) {}
                // Note: adjust fields if they differ in your database schema
                try {
                    lsp.setHinhAnh(rs.getString("HINH_ANH"));
                    lsp.setTrangThai(rs.getString("TRANG_THAI"));
                } catch (Exception ignored) {
                    // Ignore if these columns don't exist
                }
                list.add(lsp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean addLoaiSanPham(LoaiSanPham lsp) {
        String sql = "INSERT INTO LOAI_SAN_PHAM (TEN_LSP, MO_TA) VALUES (?, ?)";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, lsp.getTenLsp());
            ps.setString(2, lsp.getMoTa());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateLoaiSanPham(LoaiSanPham lsp) {
        String sql = "UPDATE LOAI_SAN_PHAM SET TEN_LSP = ?, MO_TA = ? WHERE MA_LSP = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, lsp.getTenLsp());
            ps.setString(2, lsp.getMoTa());
            ps.setInt(3, lsp.getMaLsp());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteLoaiSanPham(int maLsp) throws Exception {
        String sqlKs = "DELETE FROM KHO_SERIAL WHERE MA_BIENTHE IN (SELECT BT.MA_BIENTHE FROM BIEN_THE_SAN_PHAM BT JOIN SAN_PHAM SP ON BT.MA_SP = SP.MA_SP WHERE SP.MA_LSP = ?)";
        String sqlTk = "DELETE FROM TON_KHO WHERE MA_BIENTHE IN (SELECT BT.MA_BIENTHE FROM BIEN_THE_SAN_PHAM BT JOIN SAN_PHAM SP ON BT.MA_SP = SP.MA_SP WHERE SP.MA_LSP = ?)";
        String sqlBt = "DELETE FROM BIEN_THE_SAN_PHAM WHERE MA_SP IN (SELECT MA_SP FROM SAN_PHAM WHERE MA_LSP = ?)";
        String sqlSp = "DELETE FROM SAN_PHAM WHERE MA_LSP = ?";
        String sqlLsp = "DELETE FROM LOAI_SAN_PHAM WHERE MA_LSP = ?";
        
        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);
            
            try (PreparedStatement psKs = con.prepareStatement(sqlKs)) {
                psKs.setInt(1, maLsp);
                psKs.executeUpdate();
            }
            
            try (PreparedStatement psTk = con.prepareStatement(sqlTk)) {
                psTk.setInt(1, maLsp);
                psTk.executeUpdate();
            }
            
            try (PreparedStatement psBt = con.prepareStatement(sqlBt)) {
                psBt.setInt(1, maLsp);
                psBt.executeUpdate();
            }
            
            try (PreparedStatement psSp = con.prepareStatement(sqlSp)) {
                psSp.setInt(1, maLsp);
                psSp.executeUpdate();
            }
            
            try (PreparedStatement psLsp = con.prepareStatement(sqlLsp)) {
                psLsp.setInt(1, maLsp);
                int count = psLsp.executeUpdate();
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
}
