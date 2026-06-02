package Controller.Admin.AI;

import Controller.Admin.DashboardDAO;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * =======================================================================
 * LỚP CHỨA CÁC TOOL (FUNCTION) CHO TRỢ LÝ AI
 * =======================================================================
 * Mỗi phương thức đánh dấu @Tool sẽ được LangChain4j đăng ký như một
 * "function" mà LLM có thể gọi (Function Calling / Tool Use).
 *
 * Lớp này KHÔNG phụ thuộc vào thư viện LangChain4j khi biên dịch.
 * Các annotation @Tool chỉ được thêm khi anh tích hợp dependency thực tế.
 * Hiện tại, mỗi phương thức vẫn là public bình thường, và được gọi
 * bởi StoreAssistantService thông qua cơ chế keyword-matching đơn giản
 * (mock AI) cho mục đích demo.
 *
 * KHI TÍCH HỢP LANGCHAIN4J THẬT:
 *   1. Thêm dependency langchain4j vào pom.xml
 *   2. Thêm import dev.langchain4j.agent.tool.Tool
 *   3. Uncomment các dòng @Tool bên dưới
 * =======================================================================
 */
public class StoreReportTools {

    private static final DecimalFormat DF = new DecimalFormat("#,###");
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    // ==================================================================
    // TOOL 1: LẤY TOP SẢN PHẨM THỊNH HÀNH
    // ==================================================================
    // @Tool("Trả về danh sách top sản phẩm bán chạy nhất trong khoảng thời gian, bao gồm tên SP, danh mục, số lượng bán, doanh thu")
    public String getTrendingProducts(String fromDate, String toDate) {
        try {
            Date from = SDF.parse(fromDate);
            Date to = addOneDay(SDF.parse(toDate));
            List<Object[]> list = DashboardDAO.getTopSanPhamThinhHanh(from, to, 10);

            if (list.isEmpty()) {
                return "Không có dữ liệu sản phẩm bán chạy trong khoảng " + fromDate + " đến " + toDate + ".";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("📊 TOP SẢN PHẨM BÁN CHẠY (").append(fromDate).append(" → ").append(toDate).append("):\n\n");
            sb.append(String.format("%-4s %-30s %-18s %10s %15s\n", "STT", "Tên sản phẩm", "Danh mục", "SL bán", "Doanh thu"));
            sb.append("─".repeat(85)).append("\n");

            for (int i = 0; i < list.size(); i++) {
                Object[] row = list.get(i);
                String tenSP = (String) row[0];
                String danhMuc = (String) row[1];
                long slBan = (long) row[2];
                long doanhThu = (long) row[3];
                sb.append(String.format("%-4d %-30s %-18s %10s %15s\n",
                        i + 1,
                        truncate(tenSP, 28),
                        truncate(danhMuc, 16),
                        DF.format(slBan),
                        DF.format(doanhThu) + "đ"));
            }
            return sb.toString();
        } catch (Exception e) {
            return "❌ Lỗi khi lấy dữ liệu sản phẩm thịnh hành: " + e.getMessage();
        }
    }

    // ==================================================================
    // TOOL 2: THỐNG KÊ ĐƠN HÀNG
    // ==================================================================
    // @Tool("Trả về tổng số hóa đơn và tổng doanh thu trong khoảng thời gian")
    public String getOrderStatistics(String fromDate, String toDate) {
        try {
            Date from = SDF.parse(fromDate);
            Date to = addOneDay(SDF.parse(toDate));

            long tongDon = DashboardDAO.getTongDonHang(from, to);
            double doanhThu = DashboardDAO.getTongDoanhThu(from, to);
            double loiNhuan = DashboardDAO.getTongLoiNhuan(from, to);
            long tongSPBan = DashboardDAO.getTongSanPhamBan(from, to);
            long tongKH = DashboardDAO.getTongKhachHang(from, to);
            double tbDon = DashboardDAO.getTrungBinhGiaTriDonHang(from, to);

            StringBuilder sb = new StringBuilder();
            sb.append("📈 THỐNG KÊ ĐƠN HÀNG (").append(fromDate).append(" → ").append(toDate).append("):\n\n");
            sb.append("• Tổng số hóa đơn:          ").append(DF.format(tongDon)).append(" đơn\n");
            sb.append("• Tổng doanh thu:            ").append(DashboardDAO.formatVND(doanhThu)).append(" VNĐ\n");
            sb.append("• Tổng lợi nhuận:            ").append(DashboardDAO.formatVND(loiNhuan)).append(" VNĐ\n");
            sb.append("• Tổng sản phẩm đã bán:      ").append(DF.format(tongSPBan)).append(" sản phẩm\n");
            sb.append("• Tổng khách hàng:            ").append(DF.format(tongKH)).append(" khách\n");
            sb.append("• Trung bình giá trị đơn:     ").append(DashboardDAO.formatVND(tbDon)).append(" VNĐ\n");

            // Tỷ suất lợi nhuận
            if (doanhThu > 0) {
                double margin = loiNhuan / doanhThu * 100;
                sb.append("• Tỷ suất lợi nhuận:          ").append(String.format("%.1f", margin)).append("%\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "❌ Lỗi khi lấy thống kê đơn hàng: " + e.getMessage();
        }
    }

    // ==================================================================
    // TOOL 3: HOÁ ĐƠN GẦN ĐÂY
    // ==================================================================
    // @Tool("Trả về danh sách các hóa đơn gần đây gồm mã HD, tên khách, sản phẩm, thành tiền")
    public String getRecentInvoices(String fromDate, String toDate, int limit) {
        try {
            Date from = SDF.parse(fromDate);
            Date to = addOneDay(SDF.parse(toDate));
            if (limit <= 0 || limit > 50) limit = 10;

            List<Object[]> list = DashboardDAO.getDonHangGanDay(from, to, limit);
            if (list.isEmpty()) {
                return "Không có hóa đơn nào trong khoảng " + fromDate + " đến " + toDate + ".";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("🧾 HOÁ ĐƠN GẦN ĐÂY (").append(fromDate).append(" → ").append(toDate).append(", top ").append(limit).append("):\n\n");
            sb.append(String.format("%-8s %-22s %-28s %15s %18s\n", "Mã HD", "Khách hàng", "Sản phẩm", "Thành tiền", "Thời gian"));
            sb.append("─".repeat(95)).append("\n");

            for (Object[] row : list) {
                long maHD = (long) row[0];
                String tenKH = (String) row[1];
                String sanPham = row[2] != null ? (String) row[2] : "—";
                long thanhTien = (long) row[3];
                String thoiGian = (String) row[4];

                sb.append(String.format("%-8s %-22s %-28s %15s %18s\n",
                        "#" + maHD,
                        truncate(tenKH, 20),
                        truncate(sanPham, 26),
                        DF.format(thanhTien) + "đ",
                        thoiGian));
            }
            return sb.toString();
        } catch (Exception e) {
            return "❌ Lỗi khi lấy danh sách hóa đơn: " + e.getMessage();
        }
    }

    // ==================================================================
    // TOOL 4: DOANH THU THEO CHI NHÁNH
    // ==================================================================
    // @Tool("Trả về bảng doanh thu phân theo từng chi nhánh trong khoảng thời gian")
    public String getRevenuByBranch(String fromDate, String toDate) {
        try {
            Date from = SDF.parse(fromDate);
            Date to = addOneDay(SDF.parse(toDate));
            Map<String, Double> data = DashboardDAO.getDoanhThuTheoChiNhanh(from, to);

            if (data.isEmpty()) {
                return "Không có dữ liệu doanh thu theo chi nhánh trong khoảng " + fromDate + " đến " + toDate + ".";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("🏢 DOANH THU THEO CHI NHÁNH (").append(fromDate).append(" → ").append(toDate).append("):\n\n");
            int i = 1;
            double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                double pct = total > 0 ? entry.getValue() / total * 100 : 0;
                sb.append(String.format("%d. %-25s %15s (%5.1f%%)\n",
                        i++, entry.getKey(), DashboardDAO.formatVND(entry.getValue()), pct));
            }
            sb.append("\n  Tổng cộng: ").append(DashboardDAO.formatVND(total)).append(" VNĐ");
            return sb.toString();
        } catch (Exception e) {
            return "❌ Lỗi khi lấy doanh thu theo chi nhánh: " + e.getMessage();
        }
    }

    // ==================================================================
    // TOOL 5: DOANH THU THEO LOẠI SẢN PHẨM
    // ==================================================================
    // @Tool("Trả về bảng doanh thu phân theo từng loại sản phẩm trong khoảng thời gian")
    public String getRevenueByCategory(String fromDate, String toDate) {
        try {
            Date from = SDF.parse(fromDate);
            Date to = addOneDay(SDF.parse(toDate));
            Map<String, Double> data = DashboardDAO.getDoanhThuTheoLoaiSP(from, to);

            if (data.isEmpty()) {
                return "Không có dữ liệu phân bổ theo loại sản phẩm trong khoảng " + fromDate + " đến " + toDate + ".";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("📦 DOANH THU THEO LOẠI SẢN PHẨM (").append(fromDate).append(" → ").append(toDate).append("):\n\n");
            int i = 1;
            double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                double pct = total > 0 ? entry.getValue() / total * 100 : 0;
                sb.append(String.format("%d. %-25s %15s (%5.1f%%)\n",
                        i++, entry.getKey(), DashboardDAO.formatVND(entry.getValue()), pct));
            }
            sb.append("\n  Tổng cộng: ").append(DashboardDAO.formatVND(total)).append(" VNĐ");
            return sb.toString();
        } catch (Exception e) {
            return "❌ Lỗi khi lấy doanh thu theo loại sản phẩm: " + e.getMessage();
        }
    }

    // ==================================================================
    // TOOL 6: TÓM TẮT DOANH THU THEO THÁNG TRONG NĂM
    // ==================================================================
    // @Tool("Trả về bảng doanh thu và lợi nhuận từng tháng trong 1 năm cụ thể")
    public String getMonthlyRevenueSummary(int year) {
        try {
            Map<String, double[]> data = DashboardDAO.getDoanhThuVaLoiNhuanTheoThang(year);

            StringBuilder sb = new StringBuilder();
            sb.append("📅 DOANH THU & LỢI NHUẬN THEO THÁNG — NĂM ").append(year).append(":\n\n");
            sb.append(String.format("%-8s %18s %18s %10s\n", "Tháng", "Doanh thu", "Lợi nhuận", "Tỷ suất"));
            sb.append("─".repeat(60)).append("\n");

            double totalDT = 0, totalLN = 0;
            for (Map.Entry<String, double[]> entry : data.entrySet()) {
                double dt = entry.getValue()[0];
                double ln = entry.getValue()[1];
                totalDT += dt;
                totalLN += ln;
                String margin = dt > 0 ? String.format("%.1f%%", ln / dt * 100) : "—";
                sb.append(String.format("%-8s %18s %18s %10s\n",
                        entry.getKey(),
                        DashboardDAO.formatVND(dt),
                        DashboardDAO.formatVND(ln),
                        margin));
            }
            sb.append("─".repeat(60)).append("\n");
            String totalMargin = totalDT > 0 ? String.format("%.1f%%", totalLN / totalDT * 100) : "—";
            sb.append(String.format("%-8s %18s %18s %10s\n", "TỔNG",
                    DashboardDAO.formatVND(totalDT), DashboardDAO.formatVND(totalLN), totalMargin));
            return sb.toString();
        } catch (Exception e) {
            return "❌ Lỗi khi lấy doanh thu theo tháng: " + e.getMessage();
        }
    }

    // ==================================================================
    // TIỆN ÍCH NỘI BỘ
    // ==================================================================

    /** Cắt chuỗi nếu quá dài, thêm "..." ở cuối */
    private String truncate(String s, int maxLen) {
        if (s == null) return "—";
        return s.length() > maxLen ? s.substring(0, maxLen - 1) + "…" : s;
    }

    /** Cộng thêm 1 ngày cho toDate (để query đến hết ngày cuối) */
    private Date addOneDay(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(Calendar.DAY_OF_MONTH, 1);
        return c.getTime();
    }
}
