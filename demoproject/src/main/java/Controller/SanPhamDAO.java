package Controller;

import ConnectDB.ConnectionUtils;
import Model.SanPham;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SanPhamDAO {
    public static List<SanPham> getAllSanPham() {
        List<SanPham> list = new ArrayList<>();
        String sql = "SELECT * FROM SANPHAM";
        try (Connection con = ConnectionUtils.getMyConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                SanPham sp = new SanPham();
                sp.setMaSp(rs.getInt("MA_SP"));
                sp.setMaLsp(rs.getInt("MA_LSP"));
                sp.setTenSp(rs.getString("TEN_SP"));
                sp.setDonViTinh(rs.getString("DON_VI_TINH"));
                sp.setGiaBan(rs.getDouble("GIA_BAN"));
                sp.setThoiGianBh(rs.getInt("THOI_GIAN_BH"));
                sp.setHinhAnh(rs.getString("HINH_ANH"));
                sp.setCoQuanLySerial(rs.getInt("CO_QUAN_LY_SERIAL"));
                sp.setTrangThai(rs.getString("TRANG_THAI"));
                sp.setSoLuongDaBan(rs.getInt("SO_LUONG_DA_BAN"));
                list.add(sp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
