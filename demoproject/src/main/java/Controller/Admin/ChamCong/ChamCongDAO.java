package Controller.Admin.ChamCong;

import ConnectDB.ConnectionUtils;
import java.sql.*;
import java.util.*;

public class ChamCongDAO {

    static {
        // Tự động bổ sung cột IS_CHU_CA nếu chưa tồn tại
        boSungCotChuCa();
        // Tự động bổ sung bảng NGAY_NGHI_CUA_HANG nếu chưa tồn tại
        boSungBangNgayNghi();
    }

    public static void boSungCotChuCa() {
        String sql = "ALTER TABLE LICH_LAM_VIEC ADD IS_CHU_CA NUMBER(1) DEFAULT 0";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (Exception e) {
            // Có thể cột đã tồn tại (lỗi ORA-01430), bỏ qua lỗi
        }
    }

    public static void boSungBangNgayNghi() {
        String sql = "CREATE TABLE NGAY_NGHI_CUA_HANG (" +
                     "    NGAY_NGHI DATE NOT NULL, " +
                     "    MA_CN NUMBER NOT NULL, " +
                     "    LY_DO NVARCHAR2(500) NOT NULL, " +
                     "    PRIMARY KEY (NGAY_NGHI, MA_CN)" +
                     ")";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (Exception e) {
            // Có thể bảng đã tồn tại, bỏ qua lỗi
        }
    }

    /** Lấy lý do nghỉ cửa hàng ngày cụ thể, trả về null nếu ngày đó không nghỉ */
    public static String getNgayNghiLyDo(java.sql.Date date, long maCN) {
        String sql = "SELECT LY_DO FROM NGAY_NGHI_CUA_HANG WHERE TRUNC(NGAY_NGHI) = TRUNC(?) AND MA_CN = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, date);
            ps.setLong(2, maCN);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("LY_DO");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Đăng ký ngày nghỉ cho toàn chi nhánh của cửa hàng */
    public static boolean baoNghiCuaHang(java.sql.Date date, long maCN, String lyDo) {
        String sql = "INSERT INTO NGAY_NGHI_CUA_HANG (NGAY_NGHI, MA_CN, LY_DO) VALUES (?, ?, ?)";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, date);
            ps.setLong(2, maCN);
            ps.setString(3, lyDo);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Xóa đăng ký ngày nghỉ cửa hàng (Mở cửa trở lại) */
    public static boolean huyNghiCuaHang(java.sql.Date date, long maCN) {
        String sql = "DELETE FROM NGAY_NGHI_CUA_HANG WHERE TRUNC(NGAY_NGHI) = TRUNC(?) AND MA_CN = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, date);
            ps.setLong(2, maCN);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Lấy tất cả ngày nghỉ trong tháng được chọn của chi nhánh */
    public static Map<Integer, String> getMonthOffDays(int year, int month, long maCN) {
        Map<Integer, String> map = new HashMap<>();
        String sql = "SELECT EXTRACT(DAY FROM NGAY_NGHI) AS NGAY, LY_DO " +
                     "FROM NGAY_NGHI_CUA_HANG " +
                     "WHERE EXTRACT(YEAR FROM NGAY_NGHI) = ? " +
                     "  AND EXTRACT(MONTH FROM NGAY_NGHI) = ? " +
                     "  AND MA_CN = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            ps.setLong(3, maCN);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("NGAY"), rs.getString("LY_DO"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    // ─── CA LÀM VIỆC ───────────────────────────────────────────────

    /** Lấy tất cả ca làm việc → {MA_CA, TEN_CA, GIO_BAT_DAU, GIO_KET_THUC} */
    public static List<Object[]> getAllCaLamViec() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT MA_CA, TEN_CA, TO_CHAR(GIO_BAT_DAU, 'HH24:MI') AS GIO_BAT_DAU, TO_CHAR(GIO_KET_THUC, 'HH24:MI') AS GIO_KET_THUC " +
                     "FROM CA_LAM_VIEC ORDER BY MA_CA";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getLong("MA_CA"),
                    rs.getString("TEN_CA"),
                    rs.getString("GIO_BAT_DAU"),
                    rs.getString("GIO_KET_THUC")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    /** Thêm ca mới (MA_CA tự sinh). Trả về true nếu thành công. */
    public static boolean themCaLamViec(String tenCa, String gioBatDau, String gioKetThuc) {
        String sql = "INSERT INTO CA_LAM_VIEC (TEN_CA, GIO_BAT_DAU, GIO_KET_THUC) VALUES (?, TO_TIMESTAMP('2000-01-01 ' || ?, 'YYYY-MM-DD HH24:MI'), TO_TIMESTAMP('2000-01-01 ' || ?, 'YYYY-MM-DD HH24:MI'))";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tenCa);
            ps.setString(2, gioBatDau);
            ps.setString(3, gioKetThuc);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Xóa ca theo mã. Trả về true nếu thành công. */
    public static boolean xoaCaLamViec(long maCa) {
        String sql = "DELETE FROM CA_LAM_VIEC WHERE MA_CA = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, maCa);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ─── TỔNG HỢP THEO THÁNG ───────────────────────────────────────

    /**
     * Tổng hợp chấm công theo tháng cho calendar.
     * Trả về Map: {ngày_trong_tháng → [tổng_lịch, đã_chấm_công]}
     */
    public static Map<Integer, int[]> getMonthSummary(int year, int month) {
        Map<Integer, int[]> map = new TreeMap<>();
        String sql = "SELECT EXTRACT(DAY FROM llv.NGAY_LAM) AS NGAY, " +
                     "       COUNT(llv.MA_LLV) AS TONG_LICH, " +
                     "       COUNT(cc.MA_CC) AS DA_CHAM " +
                     "FROM LICH_LAM_VIEC llv " +
                     "LEFT JOIN CHAM_CONG cc ON llv.MA_LLV = cc.MA_LLV " +
                     "WHERE EXTRACT(YEAR FROM llv.NGAY_LAM) = ? " +
                     "  AND EXTRACT(MONTH FROM llv.NGAY_LAM) = ? " +
                     "GROUP BY EXTRACT(DAY FROM llv.NGAY_LAM) " +
                     "ORDER BY NGAY";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int ngay  = rs.getInt("NGAY");
                    int tong  = rs.getInt("TONG_LICH");
                    int cham  = rs.getInt("DA_CHAM");
                    map.put(ngay, new int[]{tong, cham});
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return map;
    }

    // ─── LỊCH LÀM VIỆC THEO NGÀY ───────────────────────────────────

    /**
     * Lấy chi tiết lịch + trạng thái chấm công của 1 ngày.
     * Trả về: {MA_LLV, MA_CC(-1 nếu chưa chấm), HO_TEN, TEN_CA,
     *          GIO_BAT_DAU, GIO_KET_THUC,
     *          GIO_VAO(null), GIO_RA(null), TRANG_THAI, GHI_CHU, IS_CHU_CA, MA_NV, MA_CA}
     */
    public static List<Object[]> getDaySchedule(java.sql.Date date) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT llv.MA_LLV, " +
                     "       NVL(cc.MA_CC, -1) AS MA_CC, " +
                     "       nv.HO_TEN, " +
                     "       ca.TEN_CA, TO_CHAR(ca.GIO_BAT_DAU, 'HH24:MI') AS GIO_BAT_DAU, TO_CHAR(ca.GIO_KET_THUC, 'HH24:MI') AS GIO_KET_THUC, " +
                     "       cc.GIO_VAO_THUC_TE, cc.GIO_RA_THUC_TE, " +
                     "       NVL(cc.TRANG_THAI, N'Chưa chấm') AS TRANG_THAI, " +
                     "       cc.GHI_CHU, " +
                     "       NVL(llv.IS_CHU_CA, 0) AS IS_CHU_CA, " +
                     "       nv.MA_NV, " +
                     "       ca.MA_CA " +
                     "FROM LICH_LAM_VIEC llv " +
                     "JOIN NHAN_VIEN nv ON llv.MA_NV = nv.MA_NV " +
                     "JOIN CA_LAM_VIEC ca ON llv.MA_CA = ca.MA_CA " +
                     "LEFT JOIN CHAM_CONG cc ON llv.MA_LLV = cc.MA_LLV " +
                     "WHERE TRUNC(llv.NGAY_LAM) = TRUNC(?) " +
                     "ORDER BY ca.GIO_BAT_DAU, nv.HO_TEN";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getLong("MA_LLV"),
                        rs.getLong("MA_CC"),
                        rs.getString("HO_TEN"),
                        rs.getString("TEN_CA"),
                        rs.getString("GIO_BAT_DAU"),
                        rs.getString("GIO_KET_THUC"),
                        rs.getTimestamp("GIO_VAO_THUC_TE"),
                        rs.getTimestamp("GIO_RA_THUC_TE"),
                        rs.getString("TRANG_THAI"),
                        rs.getString("GHI_CHU"),
                        rs.getInt("IS_CHU_CA"),
                        rs.getLong("MA_NV"),
                        rs.getLong("MA_CA")
                    });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ─── LICH_LAM_VIEC CRUD ──────────────────────────────────────────

    /** Thêm NV vào lịch làm việc cho 1 ngày + ca. */
    public static boolean themLichLamViec(long maNV, long maCa, long maCN, java.sql.Date ngayLam) {
        String sql = "INSERT INTO LICH_LAM_VI_EC (MA_NV, MA_CA, MA_CN, NGAY_LAM, IS_CHU_CA) VALUES (?, ?, ?, ?, 0)";
        // Lưu ý: Đổi tên bảng thành LICH_LAM_VI_EC -> Khoan! Tên bảng thực tế là LICH_LAM_VIEC (1 dấu gạch dưới giữa LICH và LAM, và 1 giữa LAM và VIEC).
        // Tên bảng thực tế: LICH_LAM_VIEC!
        String sqlCorrect = "INSERT INTO LICH_LAM_VIEC (MA_NV, MA_CA, MA_CN, NGAY_LAM, IS_CHU_CA) VALUES (?, ?, ?, ?, 0)";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sqlCorrect)) {
            ps.setLong(1, maNV);
            ps.setLong(2, maCa);
            ps.setLong(3, maCN);
            ps.setDate(4, ngayLam);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Xóa lịch làm việc (cascade xóa chấm công liên quan). */
    public static boolean xoaLichLamViec(long maLLV) {
        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);
            // Xóa chấm công trước (FK constraint)
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM CHAM_CONG WHERE MA_LLV = ?")) {
                ps.setLong(1, maLLV);
                ps.executeUpdate();
            }
            // Xóa lịch
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM LICH_LAM_VIEC WHERE MA_LLV = ?")) {
                ps.setLong(1, maLLV);
                ps.executeUpdate();
            }
            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (con != null) try { con.rollback(); } catch (Exception ex) {}
            return false;
        } finally {
            if (con != null) try { con.setAutoCommit(true); con.close(); } catch (Exception ex) {}
        }
    }

    /** Cập nhật ca làm việc cho một dòng phân công (Sửa lịch làm việc) */
    public static boolean suaLichLamViec(long maLLV, long maCaNew) {
        String sql = "UPDATE LICH_LAM_VIEC SET MA_CA = ? WHERE MA_LLV = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, maCaNew);
            ps.setLong(2, maLLV);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Chỉ định một nhân viên làm chủ ca cho một ca cụ thể trong ngày */
    public static boolean chiDinhChuCa(long maLLV, long maCa, java.sql.Date ngayLam) {
        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);
            // Reset các nhân viên khác cùng ca làm việc và ngày đó về 0
            String sqlReset = "UPDATE LICH_LAM_VIEC SET IS_CHU_CA = 0 WHERE MA_CA = ? AND TRUNC(NGAY_LAM) = TRUNC(?)";
            try (PreparedStatement ps = con.prepareStatement(sqlReset)) {
                ps.setLong(1, maCa);
                ps.setDate(2, ngayLam);
                ps.executeUpdate();
            }
            // Đặt nhân viên này làm chủ ca (IS_CHU_CA = 1)
            String sqlSet = "UPDATE LICH_LAM_VIEC SET IS_CHU_CA = 1 WHERE MA_LLV = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlSet)) {
                ps.setLong(1, maLLV);
                ps.executeUpdate();
            }
            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (con != null) try { con.rollback(); } catch (Exception ex) {}
            return false;
        } finally {
            if (con != null) try { con.setAutoCommit(true); con.close(); } catch (Exception ex) {}
        }
    }

    // ─── CHẤM CÔNG ─────────────────────────────────────────────────

    /**
     * Chấm công cho 1 lịch làm việc.
     * Nếu đã có bản ghi → UPDATE, chưa có → INSERT.
     */
    public static boolean chamCong(long maLLV, Timestamp gioVao, Timestamp gioRa,
                                    String trangThai, String ghiChu) {
        try (Connection con = ConnectionUtils.getMyConnection()) {
            // Kiểm tra đã tồn tại chưa
            long maCC = -1;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT MA_CC FROM CHAM_CONG WHERE MA_LLV = ?")) {
                ps.setLong(1, maLLV);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) maCC = rs.getLong(1);
                }
            }

            if (maCC != -1) {
                // UPDATE
                String sql = "UPDATE CHAM_CONG SET GIO_VAO_THUC_TE = ?, GIO_RA_THUC_TE = ?, " +
                             "TRANG_THAI = ?, GHI_CHU = ? WHERE MA_CC = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setTimestamp(1, gioVao);
                    ps.setTimestamp(2, gioRa);
                    ps.setString(3, trangThai);
                    ps.setString(4, ghiChu);
                    ps.setLong(5, maCC);
                    return ps.executeUpdate() > 0;
                }
            } else {
                // INSERT
                boolean hasMaNV = false;
                try (ResultSet rsCols = con.getMetaData().getColumns(null, null, "CHAM_CONG", "MA_NV")) {
                    if (rsCols.next()) {
                        hasMaNV = true;
                    }
                } catch (Exception colEx) {
                    // ignore
                }

                if (hasMaNV) {
                    // Schema has MA_NV in CHAM_CONG
                    String sql = "INSERT INTO CHAM_CONG (MA_LLV, MA_NV, GIO_VAO_THUC_TE, GIO_RA_THUC_TE, " +
                                 "TRANG_THAI, GHI_CHU) VALUES (?, (SELECT MA_NV FROM LICH_LAM_VIEC WHERE MA_LLV = ?), ?, ?, ?, ?)";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setLong(1, maLLV);
                        ps.setLong(2, maLLV);
                        ps.setTimestamp(3, gioVao);
                        ps.setTimestamp(4, gioRa);
                        ps.setString(5, trangThai);
                        ps.setString(6, ghiChu);
                        return ps.executeUpdate() > 0;
                    }
                } else {
                    // Original schema (no MA_NV in CHAM_CONG)
                    String sql = "INSERT INTO CHAM_CONG (MA_LLV, GIO_VAO_THUC_TE, GIO_RA_THUC_TE, " +
                                 "TRANG_THAI, GHI_CHU) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setLong(1, maLLV);
                        ps.setTimestamp(2, gioVao);
                        ps.setTimestamp(3, gioRa);
                        ps.setString(4, trangThai);
                        ps.setString(5, ghiChu);
                        return ps.executeUpdate() > 0;
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ─── TÌM KIẾM NHÂN VIÊN CHO TAB CHẤM CÔNG ─────────────────────

    /**
     * Tìm lịch làm việc của NV theo mã NV và ngày.
     * Trả về: {MA_LLV, MA_NV, HO_TEN, TEN_CA, GIO_BAT_DAU, GIO_KET_THUC,
     *          MA_CC(-1 nếu chưa), GIO_VAO, GIO_RA, TRANG_THAI, GHI_CHU}
     * hoặc null nếu không có lịch.
     */
    public static Object[] findLichByNVAndDate(long maNV, java.sql.Date date) {
        String sql = "SELECT llv.MA_LLV, llv.MA_NV, nv.HO_TEN, " +
                     "       ca.TEN_CA, TO_CHAR(ca.GIO_BAT_DAU, 'HH24:MI') AS GIO_BAT_DAU, TO_CHAR(ca.GIO_KET_THUC, 'HH24:MI') AS GIO_KET_THUC, " +
                     "       NVL(cc.MA_CC, -1) AS MA_CC, " +
                     "       cc.GIO_VAO_THUC_TE, cc.GIO_RA_THUC_TE, " +
                     "       NVL(cc.TRANG_THAI, N'Chưa chấm') AS TRANG_THAI, " +
                     "       cc.GHI_CHU " +
                     "FROM LICH_LAM_VIEC llv " +
                     "JOIN NHAN_VIEN nv ON llv.MA_NV = nv.MA_NV " +
                     "JOIN CA_LAM_VIEC ca ON llv.MA_CA = ca.MA_CA " +
                     "LEFT JOIN CHAM_CONG cc ON llv.MA_LLV = cc.MA_LLV " +
                     "WHERE llv.MA_NV = ? AND TRUNC(llv.NGAY_LAM) = TRUNC(?) " +
                     "FETCH FIRST 1 ROWS ONLY";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, maNV);
            ps.setDate(2, date);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getLong("MA_LLV"),
                        rs.getLong("MA_NV"),
                        rs.getString("HO_TEN"),
                        rs.getString("TEN_CA"),
                        rs.getString("GIO_BAT_DAU"),
                        rs.getString("GIO_KET_THUC"),
                        rs.getLong("MA_CC"),
                        rs.getTimestamp("GIO_VAO_THUC_TE"),
                        rs.getTimestamp("GIO_RA_THUC_TE"),
                        rs.getString("TRANG_THAI"),
                        rs.getString("GHI_CHU")
                    };
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /** Lấy danh sách tất cả nhân viên {MA_NV, HO_TEN}. */
    public static List<Object[]> getAllNhanVien() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT MA_NV, HO_TEN FROM NHAN_VIEN WHERE TRANG_THAI = N'Đang làm việc' ORDER BY HO_TEN";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{rs.getLong("MA_NV"), rs.getString("HO_TEN")});
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    /** Lấy MA_CN của chi nhánh đầu tiên (fallback). */
    public static long getFirstMaCN() {
        String sql = "SELECT MA_CN FROM CHI_NHANH FETCH FIRST 1 ROWS ONLY";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 1L;
    }
}
