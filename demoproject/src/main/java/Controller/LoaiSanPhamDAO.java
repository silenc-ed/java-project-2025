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
        String sql = "DELETE FROM LOAI_SAN_PHAM WHERE MA_LSP = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maLsp);
            return ps.executeUpdate() > 0;
        }
    }
}
