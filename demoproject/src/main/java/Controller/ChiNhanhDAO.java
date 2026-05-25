package Controller;

import Model.ChiNhanh;
import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ChiNhanhDAO {

    public static List<ChiNhanh> getAllChiNhanh() throws Exception {
        List<ChiNhanh> list = new ArrayList<>();
        String sql = "SELECT MA_CN, TEN_CN, DIA_CHI, SDT_HOTLINE, TRANG_THAI FROM CHI_NHANH WHERE TRANG_THAI = 'Hoạt động' ORDER BY MA_CN ASC";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                ChiNhanh cn = new ChiNhanh();
                cn.setMaCn(rs.getInt("MA_CN"));
                cn.setTenCn(rs.getString("TEN_CN"));
                cn.setDiaChi(rs.getString("DIA_CHI"));
                cn.setSdtHotline(rs.getString("SDT_HOTLINE"));
                cn.setTrangThai(rs.getString("TRANG_THAI"));
                list.add(cn);
            }
        }
        return list;
    }
}
