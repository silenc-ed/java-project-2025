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
        // Lấy thông tin cơ bản từ SAN_PHAM và giá bán nhỏ nhất từ BIEN_THE_SAN_PHAM
        String sql = "SELECT SP.*, (SELECT NVL(MIN(GIA_BAN), 0) FROM BIEN_THE_SAN_PHAM BT WHERE BT.MA_SP = SP.MA_SP AND BT.TRANG_THAI != 'Ngừng kinh doanh') AS GIA_BAN FROM SAN_PHAM SP";
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

    public static int getCoQuanLySerialByMaBienThe(int maBienThe) {
        String sql = "SELECT SP.CO_QUAN_LY_SERIAL FROM SAN_PHAM SP JOIN BIEN_THE_SAN_PHAM BT ON SP.MA_SP = BT.MA_SP WHERE BT.MA_BIENTHE = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maBienThe);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("CO_QUAN_LY_SERIAL");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1; // Default to 1 (Có serial) to be safe
    }

    public static List<SanPham> searchAdvanced(List<Integer> catIds, List<String> catNames, Double minPrice, Double maxPrice) {
        List<SanPham> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM (SELECT SP.*, (SELECT NVL(MIN(GIA_BAN), 0) FROM BIEN_THE_SAN_PHAM BT WHERE BT.MA_SP = SP.MA_SP AND BT.TRANG_THAI != 'Ngừng kinh doanh') AS GIA_BAN FROM SAN_PHAM SP) WHERE 1=1 ");
        
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
        String sql = "SELECT * FROM (SELECT SP.*, (SELECT NVL(MIN(GIA_BAN), 0) FROM BIEN_THE_SAN_PHAM BT WHERE BT.MA_SP = SP.MA_SP AND BT.TRANG_THAI != 'Ngừng kinh doanh') AS GIA_BAN FROM SAN_PHAM SP) WHERE LOWER(TEN_SP) LIKE LOWER(?)";
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
        String sql = "SELECT * FROM (SELECT SP.*, (SELECT NVL(MIN(GIA_BAN), 0) FROM BIEN_THE_SAN_PHAM BT WHERE BT.MA_SP = SP.MA_SP AND BT.TRANG_THAI != 'Ngừng kinh doanh') AS GIA_BAN FROM SAN_PHAM SP) WHERE MA_LSP = ?";
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

    public static List<SanPham> getRandomSanPham(int limit) {
        List<SanPham> list = new ArrayList<>();
        String sql = "SELECT * FROM (SELECT SP.*, (SELECT NVL(MIN(GIA_BAN), 0) FROM BIEN_THE_SAN_PHAM BT WHERE BT.MA_SP = SP.MA_SP AND BT.TRANG_THAI != 'Ngừng kinh doanh') AS GIA_BAN FROM SAN_PHAM SP) ORDER BY DBMS_RANDOM.VALUE FETCH FIRST ? ROWS ONLY";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
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

    public static List<SanPham> getTopSellingSanPham(int limit) {
        List<SanPham> list = new ArrayList<>();
        String sql = "SELECT * FROM (SELECT SP.*, (SELECT NVL(MIN(GIA_BAN), 0) FROM BIEN_THE_SAN_PHAM BT WHERE BT.MA_SP = SP.MA_SP AND BT.TRANG_THAI != 'Ngừng kinh doanh') AS GIA_BAN FROM SAN_PHAM SP) ORDER BY SO_LUONG_DA_BAN DESC FETCH FIRST ? ROWS ONLY";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
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

    public static List<SanPham> getCheapestSanPham(int limit) {
        List<SanPham> list = new ArrayList<>();
        String sql = "SELECT * FROM (SELECT SP.*, (SELECT NVL(MIN(GIA_BAN), 0) FROM BIEN_THE_SAN_PHAM BT WHERE BT.MA_SP = SP.MA_SP AND BT.TRANG_THAI != 'Ngừng kinh doanh') AS GIA_BAN FROM SAN_PHAM SP) WHERE GIA_BAN > 0 ORDER BY GIA_BAN ASC FETCH FIRST ? ROWS ONLY";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
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
        String sql = "SELECT * FROM BIEN_THE_SAN_PHAM WHERE MA_SP = ? AND TRANG_THAI != 'Ngừng kinh doanh'";
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

    public static boolean addSanPham(SanPham sp, double giaBan) throws Exception {
        String sqlSp = "INSERT INTO SAN_PHAM (MA_LSP, TEN_SP, TRANG_THAI, SO_LUONG_DA_BAN, DON_VI_TINH, MO_TA, CO_QUAN_LY_SERIAL) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlBt = "INSERT INTO BIEN_THE_SAN_PHAM (MA_SP, TEN_BIENTHE, GIA_BAN, TRANG_THAI) VALUES (?, ?, ?, ?)";
        
        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);
            
            int maSp = -1;
            try (PreparedStatement psSp = con.prepareStatement(sqlSp, new String[]{"MA_SP"})) {
                psSp.setInt(1, sp.getMaLsp());
                psSp.setString(2, sp.getTenSp());
                psSp.setString(3, sp.getTrangThai());
                psSp.setInt(4, sp.getSoLuongDaBan());
                psSp.setString(5, sp.getDonViTinh());
                psSp.setString(6, sp.getMoTa());
                psSp.setInt(7, sp.getCoQuanLySerial());
                psSp.executeUpdate();
                
                try (ResultSet rs = psSp.getGeneratedKeys()) {
                    if (rs.next()) {
                        maSp = rs.getInt(1);
                    }
                }
            }
            
            if (maSp == -1) {
                con.rollback();
                return false;
            }
            
            try (PreparedStatement psBt = con.prepareStatement(sqlBt)) {
                psBt.setInt(1, maSp);
                psBt.setString(2, sp.getTenSp() + " default");
                psBt.setDouble(3, giaBan);
                psBt.setString(4, sp.getTrangThai());
                psBt.executeUpdate();
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

    public static boolean updateSanPham(SanPham sp, double giaBan) throws Exception {
        String sqlSp = "UPDATE SAN_PHAM SET MA_LSP = ?, TEN_SP = ?, TRANG_THAI = ?, SO_LUONG_DA_BAN = ?, DON_VI_TINH = ?, MO_TA = ? WHERE MA_SP = ?";
        String sqlBt = "UPDATE BIEN_THE_SAN_PHAM SET GIA_BAN = ?, TRANG_THAI = ? WHERE MA_SP = ?";
        
        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);
            
            try (PreparedStatement psSp = con.prepareStatement(sqlSp)) {
                psSp.setInt(1, sp.getMaLsp());
                psSp.setString(2, sp.getTenSp());
                psSp.setString(3, sp.getTrangThai());
                psSp.setInt(4, sp.getSoLuongDaBan());
                psSp.setString(5, sp.getDonViTinh());
                psSp.setString(6, sp.getMoTa());
                psSp.setInt(7, sp.getMaSp());
                psSp.executeUpdate();
            }
            
            try (PreparedStatement psBt = con.prepareStatement(sqlBt)) {
                psBt.setDouble(1, giaBan);
                psBt.setString(2, sp.getTrangThai());
                psBt.setInt(3, sp.getMaSp());
                psBt.executeUpdate();
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

    public static boolean deleteSanPham(int maSp) throws Exception {
        String sqlKs = "DELETE FROM KHO_SERIAL WHERE MA_BIENTHE IN (SELECT MA_BIENTHE FROM BIEN_THE_SAN_PHAM WHERE MA_SP = ?)";
        String sqlTk = "DELETE FROM TON_KHO WHERE MA_BIENTHE IN (SELECT MA_BIENTHE FROM BIEN_THE_SAN_PHAM WHERE MA_SP = ?)";
        String sqlBt = "DELETE FROM BIEN_THE_SAN_PHAM WHERE MA_SP = ?";
        String sqlSp = "DELETE FROM SAN_PHAM WHERE MA_SP = ?";
        
        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);
            
            try (PreparedStatement psKs = con.prepareStatement(sqlKs)) {
                psKs.setInt(1, maSp);
                psKs.executeUpdate();
            }
            
            try (PreparedStatement psTk = con.prepareStatement(sqlTk)) {
                psTk.setInt(1, maSp);
                psTk.executeUpdate();
            }
            
            try (PreparedStatement psBt = con.prepareStatement(sqlBt)) {
                psBt.setInt(1, maSp);
                psBt.executeUpdate();
            }
            
            try (PreparedStatement psSp = con.prepareStatement(sqlSp)) {
                psSp.setInt(1, maSp);
                int count = psSp.executeUpdate();
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
