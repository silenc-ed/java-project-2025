package Controller.Customers.Voucher;

import ConnectDB.ConnectionUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO phục vụ phần khuyến mãi của khách hàng (Quy đổi điểm và Kho khuyến mãi cá nhân)
 */
public class CustomerVoucherDAO {

    public CustomerVoucherDAO() {
        // Tự động kiểm tra và nâng cấp cơ sở dữ liệu nếu cần
        try {
            initializeDatabaseSchema();
        } catch (Exception e) {
            System.err.println("Lỗi tự động khởi tạo cơ sở dữ liệu khuyến mãi: " + e.getMessage());
        }
    }

    /**
     * Tự động khởi tạo/cập nhật cấu trúc database nếu chưa có (Self-healing schema)
     */
    public synchronized void initializeDatabaseSchema() throws Exception {
        try (Connection con = ConnectionUtils.getMyConnection()) {
            // 1. Kiểm tra và bổ sung cột DIEM_DOI cho KHUYEN_MAI
            if (!isColumnExist(con, "KHUYEN_MAI", "DIEM_DOI")) {
                try (Statement st = con.createStatement()) {
                    st.execute("ALTER TABLE KHUYEN_MAI ADD DIEM_DOI NUMBER(10, 0) DEFAULT 0 CHECK (DIEM_DOI >= 0)");
                    System.out.println("Đã thêm cột DIEM_DOI vào bảng KHUYEN_MAI thành công!");
                }
            }

            // 2. Kiểm tra và bổ sung cột SO_LUONG_CL cho KHUYEN_MAI
            if (!isColumnExist(con, "KHUYEN_MAI", "SO_LUONG_CL")) {
                try (Statement st = con.createStatement()) {
                    st.execute("ALTER TABLE KHUYEN_MAI ADD SO_LUONG_CL NUMBER(10, 0) DEFAULT 0 CHECK (SO_LUONG_CL >= 0)");
                    System.out.println("Đã thêm cột SO_LUONG_CL vào bảng KHUYEN_MAI thành công!");
                }
            }

            // 3. Kiểm tra và tạo bảng VI_KHUYENMAI
            if (!isTableExist(con, "VI_KHUYENMAI")) {
                String createTableSQL = "CREATE TABLE VI_KHUYENMAI ("
                        + "    MA_KH NUMBER NOT NULL, "
                        + "    MA_KM NUMBER NOT NULL, "
                        + "    SO_LUONG NUMBER(5, 0) DEFAULT 1, "
                        + "    NGAY_LUU DATE DEFAULT SYSDATE, "
                        + "    PRIMARY KEY (MA_KH, MA_KM), "
                        + "    CONSTRAINT FK_VV_KH FOREIGN KEY (MA_KH) REFERENCES KHACH_HANG(MA_KH), "
                        + "    CONSTRAINT FK_VV_KM FOREIGN KEY (MA_KM) REFERENCES KHUYEN_MAI(MA_KM), "
                        + "    CONSTRAINT CHK_VV_SL CHECK (SO_LUONG >= 0) "
                        + ")";
                try (Statement st = con.createStatement()) {
                    st.execute(createTableSQL);
                    System.out.println("Đã tạo bảng VI_KHUYENMAI thành công!");
                }
            }
        }
    }

    private boolean isColumnExist(Connection con, String tableName, String columnName) {
        String sql = "SELECT 1 FROM all_tab_columns WHERE UPPER(table_name) = ? AND UPPER(column_name) = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tableName.toUpperCase());
            ps.setString(2, columnName.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTableExist(Connection con, String tableName) {
        String sql = "SELECT 1 FROM all_tables WHERE UPPER(table_name) = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tableName.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Lấy danh sách các khuyến mãi ĐANG KÍCH HOẠT và HỢP LỆ đổi bằng điểm.
     */
    public List<Map<String, Object>> getAvailablePromotions() throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        // Query các chương trình khuyến mãi có trạng thái "Có hiệu lực", còn thời hạn và yêu cầu điểm
        String sql = "SELECT KM.MA_KM, KM.TEN_KM, KM.GIA_TRI, "
                   + "KM.NGAY_BAT_DAU, KM.NGAY_KET_THUC, KM.DIEM_DOI, KM.SO_LUONG_CL, "
                   + "LKM.TEN_LOAI_KM "
                   + "FROM KHUYEN_MAI KM "
                   + "JOIN LOAI_KHUYEN_MAI LKM ON KM.MA_LOAI_KM = LKM.MA_LOAI_KM "
                   + "WHERE KM.TRANG_THAI = 'Có hiệu lực' "
                   + "  AND KM.NGAY_BAT_DAU <= CURRENT_TIMESTAMP "
                   + "  AND KM.NGAY_KET_THUC >= CURRENT_TIMESTAMP "
                   + "ORDER BY KM.DIEM_DOI ASC, KM.MA_KM DESC";

        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("MA_KM", rs.getInt("MA_KM"));
                row.put("TEN_KM", rs.getString("TEN_KM"));
                row.put("GIA_TRI", rs.getLong("GIA_TRI"));
                row.put("NGAY_BAT_DAU", rs.getTimestamp("NGAY_BAT_DAU"));
                row.put("NGAY_KET_THUC", rs.getTimestamp("NGAY_KET_THUC"));
                row.put("DIEM_DOI", rs.getInt("DIEM_DOI"));
                row.put("SO_LUONG_CL", rs.getInt("SO_LUONG_CL"));
                row.put("TEN_LOAI_KM", rs.getString("TEN_LOAI_KM"));
                results.add(row);
            }
        }
        return results;
    }

    /**
     * Lấy danh sách kho khuyến mãi cá nhân của một khách hàng (VI_KHUYENMAI join KHUYENMAI)
     */
    public List<Map<String, Object>> getMyVouchers(long maKH) throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT VK.MA_KM, VK.SO_LUONG, VK.NGAY_LUU, "
                   + "KM.TEN_KM, KM.GIA_TRI, KM.NGAY_KET_THUC, "
                   + "LKM.TEN_LOAI_KM "
                   + "FROM VI_KHUYENMAI VK "
                   + "JOIN KHUYEN_MAI KM ON VK.MA_KM = KM.MA_KM "
                   + "JOIN LOAI_KHUYEN_MAI LKM ON KM.MA_LOAI_KM = LKM.MA_LOAI_KM "
                   + "WHERE VK.MA_KH = ? "
                   + "ORDER BY VK.NGAY_LUU DESC";

        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, maKH);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("MA_KM", rs.getInt("MA_KM"));
                    row.put("SO_LUONG", rs.getInt("SO_LUONG"));
                    row.put("NGAY_LUU", rs.getDate("NGAY_LUU"));
                    row.put("TEN_KM", rs.getString("TEN_KM"));
                    row.put("GIA_TRI", rs.getLong("GIA_TRI"));
                    row.put("NGAY_KET_THUC", rs.getTimestamp("NGAY_KET_THUC"));
                    row.put("TEN_LOAI_KM", rs.getString("TEN_LOAI_KM"));
                    results.add(row);
                }
            }
        }
        return results;
    }

    /**
     * Thực hiện giao dịch đổi điểm lấy voucher trong cùng một TRANSACTION.
     */
    public void redeemVoucher(long maKH, int maKM, int diemCanDoi) throws Exception {
        Connection con = null;
        PreparedStatement psLockKH = null;
        PreparedStatement psLockKM = null;
        PreparedStatement psSubDiem = null;
        PreparedStatement psSubStock = null;
        PreparedStatement psMergeWallet = null;

        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false); // Bắt đầu Transaction

            // 1. Lock và kiểm tra điểm tích lũy của khách hàng
            String lockKHSQL = "SELECT DIEM_TICH_LUY FROM KHACH_HANG WHERE MA_KH = ? FOR UPDATE";
            psLockKH = con.prepareStatement(lockKHSQL);
            psLockKH.setLong(1, maKH);
            long diemHienTai = 0;
            try (ResultSet rsKH = psLockKH.executeQuery()) {
                if (rsKH.next()) {
                    diemHienTai = rsKH.getLong("DIEM_TICH_LUY");
                } else {
                    throw new Exception("Không tìm thấy thông tin khách hàng!");
                }
            }

            if (diemHienTai < diemCanDoi) {
                throw new Exception("Không đủ điểm! Vui lòng chọn mã khác");
            }

            // 2. Lock và kiểm tra tồn kho của mã khuyến mãi
            String lockKMSQL = "SELECT SO_LUONG_CL, TRANG_THAI, NGAY_KET_THUC FROM KHUYEN_MAI WHERE MA_KM = ? FOR UPDATE";
            psLockKM = con.prepareStatement(lockKMSQL);
            psLockKM.setInt(1, maKM);
            int tonKho = 0;
            String trangThai = "";
            Timestamp ngayKT = null;
            try (ResultSet rsKM = psLockKM.executeQuery()) {
                if (rsKM.next()) {
                    tonKho = rsKM.getInt("SO_LUONG_CL");
                    trangThai = rsKM.getString("TRANG_THAI");
                    ngayKT = rsKM.getTimestamp("NGAY_KET_THUC");
                } else {
                    throw new Exception("Không tìm thấy thông tin khuyến mãi!");
                }
            }

            // Kiểm tra trạng thái hoạt động và hạn dùng
            if (!"Có hiệu lực".equalsIgnoreCase(trangThai)) {
                throw new Exception("Chương trình khuyến mãi này hiện không hoạt động!");
            }
            if (ngayKT != null && ngayKT.before(new java.util.Date())) {
                throw new Exception("Mã khuyến mãi này đã hết hạn sử dụng!");
            }
            if (tonKho <= 0) {
                throw new Exception("Rất tiếc, mã khuyến mãi này đã hết lượt đổi.");
            }

            // 3. Thực hiện trừ điểm khách hàng
            String subDiemSQL = "UPDATE KHACH_HANG SET DIEM_TICH_LUY = DIEM_TICH_LUY - ? WHERE MA_KH = ?";
            psSubDiem = con.prepareStatement(subDiemSQL);
            psSubDiem.setInt(1, diemCanDoi);
            psSubDiem.setLong(2, maKH);
            psSubDiem.executeUpdate();

            // 4. Giảm số lượng voucher còn lại
            String subStockSQL = "UPDATE KHUYEN_MAI SET SO_LUONG_CL = SO_LUONG_CL - 1 WHERE MA_KM = ?";
            psSubStock = con.prepareStatement(subStockSQL);
            psSubStock.setInt(1, maKM);
            psSubStock.executeUpdate();

            // 5. Thêm/Cập nhật bản ghi vào ví khuyến mãi VI_KHUYENMAI (Sử dụng MERGE hoặc INSERT/UPDATE tương đương)
            String mergeSQL = "MERGE INTO VI_KHUYENMAI target "
                            + "USING (SELECT ? AS MA_KH, ? AS MA_KM FROM dual) source "
                            + "ON (target.MA_KH = source.MA_KH AND target.MA_KM = source.MA_KM) "
                            + "WHEN MATCHED THEN "
                            + "    UPDATE SET target.SO_LUONG = target.SO_LUONG + 1 "
                            + "WHEN NOT MATCHED THEN "
                            + "    INSERT (MA_KH, MA_KM, SO_LUONG, NGAY_LUU) "
                            + "    VALUES (source.MA_KH, source.MA_KM, 1, SYSDATE)";
            psMergeWallet = con.prepareStatement(mergeSQL);
            psMergeWallet.setLong(1, maKH);
            psMergeWallet.setInt(2, maKM);
            psMergeWallet.executeUpdate();

            // Commit Transaction thành công
            con.commit();
            System.out.println("Giao dịch đổi điểm thành công! MA_KH: " + maKH + ", MA_KM: " + maKM + ", Trừ: " + diemCanDoi);

        } catch (Exception ex) {
            if (con != null) {
                try {
                    con.rollback(); // Hoàn tác (Rollback) toàn bộ giao dịch nếu lỗi
                    System.out.println("Đã ROLLBACK giao dịch quy đổi điểm do lỗi: " + ex.getMessage());
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw ex; // Ném tiếp ngoại lệ để View xử lý hiển thị thông báo
        } finally {
            // Đóng các PreparedStatements và Connection an toàn
            try { if (psLockKH != null) psLockKH.close(); } catch (Exception e) {}
            try { if (psLockKM != null) psLockKM.close(); } catch (Exception e) {}
            try { if (psSubDiem != null) psSubDiem.close(); } catch (Exception e) {}
            try { if (psSubStock != null) psSubStock.close(); } catch (Exception e) {}
            try { if (psMergeWallet != null) psMergeWallet.close(); } catch (Exception e) {}
            try { if (con != null) con.close(); } catch (Exception e) {}
        }
    }
}
