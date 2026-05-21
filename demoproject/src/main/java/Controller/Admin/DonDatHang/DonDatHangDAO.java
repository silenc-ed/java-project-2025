package Controller.Admin.DonDatHang;

import ConnectDB.ConnectionUtils;
import Model.KhachHang;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * DAO quản lý đơn đặt hàng — làm việc trực tiếp với các bảng:
 * HOADON, CHITIET_HOADON, PHIEU_DICH_VU, CHITIET_SUDUNG_DICHVU,
 * PHIEU_SUA_CHUA, CHITIET_SUDUNG_LINHKIEN, KHO_SERIAL, KHUYENMAI
 */
public class DonDatHangDAO {

    // ===================== UC1: TẠO ĐƠN ĐẶT HÀNG =====================

    /**
     * Tạo đơn đặt hàng mới (transaction)
     * @param maKH mã khách hàng (nullable)
     * @param maNV mã nhân viên tạo đơn
     * @param maCN mã chi nhánh
     * @param serials danh sách serial sản phẩm (mỗi entry: serialNumber -> {maSP, donGia})
     * @param dichVuIds danh sách mã dịch vụ
     * @param phieuSCIds danh sách mã phiếu sửa chữa
     * @return mã hóa đơn (MA_HD) vừa tạo, hoặc -1 nếu lỗi
     */
    public int createOrder(Integer maKH, int maNV, int maCN,
                           List<Map<String, Object>> serials,
                           List<Integer> dichVuIds,
                           List<Integer> phieuSCIds) throws Exception {

        Connection con = null;
        int maHD = -1;

        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);

            // 1. INSERT HOADON
            String sqlHD = "INSERT INTO HOA_DON (MA_KH, MA_NV, MA_CN, TONG_TIEN_HANG, GIAM_GIA, THANH_TIEN, TRANG_THAI) " +
                           "VALUES (?, ?, ?, 0, 0, 0, N'Chờ thanh toán')";
            try (PreparedStatement ps = con.prepareStatement(sqlHD, new String[]{"MA_HD"})) {
                if (maKH != null) {
                    ps.setInt(1, maKH);
                } else {
                    ps.setNull(1, Types.INTEGER);
                }
                ps.setInt(2, maNV);
                ps.setInt(3, maCN);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        maHD = rs.getInt(1);
                    }
                }
            }

            if (maHD == -1) {
                throw new Exception("Không thể tạo hóa đơn");
            }

            // 2. INSERT CHITIET_HOADON + UPDATE KHO_SERIAL
            long tongTienSP = 0;
            if (serials != null && !serials.isEmpty()) {
                String sqlCT = "INSERT INTO CHI_TIET_HOA_DON (MA_HD, MA_SP, SERIAL_NUMBER, SO_LUONG, DON_GIA, THANH_TIEN) " +
                               "VALUES (?, ?, ?, 1, ?, ?)";
                String sqlSerial = "UPDATE KHO_SERIAL SET TRANG_THAI = N'DANG_DUOC_DAT' WHERE SERIAL_NUMBER = ? AND TRANG_THAI = N'KHA_DUNG'";

                try (PreparedStatement psCT = con.prepareStatement(sqlCT);
                     PreparedStatement psSerial = con.prepareStatement(sqlSerial)) {

                    for (Map<String, Object> item : serials) {
                        String serialNumber = (String) item.get("serialNumber");
                        int maSP = (int) item.get("maSP");
                        long donGia = (long) item.get("donGia");

                        // Update serial status
                        psSerial.setString(1, serialNumber);
                        int updated = psSerial.executeUpdate();
                        if (updated == 0) {
                            throw new Exception("Serial " + serialNumber + " không khả dụng hoặc đã được đặt!");
                        }

                        // Insert chi tiết
                        psCT.setInt(1, maHD);
                        psCT.setInt(2, maSP);
                        psCT.setString(3, serialNumber);
                        psCT.setLong(4, donGia);
                        psCT.setLong(5, donGia); // thanh_tien = don_gia * 1
                        psCT.executeUpdate();

                        tongTienSP += donGia;
                    }
                }
            }

            // 3. INSERT PHIEU_DICH_VU + CHITIET_SUDUNG_DICHVU
            long tongPhiDV = 0;
            if (dichVuIds != null && !dichVuIds.isEmpty()) {
                // Tạo phiếu dịch vụ
                int maPhieuDV = -1;
                String sqlPDV = "INSERT INTO PHIEU_DICH_VU (MA_HD, MA_NV_KYTHUAT) VALUES (?, ?)";
                try (PreparedStatement psPDV = con.prepareStatement(sqlPDV, new String[]{"MA_PHIEU_DV"})) {
                    psPDV.setInt(1, maHD);
                    psPDV.setInt(2, maNV);
                    psPDV.executeUpdate();
                    try (ResultSet rs = psPDV.getGeneratedKeys()) {
                        if (rs.next()) maPhieuDV = rs.getInt(1);
                    }
                }

                if (maPhieuDV > 0) {
                    // Lấy phí dịch vụ và insert chi tiết
                    String sqlGetDV = "SELECT MA_DV, GIA_CUOC FROM DICHVU WHERE MA_DV = ?";
                    String sqlCTDV = "INSERT INTO CHITIET_SUDUNG_DICHVU (MA_PHIEU_DV, MA_DV, PHI_DICH_VU) VALUES (?, ?, ?)";

                    try (PreparedStatement psGetDV = con.prepareStatement(sqlGetDV);
                         PreparedStatement psCTDV = con.prepareStatement(sqlCTDV)) {

                        for (int maDV : dichVuIds) {
                            // Lấy giá cước
                            long giaCuoc = 0;
                            psGetDV.setInt(1, maDV);
                            try (ResultSet rs = psGetDV.executeQuery()) {
                                if (rs.next()) {
                                    giaCuoc = rs.getLong("GIA_CUOC");
                                }
                            }

                            psCTDV.setInt(1, maPhieuDV);
                            psCTDV.setInt(2, maDV);
                            psCTDV.setLong(3, giaCuoc);
                            psCTDV.executeUpdate();

                            tongPhiDV += giaCuoc;
                        }
                    }
                }
            }

            // 4. Liên kết PHIEU_SUA_CHUA (nếu có) — tính tổng tiền linh kiện
            long tongTienLK = 0;
            if (phieuSCIds != null && !phieuSCIds.isEmpty()) {
                String sqlLK = "SELECT NVL(SUM(THANH_TIEN), 0) AS TONG FROM CHITIET_SUDUNG_LINHKIEN WHERE MA_PHIEU_SC = ?";
                try (PreparedStatement psLK = con.prepareStatement(sqlLK)) {
                    for (int maPhieuSC : phieuSCIds) {
                        psLK.setInt(1, maPhieuSC);
                        try (ResultSet rs = psLK.executeQuery()) {
                            if (rs.next()) {
                                tongTienLK += rs.getLong("TONG");
                            }
                        }
                    }
                }
            }

            // 5. UPDATE TONG_TIEN cho HOADON
            long tongTien = tongTienSP + tongPhiDV + tongTienLK;
            String sqlUpdate = "UPDATE HOA_DON SET TONG_TIEN_HANG = ?, THANH_TIEN = ? WHERE MA_HD = ?";
            try (PreparedStatement psUpd = con.prepareStatement(sqlUpdate)) {
                psUpd.setLong(1, tongTien);
                psUpd.setLong(2, tongTien);
                psUpd.setInt(3, maHD);
                psUpd.executeUpdate();
            }

            con.commit();
            return maHD;

        } catch (Exception e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    // ===================== UC2: CẬP NHẬT ĐƠN ĐẶT HÀNG =====================

    /**
     * Cập nhật đơn đặt hàng (chỉ khi trạng thái = 'Chờ thanh toán')
     */
    public void updateOrder(int maHD, Integer maKH,
                            List<Map<String, Object>> serials,
                            List<Integer> dichVuIds) throws Exception {

        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);

            // Kiểm tra trạng thái
            String checkSql = "SELECT TRANG_THAI FROM HOA_DON WHERE MA_HD = ?";
            try (PreparedStatement psCheck = con.prepareStatement(checkSql)) {
                psCheck.setInt(1, maHD);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        String tt = rs.getString("TRANG_THAI");
                        if (!"Chờ thanh toán".equals(tt)) {
                            throw new Exception("Không thể cập nhật đơn hàng ở trạng thái: " + tt);
                        }
                    } else {
                        throw new Exception("Không tìm thấy đơn hàng MA_HD = " + maHD);
                    }
                }
            }

            // Release serial cũ
            String sqlReleaseOld = "UPDATE KHO_SERIAL SET TRANG_THAI = N'KHA_DUNG' " +
                                   "WHERE SERIAL_NUMBER IN (SELECT SERIAL_NUMBER FROM CHI_TIET_HOA_DON WHERE MA_HD = ? AND SERIAL_NUMBER IS NOT NULL)";
            try (PreparedStatement psRelease = con.prepareStatement(sqlReleaseOld)) {
                psRelease.setInt(1, maHD);
                psRelease.executeUpdate();
            }

            // Xóa chi tiết cũ
            try (PreparedStatement psDel = con.prepareStatement("DELETE FROM CHI_TIET_HOA_DON WHERE MA_HD = ?")) {
                psDel.setInt(1, maHD);
                psDel.executeUpdate();
            }

            // Xóa dịch vụ cũ
            String sqlDelDV = "DELETE FROM CHITIET_SUDUNG_DICHVU WHERE MA_PHIEU_DV IN " +
                              "(SELECT MA_PHIEU_DV FROM PHIEU_DICH_VU WHERE MA_HD = ?)";
            try (PreparedStatement psDelDV = con.prepareStatement(sqlDelDV)) {
                psDelDV.setInt(1, maHD);
                psDelDV.executeUpdate();
            }
            try (PreparedStatement psDelPDV = con.prepareStatement("DELETE FROM PHIEU_DICH_VU WHERE MA_HD = ?")) {
                psDelPDV.setInt(1, maHD);
                psDelPDV.executeUpdate();
            }

            // Thêm lại chi tiết sản phẩm mới
            long tongTienSP = 0;
            if (serials != null && !serials.isEmpty()) {
                String sqlCT = "INSERT INTO CHI_TIET_HOA_DON (MA_HD, MA_SP, SERIAL_NUMBER, SO_LUONG, DON_GIA, THANH_TIEN) " +
                               "VALUES (?, ?, ?, 1, ?, ?)";
                String sqlSerial = "UPDATE KHO_SERIAL SET TRANG_THAI = N'DANG_DUOC_DAT' WHERE SERIAL_NUMBER = ? AND TRANG_THAI = N'KHA_DUNG'";

                try (PreparedStatement psCT = con.prepareStatement(sqlCT);
                     PreparedStatement psSerial = con.prepareStatement(sqlSerial)) {

                    for (Map<String, Object> item : serials) {
                        String serialNumber = (String) item.get("serialNumber");
                        int maSP = (int) item.get("maSP");
                        long donGia = (long) item.get("donGia");

                        psSerial.setString(1, serialNumber);
                        int updated = psSerial.executeUpdate();
                        if (updated == 0) {
                            throw new Exception("Serial " + serialNumber + " không khả dụng!");
                        }

                        psCT.setInt(1, maHD);
                        psCT.setInt(2, maSP);
                        psCT.setString(3, serialNumber);
                        psCT.setLong(4, donGia);
                        psCT.setLong(5, donGia);
                        psCT.executeUpdate();

                        tongTienSP += donGia;
                    }
                }
            }

            // Thêm lại dịch vụ mới
            long tongPhiDV = 0;
            if (dichVuIds != null && !dichVuIds.isEmpty()) {
                int maPhieuDV = -1;
                String sqlPDV = "INSERT INTO PHIEU_DICH_VU (MA_HD, MA_NV_KYTHUAT) VALUES (?, NULL)";
                try (PreparedStatement psPDV = con.prepareStatement(sqlPDV, new String[]{"MA_PHIEU_DV"})) {
                    psPDV.setInt(1, maHD);
                    psPDV.executeUpdate();
                    try (ResultSet rs = psPDV.getGeneratedKeys()) {
                        if (rs.next()) maPhieuDV = rs.getInt(1);
                    }
                }

                if (maPhieuDV > 0) {
                    String sqlGetDV = "SELECT GIA_CUOC FROM DICHVU WHERE MA_DV = ?";
                    String sqlCTDV = "INSERT INTO CHITIET_SUDUNG_DICHVU (MA_PHIEU_DV, MA_DV, PHI_DICH_VU) VALUES (?, ?, ?)";
                    try (PreparedStatement psGetDV = con.prepareStatement(sqlGetDV);
                         PreparedStatement psCTDV = con.prepareStatement(sqlCTDV)) {
                        for (int maDV : dichVuIds) {
                            long giaCuoc = 0;
                            psGetDV.setInt(1, maDV);
                            try (ResultSet rs = psGetDV.executeQuery()) {
                                if (rs.next()) giaCuoc = rs.getLong("GIA_CUOC");
                            }
                            psCTDV.setInt(1, maPhieuDV);
                            psCTDV.setInt(2, maDV);
                            psCTDV.setLong(3, giaCuoc);
                            psCTDV.executeUpdate();
                            tongPhiDV += giaCuoc;
                        }
                    }
                }
            }

            // Cập nhật khách hàng và tổng tiền
            long tongTien = tongTienSP + tongPhiDV;
            // Tính lại giảm giá nếu có KM
            long giamGia = 0;
            String sqlGetKM = "SELECT MA_KM FROM HOA_DON WHERE MA_HD = ?";
            try (PreparedStatement psKM = con.prepareStatement(sqlGetKM)) {
                psKM.setInt(1, maHD);
                try (ResultSet rs = psKM.executeQuery()) {
                    if (rs.next()) {
                        int maKM = rs.getInt("MA_KM");
                        if (!rs.wasNull() && maKM > 0) {
                            giamGia = calculateDiscount(con, maKM, tongTien);
                        }
                    }
                }
            }

            String sqlUpd = "UPDATE HOA_DON SET MA_KH = ?, TONG_TIEN_HANG = ?, GIAM_GIA = ?, THANH_TIEN = ? WHERE MA_HD = ?";
            try (PreparedStatement psUpd = con.prepareStatement(sqlUpd)) {
                if (maKH != null) {
                    psUpd.setInt(1, maKH);
                } else {
                    psUpd.setNull(1, Types.INTEGER);
                }
                psUpd.setLong(2, tongTien);
                psUpd.setLong(3, giamGia);
                psUpd.setLong(4, tongTien - giamGia);
                psUpd.setInt(5, maHD);
                psUpd.executeUpdate();
            }

            con.commit();

        } catch (Exception e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    // ===================== UC3: HỦY ĐƠN ĐẶT HÀNG =====================

    /**
     * Hủy đơn đặt hàng: chuyển trạng thái sang 'Đã hủy', release serial
     */
    public void cancelOrder(int maHD, String lyDoHuy) throws Exception {
        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false);

            // Kiểm tra trạng thái
            String checkSql = "SELECT TRANG_THAI FROM HOA_DON WHERE MA_HD = ?";
            try (PreparedStatement psCheck = con.prepareStatement(checkSql)) {
                psCheck.setInt(1, maHD);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        String tt = rs.getString("TRANG_THAI");
                        if (!"Chờ thanh toán".equals(tt)) {
                            throw new Exception("Chỉ có thể hủy đơn hàng ở trạng thái 'Chờ thanh toán'!");
                        }
                    }
                }
            }

            // Release serial về KHA_DUNG
            String sqlRelease = "UPDATE KHO_SERIAL SET TRANG_THAI = N'KHA_DUNG' " +
                                "WHERE SERIAL_NUMBER IN (SELECT SERIAL_NUMBER FROM CHI_TIET_HOA_DON WHERE MA_HD = ? AND SERIAL_NUMBER IS NOT NULL)";
            try (PreparedStatement psRelease = con.prepareStatement(sqlRelease)) {
                psRelease.setInt(1, maHD);
                psRelease.executeUpdate();
            }

            // Cập nhật trạng thái + lý do hủy
            String sqlCancel = "UPDATE HOA_DON SET TRANG_THAI = N'Đã hủy', LY_DO_HUY = ? WHERE MA_HD = ?";
            try (PreparedStatement psCancel = con.prepareStatement(sqlCancel)) {
                psCancel.setString(1, lyDoHuy);
                psCancel.setInt(2, maHD);
                psCancel.executeUpdate();
            }

            con.commit();

        } catch (Exception e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    // ===================== UC4: TRA CỨU ĐƠN ĐẶT HÀNG =====================

    /**
     * Lấy tất cả đơn hàng
     */
    public List<Map<String, Object>> getAllOrders() throws Exception {
        return searchOrders(null, null, null, null);
    }

    /**
     * Tìm kiếm đơn hàng
     */
    public List<Map<String, Object>> searchOrders(String keyword, String trangThai,
                                                   Timestamp dateFrom, Timestamp dateTo) throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT H.MA_HD, K.HO_TEN AS TEN_KH, K.SDT, H.MA_NV, H.THOI_GIAN_LAP, ");
        sql.append("H.TONG_TIEN_HANG, H.GIAM_GIA, H.THANH_TIEN, H.TRANG_THAI, H.PHUONG_THUC_TT, H.LY_DO_HUY ");
        sql.append("FROM HOA_DON H LEFT JOIN KHACH_HANG K ON H.MA_KH = K.MA_KH ");
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (trangThai != null && !trangThai.isEmpty() && !"Tất cả".equals(trangThai)) {
            sql.append("AND H.TRANG_THAI = ? ");
            params.add(trangThai);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (UPPER(TO_CHAR(H.MA_HD)) LIKE UPPER(?) OR UPPER(K.SDT) LIKE UPPER(?) OR UPPER(K.HO_TEN) LIKE UPPER(?)) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (dateFrom != null) {
            sql.append("AND H.THOI_GIAN_LAP >= ? ");
            params.add(dateFrom);
        }
        if (dateTo != null) {
            sql.append("AND H.THOI_GIAN_LAP <= ? ");
            params.add(dateTo);
        }

        sql.append("ORDER BY H.MA_HD DESC");

        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String) {
                    ps.setString(i + 1, (String) p);
                } else if (p instanceof Timestamp) {
                    ps.setTimestamp(i + 1, (Timestamp) p);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("MA_HD", rs.getInt("MA_HD"));
                    row.put("TEN_KH", rs.getString("TEN_KH"));
                    row.put("SDT", rs.getString("SDT"));
                    row.put("MA_NV", rs.getInt("MA_NV"));
                    row.put("THOI_GIAN_LAP", rs.getTimestamp("THOI_GIAN_LAP"));
                    row.put("TONG_TIEN_HANG", rs.getLong("TONG_TIEN_HANG"));
                    row.put("GIAM_GIA", rs.getLong("GIAM_GIA"));
                    row.put("THANH_TIEN", rs.getLong("THANH_TIEN"));
                    row.put("TRANG_THAI", rs.getString("TRANG_THAI"));
                    row.put("PHUONG_THUC_TT", rs.getString("PHUONG_THUC_TT"));
                    row.put("LY_DO_HUY", rs.getString("LY_DO_HUY"));
                    results.add(row);
                }
            }
        }
        return results;
    }

    /**
     * Lấy chi tiết sản phẩm của đơn hàng
     */
    public List<Map<String, Object>> getOrderProducts(int maHD) throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT CT.MA_CTHD, CT.MA_SP, SP.TEN_SP, CT.SERIAL_NUMBER, CT.DON_GIA, CT.THANH_TIEN " +
                     "FROM CHI_TIET_HOA_DON CT JOIN SAN_PHAM SP ON CT.MA_SP = SP.MA_SP " +
                     "WHERE CT.MA_HD = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("MA_CTHD", rs.getInt("MA_CTHD"));
                    row.put("MA_SP", rs.getInt("MA_SP"));
                    row.put("TEN_SP", rs.getString("TEN_SP"));
                    row.put("SERIAL_NUMBER", rs.getString("SERIAL_NUMBER"));
                    row.put("DON_GIA", rs.getLong("DON_GIA"));
                    row.put("THANH_TIEN", rs.getLong("THANH_TIEN"));
                    results.add(row);
                }
            }
        }
        return results;
    }

    /**
     * Lấy chi tiết dịch vụ của đơn hàng
     */
    public List<Map<String, Object>> getOrderServices(int maHD) throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT CTDV.MA_CTDV, DV.MA_DV, DV.TEN_DV, CTDV.PHI_DICH_VU " +
                     "FROM PHIEU_DICH_VU PDV " +
                     "JOIN CHITIET_SUDUNG_DICHVU CTDV ON PDV.MA_PHIEU_DV = CTDV.MA_PHIEU_DV " +
                     "JOIN DICHVU DV ON CTDV.MA_DV = DV.MA_DV " +
                     "WHERE PDV.MA_HD = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("MA_CTDV", rs.getInt("MA_CTDV"));
                    row.put("MA_DV", rs.getInt("MA_DV"));
                    row.put("TEN_DV", rs.getString("TEN_DV"));
                    row.put("PHI_DICH_VU", rs.getLong("PHI_DICH_VU"));
                    results.add(row);
                }
            }
        }
        return results;
    }

    /**
     * Lấy chi tiết phiếu sửa chữa của đơn hàng
     */
    public List<Map<String, Object>> getOrderRepairs(int maHD) throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT PSC.MA_PHIEU_SC, PSC.MO_TA, PSC.GIA_CUOC, " +
                     "NVL((SELECT SUM(THANH_TIEN) FROM CHITIET_SUDUNG_LINHKIEN WHERE MA_PHIEU_SC = PSC.MA_PHIEU_SC), 0) AS TIEN_LK " +
                     "FROM PHIEU_DICH_VU PDV " +
                     "JOIN PHIEU_SUA_CHUA PSC ON PDV.MA_PHIEU_DV = PSC.MA_PHIEU_DV " +
                     "WHERE PDV.MA_HD = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("MA_PHIEU_SC", rs.getInt("MA_PHIEU_SC"));
                    row.put("MO_TA", rs.getString("MO_TA"));
                    row.put("GIA_CUOC", rs.getLong("GIA_CUOC"));
                    row.put("TIEN_LK", rs.getLong("TIEN_LK"));
                    results.add(row);
                }
            }
        }
        return results;
    }

    // ===================== UC5: KHUYẾN MÃI =====================

    /**
     * Áp dụng khuyến mãi vào đơn hàng
     * @return giá trị giảm (số tiền)
     */
    public long applyPromotion(int maHD, int maKM) throws Exception {
        try (Connection con = ConnectionUtils.getMyConnection()) {
            // Lấy tổng tiền hàng
            long tongTien = 0;
            try (PreparedStatement ps = con.prepareStatement("SELECT TONG_TIEN_HANG FROM HOA_DON WHERE MA_HD = ?")) {
                ps.setInt(1, maHD);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) tongTien = rs.getLong("TONG_TIEN_HANG");
                }
            }

            // Validate và tính giảm giá
            long giamGia = validateAndCalculateDiscount(con, maKM, tongTien);

            // Cập nhật HOADON
            String sqlUpd = "UPDATE HOA_DON SET MA_KM = ?, GIAM_GIA = ?, THANH_TIEN = ? WHERE MA_HD = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlUpd)) {
                ps.setInt(1, maKM);
                ps.setLong(2, giamGia);
                ps.setLong(3, tongTien - giamGia);
                ps.setInt(4, maHD);
                ps.executeUpdate();
            }

            return giamGia;
        }
    }

    /**
     * Validate mã khuyến mãi và trả về số tiền giảm
     */
    public long validateAndCalculateDiscount(Connection con, int maKM, long tongTien) throws Exception {
        String sql = "SELECT GIA_TRI, RANG_BUOC_GIA_TRI, NGAY_BAT_DAU, NGAY_KET_THUC, TRANG_THAI FROM KHUYEN_MAI WHERE MA_KM = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maKM);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new Exception("Mã khuyến mãi không tồn tại!");
                }

                String trangThai = rs.getString("TRANG_THAI");
                if (!"Có hiệu lực".equals(trangThai) && !"Hiệu lực".equals(trangThai)) {
                    throw new Exception("Mã khuyến mãi không khả dụng (trạng thái: " + trangThai + ")");
                }

                Timestamp ngayBD = rs.getTimestamp("NGAY_BAT_DAU");
                Timestamp ngayKT = rs.getTimestamp("NGAY_KET_THUC");
                Timestamp now = new Timestamp(System.currentTimeMillis());

                if (now.before(ngayBD) || now.after(ngayKT)) {
                    throw new Exception("Mã khuyến mãi đã hết hạn hoặc chưa có hiệu lực!");
                }

                long giaTri = rs.getLong("GIA_TRI"); // phần trăm
                String rangBuoc = rs.getString("RANG_BUOC_GIA_TRI");

                // Kiểm tra ràng buộc giá trị: áp dụng khi tổng tiền dưới ngưỡng
                if (rangBuoc != null && !rangBuoc.trim().isEmpty()) {
                    try {
                        long threshold = Long.parseLong(rangBuoc.trim());
                        if (tongTien > threshold) {
                            throw new Exception("Đơn hàng vượt ngưỡng áp dụng khuyến mãi (" + threshold + "đ)!");
                        }
                    } catch (NumberFormatException e) {
                        // Ràng buộc không phải số, bỏ qua
                    }
                }

                // Tính giảm giá = tổng tiền * phần trăm / 100
                return tongTien * giaTri / 100;
            }
        }
    }

    private long calculateDiscount(Connection con, int maKM, long tongTien) {
        try {
            return validateAndCalculateDiscount(con, maKM, tongTien);
        } catch (Exception e) {
            return 0; // Nếu KM không hợp lệ, không giảm
        }
    }

    // ===================== HELPER METHODS =====================

    /**
     * Lấy MA_NV của nhân viên đang đăng nhập từ token
     */
    public int getCurrentMaNV() {
        String tokenValue = Common.TokenManager.getLocalToken();
        if (tokenValue == null || tokenValue.isEmpty()) return 1;

        try (Connection con = ConnectionUtils.getMyConnection()) {
            String sql = "SELECT tk.MA_NV FROM ACCOUNT_TOKEN atok " +
                         "JOIN TAIKHOAN tk ON atok.MA_TK = tk.MA_TK " +
                         "WHERE atok.TOKEN_VALUE = ? AND atok.THOI_GIAN_HET_HAN > CURRENT_TIMESTAMP AND atok.TRANG_THAI = 'Y'";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, tokenValue);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int maNV = rs.getInt("MA_NV");
                        if (!rs.wasNull()) return maNV;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1; // fallback
    }

    /**
     * Tra cứu khách hàng theo SĐT
     */
    public KhachHang getCustomerByPhone(String phone) throws Exception {
        String sql = "SELECT * FROM KHACH_HANG WHERE SDT = ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, phone.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new KhachHang(
                        rs.getLong("MA_KH"),
                        rs.getString("HO_TEN"),
                        rs.getString("SDT"),
                        rs.getString("DIA_CHI"),
                        rs.getString("EMAIL"),
                        rs.getInt("DIEM_TICH_LUY")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Tìm serial khả dụng (TRANG_THAI = 'KHA_DUNG')
     */
    public List<Map<String, Object>> searchAvailableSerials(String keyword) throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT KS.ID_SERIAL, KS.SERIAL_NUMBER, KS.MA_BIENTHE, BT.MA_SP, SP.TEN_SP, BT.TEN_BIENTHE, BT.GIA_BAN " +
                     "FROM KHO_SERIAL KS " +
                     "JOIN BIEN_THE_SAN_PHAM BT ON KS.MA_BIENTHE = BT.MA_BIENTHE " +
                     "JOIN SAN_PHAM SP ON BT.MA_SP = SP.MA_SP " +
                     "WHERE KS.TRANG_THAI = N'KHA_DUNG' " +
                     "AND (UPPER(KS.SERIAL_NUMBER) LIKE UPPER(?) OR UPPER(SP.TEN_SP) LIKE UPPER(?)) " +
                     "AND ROWNUM <= 50";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String kw = "%" + keyword.trim() + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("ID_SERIAL", rs.getInt("ID_SERIAL"));
                    row.put("SERIAL_NUMBER", rs.getString("SERIAL_NUMBER"));
                    row.put("MA_SP", rs.getInt("MA_SP"));
                    row.put("TEN_SP", rs.getString("TEN_SP"));
                    row.put("TEN_BIENTHE", rs.getString("TEN_BIENTHE"));
                    row.put("GIA_BAN", rs.getLong("GIA_BAN"));
                    results.add(row);
                }
            }
        }
        return results;
    }

    /**
     * Kiểm tra serial đã nằm trong đơn 'Chờ thanh toán' khác chưa
     */
    public boolean isSerialInPendingOrder(String serialNumber, int excludeHD) throws Exception {
        String sql = "SELECT COUNT(*) FROM CHI_TIET_HOA_DON CT " +
                     "JOIN HOA_DON H ON CT.MA_HD = H.MA_HD " +
                     "WHERE CT.SERIAL_NUMBER = ? AND H.TRANG_THAI = N'Chờ thanh toán' AND H.MA_HD != ?";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, serialNumber);
            ps.setInt(2, excludeHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Lấy danh sách dịch vụ khả dụng
     */
    public List<Map<String, Object>> getAvailableServices() throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        // Try with TRANG_THAI filter first; fall back to no filter if column doesn't exist (ORA-00904)
        String sqlWithFilter = "SELECT MA_DV, TEN_DV, MO_TA, GIA_CUOC FROM DICHVU WHERE TRANG_THAI = 1";
        String sqlFallback = "SELECT MA_DV, TEN_DV, MO_TA, GIA_CUOC FROM DICHVU";
        try (Connection con = ConnectionUtils.getMyConnection()) {
            String sqlToUse = sqlWithFilter;
            try (PreparedStatement psTest = con.prepareStatement(sqlWithFilter);
                 ResultSet rsTest = psTest.executeQuery()) {
                // If this succeeds, use results directly
                while (rsTest.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("MA_DV", rsTest.getInt("MA_DV"));
                    row.put("TEN_DV", rsTest.getString("TEN_DV"));
                    row.put("MO_TA", rsTest.getString("MO_TA"));
                    row.put("GIA_CUOC", rsTest.getLong("GIA_CUOC"));
                    results.add(row);
                }
                return results;
            } catch (SQLException e) {
                // ORA-00904: invalid identifier — column TRANG_THAI doesn't exist yet
                if (e.getErrorCode() == 904) {
                    System.out.println("[DonDatHangDAO] DICHVU.TRANG_THAI not found, falling back to unfiltered query.");
                    try (PreparedStatement ps = con.prepareStatement(sqlFallback);
                         ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Map<String, Object> row = new HashMap<>();
                            row.put("MA_DV", rs.getInt("MA_DV"));
                            row.put("TEN_DV", rs.getString("TEN_DV"));
                            row.put("MO_TA", rs.getString("MO_TA"));
                            row.put("GIA_CUOC", rs.getLong("GIA_CUOC"));
                            results.add(row);
                        }
                    }
                    return results;
                }
                throw e;
            }
        }
    }

    /**
     * Lấy danh sách phiếu sửa chữa
     */
    public List<Map<String, Object>> getRepairTickets() throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT PSC.MA_PHIEU_SC, PSC.MO_TA, PSC.GIA_CUOC, " +
                     "NVL((SELECT SUM(THANH_TIEN) FROM CHITIET_SUDUNG_LINHKIEN WHERE MA_PHIEU_SC = PSC.MA_PHIEU_SC), 0) AS TIEN_LK " +
                     "FROM PHIEU_SUA_CHUA PSC";
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("MA_PHIEU_SC", rs.getInt("MA_PHIEU_SC"));
                row.put("MO_TA", rs.getString("MO_TA"));
                row.put("GIA_CUOC", rs.getLong("GIA_CUOC"));
                row.put("TIEN_LK", rs.getLong("TIEN_LK"));
                results.add(row);
            }
        }
        return results;
    }

    /**
     * Lấy trạng thái đơn hàng
     */
    public String getOrderStatus(int maHD) throws Exception {
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement("SELECT TRANG_THAI FROM HOA_DON WHERE MA_HD = ?")) {
            ps.setInt(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("TRANG_THAI");
            }
        }
        return null;
    }
}
