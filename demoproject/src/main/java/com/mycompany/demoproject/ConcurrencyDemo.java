package com.mycompany.demoproject;

import ConnectDB.ConnectionUtils;
import Controller.Admin.DonDatHang.DonDatHangDAO;
import Controller.Admin.PhieuNhap.PhieuNhapDAO;
import Controller.Customers.Voucher.CustomerVoucherDAO;
import Controller.Admin.NhanVienDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrencyDemo {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║       DEMO TRUY XUẤT ĐỒNG THỜI (CONCURRENCY CONTROL)       ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        try {
            setupTestData();
            
            System.out.println("\n━━━ DEMO 1: LOST UPDATE — Chi tiết Phiếu nhập ━━━");
            demoLostUpdatePhieuNhap();
            
            System.out.println("\n━━━ DEMO 2: DOUBLE BOOKING — Cập nhật đơn hàng đồng thời ━━━");
            demoDoubleBookingOrder();
            
            System.out.println("\n━━━ DEMO 3: TOGGLE TRẠNG THÁI TÀI KHOẢN ━━━");
            demoToggleTrangThaiTK();
            
            System.out.println("\n━━━ DEMO 4: ĐỔI VOUCHER — (Đã an toàn nhờ FOR UPDATE) ━━━");
            demoDoiVoucher();
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanupTestData();
        }
    }

    private static void setupTestData() {
        System.out.println("[SETUP] Đang tạo dữ liệu test tạm...");
        try (Connection con = ConnectionUtils.getMyConnection()) {
            // Tạo phiếu nhập test
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO PHIEU_NHAP (MA_PN, MA_NV, MA_CN, NGAY_NHAP, TRANG_THAI) VALUES (9999, 1, 1, SYSDATE, N'Hoàn thành')")) {
                ps.executeUpdate();
            } catch (Exception e) {}
            
            // Xóa chi tiết phiếu nhập cũ nếu có
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM CHI_TIET_PHIEU_NHAP WHERE MA_PN = 9999")) {
                ps.executeUpdate();
            }
            
            // Đảm bảo có tài khoản test
            try (PreparedStatement ps = con.prepareStatement("UPDATE TAI_KHOAN SET TRANG_THAI = N'Hoạt động' WHERE MA_NV = 1")) {
                ps.executeUpdate();
            }
            
            // Setup đơn hàng
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO HOA_DON (MA_HD, MA_NV, MA_CN, TRANG_THAI) VALUES (9999, 1, 1, N'Chờ thanh toán')")) {
                ps.executeUpdate();
            } catch (Exception e) {}
            
            System.out.println("[SETUP] Hoàn tất.");
        } catch (Exception e) {
            System.out.println("[SETUP] Lỗi khi tạo dữ liệu: " + e.getMessage());
        }
    }

    private static void cleanupTestData() {
        System.out.println("\n[CLEANUP] Đang dọn dẹp dữ liệu test...");
        try (Connection con = ConnectionUtils.getMyConnection()) {
            con.prepareStatement("DELETE FROM CHI_TIET_PHIEU_NHAP WHERE MA_PN = 9999").executeUpdate();
            con.prepareStatement("DELETE FROM PHIEU_NHAP WHERE MA_PN = 9999").executeUpdate();
            con.prepareStatement("DELETE FROM CHI_TIET_HOA_DON WHERE MA_HD = 9999").executeUpdate();
            con.prepareStatement("DELETE FROM HOA_DON WHERE MA_HD = 9999").executeUpdate();
            System.out.println("[CLEANUP] Hoàn tất.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    // DEMO 1: Phiếu Nhập
    // =========================================================================
    private static void demoLostUpdatePhieuNhap() throws InterruptedException {
        int numThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        
        System.out.println("  [TEST] 10 thread cùng gọi saveChiTietPhieuNhap (+1 số lượng). Giải pháp MERGE sẽ hoạt động an toàn.");
        
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    // Cả 10 thread cùng thêm 1 sản phẩm vào PN 9999
                    PhieuNhapDAO.saveChiTietPhieuNhap(9999, 1, 1, 100000, false);
                    System.out.println("    Thread-" + Thread.currentThread().getId() + ": Cập nhật thành công");
                } catch (Exception e) {
                    System.out.println("    Thread-" + Thread.currentThread().getId() + " LỖI: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        // Kiểm tra kết quả
        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement("SELECT SO_LUONG FROM CHI_TIET_PHIEU_NHAP WHERE MA_PN = 9999 AND MA_BIENTHE = 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int sl = rs.getInt("SO_LUONG");
                if (sl == 10) {
                    System.out.println("  => KẾT QUẢ SAFE: SO_LUONG = 10 (ĐÚNG! MERGE INTO nguyên tử bảo vệ an toàn)");
                } else {
                    System.out.println("  => KẾT QUẢ UNSAFE: SO_LUONG = " + sl + " (SAI! Bị mất cập nhật do Check-Then-Act)");
                }
            }
        } catch (Exception e) {}
    }

    // =========================================================================
    // DEMO 2: Cập nhật đơn hàng
    // =========================================================================
    private static void demoDoubleBookingOrder() throws InterruptedException {
        int numThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        DonDatHangDAO dao = new DonDatHangDAO();
        
        System.out.println("  [TEST] 2 nhân viên cùng gọi updateOrder(HD-9999). Giải pháp FOR UPDATE sẽ chặn thread sau.");
        
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    List<Map<String, Object>> serials = new ArrayList<>();
                    List<Integer> dvid = new ArrayList<>();
                    dao.updateOrder(9999, null, serials, dvid);
                    System.out.println("    Thread-" + Thread.currentThread().getId() + ": updateOrder thành công (Đã lock hóa đơn)");
                } catch (Exception e) {
                    System.out.println("    Thread-" + Thread.currentThread().getId() + " LỖI chờ hoặc xung đột: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();
    }

    // =========================================================================
    // DEMO 3: Toggle trạng thái
    // =========================================================================
    private static void demoToggleTrangThaiTK() throws InterruptedException {
        int numThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        
        System.out.println("  [TEST] 2 admin cùng toggle tài khoản NV_1. Giải pháp FOR UPDATE đảm bảo tuần tự.");
        
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    String kq = NhanVienDAO.toggleTrangThaiTK(1);
                    System.out.println("    Thread-" + Thread.currentThread().getId() + ": toggle thành công, chuyển sang -> " + kq);
                } catch (Exception e) {
                    System.out.println("    Thread-" + Thread.currentThread().getId() + " LỖI: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();
        
        // Trạng thái cuối phải là Hoạt động nếu gọi 2 lần từ trạng thái Hoạt động
    }

    // =========================================================================
    // DEMO 4: Đổi Voucher
    // =========================================================================
    private static void demoDoiVoucher() throws InterruptedException {
        int numThreads = 5; // 5 người giành 1 voucher (nếu voucher còn ít)
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        
        System.out.println("  [TEST] 5 khách hàng cùng lúc đổi voucher. (Mã đã có FOR UPDATE bảo vệ)");
        
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    CustomerVoucherDAO dao = new CustomerVoucherDAO();
                    // maKH=1, maKM=1, diem=0
                    dao.redeemVoucher(1L, 1, 0);
                    System.out.println("    Thread-" + Thread.currentThread().getId() + ": đổi voucher -> THÀNH CÔNG");
                } catch (Exception e) {
                    System.out.println("    Thread-" + Thread.currentThread().getId() + " LỖI đổi voucher: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();
    }
}
