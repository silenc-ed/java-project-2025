package Controller;

import ConnectDB.ConnectionUtils;
import Model.SerialNumber;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SerialNumberDAO {

    public static List<SerialNumber> getSerialNumbersByMaBienThe(int maBienThe) throws Exception {
        List<SerialNumber> list = new ArrayList<>();
        String sql = "SELECT ks.SERIAL_NUMBER, ks.MA_BIENTHE, ks.MA_CN, cn.TEN_CN, ks.TRANG_THAI, pn.NGAY_NHAP " +
                     "FROM KHO_SERIAL ks " +
                     "LEFT JOIN CHI_NHANH cn ON ks.MA_CN = cn.MA_CN " +
                     "LEFT JOIN PHIEU_NHAP pn ON ks.MA_PN = pn.MA_PN " +
                     "WHERE ks.MA_BIENTHE = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maBienThe);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SerialNumber sn = new SerialNumber();
                    sn.setMaSerial(rs.getString("SERIAL_NUMBER"));
                    sn.setMaBienThe(rs.getInt("MA_BIENTHE"));
                    sn.setMaCn(rs.getInt("MA_CN"));
                    sn.setTenCn(rs.getString("TEN_CN"));
                    sn.setTrangThai(rs.getString("TRANG_THAI"));
                    sn.setNgayNhap(rs.getDate("NGAY_NHAP"));
                    list.add(sn);
                }
            }
        }
        return list;
    }

    public static boolean addSerialNumber(SerialNumber sn) throws Exception {
        String sql = "INSERT INTO KHO_SERIAL (SERIAL_NUMBER, MA_BIENTHE, MA_CN, TRANG_THAI) VALUES (?, ?, ?, ?)";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sn.getMaSerial());
            ps.setInt(2, sn.getMaBienThe());
            ps.setInt(3, sn.getMaCn());
            ps.setString(4, sn.getTrangThai() != null ? sn.getTrangThai() : "Trong kho");
            return ps.executeUpdate() > 0;
        }
    }

    public static boolean updateSerialNumber(SerialNumber sn, String oldMaSerial) throws Exception {
        String sql = "UPDATE KHO_SERIAL SET SERIAL_NUMBER = ?, MA_CN = ?, TRANG_THAI = ? WHERE SERIAL_NUMBER = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sn.getMaSerial());
            ps.setInt(2, sn.getMaCn());
            ps.setString(3, sn.getTrangThai());
            ps.setString(4, oldMaSerial);
            return ps.executeUpdate() > 0;
        }
    }

    public static boolean deleteSerialNumber(String maSerial) throws Exception {
        String sql = "DELETE FROM KHO_SERIAL WHERE SERIAL_NUMBER = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maSerial);
            return ps.executeUpdate() > 0;
        }
    }
}
