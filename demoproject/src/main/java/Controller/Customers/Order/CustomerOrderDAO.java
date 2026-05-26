package Controller.Customers.Order;

import ConnectDB.ConnectionUtils;
import Model.CartItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class CustomerOrderDAO {

    public static boolean placeOrder(long maKh, double discountPercent, double discountFixed, List<CartItem> items, String ptThanhToan, Integer maKm) throws Exception {
        if (items == null || items.isEmpty()) {
            throw new Exception("Giỏ hàng trống!");
        }

        double totalAmount = 0;
        for (CartItem item : items) {
            totalAmount += item.getTotalPrice();
        }
        
        double discountAmount = (totalAmount * discountPercent) + discountFixed;
        double finalAmount = totalAmount - discountAmount;
        if (finalAmount < 0) finalAmount = 0;

        Connection con = null;
        try {
            con = ConnectionUtils.getMyConnection();
            con.setAutoCommit(false); // Begin transaction

            // 1. Check and deduct inventory (TON_KHO)
            for (CartItem item : items) {
                long maBienThe = (item.getVariant() != null) ? item.getVariant().getMaBienThe() : 0;
                int reqQty = item.getQuantity();

                if (maBienThe > 0) {
                    // Check stock
                    String checkStockSql = "SELECT SO_LUONG_TON FROM TON_KHO WHERE MA_BIENTHE = ? AND MA_CN = 1 FOR UPDATE";
                    try (PreparedStatement ps = con.prepareStatement(checkStockSql)) {
                        ps.setLong(1, maBienThe);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                int currentStock = rs.getInt("SO_LUONG_TON");
                                if (currentStock < reqQty) {
                                    throw new Exception("Sản phẩm '" + item.getProduct().getTenSp() + "' không đủ số lượng trong kho. (Còn lại: " + currentStock + ")");
                                }
                            } else {
                                throw new Exception("Không tìm thấy thông tin tồn kho cho sản phẩm: " + item.getProduct().getTenSp());
                            }
                        }
                    }

                    // Deduct stock
                    String updateStockSql = "UPDATE TON_KHO SET SO_LUONG_TON = SO_LUONG_TON - ? WHERE MA_BIENTHE = ? AND MA_CN = 1";
                    try (PreparedStatement ps = con.prepareStatement(updateStockSql)) {
                        ps.setInt(1, reqQty);
                        ps.setLong(2, maBienThe);
                        ps.executeUpdate();
                    }
                }
            }

            // 2. Insert HOA_DON
            String insertHdSql = "INSERT INTO HOA_DON (MA_KH, MA_NV, MA_CN, TONG_TIEN, GIAM_GIA, THANH_TIEN, PHUONG_THUC_TT, TRANG_THAI, IS_DELETED, MA_KM) " +
                                 "VALUES (?, 1, 1, ?, ?, ?, ?, 'Đang chuẩn bị hàng', 0, ?)";
            long newHdId = -1;
            try (PreparedStatement ps = con.prepareStatement(insertHdSql, new String[]{"MA_HD"})) {
                if (maKh > 0) {
                    ps.setLong(1, maKh);
                } else {
                    ps.setNull(1, java.sql.Types.NUMERIC); // Guest checkout? Wait, column MA_KH can be null
                }
                ps.setDouble(2, totalAmount);
                ps.setDouble(3, discountAmount);
                ps.setDouble(4, finalAmount);
                ps.setString(5, ptThanhToan);
                if (maKm != null && maKm > 0) {
                    ps.setInt(6, maKm);
                } else {
                    ps.setNull(6, java.sql.Types.NUMERIC);
                }
                
                ps.executeUpdate();
                
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        newHdId = rs.getLong(1);
                    }
                }
            }

            if (newHdId == -1) {
                throw new Exception("Không thể tạo hóa đơn.");
            }

            // 3. Insert CHI_TIET_HOA_DON
            String insertCtSql = "INSERT INTO CHI_TIET_HOA_DON (MA_HD, MA_SP, SO_LUONG, DON_GIA, THANH_TIEN) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(insertCtSql)) {
                for (CartItem item : items) {
                    long maSp = item.getProduct().getMaSp();
                    double price = item.getVariant() != null ? item.getVariant().getGiaBan() : item.getProduct().getGiaBan();
                    
                    ps.setLong(1, newHdId);
                    ps.setLong(2, maSp);
                    ps.setInt(3, item.getQuantity());
                    ps.setDouble(4, price);
                    ps.setDouble(5, price * item.getQuantity());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // 4. Update VI_KHUYENMAI if a voucher was used
            if (maKh > 0 && maKm != null && maKm > 0) {
                String updateViSql = "UPDATE VI_KHUYENMAI SET SO_LUONG = SO_LUONG - 1 WHERE MA_KH = ? AND MA_KM = ? AND SO_LUONG > 0";
                try (PreparedStatement psVi = con.prepareStatement(updateViSql)) {
                    psVi.setLong(1, maKh);
                    psVi.setInt(2, maKm);
                    psVi.executeUpdate();
                }
            }

            con.commit();
            return true;

        } catch (Exception ex) {
            if (con != null) {
                try { con.rollback(); } catch (Exception rollbackEx) { rollbackEx.printStackTrace(); }
            }
            throw ex;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (Exception closeEx) { closeEx.printStackTrace(); }
            }
        }
    }
}
