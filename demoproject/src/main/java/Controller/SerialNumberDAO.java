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
        String sql = "SELECT ks.SERIAL_NUMBER, ks.MA_BIENTHE, ks.TRANG_THAI, pn.NGAY_NHAP " +
                     "FROM KHO_SERIAL ks " +
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
        try (Connection con = ConnectionUtils.getMyConnection()) {
            int maCN = -1;
            String sqlCN = "SELECT MA_CN FROM CHI_NHANH WHERE ROWNUM <= 1";
            try (PreparedStatement psCN = con.prepareStatement(sqlCN);
                 ResultSet rsCN = psCN.executeQuery()) {
                if (rsCN.next()) {
                    maCN = rsCN.getInt("MA_CN");
                }
            }
            if (maCN == -1) {
                // Bảng CHI_NHANH đang trống, chèn một chi nhánh mặc định trước để thỏa mãn khóa ngoại
                String sqlInsertCN = "INSERT INTO CHI_NHANH (TEN_CN, DIA_CHI, SDT_HOTLINE, TRANG_THAI) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psInsertCN = con.prepareStatement(sqlInsertCN)) {
                    psInsertCN.setString(1, "Chi nhánh Quận 1");
                    psInsertCN.setString(2, "Hồ Chí Minh");
                    psInsertCN.setString(3, "19001111");
                    psInsertCN.setString(4, "Hoạt động");
                    psInsertCN.executeUpdate();
                }
                // Truy vấn lại để lấy mã chi nhánh vừa tạo
                try (PreparedStatement psCN = con.prepareStatement(sqlCN);
                     ResultSet rsCN = psCN.executeQuery()) {
                    if (rsCN.next()) {
                        maCN = rsCN.getInt("MA_CN");
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, sn.getMaSerial());
                ps.setInt(2, sn.getMaBienThe());
                ps.setInt(3, maCN);
                ps.setString(4, sn.getTrangThai() != null ? sn.getTrangThai() : "Trong kho");
                return ps.executeUpdate() > 0;
            }
        }
    }

    public static boolean updateSerialNumber(SerialNumber sn, String oldMaSerial) throws Exception {
        String sql = "UPDATE KHO_SERIAL SET SERIAL_NUMBER = ?, TRANG_THAI = ? WHERE SERIAL_NUMBER = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sn.getMaSerial());
            ps.setString(2, sn.getTrangThai());
            ps.setString(3, oldMaSerial);
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
