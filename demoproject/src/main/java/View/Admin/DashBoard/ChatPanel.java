package View.Admin.DashBoard;

import Controller.Admin.AI.StoreAssistantService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * =======================================================================
 * GIAO DIỆN CHATBOT TRỢ LÝ AI
 * =======================================================================
 * Panel hiện đại hiển thị cuộc trò chuyện giữa người dùng và Trợ lý AI.
 *
 * Cấu trúc giao diện:
 *   ┌──────────────────────────────┐
 *   │  🤖 Trợ lý AI Dashboard     │  ← Header (gradient purple)
 *   │  Trạng thái: Sẵn sàng       │
 *   ├──────────────────────────────┤
 *   │                              │
 *   │  [Bot] Xin chào! ...        │  ← Chat history (JTextPane, read-only)
 *   │                              │
 *   │  [User] Top sản phẩm...     │
 *   │                              │
 *   │  [Bot] 📊 TOP SẢN PHẨM...  │
 *   │                              │
 *   ├──────────────────────────────┤
 *   │  [Quick action buttons]      │  ← Gợi ý nhanh
 *   ├──────────────────────────────┤
 *   │  [ Nhập câu hỏi... ] [Gửi]  │  ← Input bar
 *   └──────────────────────────────┘
 *
 * Tính năng:
 *   - Gửi tin nhắn bằng Enter hoặc nút Gửi
 *   - Xử lý bất đồng bộ (SwingWorker) để không đông UI
 *   - Hiệu ứng typing "Đang suy nghĩ..." khi AI đang xử lý
 *   - Các nút gợi ý nhanh (Quick Actions)
 *   - Auto-scroll xuống tin nhắn mới nhất
 * =======================================================================
 */
public class ChatPanel extends JPanel {

    // === Thành phần UI chính ===
    private JTextPane chatPane;          // Vùng hiển thị lịch sử chat (read-only)
    private JTextField txtInput;         // Ô nhập câu hỏi
    private JButton btnSend;             // Nút Gửi
    private JLabel lblStatus;            // Nhãn trạng thái (Sẵn sàng / Đang xử lý...)
    private JPanel quickActionsPanel;    // Panel chứa các nút gợi ý nhanh

    // === Logic AI ===
    private final StoreAssistantService aiService;
    private boolean isProcessing = false; // Cờ ngăn gửi nhiều lần khi AI đang xử lý

    // === Màu sắc giao diện ===
    private static final Color BG_MAIN = new Color(248, 250, 252);
    private static final Color PURPLE_DARK = new Color(88, 28, 135);
    private static final Color PURPLE_MAIN = new Color(142, 68, 193);
    private static final Color PURPLE_LIGHT = new Color(243, 232, 255);
    private static final Color USER_BG = new Color(219, 234, 254);       // Xanh nhạt cho tin user
    private static final Color BOT_BG = new Color(243, 244, 246);         // Xám nhạt cho tin bot
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color GREEN_ONLINE = new Color(34, 197, 94);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);

    // === Định dạng thời gian ===
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public ChatPanel() {
        this.aiService = new StoreAssistantService();
        initUI();
        showWelcomeMessage();
    }

    // =====================================================================
    // XÂY DỰNG GIAO DIỆN
    // =====================================================================

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(BG_MAIN);

        // 1. Header
        add(buildHeader(), BorderLayout.NORTH);

        // 2. Vùng chat chính
        add(buildChatArea(), BorderLayout.CENTER);

        // 3. Thanh nhập liệu ở dưới
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(BG_MAIN);
        bottomPanel.add(buildQuickActions());
        bottomPanel.add(buildInputBar());
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Header với gradient tím, icon AI và trạng thái online.
     */
    private JPanel buildHeader() {
        // Panel gradient custom
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, PURPLE_DARK, getWidth(), 0, PURPLE_MAIN);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, 62));
        header.setBorder(new EmptyBorder(10, 18, 10, 18));

        // Bên trái: Icon + Tiêu đề
        JPanel leftInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftInfo.setOpaque(false);

        // Icon AI dạng vòng tròn
        JLabel iconLabel = new JLabel("🤖") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iconLabel.setPreferredSize(new Dimension(40, 40));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);

        JLabel lblTitle = new JLabel("Trợ lý AI Dashboard");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);

        lblStatus = new JLabel("● Sẵn sàng trả lời");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(new Color(196, 181, 253)); // Purple light text

        titleStack.add(lblTitle);
        titleStack.add(lblStatus);

        leftInfo.add(iconLabel);
        leftInfo.add(titleStack);
        header.add(leftInfo, BorderLayout.WEST);

        // Bên phải: Nút xóa lịch sử
        JButton btnClear = new JButton("🗑 Xóa");
        btnClear.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClear.setForeground(new Color(196, 181, 253));
        btnClear.setBackground(new Color(255, 255, 255, 20));
        btnClear.setBorderPainted(false);
        btnClear.setFocusPainted(false);
        btnClear.setContentAreaFilled(false);
        btnClear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClear.setBorder(new CompoundBorder(
                new LineBorder(new Color(255, 255, 255, 40), 1, true),
                new EmptyBorder(4, 12, 4, 12)
        ));
        btnClear.addActionListener(e -> clearChat());
        btnClear.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btnClear.setForeground(Color.WHITE); }
            @Override
            public void mouseExited(MouseEvent e) { btnClear.setForeground(new Color(196, 181, 253)); }
        });
        header.add(btnClear, BorderLayout.EAST);

        return header;
    }

    /**
     * Vùng hiển thị chat — JTextPane với styled text (read-only).
     */
    private JScrollPane buildChatArea() {
        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatPane.setBackground(BG_MAIN);
        chatPane.setBorder(new EmptyBorder(12, 16, 12, 16));
        chatPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Ngăn caret nhấp nháy
        chatPane.setCaret(new DefaultCaret() {
            @Override
            public void setVisible(boolean v) { super.setVisible(false); }
        });

        JScrollPane scrollPane = new JScrollPane(chatPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_MAIN);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        return scrollPane;
    }

    /**
     * Dãy nút gợi ý nhanh (Quick Actions) để người dùng click thay vì gõ.
     */
    private JPanel buildQuickActions() {
        quickActionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        quickActionsPanel.setBackground(BG_MAIN);
        quickActionsPanel.setBorder(new EmptyBorder(4, 14, 0, 14));

        String[][] actions = {
                {"📊 SP bán chạy",          "Top sản phẩm bán chạy tháng này"},
                {"📈 Thống kê doanh thu",    "Thống kê doanh thu tháng này"},
                {"🧾 Hóa đơn gần đây",      "10 hóa đơn gần đây"},
                {"🏢 Theo chi nhánh",         "Doanh thu theo chi nhánh tháng này"},
                {"📦 Theo loại SP",           "Doanh thu theo loại sản phẩm tháng này"},
                {"❓ Trợ giúp",              "Trợ giúp"},
        };

        for (String[] action : actions) {
            JButton btn = createQuickActionButton(action[0], action[1]);
            quickActionsPanel.add(btn);
        }

        return quickActionsPanel;
    }

    /**
     * Tạo nút gợi ý nhanh có hover effect.
     */
    private JButton createQuickActionButton(String label, String query) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setForeground(PURPLE_MAIN);
        btn.setBackground(PURPLE_LIGHT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(5, 10, 5, 10));

        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(233, 213, 255));
                btn.setForeground(PURPLE_DARK);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(PURPLE_LIGHT);
                btn.setForeground(PURPLE_MAIN);
            }
        });

        // Click → Gửi query tương ứng
        btn.addActionListener(e -> {
            txtInput.setText(query);
            sendMessage();
        });

        return btn;
    }

    /**
     * Thanh nhập liệu phía dưới: TextField + nút Gửi.
     */
    private JPanel buildInputBar() {
        JPanel inputBar = new JPanel(new BorderLayout(8, 0));
        inputBar.setBackground(Color.WHITE);
        inputBar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(10, 14, 10, 14)
        ));

        // TextField nhập câu hỏi
        txtInput = new JTextField();
        txtInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtInput.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        txtInput.setBackground(new Color(249, 250, 251));

        // Placeholder text
        txtInput.setText("Nhập câu hỏi về doanh thu, sản phẩm, đơn hàng...");
        txtInput.setForeground(TEXT_SECONDARY);
        txtInput.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtInput.getForeground().equals(TEXT_SECONDARY)) {
                    txtInput.setText("");
                    txtInput.setForeground(TEXT_PRIMARY);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtInput.getText().trim().isEmpty()) {
                    txtInput.setText("Nhập câu hỏi về doanh thu, sản phẩm, đơn hàng...");
                    txtInput.setForeground(TEXT_SECONDARY);
                }
            }
        });

        // Bắt sự kiện Enter để gửi tin nhắn
        txtInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !isProcessing) {
                    sendMessage();
                }
            }
        });

        // Nút Gửi
        btnSend = new JButton("Gửi  ➤");
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSend.setForeground(Color.WHITE);
        btnSend.setBackground(PURPLE_MAIN);
        btnSend.setBorderPainted(false);
        btnSend.setFocusPainted(false);
        btnSend.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSend.setPreferredSize(new Dimension(90, 40));
        btnSend.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnSend.addActionListener(e -> sendMessage());

        // Hover effect cho nút Gửi
        btnSend.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isProcessing) btnSend.setBackground(PURPLE_DARK);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (!isProcessing) btnSend.setBackground(PURPLE_MAIN);
            }
        });

        inputBar.add(txtInput, BorderLayout.CENTER);
        inputBar.add(btnSend, BorderLayout.EAST);
        return inputBar;
    }

    // =====================================================================
    // XỬ LÝ GỬI TIN NHẮN
    // =====================================================================

    /**
     * Xử lý khi người dùng gửi tin nhắn.
     * Bước 1: Hiển thị tin nhắn user lên chat area.
     * Bước 2: Chạy SwingWorker gọi AI service (không block EDT).
     * Bước 3: Hiển thị phản hồi của AI lên chat area.
     */
    private void sendMessage() {
        // Lấy nội dung nhập
        String userText = txtInput.getText().trim();
        if (userText.isEmpty() || txtInput.getForeground().equals(TEXT_SECONDARY)) return;
        if (isProcessing) return;

        // Hiển thị tin nhắn của User
        appendMessage("Bạn", userText, true);
        txtInput.setText("");

        // Đánh dấu đang xử lý → disable input
        setProcessing(true);

        // Hiển thị trạng thái "đang suy nghĩ"
        appendTypingIndicator();

        // === Gọi AI bất đồng bộ bằng SwingWorker ===
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                // Chạy trên background thread → không block giao diện
                return aiService.chat(userText);
            }

            @Override
            protected void done() {
                try {
                    // Lấy kết quả từ AI
                    String response = get();
                    // Xóa indicator "đang suy nghĩ"
                    removeTypingIndicator();
                    // Hiển thị phản hồi AI
                    appendMessage("Trợ lý AI", response, false);
                } catch (Exception ex) {
                    removeTypingIndicator();
                    appendMessage("Trợ lý AI", "❌ Đã xảy ra lỗi: " + ex.getMessage(), false);
                } finally {
                    // Mở lại input
                    setProcessing(false);
                }
            }
        }.execute();
    }

    // =====================================================================
    // HIỂN THỊ TIN NHẮN LÊN CHAT AREA
    // =====================================================================

    /**
     * Thêm một tin nhắn (user hoặc bot) vào vùng chat.
     * Sử dụng StyledDocument để tạo hiệu ứng bong bóng chat.
     */
    private void appendMessage(String sender, String message, boolean isUser) {
        StyledDocument doc = chatPane.getStyledDocument();

        try {
            // === Timestamp ===
            String time = LocalDateTime.now().format(TIME_FMT);

            // Style cho tên người gửi
            SimpleAttributeSet senderStyle = new SimpleAttributeSet();
            StyleConstants.setBold(senderStyle, true);
            StyleConstants.setFontSize(senderStyle, 12);
            StyleConstants.setForeground(senderStyle, isUser ? new Color(37, 99, 235) : PURPLE_MAIN);

            // Style cho nội dung tin nhắn
            SimpleAttributeSet msgStyle = new SimpleAttributeSet();
            StyleConstants.setFontFamily(msgStyle, "Segoe UI");
            StyleConstants.setFontSize(msgStyle, 13);
            StyleConstants.setForeground(msgStyle, TEXT_PRIMARY);
            StyleConstants.setLineSpacing(msgStyle, 0.25f);

            // Style cho timestamp
            SimpleAttributeSet timeStyle = new SimpleAttributeSet();
            StyleConstants.setFontSize(timeStyle, 10);
            StyleConstants.setForeground(timeStyle, TEXT_SECONDARY);
            StyleConstants.setItalic(timeStyle, true);

            // Vẽ separator nhẹ
            SimpleAttributeSet sepStyle = new SimpleAttributeSet();
            StyleConstants.setForeground(sepStyle, new Color(226, 232, 240));
            StyleConstants.setFontSize(sepStyle, 6);

            // Chèn vào document
            String icon = isUser ? "👤 " : "🤖 ";
            doc.insertString(doc.getLength(), icon + sender + "  ", senderStyle);
            doc.insertString(doc.getLength(), time + "\n", timeStyle);
            doc.insertString(doc.getLength(), message + "\n\n", msgStyle);

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        // Auto-scroll xuống tin nhắn mới nhất
        SwingUtilities.invokeLater(() -> chatPane.setCaretPosition(chatPane.getDocument().getLength()));
    }

    /**
     * Hiển thị indicator "Đang suy nghĩ..." trong lúc AI xử lý.
     */
    private void appendTypingIndicator() {
        StyledDocument doc = chatPane.getStyledDocument();
        try {
            SimpleAttributeSet style = new SimpleAttributeSet();
            StyleConstants.setFontFamily(style, "Segoe UI");
            StyleConstants.setFontSize(style, 12);
            StyleConstants.setItalic(style, true);
            StyleConstants.setForeground(style, TEXT_SECONDARY);

            doc.insertString(doc.getLength(), "🤖 Trợ lý AI đang suy nghĩ...\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> chatPane.setCaretPosition(chatPane.getDocument().getLength()));
    }

    /**
     * Xóa dòng "Đang suy nghĩ..." sau khi AI đã trả lời.
     */
    private void removeTypingIndicator() {
        try {
            StyledDocument doc = chatPane.getStyledDocument();
            String text = doc.getText(0, doc.getLength());
            int idx = text.lastIndexOf("🤖 Trợ lý AI đang suy nghĩ...\n");
            if (idx >= 0) {
                doc.remove(idx, "🤖 Trợ lý AI đang suy nghĩ...\n".length());
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    // =====================================================================
    // TIỆN ÍCH
    // =====================================================================

    /** Hiển thị tin nhắn chào mừng khi mở lần đầu */
    private void showWelcomeMessage() {
        String welcome = "👋 Xin chào! Tôi là Trợ lý AI của hệ thống quản lý cửa hàng.\n\n"
                + "Tôi có thể giúp bạn tra cứu nhanh các số liệu kinh doanh:\n"
                + "  📊 Sản phẩm bán chạy\n"
                + "  📈 Thống kê doanh thu & lợi nhuận\n"
                + "  🧾 Danh sách hóa đơn gần đây\n"
                + "  🏢 Doanh thu theo chi nhánh\n"
                + "  📦 Phân bổ theo loại sản phẩm\n\n"
                + "Hãy bắt đầu bằng cách nhập câu hỏi bên dưới, hoặc bấm vào các nút gợi ý nhanh!";
        appendMessage("Trợ lý AI", welcome, false);
    }

    /** Xóa toàn bộ lịch sử chat */
    private void clearChat() {
        chatPane.setText("");
        showWelcomeMessage();
    }

    /** Đặt trạng thái xử lý (disable/enable input) */
    private void setProcessing(boolean processing) {
        isProcessing = processing;
        txtInput.setEnabled(!processing);
        btnSend.setEnabled(!processing);

        if (processing) {
            lblStatus.setText("⏳ Đang xử lý câu hỏi...");
            lblStatus.setForeground(new Color(253, 224, 71)); // Vàng
            btnSend.setBackground(new Color(148, 163, 184));  // Xám
        } else {
            lblStatus.setText("● Sẵn sàng trả lời");
            lblStatus.setForeground(new Color(196, 181, 253));
            btnSend.setBackground(PURPLE_MAIN);
            txtInput.requestFocusInWindow();
        }
    }
}
