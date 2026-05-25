package Controller;

import ConnectDB.ConnectionUtils;
import Model.ChiNhanh;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TonKhoDAO {

    // Lấy tồn kho của một biến thể tại tất cả các chi nhánh
    public static List<Map<String, Object>> getTonKhoByBienThe(int maBienThe) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT cn.MA_CN, cn.TEN_CN, tk.SO_LUONG_TON " +
                     "FROM CHI_NHANH cn " +
                     "LEFT JOIN TON_KHO tk ON cn.MA_CN = tk.MA_CN AND tk.MA_BIENTHE = ? " +
                     "WHERE cn.TRANG_THAI = 'Hoạt động' " +
                     "ORDER BY cn.MA_CN ASC";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maBienThe);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("MA_CN", rs.getInt("MA_CN"));
                    row.put("TEN_CN", rs.getString("TEN_CN"));
                    row.put("SO_LUONG_TON", rs.getInt("SO_LUONG_TON")); // null becomes 0 if not exists?
                    // ResultSet.getInt returns 0 if null
                    list.add(row);
                }
            }
        }
        return list;
    }

    // Cập nhật số lượng tồn kho cho một biến thể tại một chi nhánh
    public static boolean updateTonKho(int maBienThe, int maCn, int soLuong) throws Exception {
        String sqlCheck = "SELECT COUNT(*) FROM TON_KHO WHERE MA_BIENTHE = ? AND MA_CN = ?";
        String sqlUpdate = "UPDATE TON_KHO SET SO_LUONG_TON = ? WHERE MA_BIENTHE = ? AND MA_CN = ?";
        String sqlInsert = "INSERT INTO TON_KHO (MA_BIENTHE, MA_CN, SO_LUONG_TON) VALUES (?, ?, ?)";
        
        try (Connection con = ConnectionUtils.getMyConnection()) {
            boolean exists = false;
            try (PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {
                psCheck.setInt(1, maBienThe);
                psCheck.setInt(2, maCn);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        exists = true;
                    }
                }
            }
            
            if (exists) {
                try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdate)) {
                    psUpdate.setInt(1, soLuong);
                    psUpdate.setInt(2, maBienThe);
                    psUpdate.setInt(3, maCn);
                    return psUpdate.executeUpdate() > 0;
                }
            } else {
                try (PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
                    psInsert.setInt(1, maBienThe);
                    psInsert.setInt(2, maCn);
                    psInsert.setInt(3, soLuong);
                    return psInsert.executeUpdate() > 0;
                }
            }
        }
    }
}
