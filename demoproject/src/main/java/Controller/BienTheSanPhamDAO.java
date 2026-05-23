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
        String sql = "DELETE FROM BIEN_THE_SAN_PHAM WHERE MA_BIENTHE = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maBienThe);
            return ps.executeUpdate() > 0;
        }
    }
}
