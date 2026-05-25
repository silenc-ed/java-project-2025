package Controller.Admin.BaoHanh;

import ConnectDB.ConnectionUtils;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaoHanhAdminDAO {

    public static List<Map<String, Object>> searchWarranties(String keyword) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT BH.MA_BH, BH.SERIAL_NUMBER, SP.TEN_SP, BT.TEN_BIENTHE, " +
                     "BH.MA_HD, KH.HO_TEN AS TEN_KHACH_HANG, KH.SDT, CN.TEN_CN, " +
                     "BH.NGAY_BAT_DAU, BH.NGAY_KET_THUC, BH.TRANG_THAI " +
                     "FROM BAO_HANH BH " +
                     "JOIN KHO_SERIAL KS ON BH.SERIAL_NUMBER = KS.SERIAL_NUMBER " +
                     "JOIN BIEN_THE_SAN_PHAM BT ON KS.MA_BIENTHE = BT.MA_BIENTHE " +
                     "JOIN SAN_PHAM SP ON BT.MA_SP = SP.MA_SP " +
                     "JOIN HOA_DON HD ON BH.MA_HD = HD.MA_HD " +
                     "LEFT JOIN KHACH_HANG KH ON HD.MA_KH = KH.MA_KH " +
                     "LEFT JOIN CHI_NHANH CN ON HD.MA_CN = CN.MA_CN ";

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += "WHERE LOWER(BH.SERIAL_NUMBER) LIKE ? " +
                   "   OR LOWER(KH.SDT) LIKE ? " +
                   "   OR LOWER(KH.HO_TEN) LIKE ? ";
        }
        
        sql += "ORDER BY BH.NGAY_BAT_DAU DESC";

        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (keyword != null && !keyword.trim().isEmpty()) {
                String search = "%" + keyword.trim().toLowerCase() + "%";
                ps.setString(1, search);
                ps.setString(2, search);
                ps.setString(3, search);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("MA_BH", rs.getInt("MA_BH"));
                    map.put("SERIAL_NUMBER", rs.getString("SERIAL_NUMBER"));
                    map.put("TEN_SP", rs.getString("TEN_SP"));
                    map.put("TEN_BIENTHE", rs.getString("TEN_BIENTHE"));
                    map.put("MA_HD", rs.getInt("MA_HD"));
                    map.put("TEN_KHACH_HANG", rs.getString("TEN_KHACH_HANG") != null ? rs.getString("TEN_KHACH_HANG") : "Khách lẻ");
                    map.put("SDT", rs.getString("SDT") != null ? rs.getString("SDT") : "");
                    map.put("TEN_CN", rs.getString("TEN_CN") != null ? rs.getString("TEN_CN") : "");
                    map.put("NGAY_BAT_DAU", rs.getDate("NGAY_BAT_DAU"));
                    map.put("NGAY_KET_THUC", rs.getDate("NGAY_KET_THUC"));
                    map.put("TRANG_THAI", rs.getString("TRANG_THAI"));
                    list.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Map<String, Object>> getAllWarranties() {
        return searchWarranties(null);
    }

    public static void expireAllOverdue() {
        try (Connection con = ConnectionUtils.getMyConnection();
             CallableStatement cs = con.prepareCall("{call SP_QUET_BH_HET_HAN}")) {
            cs.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
