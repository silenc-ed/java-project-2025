package Controller.Admin;

import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardDAO {

    public static double getTongLoiNhuan(Date from, Date to) {
        String sql = "SELECT NVL(SUM(h.THANH_TIEN - NVL(cost.CHI_PHI, 0)), 0) AS LOI_NHUAN " +
                     "FROM HOADON h " +
                     "LEFT JOIN (" +
                     "  SELECT ct.MA_HD, SUM(ct.SO_LUONG * cpn.DON_GIA_NHAP) AS CHI_PHI " +
                     "  FROM CHITIET_HOADON ct " +
                     "  JOIN BIENTHE_SANPHAM bt ON ct.MA_SP = bt.MA_SP " +
                     "  JOIN (SELECT MA_BIENTHE, DON_GIA_NHAP FROM CHITIET_PHIEUNHAP " +
                     "        WHERE (MA_BIENTHE, MA_PN) IN " +
                     "        (SELECT MA_BIENTHE, MAX(MA_PN) FROM CHITIET_PHIEUNHAP GROUP BY MA_BIENTHE)) cpn " +
                     "  ON bt.MA_BIENTHE = cpn.MA_BIENTHE " +
                     "  GROUP BY ct.MA_HD" +
                     ") cost ON h.MA_HD = cost.MA_HD " +
                     "WHERE h.THOI_GIAN_LAP >= ? AND h.THOI_GIAN_LAP < ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(from.getTime()));
            ps.setTimestamp(2, new Timestamp(to.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("LOI_NHUAN");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static double getTongDoanhThu(Date from, Date to) {
        String sql = "SELECT NVL(SUM(THANH_TIEN), 0) AS DOANH_THU FROM HOADON " +
                     "WHERE THOI_GIAN_LAP >= ? AND THOI_GIAN_LAP < ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(from.getTime()));
            ps.setTimestamp(2, new Timestamp(to.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("DOANH_THU");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static Map<String, Double> getDoanhThuTheoThang(int year) {
        Map<String, Double> map = new LinkedHashMap<>();
        String sql = "SELECT TO_CHAR(THOI_GIAN_LAP, 'MM') AS THANG, " +
                     "NVL(SUM(THANH_TIEN), 0) AS DOANH_THU " +
                     "FROM HOADON " +
                     "WHERE EXTRACT(YEAR FROM THOI_GIAN_LAP) = ? " +
                     "GROUP BY TO_CHAR(THOI_GIAN_LAP, 'MM') " +
                     "ORDER BY THANG";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put("T" + Integer.parseInt(rs.getString("THANG")),
                            rs.getDouble("DOANH_THU"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static Map<String, Double> getDoanhThuTheoNgay(Date from, Date to) {
        Map<String, Double> map = new LinkedHashMap<>();
        String sql = "SELECT TO_CHAR(THOI_GIAN_LAP, 'DD/MM') AS NGAY, " +
                     "NVL(SUM(THANH_TIEN), 0) AS DOANH_THU " +
                     "FROM HOADON " +
                     "WHERE THOI_GIAN_LAP >= ? AND THOI_GIAN_LAP < ? " +
                     "GROUP BY TO_CHAR(THOI_GIAN_LAP, 'DD/MM') " +
                     "ORDER BY MIN(THOI_GIAN_LAP)";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(from.getTime()));
            ps.setTimestamp(2, new Timestamp(to.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("NGAY"), rs.getDouble("DOANH_THU"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static List<Object[]> getTopSanPhamThinhHanh(Date from, Date to, int limit) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT sp.TEN_SP, lsp.TEN_LSP, " +
                     "NVL(SUM(ct.SO_LUONG), 0) AS TONG_BAN, " +
                     "NVL(SUM(ct.THANH_TIEN), 0) AS DOANH_THU " +
                     "FROM CHITIET_HOADON ct " +
                     "JOIN HOADON h ON ct.MA_HD = h.MA_HD " +
                     "JOIN SANPHAM sp ON ct.MA_SP = sp.MA_SP " +
                     "JOIN LOAI_SANPHAM lsp ON sp.MA_LSP = lsp.MA_LSP " +
                     "WHERE h.THOI_GIAN_LAP >= ? AND h.THOI_GIAN_LAP < ? " +
                     "GROUP BY sp.MA_SP, sp.TEN_SP, lsp.TEN_LSP " +
                     "ORDER BY TONG_BAN DESC " +
                     "FETCH FIRST ? ROWS ONLY";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(from.getTime()));
            ps.setTimestamp(2, new Timestamp(to.getTime()));
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("TEN_SP"),
                        rs.getString("TEN_LSP"),
                        rs.getLong("TONG_BAN"),
                        rs.getLong("DOANH_THU")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Object[]> getTopLoaiSanPhamThinhHanh(Date from, Date to, int limit) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT lsp.TEN_LSP, " +
                     "NVL(SUM(ct.SO_LUONG), 0) AS TONG_BAN, " +
                     "NVL(SUM(ct.THANH_TIEN), 0) AS DOANH_THU " +
                     "FROM CHITIET_HOADON ct " +
                     "JOIN HOADON h ON ct.MA_HD = h.MA_HD " +
                     "JOIN SANPHAM sp ON ct.MA_SP = sp.MA_SP " +
                     "JOIN LOAI_SANPHAM lsp ON sp.MA_LSP = lsp.MA_LSP " +
                     "WHERE h.THOI_GIAN_LAP >= ? AND h.THOI_GIAN_LAP < ? " +
                     "GROUP BY lsp.MA_LSP, lsp.TEN_LSP " +
                     "ORDER BY TONG_BAN DESC " +
                     "FETCH FIRST ? ROWS ONLY";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(from.getTime()));
            ps.setTimestamp(2, new Timestamp(to.getTime()));
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("TEN_LSP"),
                        rs.getLong("TONG_BAN"),
                        rs.getLong("DOANH_THU")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static long getTongSanPhamBan(Date from, Date to) {
        String sql = "SELECT NVL(SUM(ct.SO_LUONG), 0) AS TONG FROM CHITIET_HOADON ct " +
                     "JOIN HOADON h ON ct.MA_HD = h.MA_HD " +
                     "WHERE h.THOI_GIAN_LAP >= ? AND h.THOI_GIAN_LAP < ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(from.getTime()));
            ps.setTimestamp(2, new Timestamp(to.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("TONG");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long getTongDonHang(Date from, Date to) {
        String sql = "SELECT COUNT(*) AS TONG FROM HOADON " +
                     "WHERE THOI_GIAN_LAP >= ? AND THOI_GIAN_LAP < ?";
        return countQuery(sql, from, to);
    }

    public static List<Object[]> getDonHangGanDay(Date from, Date to, int limit) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT h.MA_HD, NVL(kh.HO_TEN, 'Khách lẻ') AS HO_TEN, " +
                     "(SELECT LISTAGG(sp.TEN_SP, ', ') WITHIN GROUP (ORDER BY sp.TEN_SP) " +
                     " FROM CHITIET_HOADON ct2 JOIN SANPHAM sp ON ct2.MA_SP = sp.MA_SP " +
                     " WHERE ct2.MA_HD = h.MA_HD AND ROWNUM <= 2) AS SAN_PHAM, " +
                     "h.THANH_TIEN, " +
                     "TO_CHAR(h.THOI_GIAN_LAP, 'DD/MM/YYYY HH24:MI') AS THOI_GIAN " +
                     "FROM HOADON h " +
                     "LEFT JOIN KHACHHANG kh ON h.MA_KH = kh.MA_KH " +
                     "WHERE h.THOI_GIAN_LAP >= ? AND h.THOI_GIAN_LAP < ? " +
                     "ORDER BY h.THOI_GIAN_LAP DESC " +
                     "FETCH FIRST ? ROWS ONLY";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(from.getTime()));
            ps.setTimestamp(2, new Timestamp(to.getTime()));
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getLong("MA_HD"),
                        rs.getString("HO_TEN"),
                        rs.getString("SAN_PHAM"),
                        rs.getLong("THANH_TIEN"),
                        rs.getString("THOI_GIAN")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private static long countQuery(String sql, Date from, Date to) {
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(from.getTime()));
            ps.setTimestamp(2, new Timestamp(to.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String formatVND(double amount) {
        if (amount >= 1_000_000_000) {
            return String.format("%.1f tỷ", amount / 1_000_000_000.0);
        } else if (amount >= 1_000_000) {
            return String.format("%.1f triệu", amount / 1_000_000.0);
        } else if (amount >= 1_000) {
            return String.format("%.0f K", amount / 1_000.0);
        }
        return String.format("%.0f", amount);
    }
}
