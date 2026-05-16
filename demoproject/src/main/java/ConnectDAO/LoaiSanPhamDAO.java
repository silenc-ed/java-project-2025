package ConnectDAO;

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
        String sql = "SELECT * FROM LOAISANPHAM"; // Assuming table name is LOAISANPHAM
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                LoaiSanPham lsp = new LoaiSanPham();
                lsp.setMaLsp(rs.getInt("MA_LSP"));
                lsp.setTenLsp(rs.getString("TEN_LSP"));
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
}
