package Controller.Customers.Warranty;

import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class WarrantyCheckingProcess {
    
    public String checking(String sdt, String imei, Date[] dates) throws Exception {
        // Mặc định nếu không tìm thấy trong CSDL
        String bien = "Không tìm thấy"; 
        
        try (Connection con = ConnectionUtils.getMyConnection()) {
            String SQL = "SELECT NGAY_BAT_DAU, NGAY_KET_THUC, TRANG_THAI " + 
                         "FROM BAOHANH WHERE SERIAL_IMEI = ?";
            
            PreparedStatement ps = con.prepareStatement(SQL);
            ps.setString(1, imei.trim());
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()){
                String trangThai = rs.getString("TRANG_THAI");
                // Nhét ngày tháng vào mảng
                dates[0] = rs.getDate("NGAY_BAT_DAU");
                dates[1] = rs.getDate("NGAY_KET_THUC");            
                
                // Gán kết quả chuỗi
                if("Còn hiệu lực".equals(trangThai)) {
                    bien = "Còn hiệu lực";
                } else if("Hết hiệu lực".equals(trangThai)){
                    bien = "Hết hiệu lực";
                } else{
                    bien = "Vô hiệu lực";
                }
            }
        }
        // Bắt buộc phải có dòng return này để trả chuỗi về cho Swing
        return bien; 
    }
}