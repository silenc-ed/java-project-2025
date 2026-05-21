package Controller.Admin.BaoHanh;

import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

public class BaoHanhDAO {
    
    public static String checking(String sdt, String imei, Date[] dates) {
        String bien = "Không tìm thấy"; 
        try (Connection con = ConnectionUtils.getMyConnection()) {
            String SQL = "SELECT NGAY_BAT_DAU, NGAY_KET_THUC, TRANG_THAI " + 
                         "FROM BAO_HANH WHERE SERIAL_NUMBER = ?";
            
            try (PreparedStatement ps = con.prepareStatement(SQL)) {
                ps.setString(1, imei.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()){
                        String trangThai = rs.getString("TRANG_THAI");
                        dates[0] = rs.getDate("NGAY_BAT_DAU");
                        dates[1] = rs.getDate("NGAY_KET_THUC");            
                        
                        if("Còn hiệu lực".equals(trangThai)) {
                            bien = "Còn hiệu lực";
                        } else if("Hết hiệu lực".equals(trangThai)){
                            bien = "Hết hiệu lực";
                        } else{
                            bien = "Vô hiệu lực";
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bien; 
    }

    public static Map<String, String> getProductDetailsByImei(String imei) {
        Map<String, String> details = new HashMap<>();
        String checkProdSQL = "SELECT SP.TEN_SP, BT.TEN_BIENTHE "
                            + "FROM KHO_SERIAL KS "
                            + "JOIN BIEN_THE_SAN_PHAM BT ON KS.MA_BIENTHE = BT.MA_BIENTHE "
                            + "JOIN SAN_PHAM SP ON BT.MA_SP = SP.MA_SP "
                            + "WHERE KS.SERIAL_NUMBER = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(checkProdSQL)) {
            ps.setString(1, imei);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    details.put("TEN_SP", rs.getString("TEN_SP"));
                    details.put("TEN_BIENTHE", rs.getString("TEN_BIENTHE"));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return details;
    }
}
