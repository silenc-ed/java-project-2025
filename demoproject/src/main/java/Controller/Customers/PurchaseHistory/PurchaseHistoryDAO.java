package Controller.Customers.PurchaseHistory;

import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;

public class PurchaseHistoryDAO {

    public static class OrderItem {
        public String productName;
        public int quantity;
        public long price;
        public long total;
        
        public OrderItem(String name, int qty, long price, long total) {
            this.productName = name;
            this.quantity = qty;
            this.price = price;
            this.total = total;
        }
    }

    public static class Order {
        public long orderId;
        public Timestamp orderDate;
        public long totalAmount;
        public long discount;
        public long finalAmount;
        public String status;
        public List<OrderItem> items = new ArrayList<>();
        
        public Order(long id, Timestamp date, long total, long discount, long finalAmt, String status) {
            this.orderId = id;
            this.orderDate = date;
            this.totalAmount = total;
            this.discount = discount;
            this.finalAmount = finalAmt;
            this.status = status;
        }
    }

    public List<Order> getCustomerOrders(long customerId, String statusFilter) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT MA_HD, THOI_GIAN_LAP, TONG_TIEN, GIAM_GIA, THANH_TIEN, TRANG_THAI " +
                     "FROM HOA_DON WHERE MA_KH = ? AND IS_DELETED = 0 ";
        
        if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equals("Tất cả")) {
            sql += " AND TRANG_THAI = ? ";
        }
        sql += " ORDER BY THOI_GIAN_LAP DESC";

        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setLong(1, customerId);
            if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equals("Tất cả")) {
                ps.setString(2, statusFilter);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order(
                        rs.getLong("MA_HD"),
                        rs.getTimestamp("THOI_GIAN_LAP"),
                        rs.getLong("TONG_TIEN"),
                        rs.getLong("GIAM_GIA"),
                        rs.getLong("THANH_TIEN"),
                        rs.getString("TRANG_THAI")
                    );
                    
                    // Fetch items for this order
                    String itemSql = "SELECT sp.TEN_SP, ct.SO_LUONG, ct.DON_GIA, ct.THANH_TIEN " +
                                     "FROM CHI_TIET_HOA_DON ct " +
                                     "JOIN SAN_PHAM sp ON ct.MA_SP = sp.MA_SP " +
                                     "WHERE ct.MA_HD = ?";
                    try (PreparedStatement itemPs = con.prepareStatement(itemSql)) {
                        itemPs.setLong(1, order.orderId);
                        try (ResultSet itemRs = itemPs.executeQuery()) {
                            while (itemRs.next()) {
                                order.items.add(new OrderItem(
                                    itemRs.getString("TEN_SP"),
                                    itemRs.getInt("SO_LUONG"),
                                    itemRs.getLong("DON_GIA"),
                                    itemRs.getLong("THANH_TIEN")
                                ));
                            }
                        }
                    }
                    orders.add(order);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return orders;
    }
}
