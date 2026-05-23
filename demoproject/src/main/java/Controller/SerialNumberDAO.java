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
        String sql = "SELECT * FROM SERIAL_NUMBER WHERE MA_BIENTHE = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maBienThe);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SerialNumber sn = new SerialNumber();
                    sn.setMaSerial(rs.getString("MA_SERIAL"));
                    sn.setMaBienThe(rs.getInt("MA_BIENTHE"));
                    sn.setTrangThai(rs.getString("TRANG_THAI"));
                    sn.setNgayNhap(rs.getDate("NGAY_NHAP"));
                    list.add(sn);
                }
            }
        }
        return list;
    }

    public static boolean addSerialNumber(SerialNumber sn) throws Exception {
        String sql = "INSERT INTO SERIAL_NUMBER (MA_SERIAL, MA_BIENTHE, TRANG_THAI, NGAY_NHAP) VALUES (?, ?, ?, ?)";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sn.getMaSerial());
            ps.setInt(2, sn.getMaBienThe());
            ps.setString(3, sn.getTrangThai());
            ps.setDate(4, sn.getNgayNhap() != null ? sn.getNgayNhap() : new java.sql.Date(System.currentTimeMillis()));
            return ps.executeUpdate() > 0;
        }
    }

    public static boolean updateSerialNumber(SerialNumber sn, String oldMaSerial) throws Exception {
        String sql = "UPDATE SERIAL_NUMBER SET MA_SERIAL = ?, TRANG_THAI = ? WHERE MA_SERIAL = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sn.getMaSerial());
            ps.setString(2, sn.getTrangThai());
            ps.setString(3, oldMaSerial);
            return ps.executeUpdate() > 0;
        }
    }

    public static boolean deleteSerialNumber(String maSerial) throws Exception {
        String sql = "DELETE FROM SERIAL_NUMBER WHERE MA_SERIAL = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maSerial);
            return ps.executeUpdate() > 0;
        }
    }
}
