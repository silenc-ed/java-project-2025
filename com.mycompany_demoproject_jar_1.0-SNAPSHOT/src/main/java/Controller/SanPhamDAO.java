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
        // Lấy thông tin cơ bản từ SANPHAM và giá bán nhỏ nhất từ BIENTHE_SANPHAM
        String sql = "SELECT SP.*, (SELECT NVL(MIN(GIA_BAN), 0) FROM BIENTHE_SANPHAM BT WHERE BT.MA_SP = SP.MA_SP AND BT.TRANG_THAI != 'Ngừng kinh doanh') AS GIA_BAN FROM SANPHAM SP";
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
                sp.setMoTa(rs.getString("MO_TA"));
                list.add(sp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<SanPham> searchAdvanced(List<Integer> catIds, List<String> catNames, Double minPrice, Double maxPrice) {
        List<SanPham> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM SANPHAM WHERE 1=1 ");
        
        if (minPrice != null) {
            sql.append(" AND GIA_BAN >= ").append(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND GIA_BAN <= ").append(maxPrice);
        }

        boolean hasCatIds = catIds != null && !catIds.isEmpty();
        boolean hasCatNames = catNames != null && !catNames.isEmpty();

        if (hasCatIds || hasCatNames) {
            sql.append(" AND (");
            boolean first = true;
            
            if (hasCatIds) {
                sql.append("MA_LSP IN (");
                for (int i = 0; i < catIds.size(); i++) {
                    sql.append(catIds.get(i));
                    if (i < catIds.size() - 1) sql.append(",");
                }
                sql.append(")");
                first = false;
            }
            
            if (hasCatNames) {
                if (!first) sql.append(" OR ");
                sql.append("(");
                for (int i = 0; i < catNames.size(); i++) {
                    sql.append("LOWER(TEN_SP) LIKE '%").append(catNames.get(i).toLowerCase()).append("%'");
                    if (i < catNames.size() - 1) sql.append(" OR ");
                }
                sql.append(")");
            }
            sql.append(")");
        }

        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString());
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToSanPham(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<SanPham> searchByPriceRange(double minPrice, double maxPrice) {
        return searchAdvanced(null, null, minPrice, maxPrice);
    }

    public static List<SanPham> searchByName(String keyword) {
        List<SanPham> list = new ArrayList<>();
        String sql = "SELECT * FROM SANPHAM WHERE LOWER(TEN_SP) LIKE LOWER(?)";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSanPham(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<SanPham> getProductsByCategory(int maLsp) {
        List<SanPham> list = new ArrayList<>();
        String sql = "SELECT * FROM SANPHAM WHERE MA_LSP = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maLsp);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSanPham(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private static SanPham mapResultSetToSanPham(ResultSet rs) throws Exception {
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
        return sp;
    }

    public static List<Model.BienTheSanPham> getBienTheByMaSp(int maSp) {
        List<Model.BienTheSanPham> list = new ArrayList<>();
        String sql = "SELECT * FROM BIENTHE_SANPHAM WHERE MA_SP = ? AND TRANG_THAI != 'Ngừng kinh doanh'";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maSp);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Model.BienTheSanPham bt = new Model.BienTheSanPham();
                    bt.setMaBienThe(rs.getInt("MA_BIENTHE"));
                    bt.setMaSp(rs.getInt("MA_SP"));
                    bt.setTenBienThe(rs.getString("TEN_BIENTHE"));
                    bt.setGiaBan(rs.getDouble("GIA_BAN"));
                    bt.setTrangThai(rs.getString("TRANG_THAI"));
                    list.add(bt);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
