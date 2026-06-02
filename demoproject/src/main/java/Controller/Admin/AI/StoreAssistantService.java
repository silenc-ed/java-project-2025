package Controller.Admin.AI;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;

/**
 * =======================================================================
 * SERVICE QUẢN LÝ TRỢ LÝ AI (CHATBOT)
 * =======================================================================
 * Lớp này đóng vai trò trung gian giữa giao diện Chat (View) và các Tool
 * truy vấn Database (StoreReportTools).
 *
 * HOẠT ĐỘNG THEO 2 CHẾ ĐỘ:
 *
 * 1. CHẾ ĐỘ MOCK (mặc định — KHÔNG CẦN API KEY):
 *    → Phân tích ý định người dùng bằng keyword matching
 *    → Gọi trực tiếp StoreReportTools tương ứng
 *    → Trả về kết quả dạng text
 *
 * 2. CHẾ ĐỘ LANGCHAIN4J (khi tích hợp thật):
 *    → Tạo ChatLanguageModel (OpenAI / Ollama / Gemini)
 *    → Đăng ký StoreReportTools làm Tool
 *    → LLM tự quyết định gọi Tool nào dựa trên ngữ cảnh
 *
 * ĐỂ CHUYỂN SANG CHẾ ĐỘ LANGCHAIN4J:
 *    1. Thêm dependency langchain4j + langchain4j-open-ai vào pom.xml
 *    2. Uncomment khối code bên dưới (LANGCHAIN4J INTEGRATION)
 *    3. Đặt API Key vào biến môi trường hoặc truyền trực tiếp
 * =======================================================================
 */
public class StoreAssistantService {

    private final StoreReportTools tools;

    // Ngày mặc định cho truy vấn (30 ngày gần nhất)
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public StoreAssistantService() {
        this.tools = new StoreReportTools();
    }

    // =====================================================================
    // PHƯƠNG THỨC CHÍNH: XỬ LÝ CÂU HỎI CỦA NGƯỜI DÙNG
    // =====================================================================

    /**
     * Nhận câu hỏi dạng tự nhiên từ người dùng, phân tích ý định,
     * gọi Tool phù hợp, rồi trả về câu trả lời dạng String.
     *
     * @param userMessage Câu hỏi/yêu cầu của người dùng
     * @return Câu trả lời từ AI (hoặc mock AI)
     */
    public String chat(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Vui lòng nhập câu hỏi của bạn!";
        }

        String msg = userMessage.trim().toLowerCase();

        // ----------------------------------------------------------------
        // BƯỚC 1: Trích xuất khoảng thời gian từ câu hỏi (nếu có)
        // ----------------------------------------------------------------
        String[] dateRange = extractDateRange(userMessage);
        String fromDate = dateRange[0];
        String toDate = dateRange[1];

        // ----------------------------------------------------------------
        // BƯỚC 2: Phân loại ý định (Intent Detection) bằng keyword matching
        // ----------------------------------------------------------------
        try {
            // Kiểm tra xem có yêu cầu doanh thu theo tháng/năm cụ thể không
            int year = extractYear(msg);

            // Ý định: Doanh thu tháng trong năm
            if (matchesAny(msg, "doanh thu theo tháng", "doanh thu từng tháng",
                    "doanh thu hàng tháng", "tổng kết năm", "báo cáo năm",
                    "revenue monthly", "monthly revenue") && year > 0) {
                return tools.getMonthlyRevenueSummary(year);
            }

            // Ý định: Sản phẩm bán chạy / thịnh hành
            if (matchesAny(msg, "sản phẩm bán chạy", "top sản phẩm", "sp bán chạy",
                    "thịnh hành", "trending", "best seller", "bán chạy nhất",
                    "sản phẩm hot", "top selling")) {
                return tools.getTrendingProducts(fromDate, toDate);
            }

            // Ý định: Hóa đơn gần đây
            if (matchesAny(msg, "hóa đơn gần đây", "hóa đơn mới", "đơn hàng gần đây",
                    "invoice", "hoá đơn", "recent order", "đơn mới nhất",
                    "danh sách hóa đơn", "danh sách đơn hàng")) {
                int limit = extractLimit(msg);
                return tools.getRecentInvoices(fromDate, toDate, limit);
            }

            // Ý định: Doanh thu theo chi nhánh
            if (matchesAny(msg, "chi nhánh", "theo chi nhánh", "branch",
                    "doanh thu chi nhánh", "doanh thu từng chi nhánh")) {
                return tools.getRevenuByBranch(fromDate, toDate);
            }

            // Ý định: Doanh thu theo loại sản phẩm
            if (matchesAny(msg, "theo loại", "loại sản phẩm", "category",
                    "danh mục", "phân bổ", "cơ cấu doanh thu")) {
                return tools.getRevenueByCategory(fromDate, toDate);
            }

            // Ý định: Doanh thu theo tháng (không kèm năm → dùng năm hiện tại)
            if (matchesAny(msg, "doanh thu theo tháng", "doanh thu từng tháng",
                    "doanh thu hàng tháng")) {
                int y = year > 0 ? year : LocalDate.now().getYear();
                return tools.getMonthlyRevenueSummary(y);
            }

            // Ý định: Thống kê chung (doanh thu, tổng đơn, lợi nhuận, ...)
            if (matchesAny(msg, "thống kê", "doanh thu", "tổng đơn", "lợi nhuận",
                    "tổng quan", "revenue", "statistics", "báo cáo",
                    "bao nhiêu đơn", "tổng doanh thu", "report", "kpi")) {
                return tools.getOrderStatistics(fromDate, toDate);
            }

            // Ý định: Lời chào
            if (matchesAny(msg, "xin chào", "hello", "hi", "chào", "hey")) {
                return "👋 Xin chào! Tôi là Trợ lý AI của hệ thống quản lý cửa hàng.\n\n"
                     + "Tôi có thể giúp bạn tra cứu:\n"
                     + "  📊 Sản phẩm bán chạy\n"
                     + "  📈 Thống kê doanh thu, lợi nhuận\n"
                     + "  🧾 Hóa đơn gần đây\n"
                     + "  🏢 Doanh thu theo chi nhánh\n"
                     + "  📦 Doanh thu theo loại sản phẩm\n"
                     + "  📅 Doanh thu theo tháng trong năm\n\n"
                     + "Hãy thử hỏi: \"Top sản phẩm bán chạy tháng này\" hoặc \"Thống kê doanh thu từ 01/01/2026 đến 31/05/2026\"";
            }

            // Ý định: Trợ giúp
            if (matchesAny(msg, "help", "trợ giúp", "hướng dẫn", "giúp đỡ",
                    "bạn có thể làm gì", "chức năng")) {
                return getHelpMessage();
            }

            // Không nhận diện được → trả về gợi ý
            return "🤔 Tôi chưa hiểu rõ yêu cầu của bạn. Hãy thử hỏi theo các mẫu sau:\n\n"
                 + "  • \"Top sản phẩm bán chạy\"\n"
                 + "  • \"Thống kê doanh thu tháng này\"\n"
                 + "  • \"Hóa đơn gần đây\"\n"
                 + "  • \"Doanh thu theo chi nhánh\"\n"
                 + "  • \"Doanh thu theo loại sản phẩm\"\n"
                 + "  • \"Doanh thu theo tháng năm 2026\"\n\n"
                 + "💡 Mẹo: Bạn có thể chỉ định thời gian, ví dụ:\n"
                 + "  \"Top sản phẩm bán chạy từ 01/01/2026 đến 31/05/2026\"";

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Đã xảy ra lỗi khi xử lý yêu cầu: " + e.getMessage();
        }
    }

    // =====================================================================
    // TRÍCH XUẤT THỜI GIAN TỪ CÂU HỎI
    // =====================================================================

    /**
     * Tìm và trích xuất khoảng thời gian từ câu hỏi dạng tự nhiên.
     * Hỗ trợ:
     *   - "từ DD/MM/YYYY đến DD/MM/YYYY"
     *   - "tháng X" → từ đầu tháng đến cuối tháng hiện tại
     *   - "năm YYYY" → cả năm
     *   - Mặc định: 30 ngày gần nhất
     */
    private String[] extractDateRange(String message) {
        String msg = message.toLowerCase();

        // Pattern 1: "từ DD/MM/YYYY đến DD/MM/YYYY"
        Pattern p1 = Pattern.compile("(\\d{1,2}/\\d{1,2}/\\d{4})\\s*(?:đến|tới|-)\\s*(\\d{1,2}/\\d{1,2}/\\d{4})");
        Matcher m1 = p1.matcher(message);
        if (m1.find()) {
            return new String[]{m1.group(1), m1.group(2)};
        }

        // Pattern 2: "tháng X" hoặc "tháng X năm YYYY"
        Pattern p2 = Pattern.compile("tháng\\s+(\\d{1,2})(?:\\s*(?:năm|/)\\s*(\\d{4}))?");
        Matcher m2 = p2.matcher(msg);
        if (m2.find()) {
            int month = Integer.parseInt(m2.group(1));
            int year = m2.group(2) != null ? Integer.parseInt(m2.group(2)) : LocalDate.now().getYear();
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.plusMonths(1).minusDays(1);
            return new String[]{start.format(DTF), end.format(DTF)};
        }

        // Pattern 3: "năm YYYY"
        Pattern p3 = Pattern.compile("năm\\s+(\\d{4})");
        Matcher m3 = p3.matcher(msg);
        if (m3.find()) {
            int year = Integer.parseInt(m3.group(1));
            return new String[]{"01/01/" + year, "31/12/" + year};
        }

        // Pattern 4: "hôm nay" / "hôm qua"
        if (msg.contains("hôm nay")) {
            String today = LocalDate.now().format(DTF);
            return new String[]{today, today};
        }
        if (msg.contains("hôm qua")) {
            String yesterday = LocalDate.now().minusDays(1).format(DTF);
            return new String[]{yesterday, yesterday};
        }

        // Pattern 5: "tuần này"
        if (msg.contains("tuần này") || msg.contains("tuần nay")) {
            LocalDate now = LocalDate.now();
            LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
            return new String[]{startOfWeek.format(DTF), now.format(DTF)};
        }

        // Pattern 6: "tháng này" / "tháng trước"
        if (msg.contains("tháng này") || msg.contains("tháng nay")) {
            LocalDate now = LocalDate.now();
            LocalDate start = now.withDayOfMonth(1);
            return new String[]{start.format(DTF), now.format(DTF)};
        }
        if (msg.contains("tháng trước")) {
            LocalDate now = LocalDate.now();
            LocalDate start = now.minusMonths(1).withDayOfMonth(1);
            LocalDate end = now.withDayOfMonth(1).minusDays(1);
            return new String[]{start.format(DTF), end.format(DTF)};
        }

        // Mặc định: 30 ngày gần nhất
        LocalDate now = LocalDate.now();
        LocalDate thirtyDaysAgo = now.minusDays(30);
        return new String[]{thirtyDaysAgo.format(DTF), now.format(DTF)};
    }

    /** Trích xuất năm từ câu hỏi (ví dụ: "năm 2026") */
    private int extractYear(String msg) {
        Pattern p = Pattern.compile("(?:năm|year)\\s+(\\d{4})");
        Matcher m = p.matcher(msg);
        if (m.find()) return Integer.parseInt(m.group(1));

        // Thử tìm số 4 chữ số đứng riêng
        Pattern p2 = Pattern.compile("\\b(20\\d{2})\\b");
        Matcher m2 = p2.matcher(msg);
        if (m2.find()) return Integer.parseInt(m2.group(1));

        return -1;
    }

    /** Trích xuất số lượng limit từ câu hỏi (ví dụ: "top 5", "10 hóa đơn gần nhất") */
    private int extractLimit(String msg) {
        Pattern p = Pattern.compile("(?:top|giới hạn|limit)\\s+(\\d+)");
        Matcher m = p.matcher(msg);
        if (m.find()) return Integer.parseInt(m.group(1));

        Pattern p2 = Pattern.compile("(\\d+)\\s*(?:hóa đơn|hoá đơn|đơn hàng|đơn)");
        Matcher m2 = p2.matcher(msg);
        if (m2.find()) return Integer.parseInt(m2.group(1));

        return 10; // mặc định
    }

    /** Kiểm tra xem message có chứa bất kỳ keyword nào không */
    private boolean matchesAny(String message, String... keywords) {
        for (String kw : keywords) {
            if (message.contains(kw.toLowerCase())) return true;
        }
        return false;
    }

    // =====================================================================
    // TIN NHẮN TRỢ GIÚP
    // =====================================================================

    private String getHelpMessage() {
        return "🤖 HƯỚNG DẪN SỬ DỤNG TRỢ LÝ AI\n"
             + "══════════════════════════════════\n\n"
             + "Tôi có thể giúp bạn tra cứu các số liệu kinh doanh trực tiếp.\n"
             + "Hãy hỏi tôi bằng ngôn ngữ tự nhiên, ví dụ:\n\n"
             + "📊 SẢN PHẨM BÁN CHẠY:\n"
             + "  • \"Top sản phẩm bán chạy tháng này\"\n"
             + "  • \"Sản phẩm thịnh hành từ 01/01/2026 đến 31/05/2026\"\n\n"
             + "📈 THỐNG KÊ DOANH THU:\n"
             + "  • \"Thống kê doanh thu tháng 5\"\n"
             + "  • \"Tổng doanh thu hôm nay\"\n"
             + "  • \"Doanh thu theo tháng năm 2026\"\n\n"
             + "🧾 HOÁ ĐƠN:\n"
             + "  • \"10 hóa đơn gần đây\"\n"
             + "  • \"Danh sách hóa đơn tuần này\"\n\n"
             + "🏢 PHÂN TÍCH:\n"
             + "  • \"Doanh thu theo chi nhánh\"\n"
             + "  • \"Doanh thu theo loại sản phẩm\"\n\n"
             + "💡 MẸO: Bạn có thể dùng các cụm thời gian như:\n"
             + "  'hôm nay', 'hôm qua', 'tuần này', 'tháng này',\n"
             + "  'tháng trước', 'tháng 5', 'năm 2026',\n"
             + "  hoặc chỉ định chính xác: 'từ 01/01/2026 đến 31/05/2026'";
    }

    // =====================================================================
    // LANGCHAIN4J INTEGRATION (UNCOMMENT KHI TÍCH HỢP THẬT)
    // =====================================================================
    /*
    import dev.langchain4j.model.openai.OpenAiChatModel;
    import dev.langchain4j.service.AiServices;

    // Interface để LangChain4j sinh proxy
    interface StoreAssistant {
        String chat(String userMessage);
    }

    // Khởi tạo LangChain4j model + tool
    private StoreAssistant buildLangChainAssistant() {
        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("gpt-4o-mini")
                .build();

        return AiServices.builder(StoreAssistant.class)
                .chatLanguageModel(model)
                .tools(new StoreReportTools())
                .systemMessage("Bạn là trợ lý AI của hệ thống quản lý cửa hàng thiết bị điện tử. "
                    + "Hãy trả lời bằng tiếng Việt. Khi cần số liệu, hãy gọi các tool được cung cấp.")
                .build();
    }
    */
}
