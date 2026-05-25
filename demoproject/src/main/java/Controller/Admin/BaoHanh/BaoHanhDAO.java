package Controller.Admin.BaoHanh;

import ConnectDB.ConnectionUtils;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class BaoHanhDAO {
    
    public static Map<String, Object> getWarrantyDetails(String imei) {
        Map<String, Object> details = new HashMap<>();
        try (Connection con = ConnectionUtils.getMyConnection()) {
            String SQL = "{call SP_TRA_CUU_LICH_SU_BAO_HANH(?, ?)}";
            
            try (CallableStatement cstmt = con.prepareCall(SQL)) {
                cstmt.setString(1, imei.trim());
                cstmt.registerOutParameter(2, -10); // -10 is oracle.jdbc.OracleTypes.CURSOR
                
                cstmt.execute();
                
                try (ResultSet rs = (ResultSet) cstmt.getObject(2)) {
                    if (rs.next()) {
                        details.put("SERIAL_NUMBER", rs.getString("SERIAL_NUMBER"));
                        details.put("TEN_SP", rs.getString("TEN_SP"));
                        details.put("TEN_BIENTHE", rs.getString("TEN_BIENTHE"));
                        details.put("TRANG_THAI_SERIAL", rs.getString("TRANG_THAI_SERIAL"));
                        details.put("MA_HD", rs.getString("MA_HD"));
                        details.put("NGAY_MUA", rs.getDate("NGAY_MUA"));
                        details.put("PHUONG_THUC_TT", rs.getString("PHUONG_THUC_TT"));
                        details.put("MA_KH", rs.getString("MA_KH"));
                        details.put("TEN_KHACH_HANG", rs.getString("TEN_KHACH_HANG"));
                        details.put("SDT_KHACH_HANG", rs.getString("SDT_KHACH_HANG"));
                        details.put("CHI_NHANH_BAN", rs.getString("CHI_NHANH_BAN"));
                        details.put("BH_TU_NGAY", rs.getDate("BH_TU_NGAY"));
                        details.put("BH_DEN_NGAY", rs.getDate("BH_DEN_NGAY"));
                        details.put("SO_THANG_BH", rs.getInt("SO_THANG_BH"));
                        details.put("TRANG_THAI_BH", rs.getString("TRANG_THAI_BH"));
                        details.put("GHI_CHU_BH", rs.getString("GHI_CHU_BH"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().contains("-20090")) {
                details.put("ERROR", "NOT_FOUND");
            } else {
                details.put("ERROR", e.getMessage());
            }
        }
        return details; 
    }
}
